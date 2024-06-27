package org.kitchenware.express.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.kitchenware.express.annotation.NotNull;

public class WildcardMultiMatchs implements Predicate<String>{

	final StringMatchs [] matchs;
	final boolean emptyExp;
	
	WildcardMultiMatchs(final StringMatchs [] matchs){
		this.matchs = matchs;
		this.emptyExp = ArrayObjects.isEmpty(matchs);
	}
	

	@Override
	public boolean test(final String text) {
		if(emptyExp) {
			return true;
		}
		if(StringObjects.isEmpty(text)) {
			return false;
		}
		
		String matchText = StringObjects.toLowerCase(text);
		boolean result = false;
		StringMatchs [] matchs = this.matchs;
		for(int i = 0; i < matchs.length; i ++) {
			StringMatchs matcher = matchs [i];
			if(matcher.matchs(matchText)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		final Set<String> expressions = new LinkedHashSet<>();
		
		Builder(){}
		
		public Builder addExpressions(
				@NotNull String exp) {
			if(StringObjects.isEmptyAfterTrim(exp)) {
				return Builder.this;
			}
			
			this.expressions.add(StringObjects.toLowerCase(exp));
			return Builder.this;
		}
		
		public WildcardMultiMatchs build() {
			String [] exps = ArrayCollect.STRING.toArray(this.expressions);
			List<StringMatchs> tmp = new ArrayList<>();
			for(int i = 0; i < exps.length; i ++) {
				String exp  = exps [i];
				tmp.add(new StringMatchs(exp));
			}
			WildcardMultiMatchs matcher = new WildcardMultiMatchs(ArrayCollect.get(StringMatchs.class).toArray(tmp));
			return matcher;
		}
	}

}
