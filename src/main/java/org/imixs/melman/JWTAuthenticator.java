package org.imixs.melman;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * Client request Filter for Imixs-JWT 
 * 
 * @author rsoika
 *
 */
@Provider
public class JWTAuthenticator  implements ClientRequestFilter {

    private final String jwt;
	private final static Logger logger = Logger.getLogger(JWTAuthenticator.class.getName());


    public JWTAuthenticator(String jwt) {
        this.jwt = jwt;
        }

    public void filter(ClientRequestContext requestContext) throws IOException {
    	
    	URI uri = requestContext.getUri();
    	
    	String url=uri.toString();
    	if (!url.contains("jwt=")) {
    		logger.info("adding JSON Web Token...");
    		if (url.contains("?")) {
    			url+="&"+jwt;
    		} else {
    			url+="?"+jwt;
    		}
    		
    		try {
				requestContext.setUri(new URI(url));
			} catch (URISyntaxException e) {
				logger.severe("Failed to set JSON Web Token!");
				e.printStackTrace();
			}
    		logger.info("update uri="+url);
    		
    	}
    	
    
    }   

}
