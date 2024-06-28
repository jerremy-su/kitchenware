package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.Required;

public class ServiceInvokeResult {

	/**
	 * UUID
	 * 
	 * @see org.kitchenware.spring.web.hook.ServiceInvokerTransport#transportId
	 */
	@Required
	String transportId;

	Object result;

	Throwable caughtError;

	public Object getResult() {
		return result;
	}

	public ServiceInvokeResult setResult(Object result) {
		this.result = result;
		return ServiceInvokeResult.this;
	}

	public Throwable getCaughtError() {
		return caughtError;
	}

	public ServiceInvokeResult setCaughtError(Throwable caughtError) {
		this.caughtError = caughtError;
		return ServiceInvokeResult.this;
	}

	public String getTransportId() {
		return transportId;
	}

	public ServiceInvokeResult setTransportId(String transportId) {
		this.transportId = transportId;
		return ServiceInvokeResult.this;
	}

}
