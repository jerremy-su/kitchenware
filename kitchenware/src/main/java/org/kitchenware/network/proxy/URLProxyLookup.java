package org.kitchenware.network.proxy;

import java.net.URI;

import org.kitchenware.express.concurrent.atomic.AtomicValue;
import org.kitchenware.express.util.CollectionObjects;
import org.kitchenware.express.util.StringMatchs;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.proxy.strategy.URLMatchs;
import org.kitchenware.network.proxy.strategy.URLProxy;
import org.kitchenware.network.proxy.strategy.URLProxyStrategy;

public class URLProxyLookup {
	URLProxyStrategy strategy;
	
	public URLProxyLookup(URLProxyStrategy strategy){
		this.strategy = strategy;
	}
	
	public ProxyConfiguration lookup(URI uri) {
		return lookup(uri, null);
	}
	
	public ProxyConfiguration lookup(URI uri, AtomicValue<Throwable> caughtError) {
		ProxyConfiguration result = null;
		try {
			result = lookup0(uri);
		} catch (Throwable e) {
			if(caughtError != null) {
				caughtError.setValue(e);
			}
		}
		return result;
	}
	
	ProxyConfiguration lookup0(URI uri) {
		if(this.strategy == null || CollectionObjects.isEmpty(strategy.getProxies()) || uri == null) {
			return null;
		}
		
		for(URLProxy proxy : this.strategy.getProxies()) {
			if(proxy == null) {
				continue;
			}
			
			boolean include = match(uri, proxy.getInclude());
			if(!include) {
				continue;
			}
			boolean exclude = match(uri, proxy.getExclude());
			if(exclude) {
				continue;
			}
			
			String proxyServer = proxy.getProxyURL();
			return ProxyConfiguration.build(proxyServer);
		}
		return null;
	}

	boolean match(URI uri, URLMatchs matchs) {
		if(matchs == null) {
			return false;
		}
		String src = uri.toString();
		String pattern = matchs.getPattern();
		boolean b;
		if(matchs.isUseRegex()) {
			b = matchRegex(src, pattern);
		}else {
			b = matchWildcard(src, pattern);
		}
		return b;
	}
	
	boolean matchWildcard(String src, String pattern) {
		if(StringObjects.isEmptyAfterTrim(pattern)) {
			return false;
		}
		String [] patterns = pattern.split(",");
		for(String con : patterns) {
			if(StringObjects.isEmptyAfterTrim(con)) {
				continue;
			}
			if(new StringMatchs(
					StringObjects.toLowerCase(StringObjects.trim(con))).matchs(StringObjects.toLowerCase(src))
					) {
				return true;
			}
		}
		
		return false;
	}
	
	boolean matchRegex(String src, String pattern) {
		if(StringObjects.isEmptyAfterTrim(pattern)) {
			return false;
		}
		
		return src.toLowerCase().matches(pattern.toLowerCase());
	}
	
	
}
