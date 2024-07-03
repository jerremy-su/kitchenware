package org.kitchenware.express.function.caseby;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.function.ConsumerAccepter;
import org.kitchenware.express.util.BoolObjects;

public class CaseBy {

	ConsumerAccepter<Case> accetper;
	Predicate<Case> tester;

	CaseBy(){}
	
	public CaseBy onThen(
			@NotNull Runnable event) {
		ConsumerAccepter<Case> accepter = c -> {
			event.run();
		};
		return this.onThen(accepter);
	}
	
	public CaseBy onThen(
			@NotNull ConsumerAccepter<Case> accetper) {
		this.accetper = accetper;
		return CaseBy.this;
	}
	
	public CaseBy when(Supplier<Boolean> event) {
		Predicate<Case> tester = c -> {
			Boolean b = event.get();
			return BoolObjects.valueOf(b);
		};
		return this.when(tester);
	}
	
	public CaseBy when(Predicate<Case> tester) {
		this.tester = tester;
		return CaseBy.this;
	}
	
	public static CaseBy by() {
		return new CaseBy();
	}
	
	boolean valid()	{
		return this.accetper != null && this.tester != null;
	}
	
}
