package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.annotation.Required;
import org.kitchenware.reflect.MethodId;

public class ServiceInvokerTransport {

	/**
	 * UUID
	 */
	@Required
	String transportId;

	@Optional
	String serviceName;

	@Required
	String serviceType;

	@Required
	MethodId methodId;

	@Required
	Object[] parameters;

	public MethodId getMethodId() {
		return methodId;
	}

	public ServiceInvokerTransport setMethodId(MethodId methodId) {
		this.methodId = methodId;
		return ServiceInvokerTransport.this;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public ServiceInvokerTransport setParameters(Object[] parameters) {
		this.parameters = parameters;
		return ServiceInvokerTransport.this;
	}

	public String getTransportId() {
		return transportId;
	}

	public ServiceInvokerTransport setTransportId(String transportId) {
		this.transportId = transportId;
		return ServiceInvokerTransport.this;
	}

	public String getServiceName() {
		return serviceName;
	}

	public ServiceInvokerTransport setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return ServiceInvokerTransport.this;
	}

	public String getServiceType() {
		return serviceType;
	}

	public ServiceInvokerTransport setServiceType(String serviceType) {
		this.serviceType = serviceType;
		return ServiceInvokerTransport.this;
	}

}
