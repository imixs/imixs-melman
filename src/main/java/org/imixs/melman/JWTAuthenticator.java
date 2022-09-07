package org.imixs.melman;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * Client request Filter for Imixs-JWT 
 * 
 * @author rsoika
 *
 */
public class JWTAuthenticator  implements ClientRequestFilter {

    private final String jwt;
	private final static Logger logger = Logger.getLogger(JWTAuthenticator.class.getName());


    public JWTAuthenticator(String jwt) {
    	if (jwt!=null && jwt.contains("jwt=")) {
    		logger.warning("Wrong JWT format! JWT may not contain 'jwt=....'");
    	}
        this.jwt = jwt;
        }

    public void filter(ClientRequestContext requestContext) throws IOException {
    	URI uri = requestContext.getUri();
    	
    	String url = uri.toString();
		if (!url.contains("jwt=")) {
			logger.finest(".....adding JSON Web Token...");
			requestContext.getHeaders().add("jwt", jwt);
		}
    }   

}
