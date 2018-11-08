package ch.exense.step.examples;

import static step.planbuilder.BaseArtefacts.sequence;
import static step.planbuilder.FunctionPlanBuilder.keywordById;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.json.Json;

import org.bson.types.ObjectId;
import org.junit.Test;

import junit.framework.Assert;
import step.client.ControllerClient;
import step.client.executions.RemoteExecutionFuture;
import step.client.repository.StagingRepositoryClient;
import step.client.repository.StagingRepositoryClient.StagingContext;
import step.core.artefacts.reports.ReportNodeStatus;
import step.core.dynamicbeans.DynamicValue;
import step.core.plans.Plan;
import step.core.plans.builder.PlanBuilder;
import step.core.plans.runner.PlanRunnerResult;
import step.functions.Function;
import step.functions.type.FunctionTypeException;
import step.functions.type.SetupFunctionException;
import step.plugins.java.GeneralScriptFunction;

public class ControllerClientDemo {
	
	@Test
	public void controllerClientDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		try(ControllerClient client = new ControllerClient("http://step-enterprise-nightly.exense.ch", "admin", "init")) {
			
			// Create a DemoKeyword (javascript) and upload it to the controller
			Function keyword = uploadDemoKeyword(client);

			// Build a demo plan which calls the demo keyword
			Plan plan = PlanBuilder.create()
					.startBlock(sequence())
					.add(keywordById(keyword.getId().toString(),"{}"))
					.endBlock()
					.build();

			// Upload the plan to the controller
			client.getPlanRepository().save(plan);

			// Execute the plan on the controller
			String executionId = client.getExecutionManager().execute(plan.getId());
			
			RemoteExecutionFuture future = client.getExecutionManager().getFuture(executionId);

			// Wait for the plan execution to terminate and visit the report tree
			future.waitForExecutionToTerminate().visitReportTree(node->{
				Assert.assertEquals(ReportNodeStatus.PASSED, node.getNode().getStatus());
			});
		}
	}

	@Test
	public void remotePlanRunnerDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		try(ControllerClient client = new ControllerClient("http://step-enterprise-nightly.exense.ch", "admin", "init")) {
			// Create a DemoKeyword (javascript) and upload it to the controller
			Function keyword = uploadDemoKeyword(client);
			
			// Build a demo plan which calls the demo keyword
			Plan plan = PlanBuilder.create()
					.startBlock(sequence())
					.add(keywordById(keyword.getId().toString(),"{}"))
					.endBlock()
					.build();
	
			// Run the plan on the controller
			PlanRunnerResult result = client.getPlanRunners().getRemotePlanRunner().run(plan);
			
			// Wait for the plan execution to terminate and print the report tree to the standard output
			result.waitForExecutionToTerminate().printTree();
		}
	}
	
	@Test
	public void isolatedExecutionDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		try(ControllerClient client = new ControllerClient("http://step-enterprise-nightly.exense.ch", "admin", "init")) {
			StagingRepositoryClient stagingClient = client.getStagingRepositoryClient();
			
			// Create the staging context which creates an isolated context for the upload of execution scoped resources like Keywords, Resources, and Plans
			StagingContext context = stagingClient.createContext();
			
			// Create a DemoKeyword (javascript) and upload it to the staging context
			String attachmentId = context.upload(this.getClass().getResourceAsStream("DemoKeyword.js"), "DemoKeyword.js");
			Function keyword = createDemoKeyword(attachmentId);

			// Build a demo plan which calls the demo keyword
			Plan plan = PlanBuilder.create()
					.startBlock(sequence())
					.add(keywordById(keyword.getId().toString(),"{}"))
					.endBlock()
					.build();
			
			// Add the keyword definitions to the staging plan
			List<Function> functions = new ArrayList<>();
			functions.add(keyword);
			plan.setFunctions(functions);
	
			// Upload the plan to the staging context
			context.uploadPlan(plan);
			
			// Run the plan in the isolated context
			PlanRunnerResult result = context.run();
			
			// Wait for the plan execution to terminate and print the report tree to the standard output
			result.waitForExecutionToTerminate().printTree();
		}
	}
	
	public void remoteControllerManagementDemo() throws IOException {
		try(ControllerClient client = new ControllerClient("http://controller.url", "user", "pwd")) {
			// Shutdown the controller gracefully
			client.shutdownController();
		}
	}

	protected GeneralScriptFunction uploadDemoKeyword(ControllerClient client) throws SetupFunctionException, FunctionTypeException {
		// Upload the javascript code of the keyword
		String resourceId = client.getResourceManager().upload(new File(this.getClass().getResource("DemoKeyword.js").getFile())).getAttachmentId();
		
		GeneralScriptFunction f = createDemoKeyword(resourceId);
		
		// Save the keyword to the controller
		f = (GeneralScriptFunction) client.getFunctionManager().saveFunction(f);
		
		return f;
	}

	protected GeneralScriptFunction createDemoKeyword(String resourceId) {
		// Create the keyword configuration instance
		GeneralScriptFunction f = new GeneralScriptFunction();

		// Set the keyword attributes
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(Function.NAME, "MyDemoKeyword");
		
		f.setId(new ObjectId());
		f.setScriptLanguage(new DynamicValue<String>("javascript"));
		f.setScriptFile(new DynamicValue<String>("attachment:" + resourceId));
		f.setAttributes(attributes);
		
		// Empty keyword schema
		f.setSchema(Json.createObjectBuilder().build());
		return f;
	}
}
