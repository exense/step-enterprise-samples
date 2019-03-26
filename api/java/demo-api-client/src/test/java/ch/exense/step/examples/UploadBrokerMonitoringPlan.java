package ch.exense.step.examples;

import static step.planbuilder.BaseArtefacts.sequence;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.poi.hssf.record.SCLRecord;
import org.junit.Test;

import junit.framework.Assert;
import step.artefacts.Script;
import step.artefacts.Sequence;
import step.artefacts.Set;
import step.client.StepClient;
import step.client.executions.RemoteExecutionFuture;
import step.core.artefacts.reports.ReportNodeStatus;
import step.core.dynamicbeans.DynamicValue;
import step.core.plans.Plan;
import step.core.plans.builder.PlanBuilder;
import step.functions.type.FunctionTypeException;
import step.functions.type.SetupFunctionException;

public class UploadBrokerMonitoringPlan {
	
	public static final String runPlanOnController = "http://localhost:8080";
	public static final String monitoredController = "http://localhost:8080";
	
	public static final String brokerServiceURL = "/rest/eventbroker/events/monitoring/global";
	
	@Test
	public void uploadBrokerMonitoringPlan() throws SetupFunctionException, FunctionTypeException, IOException, TimeoutException, InterruptedException {
		
		try(StepClient client = new StepClient(runPlanOnController, "admin", "init")) {
			
			/*** Implement the blocks of our monitoring plan in pure groovy ***/
			
			// Call broker service
			Set response = new Set();
			response.setKey(new DynamicValue<String>("response"));
			DynamicValue<String> urlCallValue = new DynamicValue<String>();
			urlCallValue.setDynamic(true);
			urlCallValue.setExpression("new URL(\""+monitoredController + brokerServiceURL+"\").text");
			response.setValue(urlCallValue);
			response.setDescription("Set - call service");
			
			// Parse result
			Set myjson = new Set();
			myjson.setKey(new DynamicValue<String>("myjson"));
			DynamicValue<String> parsingValue = new DynamicValue<String>();
			parsingValue.setDynamic(true);
			parsingValue.setExpression("new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, Class.forName('java.util.Map'))");
			myjson.setValue(parsingValue);
			myjson.setDescription("Set - parse response");
			
			//Debug
			//myjson['d_size'].toLong() + ';' + myjson['d_sizeWaterMark'].toLong()
			
			
			// Save data as measurement
			Script script = new Script();
			script.setScript(
					"ma = org.rtm.commons.MeasurementAccessor.getInstance()\r\n" +
					// Get current timestamp
					"thisTime = System.currentTimeMillis()\r\n" +
					
					"Map<String, Object> dataMap1 = new HashMap()\r\n" +
					"dataMap1.put('begin', thisTime)\r\n" + 
					"dataMap1.put('eId', 'monitor')\r\n" + 
					"dataMap1.put('value', myjson['d_size'].toLong())\r\n" + 
					"dataMap1.put('name', 'broker_size')\r\n" + 
					"ma.sendStructuredMeasurement(dataMap1)\r\n" +
					
					"Map<String, Object> dataMap2 = new HashMap()\r\n" + 
					"dataMap2.put('begin', thisTime)\r\n" + 
					"dataMap2.put('eId', 'monitor')\r\n" + 
					"dataMap2.put('value', myjson['d_sizeWaterMark'].toLong())\r\n" + 
					"dataMap2.put('name', 'broker_watermark')\r\n" + 
					"ma.sendStructuredMeasurement(dataMap2)\n" +
					""
					);

			/***/
			
			// Assemble the plan blocks into a step Plan object
			Sequence root = sequence();
			root.setDescription("SelfMonitoring_Broker");
			root.getAttributes().put("name", "SelfMonitoring_Broker");
			
			Plan plan = PlanBuilder.create()
					.startBlock(root)
						.add(response)
						.add(myjson)
						.add(script)
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
			
			// TODO: Schedule
		}
	}
}
