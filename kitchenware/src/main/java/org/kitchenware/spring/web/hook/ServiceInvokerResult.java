package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.Required;

public class ServiceInvokerResult {

	/**
	 * UUID
	 * @see org.kitchenware.spring.web.hook.ServiceInvokerTransport#transportId
	 */
	@Required
	String transportId;
	
	Object result;

	Throwable caughtError;

	public Object getResult() {
		return result;
	}

	public ServiceInvokerResult setResult(Object result) {
		this.result = result;
		return ServiceInvokerResult.this;
	}

	public Throwable getCaughtError() {
		return caughtError;
	}

	public ServiceInvokerResult setCaughtError(Throwable caughtError) {
		this.caughtError = caughtError;
		return ServiceInvokerResult.this;
	}

}
