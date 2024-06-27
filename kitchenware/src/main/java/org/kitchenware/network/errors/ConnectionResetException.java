package org.kitchenware.network.errors;

import java.io.IOException;

@SuppressWarnings("serial")
public class ConnectionResetException extends IOException{
	public ConnectionResetException() {
		this("");
	}
	
	public ConnectionResetException(String message) {
		super(message);
	}

}
