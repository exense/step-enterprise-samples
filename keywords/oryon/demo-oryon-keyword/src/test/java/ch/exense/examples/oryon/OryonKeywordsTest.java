package ch.exense.examples.oryon;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.Assert;
import step.grid.io.OutputMessage;
import step.handlers.javahandler.KeywordRunner;
import step.handlers.javahandler.KeywordRunner.ExecutionContext;

public class OryonKeywordsTest {

	ExecutionContext context;
	
	public void before() {
		// Put your Keyword properties here
		Map<String, String> properties = new HashMap<>();
		
		context = KeywordRunner.getExecutionContext(properties, OryonKeywords.class);
	}
	
	//@Test
	public void test() throws Exception {
		context.run("Login","{\"Username\":\"MyUser\"}");
		OutputMessage output = context.run("Search","{\"Criteria\":\"My search criteria\"}");
		Assert.assertEquals("My Output", output.getPayload().getString("Result"));
	}
}
