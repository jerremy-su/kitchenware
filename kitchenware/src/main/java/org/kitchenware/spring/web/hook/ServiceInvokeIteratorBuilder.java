package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.NotNull;

public interface ServiceInvokeIteratorBuilder {

	static final ServiceInvokeIteratorBuilder DEFUALT = new DefaultServiceInvokeIteratorBuilder();
	
	ServiceInvokeIterator buildIterator(
			@NotNull final ServiceInvokerTransport transport
			);
	
	static class DefaultServiceInvokeIteratorBuilder implements ServiceInvokeIteratorBuilder{

		DefaultServiceInvokeIteratorBuilder() {}
		
		@Override
		public ServiceInvokeIterator buildIterator(
				@NotNull final ServiceInvokerTransport transport) {
			ServiceInvokeIterator iterator = new ServiceRPCIterator(transport);
			return iterator;
		}
		
	}
}
