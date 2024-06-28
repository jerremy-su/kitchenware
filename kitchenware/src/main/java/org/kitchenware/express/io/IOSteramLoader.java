package org.kitchenware.express.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author jerremy
 * 
 */
public class IOSteramLoader {
	public static int basicBufferUtilLen = 4096;

	public static void load(InputStream in, OutputStream out)
			throws IOException{
		load(in, out, false);
	}
	
	public static void load(InputStream in, OutputStream out, boolean autoFlush)
			throws IOException {
		byte[] buff = new byte[basicBufferUtilLen];
		for (int len = 0; len != -1; len = in.read(buff)) {
			out.write(buff, 0, len);
			if(autoFlush) {
				out.flush();
			}
		}
	}

	public static byte[] load(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		load(in, out);
		out.flush();
		out.close();
		return out.toByteArray();
	}

	public static int getBasicBufferUtilLen() {
		return basicBufferUtilLen;
	}

	public static void setBasicBufferUtilLen(int basicBufferUtilLen) {
		IOSteramLoader.basicBufferUtilLen = basicBufferUtilLen;
	}
}
