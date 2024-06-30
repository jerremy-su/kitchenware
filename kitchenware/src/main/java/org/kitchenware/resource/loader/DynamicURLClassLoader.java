package org.kitchenware.resource.loader;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.kitchenware.express.io.IOSteramLoader;

public class DynamicURLClassLoader extends ClassLoader implements Closeable{
	
	private final Set<String> invalidClassName = Collections.synchronizedSet(new LinkedHashSet<>());
	
	private final Map<URL, DynamicClassLoader> contextLoaders = new ConcurrentHashMap<>();
	private final Map<String, DynamicClassLoader> classeCaches = new ConcurrentHashMap<>();
	private final Map<String, Class> contextClasses = new ConcurrentHashMap<>();
	private final Map<URL, List<String>> classNameRef = new ConcurrentHashMap<>();
	ClassLoader parent;
	DynamicURLClassLoader treeParent;
	RefrenceURLClassLoader refClassLoader;
	String name;
	
	public DynamicURLClassLoader(String name, ClassLoader parent) {
		super(parent);
		this.name = name;
		this.parent = parent;
		refClassLoader = new RefrenceURLClassLoader(parent);
		if (parent instanceof DynamicURLClassLoader) {
			this.treeParent = (DynamicURLClassLoader)parent;
		}
	}
	
	DynamicClassLoader getCacheLoader(String className) {
		return classeCaches.get(className);
	}
	
	void putContextClass(String className, Class clazz) {
		contextClasses.put(className, clazz);
		if (treeParent != null) {
			treeParent.putContextClass(className, clazz);
		}
	}
	
	Class getContextClass(String className) {
		return contextClasses.get(className);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
		if(invalidClassName.contains(name)) {
			throw new ClassNotFoundException(name);
		}
		
		Class clazz = null;
		
		clazz = contextClasses.get(name);
		
		if(clazz == null && parent != null) {
			try {
				clazz = parent.loadClass(name);
			} catch (Throwable e) {}
		}
		
		if(clazz == null){
			DynamicClassLoader loader = classeCaches.get(name);
			if(loader != null) {
				clazz = loader.loadClassCurrent(name);
			}
		}
//		if(clazz == null) {
//			try {
//				clazz = refClassLoader.loadClass(name);
//			} catch (ClassNotFoundException e) {}
//		}
		if(clazz == null) {
			invalidClassName.add(name);
			throw new ClassNotFoundException(name);
		}
		return clazz;
	}

	@Override
	protected java.lang.Class<?> findClass(String name) throws ClassNotFoundException {
		return loadClass(name);
	}
	
	Class referenceClass(String name, byte[] b, int off, int len,
            ProtectionDomain protectionDomain) {
		return defineClass(name, b, off, len, protectionDomain);
	}

	Package referencePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
		Package result = getPackage(name);
		if(result == null) {
			result = super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
		}
		return result;
	}
	
	Class loadContextClass(String name){
		Class clazz = null;
		if (parent != null) {
			try {
				clazz = parent.loadClass(name);
			} catch (Throwable e) {}
		}
		
		return clazz;
	}
	
	public InputStream getResourceAsStream(URL url, String name) {
		
		DynamicClassLoader loader = contextLoaders.get(url);
		return loader.getResourceAsStream(name);
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		Vector<URL> result = new Vector<>();
		for(DynamicClassLoader loader : classeCaches.values()) {
			Enumeration<URL> src = loader.getResources(name);
			for(;src.hasMoreElements();) {
				result.add(src.nextElement());
			}
		}
		Enumeration<URL> src = super.getResources(name);
		for(;src.hasMoreElements();) {
			result.add(src.nextElement());
		}
		
		return result.elements();
	}
	
	@Override
	protected Package[] getPackages() {
		return super.getPackages();
	}
	
	public URL[] getURLs() {
		return contextLoaders.keySet().toArray(new URL [0]);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
//		InputStream in = refClassLoader.getResourceAsStream(name);
//		if (in == null) {
//			URL url = getResource(name);
//			if (url != null) {
//				InputStream srcIn = null;
//				try {
//					srcIn = url.openStream();
//					in = new ByteArrayInputStream(IOUtils.forceRead(srcIn));
//				} catch (Throwable e) {}
//				finally {
//					if(srcIn != null ) {
//						try {
//							srcIn.close();
//						}catch(Throwable e) {}
//						
//					}
//				}
//			}
//		}
		
		InputStream in = null;
		URL url = getResource(name);
		
		if(url == null) {
			url = getResource(DynamicClassLoader.SPRING_PLUGIN_BOOT_PATH + "/" + name);
		}
		
		if (url != null) {
			InputStream srcIn = null;
			try {
				srcIn = url.openStream();
				in = new ByteArrayInputStream(IOSteramLoader.load(srcIn));
			} catch (Throwable e) {}
			finally {
				if(srcIn != null ) {
					try {
						srcIn.close();
					}catch(Throwable e) {}
					
				}
			}
		}
//		if(in == null && parent != null) {
//			in = parent.getResourceAsStream(name);
//		}
		return in;
	}
	
	@Override
	public URL getResource(String name) {
//		URL url = refClassLoader.getResource(name);
//		if (url == null) {
//			for(DynamicClassLoader l : contextLoaders.values()) {
//				if ((url = l.getResource(name)) != null) {
//					break;
//				}
//			}
//		}
		
		URL url  = null;
		for(DynamicClassLoader l : contextLoaders.values()) {
			if ((url = l.getResource(name)) != null) {
				break;
			}
		}
		if (url == null && parent != null) {
			url = parent.getResource(name);
		}
		return url;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return super.findResources(name);
	}
	
	public void loadURL(URL url) {
		refClassLoader.addURL(url);
	}
	
	public void addURL(URL url) {
		synchronized (classeCaches) {
			refClassLoader.addURL(url);
			try {
				DynamicClassLoader classLoader = new DynamicClassLoader(this, url);
				contextLoaders.put(url, classLoader);
				List<String> classes = classLoader.getClassNames();
				classNameRef.put(url, classes);
				classes.forEach((n)->{
					classeCaches.put(n, classLoader);
				});
			} catch (Throwable e) {}
		}
	}
	
	
	public List<String> getClassRef(URL url){
		synchronized (classeCaches) {
			List<String> result = classNameRef.get(url);
			return result;
		}
	}
	
	public List<URL> getLoadedURLs(){
		return new ArrayList<>(contextLoaders.keySet());
	}
	
	class RefrenceURLClassLoader extends URLClassLoader{
		RefrenceURLClassLoader(ClassLoader parent){
			super(new URL[0], parent);
		}
		
		@Override
		protected void addURL(URL url) {
			super.addURL(url);
		}
	}
	
	@Override
	public void close() throws IOException {
		refClassLoader.close();
	}
}
