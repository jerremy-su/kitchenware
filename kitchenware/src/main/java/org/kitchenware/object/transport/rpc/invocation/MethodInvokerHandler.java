package org.kitchenware.object.transport.rpc.invocation;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.kitchenware.express.util.Errors;
import org.kitchenware.reflect.MethodId;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@SuppressWarnings("serial")
public abstract class MethodInvokerHandler implements InvocationHandler,
		MethodInterceptor, Serializable {
	private static final Set<MethodId> ownerMethods = new HashSet<>();
	static {
		for (Method method : Object.class.getDeclaredMethods()) {
			ownerMethods.add(MethodId.getId(method));
		}
	}

	@Override
	public Object intercept(Object owner, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		return fireInvoker(owner, method, args);
	}

	@Override
	public Object invoke(Object owner, Method method, Object[] args)
			throws Throwable {
		return fireInvoker(owner, method, args);
	}

	private Object fireInvoker(Object owner, Method method, Object[] args)
			throws Throwable {
		if (isOwnerMethod(method)) {
			return method.invoke(this, args);
		}
		Throwable availableException = null;
		try {
			return performInvoke(owner, method, args);
		} catch (Throwable e) {
			availableException = getCurrentThrowable(e);
		}
		{
			if (validThrowables(method, availableException)) {
				throw availableException;
			} else {
				throw Errors.throwRuntimeable(availableException);
			}
		}
	}
	private Throwable getCurrentThrowable(Throwable ex){
		while((ex.getCause() != null && 
				(ex instanceof InvocationTargetException || ex.getClass().toString().toLowerCase().contains("ejbexception")))){
			ex = ex.getCause();
		}
		return ex;
		
	}
	
	protected abstract Object performInvoke(Object owner, Method method,
			Object[] args) throws Throwable;

	boolean validThrowables(Method method, Throwable e) {
		if(RuntimeException.class.isInstance(e)){
			return true;
		}
		if(method == null){
			return false;
		}
		Class[] eTypes = method.getExceptionTypes();
		if (eTypes.length < 1) {
			return false;
		}
		for (Class c : eTypes) {
			if (c.isInstance(e)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOwnerMethod(Method method) {
		boolean b = ownerMethods.contains(MethodId.getId(method));
		return b;
	}
}
