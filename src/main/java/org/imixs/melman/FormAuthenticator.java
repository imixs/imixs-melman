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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * This RequestFilter performs a form based authentication. The filter can be
 * used with a javax.ws.rs.client.Client.
 * 
 * @author rsoika
 *
 */
public class FormAuthenticator implements ClientRequestFilter {

    private String baseUri = null;
    private String domain = null;
    private String path = null;
    private String jsessionID = null;

    private final String USER_AGENT = "Mozilla/5.0";
    private final static Logger logger = Logger.getLogger(FormAuthenticator.class.getName());

    /**
     * Creates a new FormAuthenticator based on a baseUri and a valid JSESSIONID
     * 
     * @param _baseUri
     * @param _jsessionid
     * @throws MalformedURLException
     */
    public FormAuthenticator(String _baseUri, String _jsessionid) {
        super();
        boolean debug = logger.isLoggable(Level.FINE);
        this.jsessionID = _jsessionid;
        baseUri = _baseUri;

        // extract domain and path form URL
        try {
            URL baseUrl;
            baseUrl = new URL(baseUri);
            domain = baseUrl.getHost();
            path = baseUrl.getPath();
        } catch (MalformedURLException e) {
            // something went wrong...
            logger.warning("unable to connect: " + e.getMessage());
            if (debug) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates a new FormAuthenticator based on a baseUri and a username, password.
     * The constructor post the user credentials to the endpoint /j_security_check
     * to receive a JSESSIONID.
     * 
     * @param _baseUri
     * @param username
     * @param password
     */
    public FormAuthenticator(String _baseUri, String username, String password) {
        boolean debug = logger.isLoggable(Level.FINE);
        // cookies = new ArrayList<>();

        baseUri = _baseUri;

        if (debug) {
            logger.finest("......baseUIR= " + baseUri);
        }
        // Access secure page on server. In response to this request we will receive
        // the JSESSIONID to be used for further requests.
        try {
            URL baseUrl = new URL(baseUri);
            domain = baseUrl.getHost();
            path = baseUrl.getPath();

            // Instantiate CookieManager;
            // make sure to set CookiePolicy
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(manager);
            // create the httpURLConnection... extend the base uri with
            // /j_security_check....
            URL obj = new URL(baseUri + "/j_security_check");
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
            if (debug) {
                logger.fine(".....Response Code : " + responseCode);
            }
            if (responseCode >= 200 && responseCode < 300) {
                con.connect();
                // get cookies from underlying CookieStore
                CookieStore cookieJar = manager.getCookieStore();
                List<HttpCookie> cookiesListe = cookieJar.getCookies();
                for (HttpCookie cookie : cookiesListe) {

                    // do we have a JSESSIONID?
                    if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                        jsessionID = cookie.getValue();
                        if (debug) {
                            logger.finest("......jsessionID retrieved: " + jsessionID);
                        }
                        break;
                    }
                }

                // get stream and read from it, just to close the response which is important
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }

        } catch (IOException e) {
            // something went wrong...
            logger.warning("unable to connect: " + e.getMessage());
            if (debug) {
                e.printStackTrace();
            }
        }
    }

    /**
     * returns the current JESSIONID
     * 
     * @return
     */
    public String getJsessionID() {
        return jsessionID;
    }

    /**
     * Set a new JSESSIONID
     * 
     * @param jsessionID
     */
    public void setJsessionID(String jsessionID) {
        this.jsessionID = jsessionID;
    }

    /**
     * This filter method is called for each request. The method generates a new
     * JSESSIONID cookies form the current JSESSIONID and stores the cookie in the
     * header.
     * <p>
     * Note: existing cookies will be overwritten!
     */
    public void filter(ClientRequestContext requestContext) throws IOException {

        if (jsessionID != null && !"".equals(jsessionID)) {
            ArrayList<Object> cookies = new ArrayList<>();
            logger.finest("......create new JSESSIONID cookie");
            javax.ws.rs.core.Cookie n = new javax.ws.rs.core.Cookie("JSESSIONID", jsessionID, path, domain);
            cookies.add(n);
            requestContext.getHeaders().put("Cookie", cookies);
        }
    }

}