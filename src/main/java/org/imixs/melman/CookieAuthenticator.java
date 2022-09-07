package org.imixs.melman;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Cookie;

/**
 * Client request Filter for Cookies.
 * <p>
 * This ClientRequestFilter sets a cookie in each request
 * 
 * @author rsoika
 *
 */
public class CookieAuthenticator implements ClientRequestFilter {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(CookieAuthenticator.class.getName());

	private Cookie cookie;

	public CookieAuthenticator(Cookie cookie) {
		super();
		this.cookie = cookie;
	}

	/**
	 * add cookie
	 */
	@Override
	public void filter(ClientRequestContext clientRequestContext) {
		List<Object> cookies = new ArrayList<>();
		cookies.add(this.cookie);
		clientRequestContext.getCookies().entrySet().stream().forEach(item -> cookies.add(item.getValue()));
		clientRequestContext.getHeaders().put("Cookie", cookies);
	}

}
