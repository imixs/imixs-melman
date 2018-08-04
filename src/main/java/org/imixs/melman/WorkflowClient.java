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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	 * @param workitem - a ItemCollection representing the workitem.
	 * @return updated workitem instance
	 */
	public ItemCollection processWorkitem(ItemCollection workitem) {
		Client client = null;
		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(workitem);
		try {
			client = newClient();
			Response response = client.target(baseURI + "workflow/workitem/").request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xmlWorkitem, MediaType.APPLICATION_XML));

			if (response.getStatus() == 200) {
				XMLDataCollection data = response.readEntity(XMLDataCollection.class);
				if (data != null && data.getDocument().length > 0) {
					// return first element of
					return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
				}
			}
		} catch (ResponseProcessingException e) {
			logger.severe("error requesting process document -> " + e.getMessage());
			setErrorMessage(e.getMessage());
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	/**
	 * Returns a single workItem instance by UniqueID.
	 * 
	 * @param uniqueid
	 * @param items
	 * @return workitem
	 */
	public ItemCollection getWorkitem(String uniqueid) {
		return getDocument(uniqueid);
	}

	/**
	 * Deletes a single workItem instance by UniqueID.
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public void deleteWorkitem(String uniqueid) {
		super.deleteDocument(uniqueid);
	}

	/**
	 * Returns the current task list by creator
	 * 
	 * @param userid
	 */
	public List<ItemCollection> getTaskListByCreator(String userid) {
		return getWorkitemsByResource("/tasklist/creator/" + userid);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getTaskListByOwner(String userid) {
		return getWorkitemsByResource("/tasklist/owner/" + userid);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getWorkflowEventsByWorkitem(ItemCollection workitem) {
		Client client = null;
		try {
			client = newClient();
			XMLDataCollection data = client.target(baseURI + "workflow/workitem/events/" + workitem.getUniqueID())
					.request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

			if (data != null) {
				return XMLDataCollectionAdapter.putDataCollection(data);
			}
		} catch (ResponseProcessingException e) {
			logger.severe("error requesting get events -> " + e.getMessage());
			setErrorMessage(e.getMessage());
			e.printStackTrace();
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
	 */
	private List<ItemCollection> getWorkitemsByResource(String resource) {
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
			logger.severe("error requesting " + resource + " -> " + e.getMessage());
			setErrorMessage(e.getMessage());
			e.printStackTrace();			
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}
}
