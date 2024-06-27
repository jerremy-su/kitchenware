package org.kitchenware.network.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import org.kitchenware.express.buffered.temporary.DefaultTempFactory;
import org.kitchenware.express.buffered.temporary.spi.TempContext;
import org.kitchenware.express.buffered.temporary.spi.TempEntry;
import org.kitchenware.express.function.access.UserAccessor;
import org.kitchenware.express.util.CollectionObjects;
import org.kitchenware.network.proxy.strategy.URLProxyStrategy;

public class ProxyAgent {
	static {
		System.setProperty("java.net.useSystemProxies", "true");
	}
	
	static URLProxyStrategy urlProxyStrategy;
	static URLProxyLookup proxyLookup;
	
	static final TempContext<URI, ProxyConfiguration> proxyContext = DefaultTempFactory.owner().getKeepLongTemporary(ProxyAgent.class.getName());
	
	
	public static void setUrlProxyStrategy(URLProxyStrategy urlProxyStrategy) {
		ProxyAgent.urlProxyStrategy = urlProxyStrategy;
		ProxyAgent.proxyLookup = new URLProxyLookup(urlProxyStrategy);
	}
	
	/**select operation systme global proxy
	 * @return
	 */
	public static ProxyConfiguration select(URI uri) {
		ProxyConfiguration result = null;
		TempEntry<URI, ProxyConfiguration> entry = proxyContext.get(uri, DefaultTempFactory.owner().createTimeout(30000));
		if(entry != null) {
			result = entry.getValue();
		}
		
		if(result != null) {
			return result;
		}
		URLProxyLookup proxyLookup = ProxyAgent.proxyLookup;
		if(result == null && proxyLookup != null) {
			result = proxyLookup.lookup(uri);
		}
		
		if(result == null) {
			List<Proxy> proxies = UserAccessor.functionAccess(()->{
				return ProxySelector.getDefault().select(uri);
			});
			if(CollectionObjects.assertNotEmpty(proxies)) {
				Proxy proxy = proxies.get(0);
				if(Proxy.Type.HTTP.equals(proxy.type())) {
					InetSocketAddress address = (InetSocketAddress) proxy.address();
					result = new ProxyConfiguration(address.getHostString(), address.getPort(), ProxyFunctionProtocol.HTTP);
				}else if(Proxy.Type.SOCKS.equals(proxy.type())) {
					InetSocketAddress address = (InetSocketAddress) proxy.address();
					result = new ProxyConfiguration(address.getHostString(), address.getPort(), ProxyFunctionProtocol.SOCKS5);
				}else if(Proxy.Type.DIRECT.equals(proxy.type())) {
					result = new ProxyConfiguration("0.0.0.0", -1, ProxyFunctionProtocol.DIRECT);
				}
			
			}
		}
		
		if(result == null) {
			result = new ProxyConfiguration("0.0.0.0", -1, ProxyFunctionProtocol.DIRECT);
		}
		
		proxyContext.put(DefaultTempFactory.owner().createTempEntry(uri, result));
		
		return result;
		
	}
}
