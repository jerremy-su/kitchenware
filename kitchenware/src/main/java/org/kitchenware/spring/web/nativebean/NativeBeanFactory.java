package org.kitchenware.spring.web.nativebean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.spring.web.rpc.ServiceRPC;

public class NativeBeanFactory {

	final Map<Class, NativeBean> BEANS = new ConcurrentHashMap<>();
	final ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();
	
	@Optional
	final ServiceRPC serviceRPC;
	
	public NativeBeanFactory() {
		this(null);
	}
	
	public NativeBeanFactory(
			@Optional final ServiceRPC serviceRPC
			){
		this.serviceRPC = serviceRPC;
	}
	
	
}
