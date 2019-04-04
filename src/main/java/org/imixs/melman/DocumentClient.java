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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
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
public class DocumentClient {

	public final static int DEFAULT_PAGE_SIZE = 10;
	public final static String DEFAULT_TYPE = "workitem";
	private final static Logger logger = Logger.getLogger(DocumentClient.class.getName());

	protected String baseURI = null;
	protected String sortBy;
	protected boolean sortReverse;
	protected String type = DEFAULT_TYPE;
	protected int pageSize = DEFAULT_PAGE_SIZE;
	protected int pageIndex;
	protected String items = null;

	protected List<ClientRequestFilter> requestFilterList;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public DocumentClient(String base_uri) {
		super();

		requestFilterList = new ArrayList<ClientRequestFilter>();

		if (!base_uri.endsWith("/")) {
			base_uri = base_uri + "/";
		}
		this.baseURI = base_uri;

		logger.finest("......register jax-rs client for " + base_uri + "...");
	}

	/**
	 * Register a ClientRequestFilter instance.
	 * 
	 * @param filter - request filter instance.
	 */
	public void registerClientRequestFilter(ClientRequestFilter filter) {
		logger.finest("......register new request filter: " + filter.getClass().getSimpleName());

		// client.register(filter);
		requestFilterList.add(filter);
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
	 * This method creates a new javax.ws.rs.client.Client instance using the
	 * default client builder implementation class provided by the JAX-RS
	 * implementation provider.
	 * <p>
	 * The method registers all known filter instances.
	 * <p>
	 * The client instance should be closed after the request if finished.
	 * 
	 * @return javax.ws.rs.client.Client instance
	 */
	public Client newClient() {
		Client client = ClientBuilder.newClient();
		for (ClientRequestFilter filter : requestFilterList) {
			client.register(filter);
		}
		return client;
	}

	/**
	 * Creates or updates a single document instance.
	 * 
	 * @param document - a ItemCollection representing the document.
	 * @return updated document instance
	 * @throws RestAPIException
	 */
	public ItemCollection saveDocument(ItemCollection document) throws RestAPIException {
		Client client = null;
		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(document);
		try {
			client = newClient();
			Response response = client.target(baseURI + "documents/").request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xmlWorkitem, MediaType.APPLICATION_XML));

			if (response.getStatus() == 200) {
				XMLDataCollection data = response.readEntity(XMLDataCollection.class);
				if (data != null && data.getDocument().length > 0) {
					// return first element of
					return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
				}
			}
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error save document", e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

		return null;
	}

	/**
	 * Creates a new AdminPJobInstance
	 * 
	 * @param document - a ItemCollection representing the job.
	 * @return updated job instance
	 * @throws RestAPIException
	 */
	public ItemCollection createAdminPJob(ItemCollection document) throws RestAPIException {
		Client client = null;
		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(document);
		try {
			client = newClient();

			Response response = client.target(baseURI + "adminp/jobs/").request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xmlWorkitem, MediaType.APPLICATION_XML));

			if (response.getStatus() == 200) {
				XMLDataCollection data = response.readEntity(XMLDataCollection.class);
				if (data != null && data.getDocument().length > 0) {
					// return first element of
					return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
				}
			}
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error requesting create adminPJob", e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	/**
	 * Returns a single document instance by UniqueID.
	 * 
	 * @param uniqueid
	 * @return workitem
	 * @throws RestAPIException
	 */
	public ItemCollection getDocument(String uniqueid) throws RestAPIException {
		Client client = null;
		String uri = baseURI + "documents/" + uniqueid;

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
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error requesting URL: " + uri, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	/**
	 * Deletes a single workItem or document instance by UniqueID.
	 * 
	 * @param userid
	 * @throws RestAPIException
	 */
	public void deleteDocument(String uniqueid) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();
			String uri = baseURI + "documents/" + uniqueid;
			client.target(uri).request(MediaType.APPLICATION_XML).delete();
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error delete request : " + uniqueid, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * Returns the the search result of a lucene search.
	 * <p>
	 * The method creates a search URL and requests a CustomResource by GET. The
	 * lucene query is encoded by this method. The method throws a
	 * UnsupportedEncodingException if the query string can not be encoded.
	 * 
	 * @param query - lucene search query
	 * @return result list
	 * @throws RestAPIException
	 * @throws UnsupportedEncodingException
	 */
	public List<ItemCollection> searchDocuments(String query) throws RestAPIException, UnsupportedEncodingException {
		String uri = "documents/search/";
		// encode search query...
		query = URLEncoder.encode(query, "UTF-8");
		uri = uri + query;
		// search.....
		List<ItemCollection> searchResult = getCustomResource(uri);
		return searchResult;
	}

	/**
	 * Returns the custom data list by uri GET
	 * 
	 * @param userid
	 * @param items
	 * @return result list
	 * @throws RestAPIException
	 */
	public List<ItemCollection> getCustomResource(String uri) throws RestAPIException {
		XMLDataCollection data = null;
		data = getCustomResourceXML(uri);
		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}

	}

	/**
	 * Returns the custom data list by uri GET as a collection of XMLDocument
	 * elements.
	 * 
	 * @param userid
	 * @param items
	 * @return result list of XMLDocument elements
	 * @throws RestAPIException
	 */
	public XMLDataCollection getCustomResourceXML(String uri) throws RestAPIException {
		Client client = null;
		XMLDataCollection data = null;

		// strip first / if available
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		// verify if uri has protocoll
		if (!uri.matches("\\w+\\:.*")) {
			// add base url
			uri = getBaseURI() + uri;
		}

		try {
			client = newClient();
			data = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);
			if (data != null) {
				return data;
			}
		} catch (ResponseProcessingException e) {
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error requesting URL: " + uri, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		// no data!
		return null;
	}

}
