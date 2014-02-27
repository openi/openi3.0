package org.openi.wcf.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.utils.SoftException;

/**
 * finds the RequestContextFactory
 * 
 * @task the factory should be thread safe and part of the environment
 * @author av
 * @author SUJEN
 */
public class RequestContextFactoryFinder {
	private static Logger logger = Logger
			.getLogger(RequestContextFactoryFinder.class);

	/**
	 * an instance of RequestContextFactory ist found in the users session.
	 */
	private static final String SESSION_KEY = RequestContextFactory.class
			.getName();

	/**
	 * The name of the concrete class is a configurable context attribute with
	 * this name.
	 */
	private static final String CONTEXT_KEY = "org.openi.wcf.controller.RequestContextFactory";

	public static RequestContextFactory findFactory(HttpSession session) {
		RequestContextFactory rcf = null;;
		try {
			rcf = (RequestContextFactory) session.getAttribute(SESSION_KEY);
			if (rcf == null) {
				ServletContext context = session.getServletContext();
				String className = (String) context
						.getInitParameter(CONTEXT_KEY);
				if (className == null)
					className = RequestContextFactoryImpl.class.getName();
				rcf = (RequestContextFactory) Class.forName(className)
						.newInstance();
				session.setAttribute(SESSION_KEY, rcf);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
			logger.error(null, e);
			throw new SoftException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			logger.error(null, e);
			throw new SoftException(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(null, e);
			throw new SoftException(e);
		}
		return rcf;
	}

	/**
	 * @param request
	 * @param response
	 * @param threadLocal
	 *            if true, a thread local RequestContext variable is
	 *            initialized. The thread local may be accessed via
	 *            RequestContext.instance() and must be cleaned up by the caller
	 *            via RequestContext.invalidate().
	 * 
	 * @see RequestContext#instance()
	 * @see RequestContext#invalidate()
	 */
	public static RequestContext createContext(HttpServletRequest request,
			HttpServletResponse response, boolean threadLocal) {
		HttpSession session = request.getSession(true);

		RequestContextFactory rcf = findFactory(session);
		
		RequestContext context = rcf.createContext(request, response);
		if (threadLocal)
			RequestContext.setInstance(context);
		return context;
	}
}
