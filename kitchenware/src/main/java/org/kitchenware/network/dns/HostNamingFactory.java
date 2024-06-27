package org.kitchenware.network.dns;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.buffered.temporary.DefaultTempFactory;
import org.kitchenware.express.buffered.temporary.spi.TempContext;
import org.kitchenware.express.buffered.temporary.spi.TempEntry;
import org.kitchenware.express.util.BoolObjects;

public class HostNamingFactory {
	
	static final Logger LOGGER = Logger.getLogger(HostNamingFactory.class.getName());
	
	private static final boolean USE_CACHE = BoolObjects.valueOf(System.getProperty("host.naming.cache"));
	private static final HostNamingFactory owner = new HostNamingFactory();
	public static HostNamingFactory owner() {
		return owner;
	}
	
	TempContext<String, HostCache> tempContext =  
			DefaultTempFactory
			.owner()
			.getTemporary(getClass().getName())
			;
	
	private HostNamingFactory() {}
	
	public InetSocketAddress socketAddress(String hostName, int port){
		return socketAddress(USE_CACHE, hostName, port);
	}
	
	public InetSocketAddress socketAddress(boolean cache, String hostName, int port) {
		if(cache) {
			try {
				return namingByCache(hostName, port);
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return new InetSocketAddress(hostName, port);
	}
	
	private InetSocketAddress namingByCache(String hostName, int port) throws UnknownHostException {
		int timeout = 60 * 1000;
		TempEntry<String, HostCache> temp = tempContext.get(hostName, DefaultTempFactory.owner().createTimeout(timeout));
		HostCache hostCache = null;
		if(temp != null) {
			hostCache = temp.getValue();
		}
		if(hostCache == null) {
			hostCache = new HostCache(
					hostName
					, InetAddress.getByName(hostName).getHostAddress()
					);
			
			tempContext.put(
					DefaultTempFactory
					.owner()
					.createTempEntry(hostName, hostCache)
					);
		}
		
		InetSocketAddress address = new InetSocketAddress(hostCache.getHostAddress(), port);
		return address;
	}
}
