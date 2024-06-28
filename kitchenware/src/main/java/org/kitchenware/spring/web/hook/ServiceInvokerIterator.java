package org.kitchenware.spring.web.hook;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.object.transport.rpc.flow.ObjectSerialize;

public interface ServiceInvokerIterator {

	static final Logger LOGGER = Logger.getLogger(ServiceInvokerIterator.class.getName());
	
	boolean invokeNext(
			@NotNull final ServiceHook hook) throws Exception;
	
	default boolean throwErrorReponse(
			@NotNull final ServiceHook hook, @NotNull final Throwable caughtError) throws Exception{
		
		ServiceInvokerResult result = new ServiceInvokerResult()
				.setCaughtError(caughtError)
				;
		
		try {
			HttpServletResponse response = hook.httpResponse();
			response.setContentType("application/octet-stream");
			ObjectSerialize serialize = new ObjectSerialize(result);
			serialize.writeObject(hook.httpResponse().getOutputStream());
			response.flushBuffer();
			return true;
		} catch (Throwable e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
