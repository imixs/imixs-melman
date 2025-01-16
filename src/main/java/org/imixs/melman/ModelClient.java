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

package org.imixs.melman;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openbpmn.bpmn.BPMNModel;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
	 * Posts an XML file as InputStream to the Model service.
	 *
	 * @param inputStream - InputStream pointing to a BPMN XML file
	 * @throws RestAPIException
	 */
	public void postModel(InputStream inputStream) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();

			// Konvertiere den InputStream zu einem byte array
			byte[] data = inputStream.readAllBytes();

			Response response = client.target(baseURI + "model/bpmn/")
					.request()
					.header("Content-Type", "application/xml")
					.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE));

			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				throw new RestAPIException(
						DocumentClient.class.getSimpleName(),
						RestAPIException.RESPONSE_PROCESSING_EXCEPTION,
						"error post BPMN XML -> " + response.getStatusInfo().getReasonPhrase());
			}
		} catch (ProcessingException | IOException e) {
			String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			throw new RestAPIException(
					DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION,
					"error post BPMN XML -> " + message,
					e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	public void postModel(BPMNModel model) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();

			// XML Document in byte[] konvertieren
			org.w3c.dom.Document xmlDoc = model.getDoc();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(xmlDoc), new StreamResult(baos));
			byte[] xmlData = baos.toByteArray();

			// POST Request ausführen
			Response response = client.target(baseURI + "model/bpmn/")
					.request()
					.header("Content-Type", "application/xml")
					.post(Entity.entity(xmlData, MediaType.APPLICATION_OCTET_STREAM_TYPE));

			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				throw new RestAPIException(
						DocumentClient.class.getSimpleName(),
						RestAPIException.RESPONSE_PROCESSING_EXCEPTION,
						"error post BPMN XML -> " + response.getStatusInfo().getReasonPhrase());
			}
		} catch (ProcessingException | TransformerException e) {
			String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			throw new RestAPIException(
					DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION,
					"error post BPMN XML -> " + message,
					e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * Deletes a model by its version
	 * 
	 * @param userid
	 * @throws RestAPIException
	 */
	public void deleteModel(String version) throws RestAPIException {
		Client client = null;
		try {
			client = newClient();
			String uri = baseURI + "model/" + version;
			client.target(uri).request(MediaType.APPLICATION_XML).delete();
		} catch (ProcessingException e) {
			String message = null;
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			throw new RestAPIException(DocumentClient.class.getSimpleName(),
					RestAPIException.RESPONSE_PROCESSING_EXCEPTION, "error delete model ->" + message, e);

		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

}
