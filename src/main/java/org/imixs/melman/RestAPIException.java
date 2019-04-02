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

import org.imixs.workflow.exceptions.WorkflowException;

/**
 * The RestAPIException is thrown in case a Imixs Rest API Call failed.
 * 
 * @see org.imixs.melman.DocumentClient
 * @author rsoika
 * 
 */
public class RestAPIException extends WorkflowException {

	public final static String RESPONSE_PROCESSING_EXCEPTION = "RESPONSE_PROCESSING_EXCEPTION";

	private static final long serialVersionUID = 1L;

	public RestAPIException(String aErrorContext, String aErrorCode, String message) {
		super(aErrorContext, aErrorCode, message);
	}

	public RestAPIException(String aErrorContext, String aErrorCode, String message, Exception e) {
		super(aErrorContext, aErrorCode, message, e);
	}
}
