package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.NotNull;

public interface ServiceInvokerIterator {

	boolean invokeNext(
			@NotNull ServiceHook hook) throws Exception;
}
