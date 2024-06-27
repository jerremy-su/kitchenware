package org.kitchenware.network.tcp;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyContext {

	static final ProxyContext context = new ProxyContext();
	
	final Map<InetSocketAddress, String> proxies = new ConcurrentHashMap<>();
	
	public ProxyContext() {}
	
	public void setProxy(InetSocketAddress address, String url) {
		this.proxies.put(address, url);
	}
	
	public String getProxyURL(InetSocketAddress address) {
		return this.proxies.get(address);
	}
	
	public void removeProxy(InetSocketAddress address) {
		this.proxies.remove(address);
	}
	
	public void addAllProxies(Map<InetSocketAddress, String> proxies) {
		this.proxies.putAll(proxies);
	}
	
	public void resetAll(Map<InetSocketAddress, String> proxies) {
		addAllProxies(proxies);

		Set<InetSocketAddress> currentAddressSet = this.proxies.keySet();
		Set<InetSocketAddress> addressSet = proxies.keySet();
		for(InetSocketAddress add : currentAddressSet) {
			if(!addressSet.contains(add)) {
				this.proxies.remove(add);
			}
		}
	}
	
	public void clear() {
		this.proxies.clear();
	}
	
}
