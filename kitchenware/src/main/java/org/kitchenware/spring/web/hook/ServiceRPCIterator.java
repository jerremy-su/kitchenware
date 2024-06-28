package org.kitchenware.spring.web.hook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Required;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.reflect.ClassBufferedFactory;
import org.kitchenware.reflect.MethodId;
import org.kitchenware.reflect.basic.ClassDescribe;

public class ServiceRPCIterator implements ServiceInvokeIterator{

	static final Logger LOGGER = Logger.getLogger(ServiceRPCIterator.class.getName());
	
	@Required
	final ServiceInvokerTransport transport;
	
	public ServiceRPCIterator(
			@NotNull @Required final ServiceInvokerTransport transport){
		Asserts.assertNotNull(transport, "'transport' cannot be null.");
		
		this.transport = transport;
	}
	
	@Override
	public boolean invokeNext(
			@NotNull final ServiceHook hook) throws Exception {
		
		String transportId = this.transport.getTransportId();
		ClassDescribe typeDescribe ;
		Method method;
		Object bean;
		ServiceInvokeResult invokerResult;
		
		try {
			typeDescribe = getServiceType();
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throwErrorReponse(transportId, hook, e);
			return false;
		}

		try {
			method = getMethod(typeDescribe);
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throwErrorReponse(transportId, hook, e);
			return false;
		}
		
		try {
			bean = getBean(typeDescribe.getType(), hook);
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			throwErrorReponse(transportId, hook, e);
			return false;
		}
		
		try {
			Object reuslt = method.invoke(bean, this.transport.getParameters());
			invokerResult = new ServiceInvokeResult()
					.setTransportId(this.transport.getTransportId())
					.setResult(reuslt)
					;
		} catch (Throwable e) {
			if(InvocationTargetException.class.isInstance(e)) {
				e = InvocationTargetException.class.cast(e).getTargetException();
			}
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			invokerResult = new ServiceInvokeResult()
					.setTransportId(transportId)
					.setCaughtError(e)
					;
		}
		
		writeObject(hook, invokerResult);
		return false;
	}
	
	private Object getBean(
			@NotNull final Class type, @NotNull  final ServiceHook hook ) throws Exception{
		String serviceName = this.transport.getServiceName();
		
		Object bean;
		if(StringObjects.isEmpty(serviceName)) {
			bean = hook.applicationContext().getBean(type);
		}else {
			bean = hook.applicationContext().getBean(serviceName, type);
		}
		
		return bean;
	}

	private Method getMethod(
			@NotNull final ClassDescribe typeDescribe) throws Exception{
		MethodId id = this.transport.getMethodId();
		Method method = typeDescribe.getDeclaredMethod(id);
		if(method == null) {
			throw new IllegalAccessException(String.format("Method '%s' not found.", id));
		}
		return method;
	}
	
	private ClassDescribe getServiceType() throws Throwable{
		String serviceType = this.transport.getServiceType();
		Class type = ClassBufferedFactory.getFactory().forName(serviceType);
		ClassDescribe describe = ClassDescribe.getDescribe(type);
		return describe;
	}
}
