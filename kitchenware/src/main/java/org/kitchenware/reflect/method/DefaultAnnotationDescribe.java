package org.kitchenware.reflect.method;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.ArrayCollect;
import org.kitchenware.express.util.ArrayObjects;
import org.kitchenware.express.util.Asserts;
import org.kitchenware.express.util.CollectionObjects;

public abstract class DefaultAnnotationDescribe<T extends DefaultAnnotationDescribe<?>> implements AnnotationHandler {
	
	final transient Map<Class<? extends Annotation>, Annotation> annotations = Collections.synchronizedMap(new LinkedHashMap<>());
	
	protected void installAnnotation(
			@NotNull final Annotation [] annotations) {
		if(ArrayObjects.isEmpty(annotations)) {
			return;
		}
		
		ArrayObjects.foreach(annotations, (i, a) -> {
			this.annotations.put(a.annotationType(), a);
		});
	}

	@Override
	public Set<Class<? extends Annotation>> annotationTypeSet() {
		Set<Class<? extends Annotation>> types = new LinkedHashSet<>(annotations.keySet());
		return types;
	}

	@Override
	public Set<Annotation> annotations() {
		Set<Annotation> annotations = new LinkedHashSet<>(this.annotations.values());
		return annotations;
	}

	@Override
	public <T extends Annotation> T getAnnotation(
			@NotNull final Class<T> type) {
		T t = (T) annotations.get(type);
		return t;
	}
	
	@Override
	public void setAllAnnotation(@NotNull final AnnotationHandler handler) {
		if(handler == null) {
			return;
		}
		Set<Annotation> annotations = handler.annotations();
		if(CollectionObjects.assertNotEmpty(annotations)) {
			installAnnotation(
					ArrayCollect.get(Annotation.class).toArray(annotations)
					);
		}
	}
	
	@Override
	public boolean isAnnotationPresent(
			@NotNull final Class<? extends Annotation> type) {
		Asserts.assertNotNull(type, "'type' cannot be null.");
		
		boolean b = this.annotations.containsKey(type);
		
		return b;
	}
}
