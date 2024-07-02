package org.kitchenware.spring.web.rpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.io.ByteBufferedInputStream;
import org.kitchenware.express.io.ByteBufferedOutputStream;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.netty.http.HttpNetty;
import org.kitchenware.network.netty.http.NettyHttpResponse;
import org.kitchenware.object.transport.rpc.flow.ObjectDeserialize;
import org.kitchenware.object.transport.rpc.flow.ObjectSerialize;
import org.kitchenware.object.transport.rpc.invocation.MethodInvokerHandler;
import org.kitchenware.reflect.MethodId;
import org.kitchenware.spring.web.hook.ServiceInvokeIterator;
import org.kitchenware.spring.web.hook.ServiceInvokeResult;
import org.kitchenware.spring.web.hook.ServiceInvokerTransport;

public class ServiceRPCHandler extends MethodInvokerHandler{
	
	static final Logger LOGGER = Logger.getLogger(ServiceRPCHandler.class.getName());
	

	final ServiceRPC rpc;
	
	final String serviceName;
	final Class serviceType;
	ServiceRPCHandler(
			@NotNull final ServiceRPC rpc
			, @Optional final String serviceName
			, @NotNull Class serviceType
			){
		this.rpc = rpc;
		this.serviceName = serviceName;
		this.serviceType = serviceType;
	}

	@Override
	protected Object performInvoke(
			Object owner, Method method, Object[] args) throws Throwable {
		
		MethodId methodId = MethodId.getId(method);
		
		ServiceInvokerTransport transport = new ServiceInvokerTransport()
				.setTransportId(UUID.randomUUID().toString())
				.setServiceName(this.serviceName)
				.setServiceType(this.serviceType.getName())
				.setMethodId(methodId)
				.setParameters(args)
				;
		
		ServiceInvokeIterator iterator = this.rpc.iteratorBuilder.buildIterator(transport);
		
		ServiceInvokeResult invokeResult = fireRPC(iterator);
		
		if(invokeResult.getCaughtError() != null) {
			throw invokeResult.getCaughtError();
		}
		
		return invokeResult.getResult();
	}
	
	private ServiceInvokeResult fireRPC(
			@NotNull final ServiceInvokeIterator iterator) throws Throwable{
		
		URI uri = this.rpc.uri;
		HttpNetty http = HttpNetty
				.doPost(uri)
				.contentType(ServiceInvokeIterator.STREAM_TYPE)
				;
		ServiceRPCConnection connectionHandler = this.rpc.connectionHandler;
		if(connectionHandler != null) {
			connectionHandler.handleConnection(
					ServiceRPCConnection.newConnection(http)
					);
		}
		
		ObjectSerialize serialize = new ObjectSerialize(iterator);
		ByteBufferedOutputStream buf = new ByteBufferedOutputStream();
		serialize.writeObject(buf);
		
		http.content(buf.toByteArray());
		NettyHttpResponse response = http.invoke(this.rpc.connectionOption);
		
		
		try {
			ObjectDeserialize deserialize = new ObjectDeserialize(
					new ByteBufferedInputStream(response.getContent())
					);
			
			ServiceInvokeResult result = deserialize.readObject();
			return result;
		} catch (Throwable e) {
			String err = String.format("Invalid Response: \r\n%s"
					, StringObjects.toUTF8String(response.getContent()));
			LOGGER.warning(err);
			throw new IOException(e.getMessage(), e);
		}
	}
	
}
