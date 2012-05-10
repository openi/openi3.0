package org.openi.wcf.component;

/**
 * 
 * @author SUJEN
 *
 */
public class WCFRender {

	private String xslUri;
	private boolean xslCache = true;
	private Object ref = null;
	
	public WCFRender() {
		
	}
	
	public WCFRender(String xslUri, boolean xslCache, Object ref) {
		this.xslUri = xslUri;
		this.xslCache = xslCache;
		this.ref = ref;
	}

	public String getXslUri() {
		return xslUri;
	}

	public void setXslUri(String xslUri) {
		this.xslUri = xslUri;
	}

	public boolean isXslCache() {
		return xslCache;
	}

	public void setXslCache(boolean xslCache) {
		this.xslCache = xslCache;
	}

	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}

}
