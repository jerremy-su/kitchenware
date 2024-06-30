package org.kitchenware.spring.web.hook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Required;
import org.springframework.context.ApplicationContext;

public interface ServiceHook {

	@NotNull
	HttpServletRequest httpRequest();
	
	@NotNull
	HttpServletResponse httpResponse();
	
	@NotNull 
	ApplicationContext applicationContext();
	
	
	static ServiceHook build(
			@NotNull final HttpServletRequest request
			, @NotNull final HttpServletResponse response
			, @NotNull final ApplicationContext applicationContext
			) {
		return new DefaultServiceHook(request, response, applicationContext);
	}
	
	static class DefaultServiceHook implements ServiceHook{
		
		@Required
		final HttpServletRequest request;
		
		@Required
		final HttpServletResponse response;
		
		@Required
		final ApplicationContext applicationContext;
		
		DefaultServiceHook(
				@NotNull final HttpServletRequest request
				, @NotNull final HttpServletResponse response
				, @NotNull final ApplicationContext applicationContext
				){
			this.request = request;
			this.response = response;
			this.applicationContext = applicationContext;
		}

		@Override
		public @NotNull HttpServletRequest httpRequest() {
			return this.request;
		}

		@Override
		public @NotNull HttpServletResponse httpResponse() {
			return this.response;
		}

		@Override
		public @NotNull ApplicationContext applicationContext() {
			return this.applicationContext;
		}
		
	}
}
