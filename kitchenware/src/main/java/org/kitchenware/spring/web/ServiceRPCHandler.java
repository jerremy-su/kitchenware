package org.kitchenware.spring.web;

import java.lang.reflect.Method;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.object.transport.rpc.invocation.MethodInvokerHandler;

public class ServiceRPCHandler extends MethodInvokerHandler{

	final SerivceRPC rpc;
	
	ServiceRPCHandler(@NotNull final SerivceRPC rpc){
		this.rpc = rpc;
	}

	@Override
	protected Object performInvoke(
			Object owner, Method method, Object[] args) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
}
