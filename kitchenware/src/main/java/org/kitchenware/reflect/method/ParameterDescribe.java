package org.kitchenware.reflect.method;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.Asserts;

public class ParameterDescribe extends DefaultAnnotationDescribe<ParameterDescribe>{
	
	final int parameterIndex;
	final Class type;
	
	public ParameterDescribe(
			final Class type, @NotNull final int parameterIndex){
		
		Asserts.assertNotNull(type, "'type' cannot be null.");
		
		this.type = type;
		this.parameterIndex = parameterIndex;
	}
	
	public int getParameterIndex() {
		return parameterIndex;
	}
	
	public Class getType() {
		return type;
	}
}
