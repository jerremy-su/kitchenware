package org.kitchenware.resource.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.kitchenware.express.io.IOSteramLoader;

public class DynamicClassLoader extends ClassLoader implements Closeable {
	private final Set<String> invalidClassName = Collections.synchronizedSet(new LinkedHashSet<>());
	
	public static final String SPRING_PLUGIN_BOOT = "BOOT-INF.classes.";
	public static final String SPRING_PLUGIN_BOOT_PATH = "BOOT-INF/classes";
	public static final String SPRING_PLUGIN_BOOT_LIB_PATH = "BOOT-INF/lib";
	
	private final static Map<CodeSource, ProtectionDomain> pdcache = new ConcurrentHashMap<>();
	public static final String MANIFEST_JAR_FILE_NAME = "META-INF/MANIFEST.MF";
	public static final String CLASS_TYPE_NAME = ".class";
	File jarFile;
	ProtectionDomain protectionDomain;
	CodeSource codeSource;
	boolean isDirectory;
	Manifest manifest;
//	Map<String, Class> classCaches = new HashMap<>();
	List<String> classNames = new ArrayList<>();
	Map<String, URL> resourceContext = new LinkedHashMap<>();
	DynamicURLClassLoader parent;
	URLClassLoader jarFileClassLoader;

	final Map<String, ResourceTemp<Enumeration<URL>>> resourcesTemporaries = Collections.synchronizedMap(new WeakHashMap<>());
	
	final Map<String, ResourceTemp<URL>> resourceTemporaries = Collections.synchronizedMap(new WeakHashMap<>());
	
	public DynamicClassLoader(DynamicURLClassLoader parent, URL url) throws IOException {
		this(parent, new File(URLDecoder.decode(url.getFile(), "UTF-8")));
	}

