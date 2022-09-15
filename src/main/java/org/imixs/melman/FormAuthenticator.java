package org.imixs.melman;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.workflow.services.rest.RestAPIException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Cookie;

/**
 * This RequestFilter performs a form based authentication. The filter can be
 * used with a jakarta.ws.rs.client.Client.
 * <p>
 * The FormAuthenticator provides two constructors. One expects the base API url
 * and a valid JSESSIONID, the other expects the base API url with
 * userid/password and will request a JESSIONID automatically.
 * <p>
 * We t use the <code>CookieHandler.setDefault(manger)</code> here. Maybe we
 * should refine the usage because for different threads possible the same
 * CookieHandler is used. See details here:
 * https://stackoverflow.com/questions/16305486/cookiemanager-for-multiple-threads
 * To Avoid this problem we use a internal system to only store the JSESSIONID
 * and set the cookie per each request.
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
            // compute domain/path to set the cookie later
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
        CookieHandler.setDefault(null);
        baseUri = _baseUri;
        if (debug) {
            logger.finest("......baseUIR= " + baseUri);
        }

        // Access secure page on server. In response to this request we will receive
        // the JSESSIONID to be used for further requests.
        try {
            // Instantiate CookieManager;
            // make sure to set CookiePolicy
            CookieManager manager = new CookieManager();
            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(manager);

            URL baseUrl = new URL(baseUri);
            domain = baseUrl.getHost();
            path = baseUrl.getPath();
            String loginURL = computeLoginURL(baseUri);
            logger.fine("....login page=" + loginURL);
            if (loginURL == null) {
                // default to baseUri
                loginURL = _baseUri;
            }

            // create the httpURLConnection...
            URL obj = new URL(loginURL);
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

            // now get the cookies from underlying CookieStore
            CookieStore cookieJar = manager.getCookieStore();
            List<HttpCookie> cookiesListe = cookieJar.getCookies();
            for (HttpCookie cookie : cookiesListe) {
                // do we have a JSESSIONID?
                if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                    jsessionID = cookie.getValue();
                    if (debug) {
                        logger.fine("......jsessionID retrieved from cookieStore: " + jsessionID);
                    }
                    break;
                }
            }

            logger.finest("...jsessionID=" + jsessionID);
            if (jsessionID == null) {
                logger.warning("No JSESSIONID returnd from login page!");
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
            logger.warning("unable to connect: " + e.getMessage());
            if (debug) {
                e.printStackTrace();
            }
        } finally {
            // finally reset cookei handler
            CookieHandler.setDefault(null);
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
            logger.finest("......set JSESSIONID cookie");
            Cookie n = new Cookie("JSESSIONID", jsessionID, path, domain);
            cookies.add(n);
            requestContext.getHeaders().put("Cookie", cookies);
        }
    }

    /**
     * We test the login url by appending /model/ to the api endpoint which will
     * typically cause a login page
     * 
     * @param apiURL
     * @return
     */
    private String computeLoginURL(String apiURL) {

        logger.finest("... computeLoginURL...");
        String modelURL = apiURL;
        if (!modelURL.endsWith("/")) {
            modelURL = modelURL + "/";
        }
        modelURL = modelURL + "model";
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(modelURL).openConnection();
            // optional default is GET
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setAllowUserInteraction(false);

            int iLastHTTPResult = urlConnection.getResponseCode();

            // read response if response was successful
            if (iLastHTTPResult >= 200 && iLastHTTPResult <= 299) {
                String loginPage = readResponse(urlConnection);
                if (loginPage.contains("j_security_check")) {
                    logger.finest("found Login page");
                    // now we search for somthing like this:
                    // <form method="post" action="/j_security_check">
                    int pos1 = loginPage.indexOf("j_security_check");
                    if (pos1 > -1) {
                        // end of url
                        int posEnd = loginPage.indexOf("\"", pos1);
                        // start of url
                        loginPage = loginPage.substring(0, posEnd);
                        int posStart = loginPage.lastIndexOf("=\"");
                        if (posStart > -1) {
                            String jSecurityCheck = loginPage.substring(posStart + 2, posEnd);

                            // is it an absolute path?
                            if (jSecurityCheck.startsWith("/")) {
                                int protokollPos = apiURL.indexOf("://");
                                String result = apiURL.substring(0, apiURL.indexOf("/", protokollPos + 3));

                                return result + jSecurityCheck;
                            } else {
                                if (apiURL.endsWith("/")) {
                                    return apiURL + jSecurityCheck;
                                } else {
                                    return apiURL + "/" + jSecurityCheck;
                                }
                            }
                        }
                    }

                } else {
                    logger.warning("no login page found!  ");
                    return apiURL;

                }
            } else {
                String error = "Error " + iLastHTTPResult + " - failed GET request from '" + modelURL + "'";
                logger.warning(error);
                throw new RestAPIException(iLastHTTPResult, error);
            }

        } catch (RestAPIException | IOException e1) {
            logger.severe("failed to compute login page: " + e1.getMessage());
        }

        return null;
    }

    /**
     * Reads the response from a http request.
     * 
     * @param urlConnection
     * @throws IOException
     */
    private String readResponse(URLConnection urlConnection) throws IOException {
        // get content of result
        logger.finest("......readResponse....");
        StringWriter writer = new StringWriter();
        BufferedReader in = null;
        try {
            // test if content encoding is provided
            String sContentEncoding = urlConnection.getContentEncoding();
            if (sContentEncoding == null || sContentEncoding.isEmpty()) {
                sContentEncoding = "UTF-8";
            }

            // if an encoding is provided read stream with encoding.....
            if (sContentEncoding != null && !sContentEncoding.isEmpty())
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), sContentEncoding));
            else
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.finest("......" + inputLine);
                writer.write(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                in.close();
        }

        return writer.toString();

    }

}