package org.imixs.melman;
/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.MediaType;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

/**
 * This ServiceClient is a WebService REST Client which encapsulate the
 * communication with a REST web serice based on the Imixs Workflow REST API.
 * The Implementation is based on the JAXB API 2.0.
 * 
 * 
 * @author Ralph Soika
 * 
 */
public class WorkflowClient extends DocumentClient {

	private final static Logger logger = Logger.getLogger(WorkflowClient.class.getName());

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public WorkflowClient(String base_uri) {
		super(base_uri);
	}

	/**
	 * Process a single workitem instance. If the workitem is not yet managed by the
	 * workflow manger a new instance will be created.
	 * 
	 * @param workitem
	 *            - a ItemCollection representing the workitem.
	 * @return updated workitem instance
	 * @throws RestAPIException
	 */
	public ItemCollection processWorkitem(ItemCollection workitem) throws RestAPIException {
		logger.finest("......process workitem...");

		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(workitem);
		XMLDataCollection data = postXMLDocument(baseURI + "workflow/workitem/", xmlWorkitem);

		if (data != null && data.getDocument().length > 0) {
			// return first element of
			return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
		}
		return null;
	}

	/**
	 * Returns a single workItem instance by UniqueID.
	 * <p>
	 * The method calls the workflow rest interface instead of the document rest
	 * interface to ensure the corresponding backend method is accessed.
	 * 
	 * @param uniqueid
	 * @param items
	 * @return workitem
	 * @throws RestAPIException
	 */
	public ItemCollection getWorkitem(String uniqueid) throws RestAPIException {

		Client client = null;
		String uri = baseURI + "workflow/workitem/" + uniqueid;

		// test items..
		if (items != null && !items.isEmpty()) {
			uri += "?items=" + items;
		}
		try {
			client = newClient();
			XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

			if (data != null) {
				if (data.getDocument().length == 0) {
					return null;
				}
				XMLDocument xmldoc = data.getDocument()[0];
				return XMLDocumentAdapter.putDocument(xmldoc);
			}
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error request XMLDataCollection ->" + message, e);

		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	/**
	 * Deletes a single workItem instance by UniqueID.
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 * @throws RestAPIException
	 */
	public void deleteWorkitem(String uniqueid) throws RestAPIException {
		super.deleteDocument(uniqueid);
	}

	/**
	 * Returns the current task list by creator
	 * 
	 * @param userid
	 * @throws RestAPIException
	 */
	public List<ItemCollection> getTaskListByCreator(String userid) throws RestAPIException {
		return getWorkitemsByResource("/tasklist/creator/" + userid);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 * @throws RestAPIException
	 */
	public List<ItemCollection> getTaskListByOwner(String userid) throws RestAPIException {
		return getWorkitemsByResource("/tasklist/owner/" + userid);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 * @throws RestAPIException
	 */
	public List<ItemCollection> getWorkflowEventsByWorkitem(ItemCollection workitem) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();
			XMLDataCollection data = client.target(baseURI + "workflow/workitem/events/" + workitem.getUniqueID())
					.request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

			if (data != null) {
				return XMLDataCollectionAdapter.putDataCollection(data);
			}
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(WorkflowClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error requesting EventsByWorkitem", e);

		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	/**
	 * Generic getter method returning a list of workitems resource. All elements
	 * are from the type=workitem"
	 * 
	 * @param resource
	 * @param pageSize
	 * @param pageIndex
	 * @param items
	 * @return task list for given user
	 * @throws RestAPIException
	 */
	private List<ItemCollection> getWorkitemsByResource(String resource) throws RestAPIException {
		Client client = null;
		if (!resource.startsWith("/")) {
			resource = "/" + resource;
		}

		String uri = baseURI + "workflow" + resource;

		// set type
		uri += "?type=" + getType();

		// test pagesize, pageindex
		if (pageSize > 0 || pageIndex > 0) {
			uri += "&pageSize=" + pageSize + "&pageIndex=" + pageIndex;
		}

		// test sort order
		if (getSortBy() != null) {
			uri += "&sortBy=" + getSortBy();
		}
		if (isSortReverse()) {
			uri += "&sortReverse=" + isSortReverse();
		}

		// test items..
		if (items != null && !items.isEmpty()) {
			uri += "&items=" + items;
		}
		try {
			client = newClient();
			XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);
			if (data != null) {
				return XMLDataCollectionAdapter.putDataCollection(data);
			}
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(WorkflowClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error requesting " + resource, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}
}
