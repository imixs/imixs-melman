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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;

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

}
