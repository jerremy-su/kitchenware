package org.kitchenware.network.proxy;

import java.net.URI;
import java.util.Objects;

import org.kitchenware.express.util.StringObjects;

public class ProxyConfiguration {
	
	static final ProxyConfiguration DIRECT = new ProxyConfiguration("0,0,0,0", -1, ProxyFunctionProtocol.DIRECT);
	
	public static final ProxyConfiguration build(String url) {
		if(StringObjects.isEmpty(url)) {
			return DIRECT;
		}
		
		ProxyFunctionProtocol protocol = null;
		String host = null;
		Integer port = null;
		try {
			URI uri = new URI(url);
			String schema = uri.getScheme();
			
			if ("socks".equalsIgnoreCase(schema) || "socks4".equalsIgnoreCase(schema)) {
				protocol = ProxyFunctionProtocol.SOCKS4;
			}else if ("socks5".equalsIgnoreCase(schema)) {
				protocol = ProxyFunctionProtocol.SOCKS5;
			}else if("http".equalsIgnoreCase(schema)) {
				protocol = ProxyFunctionProtocol.HTTP;
			}else {
				protocol = ProxyFunctionProtocol.DIRECT;
			}
			host = uri.getHost();
			port = uri.getPort();
		} catch (Throwable e) {}
		
		if(protocol == null
				|| host == null
				|| port == null
				|| port <= 0
				) {
			return DIRECT;
		}
		
		return new ProxyConfiguration(host, port, protocol);
	}
	
	
	String host;
	Integer port;
	ProxyFunctionProtocol protocol;
	int hash;
	public ProxyConfiguration(String host, Integer port, ProxyFunctionProtocol protocol) {
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		
		{

			int hash = 0;
			if(protocol != null) {
				hash += protocol.name().hashCode();
			}
			if(this.host != null) {
				host += host.hashCode();
			}
			host += port;
			this.hash = hash;
		}
	}
	
	public String getHost() {
		return host;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public ProxyFunctionProtocol getProtocol() {
		return protocol;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!ProxyConfiguration.class.isInstance(obj)) {
			return false;
		}
		ProxyConfiguration tmp = (ProxyConfiguration) obj;
		return Objects.equals(host, tmp.host)
				&& Objects.equals(port, tmp.port)
				&& Objects.equals(protocol, tmp.protocol);
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
	
	@Override
	public String toString() {
		if(protocol == null 
				|| StringObjects.isEmptyAfterTrim(this.host) 
				|| port <= 0
				|| Objects.equals(this.protocol, ProxyFunctionProtocol.DIRECT) 
				) {
			return ProxyFunctionProtocol.DIRECT.name();
		}
		return String.format("%s://%s:%s", protocol.name().toLowerCase(), host, port);
	}
	
	public boolean isRemoteProxy() {
		boolean b =
				(this.protocol != null && !Objects.equals(this.protocol, ProxyFunctionProtocol.DIRECT))
				&& StringObjects.assertNotEmptyAfterTrim(this.host)
				&& (this.port != null && this.port.intValue() >= 0)
				;
		return b;
	}
}
