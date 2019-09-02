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
import javax.json.JsonObject;

import org.bson.types.ObjectId;
import org.junit.Test;

import junit.framework.Assert;
import step.artefacts.reports.TestCaseReportNode;
import step.attachments.FileResolver;
import step.client.StepClient;
import step.client.executions.RemoteExecutionFuture;
import step.client.repository.StagingRepositoryClient;
import step.client.repository.StagingRepositoryClient.StagingContext;
import step.core.artefacts.reports.ReportNodeStatus;
import step.core.dynamicbeans.DynamicValue;
import step.core.plans.Plan;
import step.core.plans.builder.PlanBuilder;
import step.core.plans.runner.PlanRunnerResult;
import step.functions.Function;
import step.functions.execution.FunctionExecutionService;
import step.functions.io.Input;
import step.functions.io.Output;
import step.functions.type.FunctionTypeException;
import step.functions.type.SetupFunctionException;
import step.grid.TokenWrapper;
import step.grid.tokenpool.Interest;
import step.localrunner.LocalPlanRunner;
import step.plans.nl.parser.PlanParser;
import step.plugins.java.GeneralScriptFunction;
import step.repositories.parser.StepsParser.ParsingException;

public class StepClientDemo {

	private String controllerUrl = "http://controller.url";
	private String user = "user";
	private String password = "pwd";

	@Test
	public void controllerClientDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		try(StepClient client = new StepClient(controllerUrl, user, password)) {

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
		try(StepClient client = new StepClient(controllerUrl, user, password)) {
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
	public void planParserDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException, ParsingException {
		PlanParser planParser = new PlanParser();
		// Parse the plan in plan text format
		Plan plan = planParser.parse("For 1 to 10 \n" +
				"Echo 'HELLO' \n" +
				"End");

		// Rename the plan
		plan.getRoot().getAttributes().put("name", "My Testcase");

		try(StepClient client = new StepClient(controllerUrl, user, password)) {
			// Run the plan on the controller
			PlanRunnerResult result = client.getPlanRunners().getRemotePlanRunner().run(plan);

			// Wait for the plan execution to terminate and print the report tree to the standard output
			result.waitForExecutionToTerminate().printTree();
		}
	}

	@Test
	public void isolatedExecutionDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		try(StepClient client = new StepClient(controllerUrl, user, password)) {
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

	public void runAnExistingPlan() throws IOException, TimeoutException, InterruptedException {
		try(StepClient client = new StepClient(controllerUrl, user, password)) {
			// The ID of the plan to be executed
			String planId = "theIdOfThePlanToBeExecuted";

			// Set the execution parameters (the drop-downs that are set on the execution screen in the UI)
			Map<String, String> executionParameters = new HashMap<>();
			executionParameters.put("env", "TEST");

			// Execute the plan
			String executionId = client.getExecutionManager().execute(planId, executionParameters);

			// Wait for the execution to terminate and visit the report tree
			client.getExecutionManager().getFuture(executionId).waitForExecutionToTerminate().visitReportNodes(node->{
				if(node instanceof TestCaseReportNode) {
					// Do somethind....
				}
			});
		}
	}

	@Test
	public void functionManagerDemo() throws Exception {
		try(StepClient stepClient = new StepClient(controllerUrl, user, password)) {
			// Create a DemoKeyword (javascript) and upload it to the controller
			Function keyword = uploadDemoKeyword(stepClient);

			FunctionExecutionService functionExecutionService = stepClient.getFunctionExecutionService();

			// Select an agent token from the GRID
			Map<String, Interest> tokenSelectionCriteria = new HashMap<>();
			TokenWrapper tokenHandle = functionExecutionService.getTokenHandle(null, tokenSelectionCriteria, true);

			try {
				// Build the input object
				Input<JsonObject> input = new Input<JsonObject>();
				// Set the name of the Keyword
				input.setFunction("Echo");
				input.setProperties(new HashMap<>());
				input.setPayload(Json.createObjectBuilder().build());

				// call the keyword executing it on the remote agent
				Output<JsonObject> result = functionExecutionService.callFunction(tokenHandle, keyword, input, JsonObject.class);
				// Assert that the Keyword has been executed properly
				Assert.assertEquals("OK :)", result.getPayload().getString("Result"));			
			} finally {
				// Return the agent token to the GRID
				functionExecutionService.returnTokenHandle(tokenHandle);
			}
		}
	}

	public void remoteControllerManagementDemo() throws IOException {
		try(StepClient client = new StepClient(controllerUrl, user, password)) {
			// Shutdown the controller gracefully
			client.getControllerServicesClient().shutdownController();
		}
	}

	protected GeneralScriptFunction uploadDemoKeyword(StepClient client) throws SetupFunctionException, FunctionTypeException {
		// Upload the javascript code of the keyword
		String resourceId = client.getResourceManager().upload(new File(this.getClass().getResource("DemoKeyword.js").getFile())).getResourceId();

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
		f.setScriptFile(new DynamicValue<String>(FileResolver.RESOURCE_PREFIX + resourceId));
		f.setAttributes(attributes);

		// Empty keyword schema
		f.setSchema(Json.createObjectBuilder().build());
		return f;
	}

	@Test
	public void localExecutionDemo() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException, ParsingException {
		PlanParser planParser = new PlanParser();
		// Parse the plan in plan text format
		Plan plan = planParser.parse("For 1 to 3 \n" +
				"MyCustomKeyword someInput=\"hello\" \n" +
				"Assert yourStringInputWas = \"hello\" \n" +
				"End");
	
		// Run the plan locally by pointing to the class(es) containing the required keyword(s)
		PlanRunnerResult result = new LocalPlanRunner(MyCustomKeyword.class).run(plan);

		// Wait for the plan execution to terminate and print the report tree to the standard output
		result.waitForExecutionToTerminate().printTree();
	}
}
