package org.kitchenware.spring.web.hook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.io.ByteBufferedOutputStream;
import org.kitchenware.express.io.IOSteramLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test/rpc/hook")
public class ServiceHookController {

	@Autowired
	ApplicationContext applicationContext;
	
	
	@PostMapping("/invoke")
	public void invoke(
			@NotNull HttpServletRequest httpRequest, @NotNull HttpServletResponse respnose) throws Exception{
		ByteBufferedOutputStream buf = new ByteBufferedOutputStream();
		IOSteramLoader.load(httpRequest.getInputStream(), buf);
		
		//TODO
	}
}
