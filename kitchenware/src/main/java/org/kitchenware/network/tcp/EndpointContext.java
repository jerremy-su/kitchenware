package org.kitchenware.network.tcp;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointContext {

	final Map<InetSocketAddress, InetSocketAddress> addresses = new ConcurrentHashMap<>();
	
	public EndpointContext() {}
	
	public void setAddress(InetSocketAddress address, InetSocketAddress endpoint) {
		this.addresses.put(address, endpoint);
	}
	
	public InetSocketAddress getAddress(InetSocketAddress address) {
		return this.addresses.get(address);
	}
	
	public void removeAddress(InetSocketAddress address) {
		this.addresses.remove(address);
	}
	
	public void addAllAddresses(Map<InetSocketAddress, InetSocketAddress> addresses) {
		this.addresses.putAll(addresses);
	}
	
	public void resetAll(Map<InetSocketAddress, InetSocketAddress> addresses) {
		addAllAddresses(addresses);

		Set<InetSocketAddress> currentAddressSet = this.addresses.keySet();
		Set<InetSocketAddress> addressSet = addresses.keySet();
		for(InetSocketAddress add : currentAddressSet) {
			if(!addressSet.contains(add)) {
				this.addresses.remove(add);
			}
		}
	}
	
	public void clear() {
		this.addresses.clear();
	}
	
}
