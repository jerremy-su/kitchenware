package org.kitchenware.network.netty.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.dns.HostNamingFactory;
import org.kitchenware.network.errors.BadResponseException;
import org.kitchenware.network.errors.ConnectionResetException;
import org.kitchenware.network.netty.NettyTCPChannelStatement;
import org.kitchenware.network.netty.NettyTCPConenctionFactory;
import org.kitchenware.network.netty.NettyTCPConnection;
import org.kitchenware.network.netty.http.async.HttpAsyncCallback;
import org.kitchenware.network.proxy.ProxyAgent;
import org.kitchenware.network.proxy.ProxyConfiguration;
import org.kitchenware.network.tcp.TCPChannelOption;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.HttpConversionUtil;

public final class HttpNetty {
	
	static final boolean DEBUG = BoolObjects.valueOf(System.getProperty("debug"));
	static final Logger LOGGER = Logger.getLogger(HttpNetty.class.getName());
	
	static HttpHeaders emptyHerders = new DefaultHttpHeaders();
	static TCPChannelOption defaultTCPOption = new TCPChannelOption();
	
	static final int DEFAULT_PORT = 80;
	static final int DEFAULT_SSL_PORT= 443;
	
	final NettyTCPConnection connection;
	final NettyHttpRequest request;
	final boolean ssl;
	String hostReference;
	HttpProtocol protocol;
	boolean doAck;
	/**
	 * @param socketAddress
	 * @param ssl
	 * @param path
	 * @param method
	 */
	@Deprecated
	public HttpNetty(InetSocketAddress socketAddress, boolean ssl, String path, HttpMethod method) {
		
		String protocol = ssl ? "https" : "http";
		
		this.connection = NettyTCPConenctionFactory.getOwner().openConnection(ssl, socketAddress, null);
		if (!HttpProtocolHandler.class.isInstance(connection.getProtocolHandler())) {
			connection.setProtocolHandler(HttpProtocolHandler.getHandler());
		}
		this.request = new NettyHttpRequest(HttpVersion.HTTP_1_1, method, path);
		this.ssl = ssl;
		
		String host = socketAddress.getHostString();
		int port = socketAddress.getPort();
		if (port <= 0) {
			port = this.ssl ? DEFAULT_SSL_PORT : DEFAULT_PORT;
		}
		this.hostReference = host + ":" + port;
		
		request.head(HttpHeaderNames.HOST.toString(), hostReference);
		request.head(HttpHeaderNames.CONNECTION.toString(), HttpHeaderNames.KEEP_ALIVE.toString());
		request.head(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(), protocol);
	}
	
	public HttpNetty(URI uri, HttpMethod method) {
		String protocol = uri.getScheme();
		if (!"https".equalsIgnoreCase(protocol) && !"http".equalsIgnoreCase(protocol)) {
			throw new RuntimeException(String.format("Illegal protocol : %s", protocol));
		}
		
		this.ssl = "https".equalsIgnoreCase(protocol);
		
		String host = uri.getHost();
		if (StringObjects.isEmpty(host)) {
			throw new RuntimeException(String.format("Illegal host : %s", host));
		}
		
		int originalPort = uri.getPort();
		int port = originalPort;
		if (port <= 0) {
			port = this.ssl ? DEFAULT_SSL_PORT : DEFAULT_PORT;
		}
		
		
		this.hostReference = host;
		if(originalPort > -1) {
			this.hostReference += ":" + originalPort;
		}
		this.connection = NettyTCPConenctionFactory.getOwner().openConnection(ssl, HostNamingFactory.owner().socketAddress(host, port), ProxyAgent.select(uri));
		if (!HttpProtocolHandler.class.isInstance(connection.getProtocolHandler())) {
			connection.setProtocolHandler(HttpProtocolHandler.getHandler());
		}
		
		String path = uri.getRawPath();
		if (StringObjects.isEmpty(path)) {
			path = "/";
		}
		
		StringBuilder pathBuf = new StringBuilder();
		pathBuf.append(path);
		if (uri.getRawQuery() != null) {
			pathBuf.append("?").append(uri.getRawQuery());
		}
		
		this.request = new NettyHttpRequest(HttpVersion.HTTP_1_1, method, pathBuf.toString());
		
		request.head(HttpHeaderNames.HOST.toString(), hostReference);
		request.head(HttpHeaderNames.CONNECTION.toString(), HttpHeaderNames.KEEP_ALIVE.toString());
		request.head(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(), protocol);
	}
	
	public HttpNetty doAck(boolean ack) {
		this.doAck = ack;
		return HttpNetty.this;
	}
	
	public boolean isDoAck() {
		return doAck;
	}
	
	public HttpNetty protocol(HttpProtocol protocol) {
		this.protocol = protocol;
		return HttpNetty.this;
	}
	
	public HttpNetty head(String key, String value) {
		request.head(key, value);
		return HttpNetty.this;
	}
	
	public HttpNetty contentType(String type) {
		request.head(HttpHeaderNames.CONTENT_TYPE.toString(), type);
		return HttpNetty.this;
	}
	
	public HttpNetty contentLength(long len) {
		request.head(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(len));
		return HttpNetty.this;
	}
	
	public HttpNetty content(byte [] b) throws IOException {
		request.content(b);
		contentLength(b.length);
		return HttpNetty.this;
	}
	
	public byte [] loadContent() {
		return request.loadContent();
	}

