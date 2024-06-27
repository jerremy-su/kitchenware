package org.kitchenware.express.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Supported Type: java.util.collection, java.lang.Array, java.lang.Object
 * @author jerremy.su
 *
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Required {

	String [] igonreField() default "";
	
	/**
	 * Supported Type; java.util.collection, java.lang.Array, java.util.Map, java.lang.String
	 * @return
	 */
	boolean notEmpty() default true;
}
