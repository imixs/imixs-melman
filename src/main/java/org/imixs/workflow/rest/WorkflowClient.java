package org.imixs.workflow.rest;
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

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.ItemCollection;
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

}
