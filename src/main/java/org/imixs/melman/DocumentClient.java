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
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLCount;
import org.imixs.workflow.xml.XMLDataCollection;
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
public class DocumentClient extends AbstractClient {

	public static final String ITEM_ERROR_CODE = "$error_code";
	public static final String ITEM_ERROR_MESSAGE = "$error_message";

	public final static int DEFAULT_PAGE_SIZE = 10;
	public final static String DEFAULT_TYPE = "workitem";
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(DocumentClient.class.getName());

	protected String sortBy;
	protected boolean sortReverse;
	protected String type = DEFAULT_TYPE;
	protected int pageSize = DEFAULT_PAGE_SIZE;
	protected int pageIndex;
	protected String items = null;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public DocumentClient(String base_uri) {
		super(base_uri);
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
	 * Creates or updates a single document instance.
	 * 
	 * @param document
	 *            - a ItemCollection representing the document.
	 * @return updated document instance
	 * @throws RestAPIException
	 */
	public ItemCollection saveDocument(ItemCollection document) throws RestAPIException {
		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(document);
		XMLDataCollection data = postXMLDocument(baseURI + "documents/", xmlWorkitem);

		if (data != null && data.getDocument().length > 0) {
			// return first element of
			return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
		}
		return null;

	}

	/**
	 * Creates a new AdminPJobInstance
	 * 
	 * @param document
	 *            - a ItemCollection representing the job.
	 * @return updated job instance
	 * @throws RestAPIException
	 */
	public ItemCollection createAdminPJob(ItemCollection document) throws RestAPIException {
		XMLDocument xmlWorkitem = XMLDocumentAdapter.getDocument(document);
		XMLDataCollection data = postXMLDocument(baseURI + "adminp/jobs/", xmlWorkitem);

		if (data != null && data.getDocument().length > 0) {
			// return first element of
			return XMLDocumentAdapter.putDocument(data.getDocument()[0]);
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
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error delete document ->" + message, e);

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
	 * @param query
	 *            - lucene search query
	 * @return result list
	 * @throws RestAPIException
	 * @throws UnsupportedEncodingException
	 */
	public List<ItemCollection> searchDocuments(String query) throws RestAPIException, UnsupportedEncodingException {
		String uri = "documents/search/";
		// encode search query...
		query = URLEncoder.encode(query, "UTF-8");
		uri = uri + query;
		
		  // test pagesize, pageindex
		if (pageSize ==0) {
		    pageSize=-1;
		}
		uri += "?pageSize=" + pageSize + "&pageIndex=" + pageIndex;
        
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
		
		// search.....
		List<ItemCollection> searchResult = getCustomResource(uri);
		return searchResult;
	}

	/**
	 * Counts a given lucene search result
	 * <p>
	 * The method returns the count of documents included in the result of a given
	 * lucene query
	 * 
	 * @param query
	 *            - lucene search query
	 * @return count of total hits
	 * @throws RestAPIException
	 * @throws UnsupportedEncodingException
	 */
	public long countDocuments(String query) throws RestAPIException, UnsupportedEncodingException {
		String uri = "documents/count/";
		// encode search query...
		query = URLEncoder.encode(query, "UTF-8");
		uri = uri + query;

		// count
		Client client = null;
		XMLCount xmlcount = null;

		uri = getBaseURI() + uri;

		try {
			client = newClient();
			xmlcount = client.target(uri).request(MediaType.APPLICATION_XML).get(XMLCount.class);
			if (xmlcount != null) {
				return xmlcount.count;
			}
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error countDocuments ->" + message, e);

		} finally {
			if (client != null) {
				client.close();
			}
		}
		// no data!
		return 0;

	}

	
	/**
	 * Posts a XMLDocument to a custom resource.
	 * <p>
	 * This method expects that the response is a XMLDataCollection containing the
	 * posted document. In case of an HTTP Result other than 200=OK the method
	 * throws an exception containing the the error_code and error_message stored in
	 * the returnded XMLDocument
	 * 
	 * @param document
	 *            - a ItemCollection representing the document.
	 * @return updated document instance or null if no document was returned by the
	 *         API
	 * @throws RestAPIException
	 */
	public XMLDataCollection postXMLDocument(String uri, XMLDocument xmlWorkitem) throws RestAPIException {
		Client client = null;

		// strip first / if available
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		// verify if uri has protocoll
		if (!uri.matches("\\w+\\:.*")) {
			// add base url
			uri = getBaseURI() + uri;
		}
		
		// test items..
		if (items != null && !items.isEmpty()) {
			if (uri.contains("?")) {
				uri+="&";
			} else {
				uri+="?";
			}
			uri += "items=" + items;
		}

		try {
			client = newClient();
			Response response = client.target(uri).request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xmlWorkitem, MediaType.APPLICATION_XML));

			if (response.getStatus() < 300) {
				// read result...
				//if (response.hasEntity() && response.getLength() > 0) {
			    // see Issue #37
                if (response.hasEntity()) {
					XMLDataCollection data = response.readEntity(XMLDataCollection.class);
					if (data != null && data.getDocument().length > 0) {
						ItemCollection result = XMLDocumentAdapter.putDocument(data.getDocument()[0]);
						// HTTP OK?
						if (response.getStatus() == Response.Status.OK.getStatusCode()) {
							// return first element of
							return data;
						} else {
							// handle error code...
							throw new RestAPIException(DocumentClient.class.getSimpleName(),
									result.getItemValueString(ITEM_ERROR_CODE),
									result.getItemValueString(ITEM_ERROR_MESSAGE));
						}
					}
				}
			} else {

				// no object returned or HTTP Code >=300 -> throw a RestAPIException
				throw new RestAPIException(DocumentClient.class.getSimpleName(), "" + response.getStatus(),
						response.getStatus() + ": " + response.getStatusInfo().getReasonPhrase());
			}

		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error post XMLDocument ->" + message, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

		return null;
	}

	/**
	 * Posts a XMLDocument collection to a custom resource.
	 * 
	 * 
	 * @param documents
	 *            - a collection of ItemCollection objects
	 * @throws RestAPIException
	 */
	public void postXMLDataCollection(String uri, XMLDataCollection xmlDataCollection) throws RestAPIException {
		Client client = null;

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
			Response response = client.target(uri).request(MediaType.APPLICATION_XML)
					.post(Entity.entity(xmlDataCollection, MediaType.APPLICATION_XML));
			// no object returned - so we throw a RestAPIException
			if (response.getStatus() >= 300) {
				throw new RestAPIException(DocumentClient.class.getSimpleName(), "" + response.getStatus(),
						"" + response.getStatusInfo());
			}

		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error post XMLDataCollection ->" + message, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

	}


	

}
