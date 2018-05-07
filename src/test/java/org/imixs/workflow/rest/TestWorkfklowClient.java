package org.imixs.workflow.rest;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class for workflowCLient
 * 
 * @author rsoika
 * 
 */
public class TestWorkfklowClient {

	/**
	 * test computeDynammicDate
	 */
	@Ignore
	@Test
	public void testGetDocument() {

		BasicAuthenticator basicAuth = new BasicAuthenticator("admin", "adminadmin");

		WorkflowClient workflowCLient = new WorkflowClient("http://localhost:8080/office-rest/");

		workflowCLient.registerClientRequestFilter(basicAuth);

		ItemCollection document = workflowCLient.getWorkitem("f3357f0b-20de-40ca-8aa1-4b9f43759c0b", null);

		// compare result with test data
		Assert.assertNotNull(document);

	}

}
