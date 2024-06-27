package org.kitchenware.network.netty.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.kitchenware.network.netty.NettyTCPChannelStatement;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class DefaultNettyHttpResopnse {
	Throwable caughtError;
	HttpHeaders headers;
	ByteArrayOutputStream unlimitBuff = new ByteArrayOutputStream();
	HttpResponseStatus status;
	HttpVersion version;
	String path;
	HttpMethod method;
	boolean end;
	
	NettyTCPChannelStatement statement;
	
	public DefaultNettyHttpResopnse(NettyTCPChannelStatement statement){
		this.statement = statement;
	}
	
	public void buffHeader(HttpHeaders buffHeaders){
		this.headers = buffHeaders;
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public HttpResponseStatus getStatus() {
		return status;
	}
	
	public int getStatusCode() {
		return getStatus().code();
	}
	
	public HttpVersion getVersion() {
		return version == null ? version = HttpVersion.HTTP_1_1 : version;
	}
	
	public String readStringBuff(String encode) throws IOException{
		return unlimitBuff.toString(encode);
	}
	
	public byte [] getContent() {
		return unlimitBuff.toByteArray();
	}
	
	public int getContentLength(){
		return unlimitBuff.size();
	}
	
	public Throwable getCaughtError() {
		return caughtError;
	}
	
	void readBuf(ByteBuf buf) throws IOException{
		ByteBuf targetBuf = Unpooled.buffer(buf.readableBytes());
		buf.readBytes(targetBuf, buf.readableBytes());
		unlimitBuff.write(targetBuf.array());

		buf.release();
		targetBuf.release();
	}
	
	void buffStatus(HttpResponseStatus status){
		this.status = status;
	}
	
	void buffVersion(HttpVersion version){
		this.version = version;
	}
	
	public void buffCaughtError(Throwable caughtError) {
		this.caughtError = caughtError;
	}
	
	public void disconnect() {
		statement.disconnect(true);
	}

	public String getCipherSuite() {
		return this.statement.getSslSession().getCipherSuite();
	}
	
	 public Certificate[] getLocalCertificates() {
		 return this.statement.getSslSession().getLocalCertificates();
	 }
	 
	 public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
		 return this.statement.getSslSession().getPeerCertificates();
	 }
	 
	 public javax.net.ssl.SSLSession getSslSession() {
		 return this.statement.getSslSession();
	 }
	 
	 public NettyTCPChannelStatement getStatement() {
		return statement;
	}
}
