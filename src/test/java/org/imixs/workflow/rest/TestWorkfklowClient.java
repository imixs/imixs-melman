package org.imixs.workflow.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.OIDCAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test class for workflowCLient
 * 
 * @author rsoika
 * 
 */
public class TestWorkfklowClient {

	String baseURL = "";

	@BeforeEach
	public void setup() throws PluginException {
		baseURL = "http://localhost:8080/api/";
	}

	/**
	 * test Basic Auth
	 */
	@Disabled
	@Test
	public void testGetDocumentBasicAuth() {

		BasicAuthenticator basicAuth = new BasicAuthenticator("admin", "adminadmin");

		WorkflowClient workflowCLient = new WorkflowClient(baseURL);

		workflowCLient.registerClientRequestFilter(basicAuth);

		ItemCollection document = null;
		try {
			List<ItemCollection> result = workflowCLient.getTaskListByOwner(null);
			assertNotNull(result);
			document = result.get(0);

			// compare result with test data
			assertNotNull(document);
		} catch (RestAPIException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * test Form Based Auth
	 */
	@Disabled
	@Test
	public void testGetDocumentFormAuth() {

		try {
			FormAuthenticator basicAuth = new FormAuthenticator(baseURL, "admin", "adminadmin");

			WorkflowClient workflowCLient = new WorkflowClient(baseURL);

			workflowCLient.registerClientRequestFilter(basicAuth);

			ItemCollection document = null;

			List<ItemCollection> result = workflowCLient.getTaskListByOwner(null);
			assertNotNull(result);
			document = result.get(0);

			// compare result with test data
			assertNotNull(document);
		} catch (RestAPIException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * test OIDC
	 */
	@Disabled
	@Test
	public void testGetDocumentOIDC() {

		try {
			// Keycloak
			String tokenEndpoint = "https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token";
			OIDCAuthenticator auth = new OIDCAuthenticator(
					tokenEndpoint, "my-client", "secret", "anna", "password123");

			WorkflowClient workflowCLient = new WorkflowClient(baseURL);

			workflowCLient.registerClientRequestFilter(auth);

			ItemCollection document = null;

			List<ItemCollection> result = workflowCLient.getTaskListByOwner(null);
			assertNotNull(result);
			document = result.get(0);

			// compare result with test data
			assertNotNull(document);
		} catch (RestAPIException e) {
			fail(e.getMessage());
		}

	}

}
