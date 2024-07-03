package org.kitchenware.express.util;

import java.util.Arrays;

public class CharStreamClipIterator {

	char [] buff;
	
	public CharStreamClipIterator(){
		this(new char [0]);
	}
	
	public CharStreamClipIterator(char [] buff){
		this.buff = buff;
	}
	
	/**
	 * @param b
	 */
	public void addBuff(char [] b){
		long len = buff.length + b.length;
		if (len > Integer.MAX_VALUE) {
			throw new ArrayIndexOutOfBoundsException("Out of char array max length : " + len);
		}
		char [] tmp = new char [(int)len];
		System.arraycopy(buff, 0, tmp, 0, buff.length);
		System.arraycopy(b, 0, tmp, buff.length, b.length);
		buff = tmp;
	}
	
	public char [] clip(int off, int nextLen){
		char [] result = Arrays.copyOfRange(buff, 0, off);
		buff = Arrays.copyOfRange(buff, off + nextLen, buff.length);
		return result;
	}
	
	
	public int indexFor(char [] b){
		return this.indexFor(0, b);
	}
	
	public char [] clip(char [] b){
		int clipPosition = indexFor(0, b);
		if (clipPosition != -1) {
			return clip(clipPosition, b.length);
		}else{
			return null;
		}
	}
	
	
	public char [] clip(int i, char [] b){
		int clipPosition = indexFor(i, b);
		if (clipPosition != -1) {
			return clip(clipPosition, b.length);
		}else{
			return null;
		}
	}
	
	public int indexFor(int i, char [] b){
		int index;
		int len = buff.length;
		for (; i < len;) {
			index = 0;
			for (;index < b.length && i < len && buff [i ++] == b [index]; index++) {
				if(index == b.length -1){
					int clipPosition = i - b.length;
					return clipPosition;
				}
			}
			if (i < len && index > 0) {
				i --;
			}
		}
		return -1;
	}
	
	/**
	 * @return
	 */
	public char [] getCharArrays(){
		return Arrays.copyOf(buff, buff.length);
	}
	
	/**
	 * @return
	 */
	public int size(){
		return buff.length;
	}
	
	public char [] popcharArrays(){
		char [] result = Arrays.copyOf(buff, buff.length);
		buff = new char[0];
		return result;
	}
	
	@Override
	public String toString() {
		return new String(buff);
	}

}
