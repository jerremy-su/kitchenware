package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.NotNull;

public interface SpringServiceInvokerIterator {

	boolean invokeNext(
			@NotNull SpringServiceHook hook) throws Exception;
}
