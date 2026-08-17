package org.imixs.melman;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

/**
 * This AbstractClient provides core functionality of a JAX Rest Client and the
 * feature to register authentication filters.
 * 
 * 
 * @author Ralph Soika
 * 
 */
public abstract class AbstractClient {

	private final static Logger logger = Logger.getLogger(AbstractClient.class.getName());

	protected String baseURI = null;

	protected SSLContext sslContext = null;

	// Default timeouts to prevent indefinite blocking on unresponsive servers.
	// Can be overridden per client instance via
	// setConnectTimeout()/setReadTimeout().
	private static final long DEFAULT_CONNECT_TIMEOUT = 10;
	private static final long DEFAULT_READ_TIMEOUT = 5 * 60; // 5 minutes
	private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

	protected long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	protected long readTimeout = DEFAULT_READ_TIMEOUT;
	protected TimeUnit timeoutUnit = DEFAULT_TIMEOUT_UNIT;

	protected List<ClientRequestFilter> requestFilterList;

	/**
	 * Initialize the client by a BASE_URL.
	 * 
	 * @param base_uri
	 */
	public AbstractClient(String base_uri) {
		super();

		if (base_uri != null) {
			requestFilterList = new ArrayList<ClientRequestFilter>();

			if (!base_uri.endsWith("/")) {
				base_uri = base_uri + "/";
			}
			this.baseURI = base_uri;

			logger.finest("......register jax-rs client for " + base_uri + "...");

			// test if a noop ssl context is needed
			String useInsecure = System.getenv("IMIXS_REST_CLIENT_INSECURE");
			if (useInsecure != null && useInsecure.equalsIgnoreCase("true")) {
				try {
					initNoopTrustManager();
				} catch (KeyManagementException | NoSuchAlgorithmException e) {
					logger.severe("Failed to setup noopTrustManager: " + e.getMessage());
				}
			}

			String envReadTimeout = System.getenv("IMIXS_REST_CLIENT_READ_TIMEOUT");
			if (envReadTimeout != null) {
				try {
					readTimeout = Long.parseLong(envReadTimeout.trim());
				} catch (NumberFormatException e) {
					logger.warning("Invalid value for IMIXS_REST_CLIENT_READ_TIMEOUT='" + envReadTimeout
							+ "' - using default readTimeout=" + DEFAULT_READ_TIMEOUT + " " + DEFAULT_TIMEOUT_UNIT);
				}
			}

			String envConnectTimeout = System.getenv("IMIXS_REST_CLIENT_CONNECT_TIMEOUT");
			if (envConnectTimeout != null) {
				try {
					connectTimeout = Long.parseLong(envConnectTimeout.trim());
				} catch (NumberFormatException e) {
					logger.warning("Invalid value for IMIXS_REST_CLIENT_CONNECT_TIMEOUT='" + envConnectTimeout
							+ "' - using default connectTimeout=" + DEFAULT_CONNECT_TIMEOUT + " "
							+ DEFAULT_TIMEOUT_UNIT);
				}
			}
		} else {
			logger.severe("Endpoint not defined!");
		}
	}

	/**
	 * This helper method initializes a noop trust manager. This can be used in
	 * causes by the lack of a certificates in Java's keystore
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private void initNoopTrustManager() throws KeyManagementException, NoSuchAlgorithmException {
		logger.info("...init insecure NoopTrustManager!");
		TrustManager[] noopTrustManager = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		// get instnace of new ssl context
		sslContext = SSLContext.getInstance("ssl");
		sslContext.init(null, noopTrustManager, null);

	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public void setConnectTimeout(long timeout, TimeUnit unit) {
		this.connectTimeout = timeout;
		this.timeoutUnit = unit;
	}

	public void setReadTimeout(long timeout, TimeUnit unit) {
		this.readTimeout = timeout;
		this.timeoutUnit = unit;
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

	public List<ClientRequestFilter> getRequestFilterList() {
		return requestFilterList;
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
	 * <p>
	 * The method verifies if a sslContext exists. In this case a client this
	 * context will be generated
	 * 
	 * @return javax.ws.rs.client.Client instance
	 */
	public Client newClient() {
		ClientBuilder builder = ClientBuilder.newBuilder();
		if (sslContext != null) {
			builder.sslContext(sslContext);
		}
		if (connectTimeout > 0) {
			builder.connectTimeout(connectTimeout, timeoutUnit);
		}
		if (readTimeout > 0) {
			builder.readTimeout(readTimeout, timeoutUnit);
		}
		Client client = builder.build();
		for (ClientRequestFilter filter : requestFilterList) {
			client.register(filter);
		}
		return client;
	}

	/**
	 * Calls the /logout/ target of the rest api endpoint
	 * 
	 * @param client
	 * @throws RestAPIException
	 */
	public void logout() {
		Client client = newClient();
		if (client != null) {
			try {
				logger.finest("......perform logout at: " + getBaseURI() + "logout");
				client.target(getBaseURI() + "logout").request().get();
			} catch (NotFoundException e) {
				logger.warning("logout not possible - /logout is not defined by server endpoint!");
			} finally {
				client.close();
			}
		}
		// invalidate the requestFilterList
		requestFilterList = new ArrayList<ClientRequestFilter>();
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
