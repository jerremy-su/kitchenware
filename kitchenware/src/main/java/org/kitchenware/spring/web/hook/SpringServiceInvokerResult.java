package org.kitchenware.spring.web.hook;

import org.kitchenware.express.annotation.Required;

public class SpringServiceInvokerResult {

	/**
	 * UUID
	 * @see org.kitchenware.spring.web.hook.SpringServiceInvokerTransport#transportId
	 */
	@Required
	String transportId;
	
	Object result;

	Throwable caughtError;

	public Object getResult() {
		return result;
	}

	public SpringServiceInvokerResult setResult(Object result) {
		this.result = result;
		return SpringServiceInvokerResult.this;
	}

	public Throwable getCaughtError() {
		return caughtError;
	}

	public SpringServiceInvokerResult setCaughtError(Throwable caughtError) {
		this.caughtError = caughtError;
		return SpringServiceInvokerResult.this;
	}

}
