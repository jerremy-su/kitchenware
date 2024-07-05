package org.kitchenware.express.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**Wildcard : *, ?
 * @author jerremy.su
 *
 */
public class StringMatchs {
	private final static int EQUALS = 0;
	private final static int START_OF = 1;
	private final static int END_OF = 2;
	
	final List<MatchCondition> conditionsTemp = new ArrayList<>();
	
	String conditionResource;
	
	public StringMatchs(String conditionResource) {
		this.conditionResource = conditionResource;
		installConditionResource();
	}
	
	private void installConditionResource() {
		if(!StringObjects.assertNotEmptyAfterTrim(conditionResource)) {
			return ;
		}
		
		char [] charArray = conditionResource.toCharArray();
		
		StringBuilder buf = new StringBuilder();
		
		int matchType = EQUALS;
		
		char c;
		for(int i = 0; i < charArray.length; i ++) {
			c = charArray [i];
			if(c == '\\' && i < (charArray.length - 1)) {
				char nextChar = charArray [i + 1];
				if(nextChar == '*') {
					buf.append('*');
					i ++;
					continue;
				}
				if(nextChar == '?') {
					buf.append('?');
					i ++;
					continue;
				}
			}
			
			if(c == '?') {
				if(buf.length() > 0) {
					conditionsTemp.add(new CharacterMatchsHandler(buf.toString(), nextMatchType(matchType)));
					buf = new StringBuilder();
				}
				conditionsTemp.add(new AnyCharacterMatchHandler());
				
				matchType = EQUALS;
				continue;
			}
			
			if(c == '*') {
				matchType = nextMatchType(matchType);
				if(buf.length() > 0) {
					conditionsTemp.add(new CharacterMatchsHandler(buf.toString(), matchType));
					buf = new StringBuilder();
				}
				continue;
			}
			
			buf.append(c);
		}
		
		if(buf.length() > 0) {
			conditionsTemp.add(new CharacterMatchsHandler(buf.toString()
					, conditionsTemp.size() > 0 || matchType > EQUALS
					? nextMatchType(matchType)
					: matchType		
					));
			buf = new StringBuilder();
		}
	}
	
	public Integer [] matchsIndexes(String string, boolean onlyFirst) {

		if(string == null) {
			return new Integer [] {0};
		}
		
		if(conditionsTemp.isEmpty()) {
			return new Integer [] {0};
		}
		
		ArrayBuilder<Integer> result = ArrayBuilder.newArrayEntry(Integer.class);

		Deque<MatchCondition> conditionQueue = new LinkedList<>();
		for(MatchCondition con :  conditionsTemp) {
			conditionQueue.addLast(con.mappedImage());
		}
		
		MatchCondition condition = conditionQueue.pollFirst();
		
		char [] charArray = string.toCharArray();
		char c;
		for(int i = 0; i < charArray.length; i ++) {
			c = charArray [i];
			condition.putChar(c);
			
			if(condition.next()) {
				result.add(i);
				
				if(onlyFirst) {
					return result.toArray();
				}
				if(conditionQueue.isEmpty()) {
					return result.toArray();
				}else {
					condition = conditionQueue.pollFirst();
				}
			}
		}
		
		return new Integer [0];
	
	}
	
	public boolean matchs(String string) {
		
		if(string == null) {
			return string == this.conditionResource;
		}
		
		if(conditionsTemp.isEmpty()) {
			return true;
		}

		Deque<MatchCondition> conditionQueue = new LinkedList<>();
		for(MatchCondition con :  conditionsTemp) {
			conditionQueue.addLast(con.mappedImage());
		}
		
		MatchCondition condition = conditionQueue.pollFirst();
		
		char [] charArray = string.toCharArray();
		char c;
		for(int i = 0; i < charArray.length; i ++) {
			c = charArray [i];
			condition.putChar(c);
			
			if(condition.next()) {
				if(conditionQueue.isEmpty()) {
					return true;
				}else {
					condition = conditionQueue.pollFirst();
				}
			}
		}
		
		return false;
	}
	
	private int nextMatchType(int matchType) {
		
		if(EQUALS == matchType) {
			return START_OF;
		}
		if(START_OF == matchType) {
			return END_OF;
		}
		
		return matchType;
	}
	
	interface MatchCondition{
		boolean next();
		
		void putChar(char c);
		
		int matchType();
		
		int conditionSize();
		
		MatchCondition mappedImage();
	}
	
	class AnyCharacterMatchHandler implements MatchCondition{

		@Override
		public boolean next() {
			return true;
		}

		@Override
		public void putChar(char c) {}
		
		public int matchType() {
			return -1;
		}

		@Override
		public int conditionSize() {
			return 1;
		}

		@Override
		public MatchCondition mappedImage() {
			return new AnyCharacterMatchHandler();
		}
	}
	
	class CharacterMatchsHandler implements MatchCondition{
		StringBuilder characterBuf = new StringBuilder();
		String matchCondition;
		
		final int matchType;
		
		CharacterMatchsHandler(String matchCondition, int matchType){
			this.matchCondition = matchCondition;
			this.matchType = matchType;
		}
		
		@Override
		public boolean next() {
			if(characterBuf.length() < matchCondition.length()) {
				return false;
			}
			
			switch (matchType) {
			case EQUALS:
				return characterBuf.toString().equals(matchCondition);
			case START_OF:
				return characterBuf.toString().startsWith(matchCondition);
			case END_OF:
				return characterBuf.toString().endsWith(matchCondition);
			default:
				return false;
			}
			
		}

		@Override
		public void putChar(char c) {
			characterBuf.append(c);
		}
		
		public int matchType() {
			return matchType;
		}

		@Override
		public int conditionSize() {
			return characterBuf.length();
		}

		@Override
		public MatchCondition mappedImage() {
			return new CharacterMatchsHandler(matchCondition, matchType);
		}
	}
	
	
	public static StringMatchs [] buildMatchs(String matchs) {
		if(StringObjects.isEmptyAfterTrim(matchs)) {
			return new StringMatchs [0];
		}
		ArrayBuilder<StringMatchs> result = ArrayBuilder.newArrayEntry(StringMatchs.class);
		for(String s : matchs.split(",")) {
			if(StringObjects.isEmptyAfterTrim(s)) {
				continue;
			}
			
			result.add(new StringMatchs(s));
		}
		
		return result.toArray();
	}
	
	public static boolean hasMatch(StringMatchs [] matchs, String pattern) {
		if(StringObjects.isEmptyAfterTrim(pattern)) {
			return false;
		}
		if(ArrayObjects.isEmpty(matchs)) {
			return true;
		}
		for(StringMatchs m : matchs) {
			if(m.matchs(pattern)) {
				return true;
			}
		}
		
		return false;
	}
	
}
