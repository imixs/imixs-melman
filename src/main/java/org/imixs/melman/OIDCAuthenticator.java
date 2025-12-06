package org.imixs.melman;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * This RequestFilter performs an OIDC (OpenID Connect) authentication using the
 * Resource Owner Password Credentials (ROPC) grant type. The filter can be used
 * with a jakarta.ws.rs.client.Client.
 * <p>
 * The OIDCAuthenticator requests an access token from an OIDC provider (e.g.,
 * Keycloak, Auth0) and adds it as a Bearer token to each HTTP request.
 * <p>
 * Example usage with Keycloak:
 * 
 * <pre>
 * String tokenEndpoint = "https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token";
 * OIDCAuthenticator auth = new OIDCAuthenticator(tokenEndpoint, "my-client-id", "my-client-secret", "anna", "secret");
 * 
 * Client client = ClientBuilder.newClient();
 * client.register(auth);
 * </pre>
 * <p>
 * The authenticator supports automatic token refresh when the access token
 * expires.
 * 
 * @author rsoika
 * @see https://github.com/imixs/imixs-security/tree/main/imixs-oidc
 */
public class OIDCAuthenticator implements ClientRequestFilter {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String AUTH_EXCEPTION = "AUTH_EXCEPTION";
    private static final int TOKEN_EXPIRY_BUFFER_SECONDS = 30; // 30 sec Buffer

    private static final Logger logger = Logger.getLogger(OIDCAuthenticator.class.getName());

    // private String baseUri = null;
    private String tokenEndpoint = null;
    private String clientId = null;
    private String clientSecret = null;
    private String username = null;
    private String password = null;
    private String scope = "openid profile email";

    private String accessToken = null;
    private String refreshToken = null;
    private long expiresAt = 0;
    private long refreshExpiresAt = 0;
    private boolean debug = false;

    /**
     * Creates a new OIDCAuthenticator with an existing access token.
     * <p>
     * Use this constructor if you already have a valid access token. Token refresh
     * is not supported in this mode.
     * 
     * @param accessToken the existing access token
     */
    public OIDCAuthenticator(String accessToken) {
        super();
        this.accessToken = accessToken;
        this.expiresAt = Long.MAX_VALUE;
    }

