package org.kitchenware.spring.web;

import java.net.URI;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.spring.web.hook.ServiceInvokeIteratorBuilder;

public class SerivceRPC {

	transient ServiceInvokeIteratorBuilder iteratorBuilder = ServiceInvokeIteratorBuilder.DEFUALT;

	final URI uri;
	
	public SerivceRPC(@NotNull final URI uri) {
		Asserts.assertNotNull(uri, "'uri' cannot be null.");
		
		this.uri = uri;
	}
	
	public ServiceInvokeIteratorBuilder getIteratorBuilder() {
		return iteratorBuilder;
	}

	public SerivceRPC setIteratorBuilder(ServiceInvokeIteratorBuilder iteratorBuilder) {
		this.iteratorBuilder = iteratorBuilder;
		return SerivceRPC.this;
	}

}
