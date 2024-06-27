package org.kitchenware.network.proxy.strategy;

public class URLMatchs {

	/**
	 * wildcard matchs
	 * if useRegex -> use regex to match url
	 */
	String pattern;

	boolean useRegex;
	
	public URLMatchs() {}
	
	public URLMatchs(String pattern) {
		this.pattern = pattern;
	}
	
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public boolean isUseRegex() {
		return useRegex;
	}
	
	public void setUseRegex(boolean useRegex) {
		this.useRegex = useRegex;
	}
}