	public DynamicClassLoader(DynamicURLClassLoader parent, File jarFile)
			throws IOException {
		super(parent);
		this.parent = parent;
		if (!jarFile.exists()) {
			throw new NullPointerException(String.format("File '%s' cannot exists.", jarFile));
		}
		this.jarFile = jarFile;
		isDirectory = jarFile.isDirectory();
		codeSource = new CodeSource(jarFile.toURI().toURL(), new java.security.cert.Certificate[0]);
		if (!isDirectory) {
			new JarFile(jarFile).close();
			jarFileClassLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() });
		} else {
			protectionDomain = new ProtectionDomain(codeSource, null, this, null);
		}
		initialzationClassEntrys();
	}

	private void initialzationClassEntrys() throws IOException {
		if (isDirectory) {
			processJARDirectory(jarFile.getAbsolutePath().length() + 1);
		} else {
			processJARFile();
		}
	}

	private void processJARDirectory(int validLen) throws IOException {
		visitDirectory(validLen, jarFile);
	}

	private void visitDirectory(int validLen, File directFile) throws IOException {
		InputStream in;
		ByteArrayOutputStream out;
		String className;
		String clipPath;
		Class clazz = null;
		for (File f : directFile.listFiles()) {
			if (f.isDirectory()) {
				visitDirectory(validLen, f);
			}
			clipPath = f.getAbsolutePath();
			clipPath = clipPath.substring(validLen, clipPath.length());
			resourceContext.put(clipPath.replace("\\", "/"), f.toURI().toURL());
			if (clipPath.toLowerCase().endsWith(CLASS_TYPE_NAME)) {
				className = clipPath.substring(0, clipPath.lastIndexOf(".")).replace("\\", ".").replace("/", ".");
				if(className.startsWith(SPRING_PLUGIN_BOOT)) {
					className = className.substring(SPRING_PLUGIN_BOOT.length());
				}
				// try {
				// clazz = parent.loadClass(className);
				// }catch(Throwable e) {}
				classNames.add(className);
				// if (clazz == null) {
				// in = new FileInputStream(f);
				// out = new ByteArrayOutputStream();
				// try {
				// IOSteramLoader.load(in, out);
				// clazz = defineClass(classEntrys, className, out.toByteArray(), 0,
				// out.size());
				// }catch(Throwable e) {}
				// finally {
				// in.close();
				// }
				// }
				// if(clazz != null){
				//// System.out.println("DynamicClassLoader : dynamic initial class by file - "
				// + className);
				// classEntrys.put(className, clazz);
				// }
				continue;
			} else if (clipPath.equalsIgnoreCase("META-INF\\MANIFEST.MF") || clipPath.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
				in = new FileInputStream(f);
				out = new ByteArrayOutputStream();
				IOSteramLoader.load(in, out);
				manifest = new Manifest(new ByteArrayInputStream(out.toByteArray()));
				in.close();
			}
		}
	}

	private void processJARFile() throws IOException {
		JarInputStream in = new JarInputStream(new FileInputStream(jarFile));
		manifest = in.getManifest();
		// ByteArrayOutputStream out;
		String className;
		String entryName;
		// Class clazz = null;
		for (JarEntry entry = null; (entry = in.getNextJarEntry()) != null;) {
			if ((entryName = entry.getName()).toLowerCase().endsWith(CLASS_TYPE_NAME)) {
				className = entryName.substring(0, entryName.lastIndexOf(".")).replace("/", ".");
				if(className.startsWith(SPRING_PLUGIN_BOOT)) {
					className = className.substring(SPRING_PLUGIN_BOOT.length());
				}
				// out = new ByteArrayOutputStream();
				// IOSteramLoader.load(in, out);
				// clazz = defineClass(classEntrys, className, out.toByteArray(), 0,
				// out.size());
				// if(clazz != null){
				//// System.out.println("DynamicClassLoader : dynamic load class by jar - " +
				// className);
				// classEntrys.put(className, clazz);
				// }
				classNames.add(className);
			}
			in.closeEntry();
		}
		in.close();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream in = null;
		URL url = getResource(name);
//		if(url == null) {
//			url = getResource(SPRING_PLUGIN_BOOT_PATH + "/" + name);
//		}
		if (url != null) {
			InputStream srcIn = null;
			try {
				srcIn = url.openStream();
				in = new ByteArrayInputStream(IOSteramLoader.load(srcIn));
			} catch (Throwable e) {
			} finally {
				if (srcIn != null) {
					try {
						srcIn.close();
					} catch (Throwable e) {
					}

				}
			}
		}
		return in;
	}

	@Override
	public synchronized Enumeration<URL> getResources(String name) throws IOException {
		ResourceTemp<Enumeration<URL>> temp = this.resourcesTemporaries.get(name);
		
		if(temp != null) {
			return temp.obj;
		}
		
		Enumeration<URL> result = getResources0(name);
		this.resourcesTemporaries.put(name, new ResourceTemp<>(result));
		return result;
	}
	
	private Enumeration<URL> getResources0(String name) throws IOException {
		Vector<URL> result = new Vector<>();
		if(jarFileClassLoader != null) {
			Enumeration<URL> src = jarFileClassLoader.findResources(name);
			for(;src.hasMoreElements();) {
				result.add(src.nextElement());
			}
			if(result.isEmpty()) {
				src = jarFileClassLoader.findResources(SPRING_PLUGIN_BOOT_PATH + "/" + name);
				for(;src.hasMoreElements();) {
					result.add(src.nextElement());
				}
			}
		}else {
			resourceContext.forEach((k, v)->{
				if(k.startsWith(name)) {
					result.add(v);
				}
			});
		}
		return result.elements();
	}
	
	@Override
	public URL getResource(String name) {
		if(name == null) {
			return null;
		}
		ResourceTemp<URL> temp = resourceTemporaries.get(name);
		if(temp != null) {
			return temp.obj;
		}
		
		URL url = resourceContext.get(name);
		if (url == null && jarFileClassLoader != null) {
			url = jarFileClassLoader.getResource(name);
			if(url == null) {
				url = jarFileClassLoader.getResource(SPRING_PLUGIN_BOOT_PATH + "/" + name);
			}
		}
		resourceTemporaries.put(name, new ResourceTemp<>(url));
		return url;
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
	
	public Class defineClass(String className) {
		Class clazz = parent.loadContextClass(className);
		if (clazz == null) {
			try {
				String path = className.replace('.', '/').concat(".class");
				ByteArrayOutputStream buffout = new ByteArrayOutputStream();
				InputStream in = getResourceAsStream(path);
				if (in != null) {
					int i = className.lastIndexOf('.');
					String pkgname = className.substring(0, i);
					referencePackage(pkgname
							, null, null, null, null, null, null, null);
					IOSteramLoader.load(in, buffout);
					clazz = referenceClass(className, buffout.toByteArray(), 0, buffout.size(),
							getProtectionDomain(codeSource));
					setClassAssertionStatus(className, true);
					parent.putContextClass(className, clazz);
				}else {
					DynamicClassLoader nextClassLoader = parent.getCacheLoader(className);
					if(nextClassLoader != null) {
						clazz = nextClassLoader.loadClass(className);
					}
				}
			} catch (Throwable e) {
			}
		}
		return clazz;
	}

	private ProtectionDomain getProtectionDomain(CodeSource cs) {
		if (cs == null)
			return null;

		ProtectionDomain pd = null;
//		synchronized (pdcache)
		{
			pd = pdcache.get(cs);
			if (pd == null) {
				pd = new ProtectionDomain(cs, new Permissions(), this, null);
				pdcache.put(cs, pd);
			}
		}
		return pd;
	}

	// protected Class loadClassByParent(String name){
	// Class clazz;
	// try {
	// return parent == null ? null : parent.loadClass(name);
	// } catch (Throwable e) {
	// return null;
	// }
	// }

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
		if(invalidClassName.contains(name)) {
			throw new ClassNotFoundException(name);
		}
		
		Class clazz = loadClassCurrent(name);
		if (clazz == null) {
			invalidClassName.add(name);
			throw new ClassNotFoundException("DynamicClassLoader : " + name);
		}
		return clazz;
	}
	
	public Class<?> loadClassCurrent(String name){
		Class clazz = parent.getContextClass(name);
		if (clazz == null) {
			clazz = defineClass(name);
		}
		return clazz;
	}
	
	List<String> getClassNames() {
		return new ArrayList<>(classNames);
	}

	public File getJarFile() {
		return jarFile;
	}

	public Manifest getManifest() {
		return manifest;
	}

	@Override
	public void close() throws IOException {
		if (jarFileClassLoader != null) {
			jarFileClassLoader.close();
		}
	}
	
	class ResourceTemp<T> {
		T obj;
		
		ResourceTemp(T obj){
			this.obj = obj;
		}
		
	}
}
