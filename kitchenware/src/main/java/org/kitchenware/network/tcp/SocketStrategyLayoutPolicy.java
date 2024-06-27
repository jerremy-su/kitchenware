package org.kitchenware.network.tcp;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.kitchenware.express.util.StringObjects;

public class SocketStrategyLayoutPolicy {

	static final SocketStrategyLayoutPolicy policy = new SocketStrategyLayoutPolicy();
	
	static final String DIRECT_UNPROXY = "DIRECT://localhost";
	
	public static SocketStrategyLayoutPolicy getPolicy() {
		return policy;
	}
	
	final EndpointContext endpoints = new EndpointContext();
	final ProxyContext proxies = new ProxyContext();

	List<SocketStrategy> strategies;
	
	private SocketStrategyLayoutPolicy() {}
	
	public void setStrategies(List<SocketStrategy> strategies) {
		List<SocketStrategy> list = new ArrayList<>();
		if(strategies != null) {
			list.addAll(strategies);
		}
		this.strategies = list;
		endpoints.clear();
		proxies.clear();
	}
	
	public String lookupProxyURL(InetSocketAddress address) { 
		String url = proxies.getProxyURL(address);
		if(StringObjects.assertNotEmptyAfterTrim(url)) {
			return url;
		}
		
		String endpoint = String.format("%s:%s", address.getHostString(), address.getPort());
		
		if(this.strategies == null) {
			url = DIRECT_UNPROXY;
			proxies.setProxy(address, url);
			return url;
		}
		
		for(SocketStrategy strategy : this.strategies) {
			if(strategy.matchs(endpoint)) {
				if(strategy.getContainer() != null) {
					url = strategy.getContainer().getProxyURL();
				}
			}
			if(url != null) {
				break;
			}
		}
		
		if(url == null) {
			url = DIRECT_UNPROXY;
		}
		
		proxies.setProxy(address, url);
		return url;
	}
	
	public InetSocketAddress lookupAddress(InetSocketAddress address) {
		InetSocketAddress result = this.endpoints.getAddress(address);
		if(result != null) {
			return result;
		}
		
		String endpoint = String.format("%s:%s", address.getHostString(), address.getPort());
		
		if(this.strategies == null) {
			result = address;
			endpoints.setAddress(address, result);
			return result;
		}
		
		for(SocketStrategy strategy : this.strategies) {
			if(strategy.matchs(endpoint)) {
				if(strategy.getContainer() != null) {
					result = strategy.getContainer().getEndpoint();
				}
			}
			if(result != null) {
				break;
			}
		}
		
		if(result == null) {
			result = address;
		}
		
		endpoints.setAddress(address, result);
		return result;
	}
}

