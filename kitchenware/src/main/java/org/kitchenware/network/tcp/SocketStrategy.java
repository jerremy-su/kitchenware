package org.kitchenware.network.tcp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.util.StringMatchs;
import org.kitchenware.express.util.StringObjects;

public class SocketStrategy {

	static final Logger LOGGER = Logger.getLogger(SocketStrategy.class.getName());
	
	String pattern;
	String regex;
	
	SocketStrategyContainer container;
	public SocketStrategy(String pattern, String regex, SocketStrategyContainer container){
		this.pattern = pattern;
		this.regex = regex;
		this.container = container;
	}
	
	public boolean matchs(String url) {
		if(StringObjects.isEmptyAfterTrim(url)) {
			return false;
		}
		
		if(StringObjects.assertNotEmptyAfterTrim(this.pattern)){
			String pattern = StringObjects.forceTrim(this.pattern);
			for(String s : pattern.toLowerCase().split(",")) {
				if(new StringMatchs(s).matchs(url.toLowerCase())) {
					return true;
				}
			}
		}
		
		try {
			if(StringObjects.assertNotEmptyAfterTrim(regex)
					&& url.toLowerCase().matches(regex.toLowerCase())
					) {
				return true;
			}
			
		} catch (Throwable e) {
			String err = String.format("Invalid regex: %s; Error: %s", regex, e.getMessage());
			LOGGER.log(Level.WARNING, err, e);
		}
		
		return false;
	}
	
	public SocketStrategyContainer getContainer() {
		return container;
	}
}
