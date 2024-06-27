package org.kitchenware.network.dns;

public class HostCache {

	String hostName;
	String hostAddress;
	
	public HostCache(String hostName, String hostAddress) {
		this.hostName = hostName;
		this.hostAddress = hostAddress;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public String getHostAddress() {
		return hostAddress;
	}
}
