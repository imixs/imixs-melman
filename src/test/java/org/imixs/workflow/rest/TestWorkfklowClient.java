package org.imixs.workflow.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
	@Disabled
	@Test
	public void testGetDocument() {

		BasicAuthenticator basicAuth = new BasicAuthenticator("admin", "adminadmin");

		WorkflowClient workflowCLient = new WorkflowClient("http://localhost:8080/office-rest/");

		workflowCLient.registerClientRequestFilter(basicAuth);

		ItemCollection document = null;
		try {
			document = workflowCLient.getWorkitem("f3357f0b-20de-40ca-8aa1-4b9f43759c0b");
		} catch (RestAPIException e) {
			fail(e.getMessage());
		}

		// compare result with test data
		assertNotNull(document);

	}

}
