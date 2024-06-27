package org.kitchenware.network.proxy.strategy;

public class URLProxy {

	String proxyURL;
	
	URLMatchs include;
	
	URLMatchs exclude;

	public URLMatchs getInclude() {
		return include;
	}

	public void setInclude(URLMatchs include) {
		this.include = include;
	}

	public URLMatchs getExclude() {
		return exclude;
	}

	public void setExclude(URLMatchs exclude) {
		this.exclude = exclude;
	}

	public String getProxyURL() {
		return proxyURL;
	}

	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}
	
	
}
