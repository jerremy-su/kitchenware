package org.kitchenware.spring.web;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.tcp.TCPChannelOption;
import org.kitchenware.object.transport.rpc.invocation.InstanceProxy;
import org.kitchenware.spring.web.hook.ServiceInvokeIteratorBuilder;
import org.springframework.stereotype.Service;

public class ServiceRPC {
	
	static final Logger LOGGER = Logger.getLogger(ServiceRPC.class.getName());

	transient ServiceInvokeIteratorBuilder iteratorBuilder = ServiceInvokeIteratorBuilder.DEFUALT;

	final URI uri;

	final TCPChannelOption connectionOption = new TCPChannelOption();
	
	final Map<Class, Object> services = new ConcurrentHashMap<>();
	final Map<String, Object> namingServices = new ConcurrentHashMap<>();
	final ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();

	public ServiceRPC(@NotNull final URI uri) {
		Asserts.assertNotNull(uri, "'uri' cannot be null.");

		this.uri = uri;
	}

	public <T> T getService(
			final Class<T> serviceType) {
		
		Object rpcBean = services.get(serviceType);
		
		if(rpcBean != null) {
			return (T) rpcBean;
		}
		
		Service service = serviceType.getAnnotation(Service.class);
		if(StringObjects.assertNotEmpty(service.value())) {
			
			return getService(serviceType, service.value());
		}
		
		Lock lock = this.LOCKS.get(serviceType);
		lock.lock();
		
		try {
			rpcBean = this.services.get(serviceType);
			if(rpcBean == null) {
				ServiceRPCHandler handler = new ServiceRPCHandler(
						this, null, serviceType);
				rpcBean = InstanceProxy.newInstance(serviceType, handler);
				this.services.put(serviceType, rpcBean);
			}
		
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}finally {
			lock.unlock();
		}
	
		return (T) rpcBean;
	}
	
	public <T> T getService(
			final @NotNull Class<T> serviceType, final @NotNull String serviceName) {
		Object rpcBean = this.namingServices.get(serviceName);
		if(rpcBean != null) {
			return (T) rpcBean;
		}
		
		Lock lock = this.LOCKS.get(serviceName);
		lock.lock();
		try {
			rpcBean = this.namingServices.get(serviceName);
			if(rpcBean == null) {
				ServiceRPCHandler handler = new ServiceRPCHandler(
						this, serviceName, serviceType);
				rpcBean = InstanceProxy.newInstance(serviceType, handler);
				this.namingServices.put(serviceName, rpcBean);
			}
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}finally {
			lock.unlock();
		}
		
		return (T) rpcBean;
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
