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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

/**
 * This AbstractClient provides core functionallity of a JAX Rest Client and the
 * feature to register authentication filters.
 * 
 * 
 * @author Ralph Soika
 * 
 */
public abstract class AbstractClient {

	private final static Logger logger = Logger.getLogger(AbstractClient.class.getName());

	protected String baseURI = null;

	protected List<ClientRequestFilter> requestFilterList;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public AbstractClient(String base_uri) {
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
		} catch (NotFoundException | ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(EventLogClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "GET " + uri + " failed ->" + message, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		// no data!
		return null;
	}

	/**
	 * Returns a WebTarget object for a given uri.
	 * 
	 * @param uri
	 * @return WebTarget
	 * @throws RestAPIException
	 */
	public WebTarget getWebTarget(String uri) throws RestAPIException {
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
		client = newClient();
		return client.target(uri);
	}

}
