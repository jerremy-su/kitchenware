package org.kitchenware.spring.web.hook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.springframework.context.ApplicationContext;

public interface ServiceHook {

	@NotNull
	HttpServletRequest httpRequest();
	
	@NotNull
	HttpServletResponse httpResponse();
	
	@NotNull 
	ApplicationContext applicationContext();
	
}
