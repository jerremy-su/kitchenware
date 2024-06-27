package org.kitchenware.network.errors;

import java.io.IOException;

@SuppressWarnings("serial")
public class ConnectionAbordException extends IOException {

	public ConnectionAbordException() {
		this("");
	}
	
	public ConnectionAbordException(String message) {
		super(message);
	}
}
