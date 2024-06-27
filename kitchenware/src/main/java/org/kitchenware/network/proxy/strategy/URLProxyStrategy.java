package org.kitchenware.network.proxy.strategy;

import java.util.ArrayList;
import java.util.List;

public class URLProxyStrategy {

	List<URLProxy> proxies;
	
	public List<URLProxy> getProxies() {
		if(proxies == null) {
			proxies = new ArrayList<>();
		}
		return proxies;
	}
}
