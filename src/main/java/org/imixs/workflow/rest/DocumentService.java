package org.imixs.workflow.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.imixs.workflow.xml.XMLDataCollection;

@Path("/documents")
@Consumes({ MediaType.APPLICATION_XML})
public interface DocumentService {

	
	@GET
	@Path("/{uniqueid}")
	public XMLDataCollection getDocument(@PathParam("uniqueid") String uniqueid, @QueryParam("items") String items) ;
}
  