    /**
     * Creates a new OIDCAuthenticator with username and password credentials.
     * <p>
     * The constructor immediately requests an access token from the OIDC provider
     * using the Resource Owner Password Credentials grant type.
     * 
     * @param tokenEndpoint the OIDC token endpoint URL (e.g.,
     *                      "https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token")
     * @param clientId      the OIDC client ID
     * @param clientSecret  the OIDC client secret (can be null for public clients)
     * @param username      the user's username
     * @param password      the user's password
     * @throws RestAPIException if authentication fails
     */
    public OIDCAuthenticator(String tokenEndpoint, String clientId, String clientSecret,
            String username, String password) {
        super();
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a new OIDCAuthenticator with username, password and custom scope.
     * 
     * @param tokenEndpoint the OIDC token endpoint URL
     * @param clientId      the OIDC client ID
     * @param clientSecret  the OIDC client secret (can be null for public clients)
     * @param username      the user's username
     * @param password      the user's password
     * @param scope         the OAuth scope (e.g., "openid profile email")
     * @throws RestAPIException if authentication fails
     */
    public OIDCAuthenticator(String tokenEndpoint, String clientId, String clientSecret,
            String username, String password, String scope) {
        super();
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.scope = scope;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Returns the current access token.
     * 
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets a new access token manually.
     * 
     * @param accessToken the new access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Returns the current refresh token.
     * 
     * @return the refresh token, or null if not available
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Returns the scope used for token requests.
     * 
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope for token requests.
     * 
     * @param scope the scope (e.g., "openid profile email")
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Checks if the current access token is expired.
     * 
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    /**
     * This filter method is called for each request. It adds the Bearer token to
     * the Authorization header.
     * <p>
     * If the access token is expired and a refresh token is available, the
     * authenticator will attempt to refresh the access token automatically.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {

        try {
            if (accessToken == null) {
                // initialize token..
                requestAccessToken();
            } else if (isTokenExpired() && tokenEndpoint != null) {
                // Check if token needs refresh
                if (refreshToken != null && System.currentTimeMillis() < refreshExpiresAt) {
                    refreshAccessToken();
                } else if (username != null && password != null) {
                    // Refresh token expired or not available - request new token
                    requestAccessToken();
                }
            }
            if (accessToken != null && !accessToken.isEmpty()) {
                if (debug) {
                    logger.info("├── setting Bearer token");
                }
                requestContext.getHeaders().add("Authorization", "Bearer " + accessToken);
            } else {
                logger.warning("├── Invalid access token!");
            }
        } catch (RestAPIException e) {
            logger.warning("Failed to refresh/obtain access token: " + e.getMessage());
            throw new IOException("Failed to obtain access token", e);
        }
    }

    /**
     * Requests a new access token from the OIDC provider using the Resource Owner
     * Password Credentials grant type.
     * 
     * @throws RestAPIException if the token request fails
     */
    private void requestAccessToken() throws RestAPIException {

        if (debug) {
            logger.info("├── Requesting OIDC access token from: " + tokenEndpoint);
        }

        HttpURLConnection con = null;
        try {
            URL url = new URL(tokenEndpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            con.setDoInput(true);

            // Build the request body
            StringBuilder params = new StringBuilder();
            params.append("grant_type=password");
            params.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            if (clientSecret != null && !clientSecret.isEmpty()) {
                params.append("&client_secret=").append(URLEncoder.encode(clientSecret, StandardCharsets.UTF_8));
            }
            params.append("&username=").append(URLEncoder.encode(username, StandardCharsets.UTF_8));
            params.append("&password=").append(URLEncoder.encode(password, StandardCharsets.UTF_8));
            if (scope != null && !scope.isEmpty()) {
                params.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
            }

            // Send the request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(params.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            if (debug) {
                logger.info("│   ├── Token endpoint response code: " + responseCode);
            }
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(con);
                parseTokenResponse(response);
                if (debug) {
                    logger.info("│   ├── Access token successfully obtained");
                }
            } else {
                String errorResponse = readErrorResponse(con);
                throw new RestAPIException(getClass().getSimpleName(), AUTH_EXCEPTION,
                        "Failed to obtain access token. HTTP " + responseCode + ": " + errorResponse);
            }
        } catch (IOException e) {
            throw new RestAPIException(getClass().getSimpleName(), AUTH_EXCEPTION,
                    "Failed to connect to token endpoint: " + e.getMessage(), e);
        }
    }

    /**
     * Refreshes the access token using the refresh token.
     * 
     * @throws RestAPIException if the token refresh fails
     */
    private void refreshAccessToken() throws RestAPIException {

        if (debug) {
            logger.fine("Refreshing OIDC access token...");
        }

        HttpURLConnection con = null;
        try {
            URL url = new URL(tokenEndpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            con.setDoInput(true);

            // Build the refresh request body
            StringBuilder params = new StringBuilder();
            params.append("grant_type=refresh_token");
            params.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            if (clientSecret != null && !clientSecret.isEmpty()) {
                params.append("&client_secret=").append(URLEncoder.encode(clientSecret, StandardCharsets.UTF_8));
            }
            params.append("&refresh_token=").append(URLEncoder.encode(refreshToken, StandardCharsets.UTF_8));

            // Send the request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(params.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String response = readResponse(con);
                parseTokenResponse(response);
                if (debug) {
                    logger.info("│   ├── Access token successfully refreshed");
                }
            } else {
                String errorResponse = readErrorResponse(con);
                throw new RestAPIException(getClass().getSimpleName(), AUTH_EXCEPTION,
                        "Failed to refresh access token. HTTP " + responseCode + ": " + errorResponse);
            }
        } catch (IOException e) {
            throw new RestAPIException(getClass().getSimpleName(), AUTH_EXCEPTION,
                    "Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the JSON token response and extracts tokens and expiration times.
     * 
     * @param response the JSON response string
     */
    private void parseTokenResponse(String response) {
        JsonReader reader = Json.createReader(new StringReader(response));
        JsonObject json = reader.readObject();
        reader.close();

        this.accessToken = json.getString("access_token", null);
        this.refreshToken = json.getString("refresh_token", null);

        // Calculate absolute expiration times
        long now = System.currentTimeMillis();
        int expiresIn = json.getInt("expires_in", 300);
        int refreshExpiresIn = json.getInt("refresh_expires_in", 1800);

        // Subtract buffer to refresh before actual expiration
        // This accounts for clock skew and network latency
        int effectiveExpiresIn = Math.max(expiresIn - TOKEN_EXPIRY_BUFFER_SECONDS, 0);
        int effectiveRefreshExpiresIn = Math.max(refreshExpiresIn - TOKEN_EXPIRY_BUFFER_SECONDS, 0);

        this.expiresAt = now + effectiveExpiresIn * 1000L;
        this.refreshExpiresAt = now + effectiveRefreshExpiresIn * 1000L;

        if (debug) {
            logger.info("│   ├── Token expires in " + expiresIn + " seconds (refresh at " + effectiveExpiresIn + "s)");
        }
    }

    /**
     * Reads the response from an HTTP connection.
     */
    private String readResponse(HttpURLConnection con) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }

    /**
     * Reads the error response from an HTTP connection.
     */
    private String readErrorResponse(HttpURLConnection con) {
        try {
            StringBuilder response = new StringBuilder();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            return "Unable to read error response";
        }
    }
}
