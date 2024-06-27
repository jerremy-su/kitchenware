package org.kitchenware.reflect.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.ArrayCollect;
import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.reflect.CloneSupplier;
import org.kitchenware.reflect.MethodId;

public class MethodDescribe extends DefaultAnnotationDescribe<MethodDescribe> implements CloneSupplier<MethodDescribe>{
	
	final Map<Integer, ParameterDescribe> parameters = Collections.synchronizedMap(new TreeMap<>());
	
	Class resultType;
	Class [] parameterTypes;
	
	final MethodId id;
	public MethodDescribe(
			@NotNull final MethodId id) {
		this.id = id;
	}
	
	public MethodId getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
	
	public int getParameterSize() {
		int result = this.parameters.size();
		return result;
	}
	
	public ParameterDescribe [] getParameters() {
		return ArrayCollect.get(ParameterDescribe.class).toArray(this.parameters.values());
	}
	
	public ParameterDescribe getParameter(int index) {
		ParameterDescribe parameter = parameters.get(index);
		return parameter;
	}
	
	void installMethod(
			@NotNull final Method method) {

		Asserts.assertNotNull(method, "'method' not found.");
		installMethodMetadatas(method);
		installMethodParameters(method);
	}
	
	private void installMethodMetadatas(
			@NotNull final Method method) {
		
		if(this.parameterTypes == null) {
			this.parameterTypes = method.getParameterTypes();
		}
		if(this.resultType == null) {
			this.resultType = method.getReturnType();
		}
		
		Annotation [] annotations = method.getAnnotations();
		installAnnotation(annotations);
	}
	
	private void installMethodParameters(
			@NotNull final Method method) {
		
		Class [] types = method.getParameterTypes();
		
		if(ArrayObjects.isEmpty(types)) {
			return;
		}
		
		Annotation [][] annotations = method.getParameterAnnotations();

		for(int i = 0; i < types.length; i ++) {
			Class type = types [i];
			ParameterDescribe parameter = this.parameters.get(Integer.valueOf(i));
			if(parameter == null) {
				this.parameters.put(Integer.valueOf(i), parameter = new ParameterDescribe(type, i));
			}
			Annotation [] ans = annotations [i];
			parameter.installAnnotation(ans);
		}
	}
}
