package org.openi.acl;

/**
 * 
 * @author SUJEN
 *
 */
public class AccessDeniedException extends Exception {

	public AccessDeniedException() {
		super();
	}

	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessDeniedException(String message) {
		super(message);
	}

	public AccessDeniedException(Throwable cause) {
		super(cause);
	}

}
