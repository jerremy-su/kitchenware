package org.kitchenware.object.transport.rpc.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kitchenware.express.util.ArrayCollect;
import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.EmptyArray;

import sun.misc.Unsafe;

public class ClassFlow {
	static final Package jutilPackage = Package.getPackage("java.util");
	static final Unsafe unsafe;
	static{
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe)f.get(null);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	final Class type;
	final Class componentType;
	final Map<String, FieldFlow> fields = new LinkedHashMap<String, FieldFlow>();
	final FieldFlow [] requiredFields;
	
	final Map<String, Method [] > declaredMethods = new LinkedHashMap<>();
	
	final boolean typeInterface;
	final boolean typeArray;
	Constructor constructor;
	ClassFlow parent;
	public ClassFlow(Class type){
		this.typeArray = type.isArray();
		this.typeInterface = type.isInterface();
		this.type = type;
		if (typeArray) {
			componentType = type.getComponentType(); 
			this.requiredFields = EmptyArray.getInstance().array(FieldFlow.class);
		}else{
			componentType = null;
			List<FieldFlow> requiredFields = new ArrayList<>();
			for (Field f : type.getDeclaredFields()) {
				if (Modifier.isStatic(f.getModifiers())
						|| (!type.getPackage().equals(jutilPackage) && Modifier.isTransient(f.getModifiers()))) {
					continue;
				}
				if (Modifier.isFinal(f.getModifiers())) {
					remodifyFieldFinalProperty(f);
				}
				f.setAccessible(true);
				FieldFlow field = new FieldFlow(f);
				fields.put(f.getName(), field);
				if(field.isRequired()) {
					requiredFields.add(field);
				}
			}
			this.requiredFields = ArrayCollect.get(FieldFlow.class).toArray(requiredFields);
			try {
				Constructor constructor = type.getDeclaredConstructor();
				constructor.setAccessible(true);
				this.constructor = constructor;
			} catch (Throwable e) {}
		}
		Class superType = type.getSuperclass();
		/**------------------B BUG2433(Jerremy 2018.06.19)------------------*/
		if (superType != null ) {
			ClassFlow parentMetadata = ObjectFlow.tbmdContext.get(superType);
			if(parentMetadata == null) {
				ObjectFlow.tbmdContext.put(superType, parentMetadata = new ClassFlow(superType));
			}
			this.parent = parentMetadata;
		}
		/**------------------E BUG2433(Jerremy 2018.06.19)------------------*/
		
		if(!this.type.isArray()){
			Map<String, List<Method>> methods = new LinkedHashMap<>();
			for(Method m : this.type.getDeclaredMethods()) {
				if(Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				
				List<Method> tmp = methods.get(m.getName());
				if(tmp == null) {
					methods.put(m.getName(), tmp = new ArrayList<>());
				}
				tmp.add(m);
			}
			
			methods.forEach((name, ms) -> {
				this.declaredMethods.put(
						name, ArrayCollect.get(Method.class).toArray(ms));
			});
		}
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
	
	public FieldFlow [] getRequiredFields() {
		List<FieldFlow> fields = new ArrayList<>();
		ClassFlow md = this;
		for(;md != null;) {
			if(ArrayObjects.assertArrayNotEmpty(md.requiredFields)) {
				fields.addAll(Arrays.asList(md.requiredFields));
			}
			md = md.parent;
		}
		return ArrayCollect.get(FieldFlow.class).toArray(fields);
	}
	
	public Method [] getDeclaredMethods() {
		List<Method> methods = new ArrayList<>();
		ClassFlow md = this;
		for(;md != null;) {
			for(java.util.Map.Entry<String, Method []> en : md.declaredMethods.entrySet()) {
				methods.addAll(Arrays.asList(en.getValue()));
			}
			md = md.parent;
		}
		return ArrayCollect.get(Method.class).toArray(methods);
	}
	
	public Method [] getDeclaredMethod(String methodName) {
		List<Method> methods = new ArrayList<>();
		ClassFlow md = this;
		for(;md != null;) {
			Method [] tmp = md.declaredMethods.get(methodName);
			if(ArrayObjects.assertArrayNotEmpty(tmp)) {
				methods.addAll(Arrays.asList(tmp));
			}
			md = md.parent;
		}
		return ArrayCollect.get(Method.class).toArray(methods);
	}
	
	public String [] getFieldNames() {
		List<String> result = new ArrayList<>();
		ClassFlow md = this;
		while(md != null){
			result.addAll(md.fields.keySet());
			md = md.parent;
		}
		return ArrayCollect.STRING.toArray(result);
	}
	
	public FieldFlow [] getFiledArray(){
		List<FieldFlow> result = new ArrayList<>();
		ClassFlow md = this;
		while(md != null){
			result.addAll(md.fields.values());
			md = md.parent;
		}
		return result.toArray(new FieldFlow [0]);
	}
	
	public FieldFlow getField(String name){
		ClassFlow md = this;
		FieldFlow f = md.fields.get(name); 
		while(f == null && (md = md.parent) != null){
			f = md.fields.get(name); 
		}
		return f;
	}
	
	Object unsafeNewInstance() throws Throwable{
		return unsafe.allocateInstance(type);
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
					return unsafe.allocateInstance(type);
				} catch (Throwable e3) {
					throw e3;
				}
			}
		}
	
	}
}
