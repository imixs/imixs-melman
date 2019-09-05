package org.imixs.melman;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * This RequestFilter performs a form based authentication. The filter can be
 * used with a javax.ws.rs.client.Client.
 * 
 * @author rsoika
 *
 */
@Provider
public class FormAuthenticator implements ClientRequestFilter {

	private ArrayList<Object> cookies;
	private final String USER_AGENT = "Mozilla/5.0";
	private final static Logger logger = Logger.getLogger(FormAuthenticator.class.getName());

	public FormAuthenticator(String baseUri, String username, String password) {
		cookies = new ArrayList<>();
		// extend the base uri with /j_security_check....
		baseUri += "/j_security_check";
		logger.finest("......baseUIR= " + baseUri);
		// Access secure page on server. In response to this request we will receive
		// the JSESSIONID to be used for further requests.
		try {
			// Instantiate CookieManager;
			// make sure to set CookiePolicy
			CookieManager manager = new CookieManager();
			manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(manager);
			// create the httpURLConnection...
			URL obj = new URL(baseUri);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("Connection", "close");

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			// add Post parameters
			String urlParameters = "j_username=" + username + "&j_password=" + password;
			// Send post request
			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			logger.fine(".....Response Code : " + responseCode);
			con.connect();
			// get cookies from underlying CookieStore
			CookieStore cookieJar = manager.getCookieStore();
			List<HttpCookie> cookiesListe = cookieJar.getCookies();
			for (HttpCookie cookie : cookiesListe) {
				// create a jax-rs cookie...
				javax.ws.rs.core.Cookie n = new javax.ws.rs.core.Cookie(cookie.getName(), cookie.getValue(),
						cookie.getPath(), cookie.getDomain());
				cookies.add(n);
				logger.finest("......CookieHandler retrieved cookie: " + cookie);
			}

			// get stream and read from it, just to close the response which is important
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} catch (IOException e) {
			// something went wrong...
			e.printStackTrace();
		}

	}

	/**
	 * In the filter method we put the cookies form the login into the request.
	 */
	public void filter(ClientRequestContext requestContext) throws IOException {
		if (cookies != null) {
			requestContext.getHeaders().put("Cookie", cookies);
		}
	}

}