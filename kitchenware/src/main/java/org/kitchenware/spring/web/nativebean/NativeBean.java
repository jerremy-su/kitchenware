package org.kitchenware.spring.web.nativebean;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Required;

public class NativeBean {

	@Required
	final Class implementsType;
	
	@Required
	final NativeBeanFactory beanFactory;
	
	NativeBean(
			@NotNull NativeBeanFactory beanFactory
			, @NotNull Class implementsType
			){
		this.beanFactory = beanFactory;
		this.implementsType = implementsType;
	}
}
