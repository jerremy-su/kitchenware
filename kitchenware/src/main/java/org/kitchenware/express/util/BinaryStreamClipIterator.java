package org.kitchenware.express.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**CMBG-17532 jerremy.su 2021-05-19 14:47:41
 * @author jerremy
 *
 */
public class BinaryStreamClipIterator {
	byte [] buff;
	
	public BinaryStreamClipIterator(){
		this(new byte [0]);
	}
	
	public BinaryStreamClipIterator(byte [] buff){
		this.buff = buff;
	}
	
	/**
	 * @param b
	 */
	public void addBuff(byte [] b){
		long len = buff.length + b.length;
		if (len > Integer.MAX_VALUE) {
			throw new ArrayIndexOutOfBoundsException("Out of byte array max length : " + len);
		}
		byte [] tmp = new byte [(int)len];
		System.arraycopy(buff, 0, tmp, 0, buff.length);
		System.arraycopy(b, 0, tmp, buff.length, b.length);
		buff = tmp;
	}
	
	public byte [] clip(int off, int nextLen){
		byte [] result = Arrays.copyOfRange(buff, 0, off);
		buff = Arrays.copyOfRange(buff, off + nextLen, buff.length);
		return result;
	}
	
	
	public int indexFor(byte [] b){
		return this.indexFor(0, b);
	}
	
	public byte [] clip(byte [] b){
		int clipPosition = indexFor(0, b);
		if (clipPosition != -1) {
			return clip(clipPosition, b.length);
		}else{
			return null;
		}
	}
	
	
	public byte [] clip(int i, byte [] b){
		int clipPosition = indexFor(i, b);
		if (clipPosition != -1) {
			return clip(clipPosition, b.length);
		}else{
			return null;
		}
	}
	
	public int indexFor(int i, byte [] b){
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
	
	/**锟斤拷取剩锟斤拷未锟矫硷拷锟斤拷锟斤拷锟斤拷
	 * @return
	 */
	public byte [] getByteArrays(){
		return Arrays.copyOf(buff, buff.length);
	}
	
	/**
	 * @return
	 */
	public int size(){
		return buff.length;
	}
	
	public byte [] popByteArrays(){
		byte [] result = Arrays.copyOf(buff, buff.length);
		buff = new byte[0];
		return result;
	}
	
	public String toString(String charset) throws UnsupportedEncodingException {
		return new String(buff, charset);
	}
	
	@Override
	public String toString() {
		return new String(buff);
	}
}
