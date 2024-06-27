package org.kitchenware.reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassTypesComponent {
	final Map<String, ClassDescription> context = new ConcurrentHashMap<>();
	
	ClassTypesComponent(){}
	
	public void set(String name, ClassDescription desc) {
		context.put(name, desc);
	}
	
	public ClassDescription get(String name) {
		return context.get(name);
	}
}
