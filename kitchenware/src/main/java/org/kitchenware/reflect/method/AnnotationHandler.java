package org.kitchenware.reflect.method;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.Asserts;

public interface AnnotationHandler {

	Set<Class<? extends Annotation>> annotationTypeSet();
	
	Set<Annotation> annotations();
	
	<T extends Annotation> T getAnnotation(@NotNull final Class<T> type);
	
	boolean isAnnotationPresent(@NotNull final Class<? extends Annotation> type);
	
	void setAllAnnotation(@NotNull final AnnotationHandler handler);
	
	default boolean isAnnotation(
			@NotNull final Class<? extends Annotation> type) {
		
		Asserts.assertNotNull(type, "‘type’ cannot be null.");
		
		Set<Class<? extends Annotation>> annotationTypes = annotationTypeSet();
		
		boolean b = annotationTypes.contains(type);
		return b;
	}
}
