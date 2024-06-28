package org.kitchenware.spring.web;

import java.net.URI;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.network.tcp.TCPChannelOption;
import org.kitchenware.spring.web.hook.ServiceInvokeIteratorBuilder;

public class ServiceRPC {

	transient ServiceInvokeIteratorBuilder iteratorBuilder = ServiceInvokeIteratorBuilder.DEFUALT;

	final URI uri;

	final TCPChannelOption connectionOption = new TCPChannelOption();

	public ServiceRPC(@NotNull final URI uri) {
		Asserts.assertNotNull(uri, "'uri' cannot be null.");

		this.uri = uri;
	}

	public ServiceInvokeIteratorBuilder getIteratorBuilder() {
		return iteratorBuilder;
	}

	public ServiceRPC setIteratorBuilder(ServiceInvokeIteratorBuilder iteratorBuilder) {
		if (iteratorBuilder == null) {
			return ServiceRPC.this;
		}

		this.iteratorBuilder = iteratorBuilder;
		return ServiceRPC.this;
	}

	public URI getUri() {
		return uri;
	}

	public TCPChannelOption getConnectionOption() {
		return connectionOption;
	}

}
