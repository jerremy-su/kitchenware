package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.annotation.Required;
import org.kitchenware.reflect.MethodId;

public class SpringServiceInvokerTransport {

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
	boolean mappingServiceName;

	@Required
	MethodId methodId;

	@Required
	Object[] parameters;

	public boolean isMappingServiceName() {
		return mappingServiceName;
	}

	public SpringServiceInvokerTransport setMappingServiceName(boolean mappingServiceName) {
		this.mappingServiceName = mappingServiceName;
		return SpringServiceInvokerTransport.this;
	}

	public MethodId getMethodId() {
		return methodId;
	}

	public SpringServiceInvokerTransport setMethodId(MethodId methodId) {
		this.methodId = methodId;
		return SpringServiceInvokerTransport.this;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public SpringServiceInvokerTransport setParameters(Object[] parameters) {
		this.parameters = parameters;
		return SpringServiceInvokerTransport.this;
	}

	public String getTransportId() {
		return transportId;
	}

	public SpringServiceInvokerTransport setTransportId(String transportId) {
		this.transportId = transportId;
		return SpringServiceInvokerTransport.this;
	}

	public String getServiceName() {
		return serviceName;
	}

	public SpringServiceInvokerTransport setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return SpringServiceInvokerTransport.this;
	}

	public String getServiceType() {
		return serviceType;
	}

	public SpringServiceInvokerTransport setServiceType(String serviceType) {
		this.serviceType = serviceType;
		return SpringServiceInvokerTransport.this;
	}

}
