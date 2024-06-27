package org.kitchenware.reflect.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.Asserts;

public interface TypeDescribeFactory {

	static final TypeDescribeFactory FACTORY = new TypeDescriptionFactoryImpl();
	
	TypeDescribe description(
			@NotNull final Class type);
	
	static class TypeDescriptionFactoryImpl implements TypeDescribeFactory{

		final Map<Class, TypeDescribe> types = new ConcurrentHashMap<>();
		
		TypeDescriptionFactoryImpl(){}
		
		@Override
		public TypeDescribe description(@NotNull final Class type) {
			
			Asserts.assertNotNull(type, "'type' cannot be null.");
			
			if(type.isArray()) {
				throw new RuntimeException(String.format("Unknown support array type '%s'", type.getName()));
			}
			TypeDescribe description = this.types.get(type);
			if(description == null) {
				this.types.put(type, description = new TypeDescribe(TypeDescriptionFactoryImpl.this, type));
			}
			return description;
		}
		
	}
}
