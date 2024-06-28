package org.kitchenware.spring.web.hook;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.object.transport.rpc.flow.ObjectSerialize;

public interface ServiceInvokeIterator {

	static final Logger LOGGER = Logger.getLogger(ServiceInvokeIterator.class.getName());
	
	boolean invokeNext(
			@NotNull final ServiceHook hook) throws Exception;
	
	default void throwErrorReponse(
			@NotNull final String transportId, @NotNull final ServiceHook hook, @NotNull final Throwable caughtError) throws Exception{
		
		ServiceInvokeResult result = new ServiceInvokeResult()
				.setTransportId(transportId)
				.setCaughtError(caughtError)
				;
		
		writeObject(hook, result);
	}
	
	default void writeObject(
			@NotNull final ServiceHook hook, @NotNull final Object src) throws Exception{
		try {
			HttpServletResponse response = hook.httpResponse();
			response.setContentType("application/octet-stream");
			ObjectSerialize serialize = new ObjectSerialize(src);
			serialize.writeObject(hook.httpResponse().getOutputStream());
			response.flushBuffer();
		} catch (Throwable e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
