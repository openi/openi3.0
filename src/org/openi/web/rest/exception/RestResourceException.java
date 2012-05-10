package org.openi.web.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * 
 * @author SUJEN
 *
 */
public class RestResourceException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	public RestResourceException(String expMsg) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR)
	             .entity(expMsg).type(MediaType.TEXT_PLAIN).build());
	}
}
