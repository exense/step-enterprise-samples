package ch.exense.step.examples.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import step.client.StepClient;
import step.core.artefacts.AbstractArtefact;
import step.core.artefacts.ArtefactAccessor;
import step.core.plans.Plan;
import step.core.plans.PlanRepository;
import step.functions.Function;
import step.functions.manager.FunctionManager;
import step.functions.type.FunctionTypeException;
import step.functions.type.SetupFunctionException;
import step.plugins.functions.types.CompositeFunction;

public class StepClientExportDemo {
	
	@Test
	public void migrateCompositeKeywordsDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		// Login to the origin and target controller
		try(StepClient originControllerClient = new StepClient("https://step-public-demo.stepcloud.ch", "admin", "public");
			StepClient targetControllerClient = new StepClient("http://...", "admin", "init");) {
			
			ArtefactAccessor originArtefactAccessor = originControllerClient.getRemoteAccessors().getArtefactAccessor();
			PlanRepository originPlanRepository = originControllerClient.getPlanRepository();
			FunctionManager originFunctionManager = originControllerClient.getFunctionManager();

			PlanRepository targetPlanRepository = targetControllerClient.getPlanRepository();
			FunctionManager targetFunctionManager = targetControllerClient.getFunctionManager();
			
			HashMap<String, String> attributes = new HashMap<>();
			attributes.put(Function.NAME, "TestComposite1");
			// get the composite function (keyword) definition by name
			Function function = originFunctionManager.getFunctionByAttributes(attributes);
			
			if(function instanceof CompositeFunction) {
				CompositeFunction compositeFunction = (CompositeFunction) function;
				
				// get the ID of the root artefact of the composite plan
				String rootArtefactId = compositeFunction.getArtefactId();
				// get the root artefact of the composite plan
				AbstractArtefact rootArtefact = originArtefactAccessor.get(rootArtefactId);
				// get the composite plan (loads the artefact node recursively)
				Plan compositePlan = originPlanRepository.load(rootArtefact.getAttributes());
				
				// save the composite function to the target controller
				targetFunctionManager.saveFunction(compositeFunction);
				// workaround: to be done twice to override default function initialization...
				targetFunctionManager.saveFunction(compositeFunction);
				// save the composite plan to the target controller
				targetPlanRepository.save(compositePlan);
			}
		}
	}
}
