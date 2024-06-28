package org.kitchenware.reflect.basic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.concurrent.ConcurrentLockFactory;
import org.kitchenware.express.util.ArrayCollect;
import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.EmptyArray;
import org.kitchenware.reflect.MethodId;
import org.kitchenware.unsafe.Unsafe;

public class ClassDescribe {
	static final Logger LOGGER = Logger.getLogger(ClassDescribe.class.getName());
	
	static final Package jutilPackage = Package.getPackage("java.util");
	
	final static Map<Class, ClassDescribe> tbmdContext = new ConcurrentHashMap<>();
	final static ConcurrentLockFactory LOCKS = new ConcurrentLockFactory();
	
	public static ClassDescribe getDescribe(
			@NotNull Class clazz){
		if(clazz == null) {
			return null;
		}
		
		ClassDescribe md = tbmdContext.get(clazz);
		if(md != null) {
			return md;
		}
		
		Lock lock = LOCKS.get(clazz);
		lock.lock();
		try {
			md = tbmdContext.get(clazz);
			if(md == null) {
				tbmdContext.put(clazz, md = new ClassDescribe(clazz));
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}finally {
			lock.unlock();
		}
		
		return md;
	}
	
	final Class type;
	final Class componentType;
	final Map<String, FieldDescribe> fields = new LinkedHashMap<String, FieldDescribe>();
	final FieldDescribe [] requiredFields;
	
	final Map<MethodId, Method> declaredMethods = new LinkedHashMap<>();
	
	final boolean typeInterface;
	final boolean typeArray;
	Constructor constructor;
	ClassDescribe parent;
	ClassDescribe(Class type){
		this.typeArray = type.isArray();
		this.typeInterface = type.isInterface();
		this.type = type;
		if (typeArray) {
			componentType = type.getComponentType(); 
			this.requiredFields = EmptyArray.getInstance().array(FieldDescribe.class);
		}else{
			componentType = null;
			List<FieldDescribe> requiredFields = new ArrayList<>();
			
			if(! typeInterface) {
				//install constructor
				try {
					Constructor constructor = type.getDeclaredConstructor();
					constructor.setAccessible(true);
					this.constructor = constructor;
				} catch (Throwable e) {}
				
				//install fields
				for (Field f : type.getDeclaredFields()) {
					if (Modifier.isStatic(f.getModifiers())
							|| (!type.getPackage().equals(jutilPackage) && Modifier.isTransient(f.getModifiers()))) {
						continue;
					}
					if (Modifier.isFinal(f.getModifiers())) {
						remodifyFieldFinalProperty(f);
					}
					f.setAccessible(true);
					FieldDescribe field = new FieldDescribe(f);
					fields.put(f.getName(), field);
					if(field.isRequired()) {
						requiredFields.add(field);
					}
				}
			}
			
			this.requiredFields = ArrayCollect.get(FieldDescribe.class).toArray(requiredFields);
			
		}
		Class superType = type.getSuperclass();
		if (superType != null ) {
			ClassDescribe parentMetadata = getDescribe(superType);
			this.parent = parentMetadata;
		}
		
		installMethods();
	}
	
	private void installMethods() {
		if(this.typeArray) {
			return;
		}
		Method [] methods;
		if(this.typeInterface) {
			methods = this.type.getMethods();
		}else {
			methods = this.type.getDeclaredMethods();
		}
		
		ArrayObjects.foreach(methods, (i, method)->{
			MethodId id = MethodId.getId(method);
			this.declaredMethods.put(id, method);
		});
	}
	
	private void remodifyFieldFinalProperty(Object src){
		try {
			Field modifiersField = src.getClass().getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			int modifiers = modifiersField.getInt(src);
			modifiersField.setInt(src, modifiers &~ Modifier.FINAL);
		} catch (Throwable e) {}
	}
	
	public boolean isArray() {
		return this.typeArray;
	}
	
	public boolean isInterface() {
		return this.typeInterface;
	}
	
	public Constructor getConstructor() {
		return constructor;
	}
	
	public FieldDescribe [] getRequiredFields() {
		List<FieldDescribe> fields = new ArrayList<>();
		ClassDescribe md = this;
		for(;md != null;) {
			if(ArrayObjects.assertArrayNotEmpty(md.requiredFields)) {
				fields.addAll(Arrays.asList(md.requiredFields));
			}
			md = md.parent;
		}
		return ArrayCollect.get(FieldDescribe.class).toArray(fields);
	}
	
	public Method [] getDeclaredMethods() {
		
		Class type = this.type;
		Map<MethodId, Method> methods = new LinkedHashMap<>();
		
		for(;type != null;) {
			Map<MethodId, Method> typeMethods = getDescribe(type).declaredMethods;
			typeMethods.forEach((id, method) -> {
				if(methods.containsKey(id)) {
					return;
				}
				methods.put(id, method);
			});
			type = type.getSuperclass();
		}
		
		return ArrayCollect.get(Method.class).toArray(methods.values());
	}
	
	public Method getDeclaredMethod(
			@NotNull MethodId id) {
		if(id == null) {
			return null;
		}
		
		Method method = null;
		
		Class type = this.type;
		for(;method == null && type != null;) {
			ClassDescribe typeDescribe = getDescribe(type);
			method = typeDescribe.declaredMethods.get(id);
			if(method == null) {
				type = type.getSuperclass();
			}
		}
		return method;
	}
	
	public String [] getFieldNames() {
		List<String> result = new ArrayList<>();
		ClassDescribe md = this;
		while(md != null){
			result.addAll(md.fields.keySet());
			md = md.parent;
		}
		return ArrayCollect.STRING.toArray(result);
	}
	
	public FieldDescribe [] getFiledArray(){
		List<FieldDescribe> result = new ArrayList<>();
		ClassDescribe md = this;
		while(md != null){
			result.addAll(md.fields.values());
			md = md.parent;
		}
		return result.toArray(new FieldDescribe [0]);
	}
	
	public FieldDescribe getField(String name){
		ClassDescribe md = this;
		FieldDescribe f = md.fields.get(name); 
		while(f == null && (md = md.parent) != null){
			f = md.fields.get(name); 
		}
		return f;
	}
	
	public Object unsafeNewInstance() throws Throwable{
		return Unsafe.getUnsafe().allocateInstance(type);
	}
	
	public Object newInstance() throws Throwable{
		return newInstance(this.constructor);
	}
	
	public Object newInstance(Constructor constructor) throws Throwable{
		if (constructor != null) {
			try {
				return constructor.newInstance();
			} catch (Throwable e) {
				return newInstance(null);
			}
		}else{
			try {
				return type.newInstance();
			} catch (Throwable e2) {
				try {
					return Unsafe.getUnsafe().allocateInstance(type);
				} catch (Throwable e3) {
					throw e3;
				}
			}
		}
	
	}
}
