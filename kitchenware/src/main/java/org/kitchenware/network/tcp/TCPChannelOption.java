package org.kitchenware.network.tcp;

import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.express.util.NumberObjects;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.proxy.ProxyConfiguration;
import org.kitchenware.network.proxy.ProxyFunctionProtocol;
import org.kitchenware.reflect.CloneSupplier;

public class TCPChannelOption implements CloneSupplier<TCPChannelOption> {
	
	public static int getDefaultConnectionTimeout() {
		Integer value = NumberObjects.toInteger(System.getProperty("tcp.connect.timeout"));
		if(value == null) {
			value = NumberObjects.toInteger(System.getProperty("netty.tcp.connect.timeout"));
		}
		return NumberObjects.toInteger(value, 6000);
	}
	
	public static int getDefaultSoTimeout() {
		Integer value = NumberObjects.toInteger(System.getProperty("tcp.so.timeout"), 3 * 60 * 1000);
		if(value == null) {
			value = NumberObjects.toInteger(System.getProperty("netty.tcp.so.timeout"), 3 * 60 * 1000);
		}
		return NumberObjects.toInteger(value, 3 * 60 * 1000);
	}
	
	public static boolean getDefaultSoReuseaddr() {
		String config = System.getProperty("tcp.so.reuseaddr");
		if(config == null) {
			config = System.getProperty("netty.tcp.so.reuseaddr");
		}
		boolean value = BoolObjects.valueOf(config);
		return value;
	}
	
	boolean keepAlive = true;
	boolean tcpNoDelay = true;
	int connectionTimeout;
	int soTimeout;
	boolean so_reuseaddr = getDefaultSoReuseaddr();//CMBG-33009 jerremy.su 2022-08-31 10:34:58 
	ProxyConfiguration proxy;
	/**
	 * 是否在TLS扩展槽接受alpn握手
	 */
	boolean alpnInTls = true;
	
	public TCPChannelOption() {
		this(getDefaultConnectionTimeout(), getDefaultSoTimeout());
	}
	
	public TCPChannelOption(int connectionTimeout, int soTimeout) {
		this.connectionTimeout = connectionTimeout;
		this.soTimeout = soTimeout;
		installProxyProperties();
	}
	
	
	void installProxyProperties() {
		String host;
		String portChars;
		if(
				! StringObjects.isEmpty(host = System.getProperty("http.proxyHost"))
				&& ! StringObjects.isEmpty(portChars = System.getProperty("http.proxyPort"))
				) {
			try {
				setProxy(new ProxyConfiguration(host, Integer.valueOf(portChars), ProxyFunctionProtocol.HTTP));
				return;
			} catch (Throwable e) {}
		}
		
		if (
				! StringObjects.isEmpty(host = System.getProperty("socksProxyHost"))
				&& ! StringObjects.isEmpty(portChars = System.getProperty("socksProxyPort"))
				) {
			try {
				setProxy(new ProxyConfiguration(host, Integer.valueOf(portChars), ProxyFunctionProtocol.SOCKS5));
			} catch (Exception e) {}
		}
	}
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	public TCPChannelOption setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return TCPChannelOption.this;
	}
	
	public int getSoTimeout() {
		return soTimeout;
	}
	
	public TCPChannelOption setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		return TCPChannelOption.this;
	}
	
	public boolean isKeepAlive() {
		return keepAlive;
	}
	
	public TCPChannelOption setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		return TCPChannelOption.this;
	}
	
	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}
	
	public TCPChannelOption setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
		return TCPChannelOption.this;
	}
	
	public boolean isSo_reuseaddr() {
		return so_reuseaddr;
	}
	
	public void setSo_reuseaddr(boolean so_reuseaddr) {
		this.so_reuseaddr = so_reuseaddr;
	}
	
	public ProxyConfiguration getProxy() {
		return proxy;
	}
	
	public void setProxy(ProxyConfiguration proxy) {
		this.proxy = proxy;
	}
	
	public TCPChannelOption cloneOption() {
		return this.cloneTo();
	}
	
	public boolean isAlpnInTls() {
		return alpnInTls;
	}
	
	public void setAlpnInTls(boolean alpnInTls) {
		this.alpnInTls = alpnInTls;
	}
}
