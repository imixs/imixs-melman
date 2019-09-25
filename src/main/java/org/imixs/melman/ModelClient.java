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

import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.bpmn.BPMNModel;

/**
 * This ServiceClient is a WebService REST Client which encapsulate the
 * communication with a REST web serice based on the Imixs Workflow REST API.
 * The Implementation is based on the JAXB API 2.0.
 * 
 * 
 * @author Ralph Soika
 * 
 */
public class ModelClient extends AbstractClient {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(ModelClient.class.getName());

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public ModelClient(String base_uri) {
		super(base_uri);
	}

	/**
	 * Posts a byte array of a BPMN Model to the Model service.
	 * 
	 * @param data - byte array of a bpmn model file
	 *
	 * @throws RestAPIException
	 */
	public void postModel(BPMNModel model) throws RestAPIException {

		Client client = null;

		try {
			client = newClient();

			client.register(new BPMNWriter());
			client.target(baseURI + "model/bpmn/").request(MediaType.APPLICATION_XML)
					.post(Entity.entity(model, MediaType.APPLICATION_XML));

		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error post BPMNModel ->" + message, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

		return;
	}

}
