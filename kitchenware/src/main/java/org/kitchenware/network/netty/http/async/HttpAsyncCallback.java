package org.kitchenware.network.netty.http.async;

import org.kitchenware.network.netty.http.HttpSession;

public interface HttpAsyncCallback {

	
	void handle(HttpSession session) throws Throwable;
}
