package org.kitchenware.spring.web.hook;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.object.transport.rpc.flow.ObjectDeserialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test/rpc/hook")
public class ServiceHookController {

	static final Logger LOGGER = Logger.getLogger(ServiceHookController.class.getName());
	
	@Autowired
	ApplicationContext applicationContext;
	
	
	@PostMapping("/invoke")
	public void invoke(
			@NotNull HttpServletRequest httpRequest, @NotNull HttpServletResponse response) throws Exception{
		ServiceInvokeIterator iterator = readIterator(httpRequest.getInputStream());
		ServiceHook serviceHook = ServiceHook.build(httpRequest, response, applicationContext);
		iterator.invokeNext(serviceHook);
		response.flushBuffer();
	}
	
	private ServiceInvokeIterator readIterator(
			@NotNull InputStream inputStream
			) throws Exception{
		ServiceInvokeIterator loadIterator = null;
		Throwable caughtError = null;
		
		try {
			ObjectDeserialize deserializer = new ObjectDeserialize(inputStream);
			Object loadObject = deserializer.readObject();
			if(! ServiceInvokeIterator.class.isInstance(loadObject)) {
				throw new IllegalAccessException(String.format("Invalid transport type"));
			}
			loadIterator = ServiceInvokeIterator.class.cast(loadObject);
		} catch (Throwable e) {
			caughtError = e;
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		
		if(caughtError != null) {
			throw new IOException("Invalid Request", caughtError);
		}
		
		return loadIterator;
	}
}
