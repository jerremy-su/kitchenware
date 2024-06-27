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
			return performInvoke(owner, method, args);/**BUG11551(Jerremy 2017.02.17)*/
		} catch (Throwable e) {/**BUG11325(Jerremy 2016.11.22)*/
			/**------------------B BUGjira1427(Jerremy 2018.02.06)------------------*/
//			if (InvocationTargetException.class.isInstance(e)) {
//				InvocationTargetException ex = (InvocationTargetException) e;
//				if (ex.getTargetException() != null) {
//					availableException = ex.getTargetException();
//				}
//			}
//			if (availableException == null) {
//				availableException = e;
//			}
			availableException = getCurrentThrowable(e);
			/**------------------E BUGjira1427(Jerremy 2018.02.06)------------------*/
		}
		{// ���쳣���п����Դ���
			if (validThrowables(method, availableException)) {
				throw availableException;
			} else {
				/**------------------B BUG11438(Jerremy 2017.01.06)------------------*/
//				RuntimeException throwRuntime = new RuntimeException(
//						String.format("%s : %s", availableException.getClass()
//								.getName(), availableException.getMessage()), availableException);/**BUG11163(Jerremy 2016.09.02)*/
//				throwRuntime.setStackTrace(availableException.getStackTrace());
				throw Errors.throwRuntimeable(availableException);
				/**------------------E BUG11438(Jerremy 2017.01.06)------------------*/
			}
		}
	}
	/**------------------B BUGjira1427(Jerremy 2018.02.06)------------------*/
	private Throwable getCurrentThrowable(Throwable ex){
		while((ex.getCause() != null && 
				(ex instanceof InvocationTargetException || ex.getClass().toString().toLowerCase().contains("ejbexception")))){
			ex = ex.getCause();
		}
		return ex;
		
	}
	/**------------------E BUGjira1427(Jerremy 2018.02.06)------------------*/
	protected abstract Object performInvoke(Object owner, Method method,
			Object[] args) throws Throwable;

	boolean validThrowables(Method method, Throwable e) {
		/**------------------B BUG11551(Jerremy 2017.02.17)------------------*/
		if(RuntimeException.class.isInstance(e)){
			return true;
		}
		if(method == null){
			return false;
		}
		/**------------------E BUG11551(Jerremy 2017.02.17)------------------*/
		Class[] eTypes = method.getExceptionTypes();
		if (eTypes.length < 1) {
			return false;
		}
		for (Class c : eTypes) {
			if (c.isInstance(e)) {/**BUG11163(Jerremy 2016.09.02)*//**BUG11551(Jerremy 2017.02.17)*/
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
