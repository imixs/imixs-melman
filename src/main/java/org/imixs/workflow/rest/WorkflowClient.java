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
import javax.ws.rs.core.MediaType;

//import org.glassfish.jersey.jackson.JacksonFeature;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;
 
/**
 * This ServiceClient is a WebService REST Client which encapsulate the
 * communication with a REST web serice based on the Imixs Workflow REST API.
 * The Implementation is based on the JAXB API.
 * 
 * The ServiceClient supports methods for posting EntityCollections and
 * XMLItemCollections.
 * 
 * The post method expects the rest service URI and a Dataobject based ont the
 * Imixs Workflow XML API
 * 
 * 
 * http://www.adam-bien.com/roller/abien/entry/client_side_http_basic_access
 * 
 * 
 * 
 * @see org.imixs.workflow.jee.rest
 * @author Ralph Soika
 * 
 */
public class WorkflowClient {

	
	
	private final static Logger logger = Logger.getLogger(WorkflowClient.class.getName());

	public ItemCollection getDocumentCustom(String uniqueid, String items) {

		//Client client = ClientBuilder.newClient().register(JacksonFeature.class);
		 
		Client client = ClientBuilder.newClient();
		 
		client.register(new BasicAuthenticator("admin", "adminadmin"));
		 

		// Client client = ClientFactory.newClient();
		// WebTarget target = client.target("http://example.com/shop");
		// Form form = new Form().param("customer", "Bill")
		// .param("product", "IPhone 5")
		// .param("CC", "4444 4444 4444 4444");
		//
		// XMLDataCollection data=client.

		XMLDataCollection data = client.target("http://localhost:8080/office-rest/documents/" + uniqueid)
				.request(MediaType.APPLICATION_XML).get(XMLDataCollection.class);

		if (data == null) {
			return null;
		} else {
			if (data.getDocument().length==0) {
				return null;
			}
			XMLDocument xmldoc = data.getDocument()[0];

			return XMLDocumentAdapter.putDocument(xmldoc);
		}

	//	return null;
	}

	/*
	 * public ItemCollection getDocument(String uniqueid, String items) throws
	 * MalformedURLException {
	 * 
	 * URL apiUrl = new URL("http://localhost:8080/office-rest"); DocumentService
	 * documentService =
	 * RestClientBuilder.newBuilder().baseUrl(apiUrl).build(DocumentService.class);
	 * 
	 * XMLDataCollection docs = documentService.getDocument(uniqueid, items); if
	 * (docs == null) { return null; } else { XMLDocument xmldoc =
	 * docs.getDocument()[0];
	 * 
	 * return XMLDocumentAdapter.putDocument(xmldoc); } }
	 */

}
