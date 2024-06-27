package org.kitchenware.network.errors;

import java.io.IOException;

@SuppressWarnings("serial")
public class BadResponseException  extends IOException {

	public BadResponseException() {
		this("");
	}
	
	public BadResponseException(String message) {
		super(message);
	}

	public BadResponseException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
