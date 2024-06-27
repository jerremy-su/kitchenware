package org.kitchenware.network.netty.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class DefaultNettyHttpRequest {

	final HttpHeaders headers = new DefaultHttpHeaders();
	final HttpMethod method;
	final HttpVersion protocolVersion;
	
	ByteArrayOutputStream unlimitBuff = new ByteArrayOutputStream();
	String path;


	public DefaultNettyHttpRequest(HttpVersion protocolVersion, HttpMethod method, String path) {
		this.protocolVersion = protocolVersion;
		this.method = method;
		this.path = path;
		validParam();
	}

	void validParam(){
		if (protocolVersion == null) {
			throw new RuntimeException("Illegal parameter : protocolVersion is null.");
		}
		if (method == null) {
			throw new RuntimeException("Illegal parameter : method is null.");
		}
		if (path == null) {
			throw new RuntimeException("Illegal parameter : path is null.");
		}
	}
	
	public DefaultNettyHttpRequest head(String key, String value) {
		headers.set(key, value);
		return DefaultNettyHttpRequest.this;
	}

	public DefaultNettyHttpRequest content(byte[] b) throws IOException {
		unlimitBuff = new ByteArrayOutputStream();
		unlimitBuff.write(b);
		unlimitBuff.flush();
		return DefaultNettyHttpRequest.this;
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}

	public ByteBuf getHeapBuff() {
		return Unpooled.copiedBuffer(readUnlimitBuff());
	}

	byte[] readUnlimitBuff() {
		byte[] buf = unlimitBuff.toByteArray();
		return buf;
	}

	public byte [] loadContent() {
		return this.unlimitBuff.toByteArray();
	}
	
	public int getContentLength() {
		return unlimitBuff.size();
	}

	public String getPath() {
		return path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public HttpVersion getProtocolVersion() {
		return protocolVersion;
	}
	
}
