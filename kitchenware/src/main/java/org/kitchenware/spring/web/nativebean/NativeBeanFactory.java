package org.kitchenware.spring.web.nativebean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.spring.web.rpc.ServiceRPC;

public class NativeBeanFactory {

	static final Logger LOGGER = Logger.getLogger(NativeBeanFactory.class.getName());
	
	final Map<Class, NativeBean> BEANS = new ConcurrentHashMap<>();
	final ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();
	
	@Optional
	final ServiceRPC serviceRPC;
	
	final boolean rpcSupported;
	public NativeBeanFactory() {
		this(null);
	}
	
	public NativeBeanFactory(
			@Optional final ServiceRPC serviceRPC
			){
		this.serviceRPC = serviceRPC;
		this.rpcSupported = this.serviceRPC != null;
	}
	
	public NativeBean typeOf(Class type) {
		NativeBean bean = this.BEANS.get(type);
		if(bean != null) {
			return bean;
		}
		
		Lock lock = LOCKS.get(type);
		lock.lock();
		try {
			bean = this.BEANS.get(type);
			if(bean == null) {
				bean = new NativeBean(NativeBeanFactory.this, type);
				this.BEANS.put(type, bean);
			}
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}finally {
			lock.unlock();
		}
		
		return bean;
	}
	
	public ServiceRPC getServiceRPC() {
		return serviceRPC;
	}
	
	
	public boolean rpcSupported() {
		return this.rpcSupported;
	}
}
