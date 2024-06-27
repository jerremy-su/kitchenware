package org.kitchenware.reflect.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.ArrayCollect;
import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.reflect.MethodId;
import org.kitchenware.reflect.basic.ClassDescribe;

public class TypeDescribe extends DefaultAnnotationDescribe<TypeDescribe>{

	final Map<MethodId, MethodDescribe> methods = Collections.synchronizedMap(new LinkedHashMap<>());
	final Class type;
		
	final TypeDescribeFactory typeFactory;
	TypeDescribe(
			@NotNull final TypeDescribeFactory typeFactory
			, @NotNull final Class type) {
		this.typeFactory = typeFactory;
		this.type = type;
		installType(type);
	}
	
	public MethodDescribe getMethod(
			@NotNull final Method method) {
		
		if(method == null) {
			return null;
		}
		
		MethodId id = MethodId.getId(method);
		
		MethodDescribe result = getMethod(id);
		return result;
	}
	
	public MethodDescribe getMethod(
			@NotNull final MethodId id) {
		
		if(id == null) {
			return null;
		}
		
		
		MethodDescribe method = this.methods.get(id);
		return method;
	}
	
	public MethodDescribe [] methods() {
		MethodDescribe [] methods = ArrayCollect.get(MethodDescribe.class)
				.toArray(this.methods.values());
		return methods;
	}
	
	private void installType(
			@NotNull final Class type) {
		ClassDescribe metadata = ClassDescribe.getDescribe(type);
		if(metadata.isArray()) {
			return;
		}
		installTypeMethods(type, metadata);
		 
		if(! metadata.isInterface()) {
			Class [] typeInterfaces = type.getInterfaces();
			
			Class typeInterface;
			for(int i = 0; i < typeInterfaces.length; i ++) {
				typeInterface =  typeInterfaces [i];
				TypeDescribe interfaceDescription = this.typeFactory.description(typeInterface);
				installTypeDescription(interfaceDescription);
			}
		}
		installTypeAnnotations(type);
	}
	
	private void installTypeDescription(
			@NotNull final TypeDescribe description) {
		if(description == null) {
			return;
		}
		
		this.setAllAnnotation(description);
		
		MethodDescribe [] methods = ArrayCollect.get(
				MethodDescribe.class).toArray(description.methods.values());
		
		for(int i = 0; i < methods.length; i ++) {
			MethodDescribe method = methods [i].cloneTo();
			MethodDescribe currentMethod = this.methods.get(method.getId());
			if(currentMethod == null) {
				this.methods.put(method.getId(), currentMethod = method);
			}else {
				installMethodDescription(method, currentMethod);
			}
		}
	}
	
	private void installMethodDescription(
			@NotNull final MethodDescribe method, @NotNull final MethodDescribe currentMethod) {
		currentMethod.setAllAnnotation(method);
		
		int parameterSize = currentMethod.getParameterSize();
		
		for(int i = 0; i < parameterSize; i ++) {
			ParameterDescribe parameter = method.getParameter(i);
			ParameterDescribe currentParameter = currentMethod.getParameter(i);
			currentParameter.setAllAnnotation(parameter);
		}
	}
	
	private void installTypeAnnotations(@NotNull final Class type) {
		Annotation [] annotations = type.getAnnotations();
		installAnnotation(annotations);
	}
	
	private void installTypeMethods(@NotNull final Class type, @NotNull final ClassDescribe metadata) {
		if(metadata.isArray()) {
			return;
		}
		
		Method [] methods = metadata.getDeclaredMethods();
		installMethods(methods);
	}
	
	private void installMethods(@NotNull final Method [] method) {
		ArrayObjects.foreach(method, (i, m)->{
			installMethod(m);
		});
	}
	
	private void installMethod(@NotNull final Method method) {
		MethodId id = MethodId.getId(method);
		MethodDescribe methodDescription = methods.get(id);
		if(methodDescription == null) {
			methods.put(id, methodDescription = new MethodDescribe(id));
		}
		methodDescription.installMethod(method);
	}
}
