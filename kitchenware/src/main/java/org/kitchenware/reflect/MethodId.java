package org.kitchenware.reflect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.express.concurrent.ConcurrentOptional;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.express.util.StringObjects;

public final class MethodId {

	static final Map<Method, MethodId> CONTEXT = new ConcurrentHashMap<>();
	static final ConcurrentLockFactory LOCKS  = new ConcurrentLockFactory();
	
	public static MethodId getId(@NotNull final Method method) {
		if(method == null) {
			return null;
		}
		
		MethodId id = CONTEXT.get(method);
		if(id != null) {
			return id;
		}
		
		Lock lock = LOCKS.get(method);
		id = ConcurrentOptional.optional(lock)
				.ofNullable(()-> CONTEXT.get(method))
				.orElseGet(()-> {
					MethodId newId = new MethodId(method);
					CONTEXT.put(method, newId);
					return newId;
				});
		return id;
	}
	
	final String id;
	
	private MethodId(@NotNull final Method method) {
		this(generateId(method));
	}
	
	private MethodId(@NotNull final String id) {
		Asserts.assertNotNull(id, "'id' cannot be null.");
		
		this.id = id;
	}
	
	static String generateId(@NotNull final Method method) {
		Asserts.assertNotNull(method, "'method' cannot be null.");
		
		String methodName = method.getName();
		StringBuilder sb = new StringBuilder();
		sb.append(methodName + "(");
		Class[] params = method.getParameterTypes(); // avoid clone
		for (int j = 0; j < params.length; j++) {
			sb.append(getTypeName(params[j]));
			if (j < (params.length - 1))
				sb.append(",");
		}
		sb.append(")");
		return sb.toString();
	}
	
	static String getTypeName(final Class type) {
		if (type.isArray()) {
			try {
				Class cl = type;
				int dimensions = 0;
				while (cl.isArray()) {
					dimensions++;
					cl = cl.getComponentType();
				}
				StringBuilder sb = new StringBuilder();
				sb.append(cl.getName());
				for (int i = 0; i < dimensions; i++) {
					sb.append("[]");
				}
				return sb.toString();
			} catch (Throwable e) { /* FALLTHRU */
			}
		}
		return type.getName();
	}
	
	@Override
	public String toString() {
		return this.id;
	}	
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) { 
		if(! MethodId.class.isInstance(obj)) {
			return false;
		}
		
		MethodId target = (MethodId) obj;
		boolean b = StringObjects.assertEquals(this.id, target.id);
		return b;
	}
}
