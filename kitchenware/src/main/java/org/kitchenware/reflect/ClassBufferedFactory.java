package org.kitchenware.reflect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassBufferedFactory {
	private static ClassBufferedFactory factory = new ClassBufferedFactory();
	public static ClassBufferedFactory getFactory() {
		return factory;
	}
	
	
	
	final Set<ClassLoader> defaultClassLoaderSet = Collections.synchronizedSet(new LinkedHashSet<>());
	final Map<ClassLoader, ClassTypesComponent> context = new ConcurrentHashMap<>();
	
	final Map<String, Class> basicClassContext = new LinkedHashMap<String, Class>();
	final Class [] standardJAVATypes = {
			byte.class, boolean.class, 
			short.class, int.class, char.class,
			float.class, long.class, double.class
			,
			Byte.class, Boolean.class, 
			Short.class, Integer.class, Character.class,
			Float.class, Long.class, Double.class
		};
	
	private ClassBufferedFactory() {
		initialization();
	}

	private void initialization() {
		for (Class c : standardJAVATypes) {
			basicClassContext.put(c.getName(), c);
		}
	}
	
	public <T> Class<T> forName(String name)  throws ClassNotFoundException{
		return forName(name, getContextClassLoader());
	}
	
	public <T> Class<T> forName(String name, ClassLoader ... loaders) throws ClassNotFoundException{ 
		if(basicClassContext.containsKey(name)) {
			return basicClassContext.get(name);
		}
		
		if (loaders == null || loaders.length < 1) {
			loaders = new ClassLoader []{
					getContextClassLoader()
					};
		}
		ClassDescription desc = null;
		
		{
			//Load class by class loader parameters
			for(ClassLoader loader : loaders) {
				try {
					desc = loadClassDescription(loader, name);
					if (desc != null) {
						break;
					}
				} catch (Throwable e) {
					continue;
				}
			}
		}
		
		{
			//Load class by default registed class loaders
			if (desc == null) {
				for(ClassLoader loader : getDefaultClassLoaders()) {
					try {
						desc = loadClassDescription(loader, name);
						if (desc != null) {
							break;
						}
					} catch (Throwable e) {
						continue;
					}
				}
			}
		}
		
		if (desc == null) {
			throw new ClassNotFoundException(name);
		}
		
		return desc.getType();
	}
	
	public ClassDescription loadClassDescription(ClassLoader loader, String name) throws ClassNotFoundException{
		ClassTypesComponent component = getComponent(loader);
		ClassDescription desc = component.get(name);
		if (desc == null) {
			try {
				component.set(name, desc = new ClassDescription(loader.loadClass(name)));
			} catch (Throwable e) {
				component.set(name, desc = new ClassDescription(Class.forName(name)));
			}
			
		}
		return desc;
	}
	
	
	ClassTypesComponent getComponent() {
		return getComponent(getContextClassLoader());
	}
	
	ClassTypesComponent getComponent(Class refType) {
		return getComponent(refType.getClassLoader());
	}
	
	ClassTypesComponent getComponent(ClassLoader loader) {
		ClassTypesComponent comp = context.get(loader);
		if (comp == null) {
			context.put(loader, comp = new ClassTypesComponent());
		}
		return comp;
	}
	

	public ClassLoader getContextClassLoader() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = ClassBufferedFactory.class.getClassLoader();
		}
		return loader;
	}
	
	public ClassLoader [] getDefaultClassLoaders() {
		return defaultClassLoaderSet.toArray(new ClassLoader [0]);
	}
	
	public void addDefaultClassLoader(ClassLoader defaultClassLoader) {
		defaultClassLoaderSet.add(defaultClassLoader);
	}
}
