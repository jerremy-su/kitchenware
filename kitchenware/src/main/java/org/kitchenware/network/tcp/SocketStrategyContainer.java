package org.kitchenware.network.tcp;

import java.net.InetSocketAddress;

public class SocketStrategyContainer {

	/**
	 * 代理地址: http://127.0.0.1:1080
	 */
	String proxyURL;
	
	/**
	 * 转换地址
	 */
	InetSocketAddress endpoint;
	
	public SocketStrategyContainer() {}
	
	public SocketStrategyContainer(String proxyURL, InetSocketAddress endpoint) {
		this.proxyURL = proxyURL;
		this.endpoint = endpoint;
	}
	
	public String getProxyURL() {
		return proxyURL;
	}
	
	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}
	
	public InetSocketAddress getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(InetSocketAddress endpoint) {
		this.endpoint = endpoint;
	}
	
}
