package org.kitchenware.express.function.caseby;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.annotation.Required;
import org.kitchenware.express.util.Asserts;

public class CaseNotNull<T> extends CaseBy{

	
	public static <T> CaseNotNull<T> of(
			@Optional final Supplier<T> supplier){
		return new CaseNotNull<>(supplier);
	}
	
	public static <T> CaseNotNull<T> of(
			@Optional final T src){
		return new CaseNotNull<>(src);
	}
	
	@Optional
	final Supplier<T> supplier;
	
	T src;
	
	@Required
	transient Consumer<T> event;
	
	CaseNotNull(
			@Optional T src){
		this(()-> src);
	}
		
	
	CaseNotNull(
			@Optional final Supplier<T> supplier){
		
		Asserts.assertNotNull(supplier, "'supplier' cannot be null.");
		
		this.supplier = supplier;
		this.when(()-> 
			(this.src = supplier.get()) != null
			);
	}
	
	public CaseNotNull<T> accept(
			@NotNull final Consumer<T> event){
		Asserts.assertNotNull(event, "'event' cannot be null.");
		
		this.onThen(()->{
			event.accept(this.src);
		});
		
		return CaseNotNull.this;
	}
}
