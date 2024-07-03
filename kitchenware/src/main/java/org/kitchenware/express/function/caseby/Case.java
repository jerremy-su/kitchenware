package org.kitchenware.express.function.caseby;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.annotation.Optional;
import org.kitchenware.express.function.BOOL;
import org.kitchenware.express.function.ConsumerAccepter;
import org.kitchenware.express.util.ArrayCollect;

public interface Case {

	Case first(@NotNull final CaseBy in);
	
	Case next(@NotNull final CaseBy in);
	
	Case delete(@NotNull final CaseBy in);
	
	Case errorHandler(@Optional final Consumer<Throwable> h);
	
	Case doSwitch(@Optional final ConsumerAccepter<CaseBy []> after);
	
	Case doElseIf(@Optional final ConsumerAccepter<CaseBy> after);
	
	default Case doSwitch() {
		return doSwitch(null);
	}
	
	default Case doElseIf() {
		return doElseIf(null);
	}
	
	public static Case of() {
		return new CaseAccepter();
	}
	
	static class CaseAccepter implements Case, BOOL{
		
		static final Logger LOGGER = Logger.getLogger(CaseAccepter.class.getName());
		
		final Deque<CaseBy> cases = new LinkedList<>();
		transient Consumer<Throwable> errorHandler;
		
		@Override
		public Case first(@NotNull CaseBy in) {
			cases.addFirst(in);
			return CaseAccepter.this;
		}
		
		@Override
		public Case next(@NotNull CaseBy in) {
			cases.addLast(in);
			return CaseAccepter.this;
		}

		@Override
		public Case delete(@NotNull CaseBy in) {
			cases.remove(in);
			return CaseAccepter.this;
		}

		@Override
		public Case errorHandler(Consumer<Throwable> h) {
			this.errorHandler = h;
			return CaseAccepter.this;
		}
		
		private boolean validCase(CaseBy event) {
			boolean b = notNull(event)
					&& IS(event.valid())
					&& IS(event.tester.test(CaseAccepter.this));
			return b;
		}
		
		@Override
		public Case doSwitch(@Optional ConsumerAccepter<CaseBy []> resultHandler) {
			Throwable caughtError = null;
			List<CaseBy> resultSet = new ArrayList<>();
			try {
				for(; cases.size() > 0;) {
					CaseBy event = cases.pollFirst();
					if(NOT(validCase(event))) {
						continue;
					}
					event.accetper.accept(CaseAccepter.this);
					resultSet.add(event);
				}
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
				caughtError = e;
			}
			
			if(NULL(caughtError) && notNull(resultHandler)
					) {
				try {
					resultHandler.accept(ArrayCollect.get(CaseBy.class).toArray(resultSet));
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
					caughtError = e;
				}
			}
			
			if(notNull(caughtError) && notNull(this.errorHandler)) {
				this.errorHandler.accept(caughtError);
			}
			
			return CaseAccepter.this;
		}

		@Override
		public Case doElseIf(@Optional ConsumerAccepter<CaseBy> resultHandler) {
			Throwable caughtError = null;
			CaseBy result = null;
			try {
				for(; result == null && cases.size() > 0; ) {
					CaseBy event = cases.pollFirst();
					if(IS(validCase(event))) {
						event.accetper.accept(CaseAccepter.this);
						result = event;
						break;
					}
				}
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
				caughtError = e;
			}
			
			if(NULL(caughtError) && notNull(resultHandler)
					) {
				try {
					resultHandler.accept(result);
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
					caughtError = e;
				}
			}
			
			if(notNull(caughtError) && notNull(this.errorHandler)) {
				this.errorHandler.accept(caughtError);
			}
			
			return CaseAccepter.this;
		}
		
	}

}
