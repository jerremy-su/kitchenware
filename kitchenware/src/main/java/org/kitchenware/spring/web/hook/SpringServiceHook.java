package org.kitchenware.spring.web.hook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SpringServiceHook {

	HttpServletRequest httpRequest();
	
	HttpServletResponse httpResponse();
	
	
}
