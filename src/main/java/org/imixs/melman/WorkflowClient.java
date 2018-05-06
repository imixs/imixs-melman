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
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
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
public class WorkflowClient {

	private final static Logger logger = Logger.getLogger(WorkflowClient.class.getName());

	private Client client = null;
	private String base_uri = null;

	public WorkflowClient(String base_uri) {
		super();

		if (!base_uri.endsWith("/")) {
			base_uri = base_uri + "/";
		}
		this.base_uri = base_uri;

		logger.finest("......register jax-rs client for " + base_uri + "...");
		client = ClientBuilder.newClient();
	}

	public void registerClientRequestFilter(ClientRequestFilter filter) {
		logger.info("......register new request filter: " + filter.getClass().getSimpleName());
		client.register(filter);
	}

	/**
	 * Process a workitem
	 * 
	 * @param workitem
	 * @return updated instance
	 */
	public ItemCollection processWorkitem(ItemCollection workitem) {

		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(workitem);

		Response response = client.target(base_uri + "workflow/workitem/").request(MediaType.APPLICATION_XML)
				.post(Entity.entity(xmlWorkitem, MediaType.APPLICATION_XML));

		if (response.getStatus() == 200) {
			XMLDataCollection data = response.readEntity(XMLDataCollection.class);
			if (data != null && data.getDocument().length > 0) {
				// return first element of
				return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
			}
		}

		return null;
	}

	/**
	 * returns a document instance by UniqueID
	 * 
	 * @param uniqueid
	 * @param items
	 * @return document instance
	 */
	public ItemCollection getDocumentCustom(String uniqueid, String items) {

		XMLDataCollection data = client.target(base_uri + "documents/" + uniqueid).request(MediaType.APPLICATION_XML)
				.get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			if (data.getDocument().length == 0) {
				return null;
			}
			XMLDocument xmldoc = data.getDocument()[0];

			return XMLDocumentAdapter.putDocument(xmldoc);
		}

	}

	/**
	 * returns a workItem instance by UniqueID. This is just a wrapper for
	 * getDocumentCustom
	 * 
	 * @param uniqueid
	 * @param items
	 * @return workitem
	 */
	public ItemCollection getWorkitem(String uniqueid, String items) {
		return getDocumentCustom(uniqueid, items);
	}

	/**
	 * Returns the current task list by creator
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getTaskListByCreator(String userid, int pageSize, int pageIndex, String items) {
		return getWorkitemsByResource("/tasklist/creator/" + userid, 5, 0, null);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getTaskListByOwner(String userid, int pageSize, int pageIndex, String items) {
		return getWorkitemsByResource("/tasklist/owner/" + userid, 5, 0, null);
	}

	/**
	 * Returns the current task list by owner
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getWorkflowEventsByWorkitem(ItemCollection workitem) {

		XMLDataCollection data = client.target(base_uri + "workflow/workitem/events/" + workitem.getUniqueID())
				.request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}
	}
	
	

	/**
	 * Returns the custom data list by uri
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getCustomDataList(String uri) {
		XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);
		
		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}
	}

	/**
	 * Generic getter method returning a workitem resource
	 * 
	 * @param resource
	 * @param pageSize
	 * @param pageIndex
	 * @param items
	 * @return task list for given user
	 */
	private List<ItemCollection> getWorkitemsByResource(String resource, int pageSize, int pageIndex, String items) {

		String uri = base_uri + "workflow/" + resource + "?";
		if (pageSize > 0 || pageIndex > 0) {
			uri += "&pageSize=" + pageSize + "&pageIndex=" + pageIndex;
		}
		if (items != null && !items.isEmpty()) {
			uri += "&items=" + items;
		}

		XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}

	}
}
