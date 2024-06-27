package org.kitchenware.express.function;

import java.util.function.Consumer;

import org.kitchenware.express.util.Errors;

@FunctionalInterface
public interface ConsumerAccepter<T>{

	void accept(T t) throws Exception;
	
	default void error(Throwable caughtError) {
		
	}
	
	default Consumer<T> toConsumer(){
		return toConsumer(null);
	}
	
	default Consumer<T> toConsumer(Consumer<Throwable> errorHandler){
		Consumer<T> function = t -> {
			try {
				accept(t);
			} catch (Throwable e) {
				if(errorHandler == null) {
					throw Errors.throwRuntimeable(e);
				}else {
					errorHandler.accept(e);
				}
			}
		};
		return function;
	}
	
	default boolean acceptNext() {
		return true;
	}
}
