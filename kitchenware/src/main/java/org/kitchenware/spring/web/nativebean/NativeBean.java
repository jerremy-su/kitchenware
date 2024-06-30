package org.kitchenware.spring.web.nativebean;

import java.util.Objects;

import javax.annotation.Resource;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Required;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.reflect.basic.ClassDescribe;
import org.kitchenware.reflect.basic.FieldDescribe;
import org.kitchenware.spring.web.annotation.Impl;
import org.springframework.beans.factory.annotation.Autowired;

public class NativeBean {

	@Required
	final Class implementsType;
	
	@Required
	final NativeBeanFactory beanFactory;
	
	final ClassDescribe classDescribe;
	
	final Object bean;
	NativeBean(
			@NotNull NativeBeanFactory beanFactory
			, @NotNull Class implementsType
			) throws Exception{
		
		this.beanFactory = beanFactory;
		this.implementsType = implementsType;
		this.classDescribe = ClassDescribe.getDescribe(implementsType);
		
		try {
			this.bean = this.classDescribe.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		installBean(this.bean);
	}
	
	private void installBean(@NotNull final Object bean) throws Exception{
		FieldDescribe [] fields = this.classDescribe.getFields();
		
		for(int i = 0; i < fields.length; i ++) {
			FieldDescribe field = fields [i];
			
			if(field.isAnnotationPresent(Impl.class)) {
				installNativeField(bean, field, field.getAnnotation(Impl.class));
			}else if(
					field.isAnnotationPresent(Autowired.class)
					|| field.isAnnotationPresent(Resource.class)
					){
				installRPCField(bean, field);
			}
		}
	}
	
	private void installNativeField(
			@NotNull final Object bean, @NotNull final FieldDescribe field, @NotNull final Impl impl) throws Exception{
		Class implType = impl.value();
		
		if(! implType.isAssignableFrom(field.getType())) {
			throw new IllegalAccessException(String.format("Failed resolve field: %s", field.getField().toGenericString()));
		}
		
		NativeBean nativeBean;
		if(Objects.equals(implType, this.implementsType)) {
			nativeBean = this;
		}else {
			nativeBean = this.beanFactory.getBean(implType);
		}
		
		field.getField().set(bean, nativeBean.getBean());
	}
	
	private void installRPCField(
			@NotNull final Object bean, @NotNull final FieldDescribe field) throws Exception{
		Class serviceType = field.getType();
		String serviceName = null;
		if(field.isAnnotationPresent(Resource.class)) {
			Resource resource = field.getAnnotation(Resource.class);
			serviceName = resource.name();
		}
		
		Object rpcBean;
		if(StringObjects.isEmptyAfterTrim(serviceName)) {
			rpcBean = this.beanFactory.getServiceRPC().getService(serviceType);
		}else {
			rpcBean = this.beanFactory.getServiceRPC().getService(serviceType, serviceName);
		}
		
		field.getField().set(bean, rpcBean);
	}
	
	public <T> T getBean() {
		return (T) bean;
	}
	
}
