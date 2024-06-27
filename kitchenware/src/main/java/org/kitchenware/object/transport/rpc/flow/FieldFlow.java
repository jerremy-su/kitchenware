package org.kitchenware.object.transport.rpc.flow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.kitchenware.express.annotation.Required;
import org.kitchenware.reflect.Getter;
import org.kitchenware.reflect.GetterFactory;
import org.kitchenware.reflect.InstanceAllocator;
import org.kitchenware.reflect.Setter;
import org.kitchenware.reflect.SetterFactory;

public class FieldFlow {
	final Field field;
	final long offset;
	final Getter getter;
	final Setter setter;
	final int hash;
	final boolean required;
	final boolean requiredAndNotEmpty;
	
	FieldFlow(Field field){
		this.field = field;
		this.hash = field.hashCode();
		this.offset = InstanceAllocator.fieldOffSet(field);
		
		Class type = field.getType();
		this.getter = GetterFactory.getter(type);
		this.setter = SetterFactory.setter(type);
		
		Required required = field.getAnnotation(Required.class);
		this.required = required != null;
		this.requiredAndNotEmpty = required != null && required.notEmpty();
	}
	
	public Field getField() {
		return field;
	}
	
	public String getName() {
		return this.field.getName();
	}
	
	public Object get(Object src) {
		return this.getter.get(src, this.offset);
	}
	
	public void set(Object src, Object value) {
		this.setter.put(src, this.offset, value);
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public boolean isRequiredAndNotEmpty() {
		return requiredAndNotEmpty;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!FieldFlow.class.isInstance(obj)) {
			return false;
		}
		FieldFlow target = (FieldFlow) obj;
		boolean result = Objects.equals(this.field, target.field);
		return result;
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return this.field.isAnnotationPresent(annotationClass);
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return this.field.getAnnotation(annotationClass);
	}
	
	@Override
	public int hashCode() {
		return this.hash;
	}
	
	@Override
	public String toString() {
		return this.field.toString();
	}
}