	public NettyHttpResponse invokeIO() throws IOException{
		try {
			return invoke(defaultTCPOption);
		} catch (Throwable e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	public NettyHttpResponse invokeIO(TCPChannelOption option) throws IOException{
		try {
			return invoke(option);
		} catch (Throwable e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	public NettyHttpResponse invoke() throws Throwable{
		return invoke(defaultTCPOption);
	}
	
	
	public NettyHttpResponse invoke(TCPChannelOption option) throws Throwable{
		
		HttpSession session = connect(option);
		NettyTCPChannelStatement statement = session.getStatement();
		
		if(this.doAck) {
			//CMBG-25303 jerremy.su 2022-02-14 10:22:22 等待服务端回传http header
			try {
				session.awaitAck();
			} catch (Throwable e) {
				statement.disconnect(true);
				if(DEBUG) {
					LOGGER.log(Level.WARNING, e.getMessage() , e);
				}
			}
			
			if(!session.isAckSuccessfully()) {
				throw new ConnectionResetException(
						String.format("Connection reset; channel : %s", statement.getChannel())
						);
			}
		}
		
		long start  = System.currentTimeMillis();
		session.await();
		long end = System.currentTimeMillis() - start;
		
		if (session.getStatement().getCaughtError() != null) {
			statement.disconnect(true);
			throw session.getStatement().getCaughtError();
		}
		
		if (session.getResponse().getStatus() == null) {
			statement.disconnect(true);
			Throwable exception;
			if (end >= option.getSoTimeout()) {
				exception = new BadResponseException(String.format("[%s] response failure - read timeout : %s"
						, statement.getChannel()
						, option.getSoTimeout() + " ms"));
			}else {
				exception = new ConnectionResetException(
						String.format("Connection reset; channel : %s", statement.getChannel())
						);
			}
			throw exception;
		}
		
		return session.getResponse();
	}
	
	public HttpSession invokeAsyncIO(@NotNull HttpAsyncCallback callback) throws IOException{
		return this.invokeAsyncIO(defaultTCPOption, callback);
	}
	
	public HttpSession invokeAsyncIO(@NotNull TCPChannelOption option, @NotNull HttpAsyncCallback callback) throws IOException{
		try {
			return invokeAsync(option, callback);
		} catch (Throwable e) {
			throw new IOException(e.getMessage(), e);
		}
		
	}
	
	public HttpSession invokeAsync(@NotNull TCPChannelOption option, @NotNull HttpAsyncCallback callback) throws Throwable{
		if(callback == null) {
			throw new IOException("Callback Cannot be null.");
		}
		HttpSession session = connect(option, callback);
		return session;
	}
	
	
	HttpSession connect(TCPChannelOption option) throws Throwable{
		return this.connect(option, null);
	}
	
	HttpSession connect(TCPChannelOption option, HttpAsyncCallback asyncCallback) throws Throwable{
		option = option.cloneOption();
		
		ProxyConfiguration proxy = option.getProxy();
		if(proxy == null || !proxy.isRemoteProxy()) {
			proxy = this.connection.getDefaultProxy();
		}
		
		if(proxy != null && proxy.isRemoteProxy()) {
			request.head("netty-proxy-agent", StringObjects.valueOf(proxy));
		}
		
		if(this.doAck) {
			request.head("netty-ack", "true");
		}
		
		if(this.protocol != null
				&& !Objects.equals(HttpProtocol.H2, this.protocol)) {
			option.setAlpnInTls(false);
		}
		
		NettyTCPChannelStatement statement = connection.openTCPChannelStatement(ssl, option);
		HttpSession session = new HttpSession(
				this.doAck
				, statement
				, option
				, request
				, new NettyHttpResponse(statement)
				);
		
		if(asyncCallback != null) {
			session.asyncCallback(asyncCallback);
		}
		
		connection.attachmentRegistry(statement.getChannel(), session);
		
		ByteBuf content = request.getHeapBuff();
		
		DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(
				request.getProtocolVersion(), 
				request.getMethod(),
				request.getPath(),
				content,
				request.getHeaders(),
				emptyHerders
				);
		
		statement.channelWrite(option, httpRequest);
		
		if(this.doAck) {
			//CMBG-25303 jerremy.su 2022-02-14 10:22:22 等待服务端回传http header
			try {
				session.awaitAck();
			} catch (Throwable e) {
				statement.disconnect(true);
				if(DEBUG) {
					LOGGER.log(Level.WARNING, e.getMessage() , e);
				}
			}
			
			if(!session.isAckSuccessfully()) {
				throw new ConnectionResetException(
						String.format("Connection reset; channel : %s", statement.getChannel())
						);
			}
		}
		
		return session;
	}
	
	public NettyTCPConnection parentConnection() {
		return this.connection;
	}
	
	public NettyHttpRequest getRequestEntity() {
		return request;
	}
	
	public HttpHeaders getRequestHeaders() {
		return this.request.getHeaders();
	}
	
	public static HttpNetty doGet(URI uri) {
		return new HttpNetty(uri, HttpMethod.GET);
	}
	
	public static HttpNetty doOptions(URI uri) {
		return new HttpNetty(uri, HttpMethod.OPTIONS);
	}
	
	public static HttpNetty doHead(URI uri) {
		return new HttpNetty(uri, HttpMethod.HEAD);
	}
	
	public static HttpNetty doPost(URI uri) {
		return new HttpNetty(uri, HttpMethod.POST);
	}
	
	public static HttpNetty doPut(URI uri) {
		return new HttpNetty(uri, HttpMethod.PUT);
	}

	public static HttpNetty doPatch(URI uri) {
		return new HttpNetty(uri, HttpMethod.PATCH);
	}
	
	public static HttpNetty doDelete(URI uri) {
		return new HttpNetty(uri, HttpMethod.DELETE);
	}
	
	public static HttpNetty doTrace(URI uri) {
		return new HttpNetty(uri, HttpMethod.TRACE);
	}
	
	public static HttpNetty doConnect(URI uri) {
		return new HttpNetty(uri, HttpMethod.CONNECT);
	}
}
