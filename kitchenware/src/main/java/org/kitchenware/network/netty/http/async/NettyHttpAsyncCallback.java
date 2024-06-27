package org.kitchenware.network.netty.http.async;

import org.kitchenware.network.netty.http.DefaultNettyHttpSession;

public interface NettyHttpAsyncCallback {

	
	void handle(DefaultNettyHttpSession session) throws Throwable;
}
