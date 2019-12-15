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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

/**
 * This ServiceClient is a WebService REST Client which encapsulate the
 * communication with Imixs EventLog REST service. The Implementation is based
 * on the JAXB API 2.0.
 * 
 * 
 * @author Ralph Soika
 * 
 */
public class EventLogClient extends AbstractClient {

	public static final String ITEM_ERROR_CODE = "$error_code";
	public static final String ITEM_ERROR_MESSAGE = "$error_message";

	public final static int DEFAULT_PAGE_SIZE = 10;

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(EventLogClient.class.getName());

	protected int pageSize = DEFAULT_PAGE_SIZE;
	protected int pageIndex;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public EventLogClient(String base_uri) {
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

	/**
	 * Deletes a single workItem or document instance by UniqueID.
	 * 
	 * @param userid
	 * @throws RestAPIException
	 */
	public void deleteEventLogEntry(String eventLogID) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();
			String uri = baseURI + "eventlog/" + eventLogID;
			client.target(uri).request(MediaType.APPLICATION_XML).delete();
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error delete eventLog ->" + message, e);
	
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}


	/**
	 * Loads a collection of EventLog entries for a specific topic
	 * 
	 * @param topic
	 *            - list of topics
	 * @return eventLog entries
	 * @throws RestAPIException
	 */
	public List<ItemCollection> searchEventLog(String... topic) throws RestAPIException {
		List<ItemCollection> eventLogEntries = null;

		String topicList = "";
		for (String _topic : topic) {
			topicList += _topic + "~";
		}
		if (topicList.endsWith("~")) {
			topicList = topicList.substring(0, topicList.length() - 1);
		}

		// load eventLog entries.....
		eventLogEntries = getCustomResource("/eventlog/" + topicList);
		logger.finest("......" + eventLogEntries.size() + " event log entries found");

		return eventLogEntries;
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
	private XMLDataCollection getCustomResourceXML(String uri) throws RestAPIException {
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
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(EventLogClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION,
					"error requesting custom XMLDataCollection ->" + message, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		// no data!
		return null;
	}

	/**
	 * Returns the custom data list by uri GET
	 * 
	 * @param userid
	 * @param items
	 * @return result list
	 * @throws RestAPIException
	 */
	private List<ItemCollection> getCustomResource(String uri) throws RestAPIException {
		XMLDataCollection data = null;
		data = getCustomResourceXML(uri);
		if (data == null) {
			return null;
		} else {
			return XMLDataCollectionAdapter.putDataCollection(data);
		}

	}

}