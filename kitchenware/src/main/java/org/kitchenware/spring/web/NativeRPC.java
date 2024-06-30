package org.kitchenware.spring.web;

import org.kitchenware.express.annotation.Optional;

public class NativeRPC {

	@Optional
	final ServiceRPC serviceRPC;
	
	public NativeRPC() {
		this(null);
	}
	
	public NativeRPC(
			@Optional final ServiceRPC serviceRPC
			){
		this.serviceRPC = serviceRPC;
	}
}
