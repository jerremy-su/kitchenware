package org.kitchenware.object.transport.rpc.flow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FlowBits {

	final byte [] BYTE = new byte [1];
	final byte [] BOOLEAN = new byte [1];
	final byte [] CHAR = new byte [2];
	final byte [] SHORT = new byte [2];
	final byte [] INT = new byte [4];
	final byte [] FLOAT = new byte [4];
	final byte [] LONG = new byte [8];
	final byte [] DOUBLE = new byte [8];
			
			
	byte getByte(InputStream in) throws IOException {
		in.read(BYTE);
		return BYTE[0];
	}
	
	boolean getBoolean(InputStream in) throws IOException {
		in.read(BOOLEAN);
		return BOOLEAN[0] != 0;
	}

	char getChar(InputStream in) throws IOException {
		in.read(CHAR);
		return (char) (((CHAR[1] & 0xFF) << 0) + ((CHAR[0]) << 8));
	}

	short getShort(InputStream in) throws IOException {
		in.read(SHORT);
		return (short) (((SHORT[1] & 0xFF) << 0) + ((SHORT[0]) << 8));
	}

	int getInt(InputStream in) throws IOException {
		in.read(INT);
		return ((INT[3] & 0xFF) << 0) + ((INT[2] & 0xFF) << 8)
				+ ((INT[1] & 0xFF) << 16) + ((INT[0]) << 24);
	}

	float getFloat(InputStream in) throws IOException {
		in.read(FLOAT);
		int i = ((FLOAT[3] & 0xFF) << 0) + ((FLOAT[2] & 0xFF) << 8)
				+ ((FLOAT[1] & 0xFF) << 16) + ((FLOAT[0]) << 24);
		return Float.intBitsToFloat(i);
	}

	long getLong(InputStream in) throws IOException {
		in.read(LONG);
		return ((LONG[7] & 0xFFL) << 0) + ((LONG[6] & 0xFFL) << 8)
				+ ((LONG[5] & 0xFFL) << 16) + ((LONG[4] & 0xFFL) << 24)
				+ ((LONG[3] & 0xFFL) << 32) + ((LONG[2] & 0xFFL) << 40)
				+ ((LONG[1] & 0xFFL) << 48) + (((long) LONG[0]) << 56);
	}

	double getDouble(InputStream in) throws IOException {
		in.read(DOUBLE);
		long j = ((DOUBLE[7] & 0xFFL) << 0) + ((DOUBLE[6] & 0xFFL) << 8)
				+ ((DOUBLE[5] & 0xFFL) << 16) + ((DOUBLE[4] & 0xFFL) << 24)
				+ ((DOUBLE[3] & 0xFFL) << 32) + ((DOUBLE[2] & 0xFFL) << 40)
				+ ((DOUBLE[1] & 0xFFL) << 48) + (((long) DOUBLE[0]) << 56);
		return Double.longBitsToDouble(j);
	}

	/*
	 * Methods for packing primitive values into byte arrays starting at given
	 * offsets.
	 */

	void putByte(OutputStream out, byte val) throws IOException{
		BYTE [0] = val;
		out.write(BYTE);
	}
	
	void putBoolean(OutputStream out, boolean val)
			throws IOException {
		BOOLEAN[0] = (byte) (val ? 1 : 0);
		out.write(BOOLEAN);
	}

	void putChar(OutputStream out, char val) throws IOException {
		CHAR[1] = (byte) (val >>> 0);
		CHAR[0] = (byte) (val >>> 8);
		out.write(CHAR);
	}

	void putShort(OutputStream out, short val)
			throws IOException {
		SHORT[1] = (byte) (val >>> 0);
		SHORT[0] = (byte) (val >>> 8);
		out.write(SHORT);
	}

	void putInt(OutputStream out, int val) throws IOException {
		INT[3] = (byte) (val >>> 0);
		INT[2] = (byte) (val >>> 8);
		INT[1] = (byte) (val >>> 16);
		INT[0] = (byte) (val >>> 24);
		out.write(INT);
	}

	void putFloat(OutputStream out, float val)
			throws IOException {
		int i = Float.floatToIntBits(val);
		FLOAT[3] = (byte) (i >>> 0);
		FLOAT[2] = (byte) (i >>> 8);
		FLOAT[1] = (byte) (i >>> 16);
		FLOAT[0] = (byte) (i >>> 24);
		out.write(FLOAT);
	}

	void putLong(OutputStream out, long val) throws IOException {
		LONG[7] = (byte) (val >>> 0);
		LONG[6] = (byte) (val >>> 8);
		LONG[5] = (byte) (val >>> 16);
		LONG[4] = (byte) (val >>> 24);
		LONG[3] = (byte) (val >>> 32);
		LONG[2] = (byte) (val >>> 40);
		LONG[1] = (byte) (val >>> 48);
		LONG[0] = (byte) (val >>> 56);
		out.write(LONG);
	}

	void putDouble(OutputStream out, double val)
			throws IOException {
		long j = Double.doubleToLongBits(val);
		DOUBLE[7] = (byte) (j >>> 0);
		DOUBLE[6] = (byte) (j >>> 8);
		DOUBLE[5] = (byte) (j >>> 16);
		DOUBLE[4] = (byte) (j >>> 24);
		DOUBLE[3] = (byte) (j >>> 32);
		DOUBLE[2] = (byte) (j >>> 40);
		DOUBLE[1] = (byte) (j >>> 48);
		DOUBLE[0] = (byte) (j >>> 56);
		out.write(DOUBLE);
	}
	
	void putByteArray(OutputStream out, byte [] b) throws IOException{
		out.write(b);
	}
	
	void putByteArray(OutputStream out, Byte [] b) throws IOException{
		byte [] buff = new byte [b.length];
		for(int i = 0; i < b.length; i ++){
			buff [i] = Byte.valueOf(b [i]);
		}
		out.write(buff);
	}
}
