package ch.exense.step.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import step.artefacts.reports.TestCaseReportNode;
import step.client.ControllerClient;

public class ControllerAPIDemo {

	@Test
	public void runAnExistingPlan() throws IOException, TimeoutException, InterruptedException {
		try(ControllerClient client = new ControllerClient("http://step-enterprise-nightly.exense.ch", "admin", "init")) {
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
}
