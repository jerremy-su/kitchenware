package org.kitchenware.reflect;

public class ClassDescription {
	ClassLoader loader;
	Class type;
	
	ClassDescription(Class type){
		this(type.getClassLoader(), type);
	}
	
	ClassDescription(ClassLoader loader, Class type){
		this.loader = loader;
		this.type = type;
	}
	
	public ClassLoader getLoader() {
		return loader;
	}
	
	public Class getType() {
		return type;
	}
}
