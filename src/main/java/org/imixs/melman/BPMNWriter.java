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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.imixs.workflow.bpmn.BPMNModel;


/**
 * This MessageBodyWriter generates an byte stream representation from a BPMN Model
 * 
 * @author rsoika
 *
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class BPMNWriter implements MessageBodyWriter<BPMNModel> {

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return BPMNModel.class.isAssignableFrom(type);
	}

	public void writeTo(BPMNModel model, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				entityStream));
		
		String s=new String(model.getRawData());
		
		bw.write(s);
		
		bw.flush();
		
		
//		entityStream.write(model.getRawData());
//		
//		entityStream.close();
//	
	}

	public long getSize(BPMNModel model, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4) {
		return model.getRawData().length;
	}

	
	
	

	
	
	
}
