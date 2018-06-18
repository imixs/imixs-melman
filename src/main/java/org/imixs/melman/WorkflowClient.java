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

	public final static int DEFAULT_PAGE_SIZE = 10;
	public final static String DEFAULT_TYPE = "workitem";

	private final static Logger logger = Logger.getLogger(WorkflowClient.class.getName());

	private Client client = null;
	private String baseURI = null;
	private String sortBy;
	private boolean sortReverse;
	private String type = DEFAULT_TYPE;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private int pageIndex;
	private String items = null;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public WorkflowClient(String base_uri) {
		super();

		if (!base_uri.endsWith("/")) {
			base_uri = base_uri + "/";
		}
		this.baseURI = base_uri;

		logger.finest("......register jax-rs client for " + base_uri + "...");
		client = ClientBuilder.newClient();
	}

	/**
	 * Register a ClientRequestFilter instance.
	 * 
	 * @param filter
	 *            - request filter instance.
	 */
	public void registerClientRequestFilter(ClientRequestFilter filter) {
		logger.info("......register new request filter: " + filter.getClass().getSimpleName());
		client.register(filter);
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	/**
	 * retruns the document type. The default value is "workitem"
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public boolean isSortReverse() {
		return sortReverse;
	}

	public void setSortReverse(boolean sortReverse) {
		this.sortReverse = sortReverse;
	}

	public void setSortOrder(String sortBy, boolean sortReverse) {
		setSortBy(sortBy);
		setSortReverse(sortReverse);
	}

	/**
	 * Process a single workitem instance. If the workitem is not yet managed by the
	 * workflow manger a new instance will be created.
	 * 
	 * @param workitem
	 *            - a ItemCollection representing the workitem.
	 * @return updated workitem instance
	 */
	public ItemCollection processWorkitem(ItemCollection workitem) {

		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(workitem);

		Response response = client.target(baseURI + "workflow/workitem/").request(MediaType.APPLICATION_XML)
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
	 * Returns a single workItem or document instance by UniqueID.
	 * 
	 * @param uniqueid
	 * @param items
	 * @return workitem
	 */
	public ItemCollection getWorkitem(String uniqueid) {

		String uri = baseURI + "documents/" + uniqueid;

		// test items..
		if (items != null && !items.isEmpty()) {
			uri += "?items=" + items;
		}

		XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

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
	 * Deletes a single workItem or document instance by UniqueID.
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public void deleteWorkitem(String uniqueid) {
		String uri = baseURI + "documents/" + uniqueid;
		client.target(uri).request(MediaType.APPLICATION_XML).delete(XMLDataCollection.class);
	}

	/**
	 * Returns the current task list by creator
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
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

		XMLDataCollection data = client.target(baseURI + "workflow/workitem/events/" + workitem.getUniqueID())
				.request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}
	}

	/**
	 * Returns the custom data list by uri GET
	 * 
	 * @param userid
	 * @param items
	 * @return task list for given user
	 */
	public List<ItemCollection> getCustomResource(String uri) {
		XMLDataCollection data = null;
		
		// strip first / if available
		if (uri.startsWith("/")) {
			uri=uri.substring(1);
		}
		// verify if uri has protocoll
		if (!uri.matches("\\w+\\:.*")) {
			// add base url
			uri=getBaseURI() + uri;
		}
		data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);
		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}
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

		XMLDataCollection data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}

	}
}
