package org.kitchenware.network.netty.http;

import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.netty.NettyProtocolHandler;
import org.kitchenware.network.netty.NettyTCPChannelStatement;
import org.kitchenware.network.netty.NettyTCPConnection;
import org.kitchenware.network.netty.SettingsHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;

public final class HttpProtocolHandler implements NettyProtocolHandler{
	static final boolean DEBUG = BoolObjects.valueOf(System.getProperty("debug"));
	static HttpProtocolHandler handler = new HttpProtocolHandler();
	
	static int MAX_H2_BUF_LEN = 10 * 1024 * 1024;
	
	static HttpProtocolHandler getHandler() {
		return handler;
	}
	private HttpProtocolHandler() {}
	
	@Override
	public void installChannel(NettyTCPChannelStatement statement, Channel channel, ChannelPipeline pipeline) throws Exception {
		if(StringObjects.assertEqualsIgnoreCase("h2", statement.getApplicationProtocol())) {
			if(statement.getSslSession() != null) {
				DefaultHttp2Connection connection = new DefaultHttp2Connection(false);
				
				HttpToHttp2ConnectionHandler h2connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
						.frameListener(new DelegatingDecompressorFrameListener(connection
								, new InboundHttp2ToHttpAdapterBuilder(connection)
								.maxContentLength(MAX_H2_BUF_LEN)
								.propagateSettings(true)
								.build()
								))
						.connection(connection)
						.build();
				pipeline.addLast(h2connectionHandler);
				pipeline.addLast(new SettingsHandler(statement.getPromise()));
				statement.setPromiseAccessible(true);
			}
		}else {
			pipeline.addLast("http-decoder", new HttpResponseDecoder());
			pipeline.addLast("http-encoder", new HttpRequestEncoder());
			if(statement.getPromise() != null) {
				statement.getPromise().setSuccess();
			}
			statement.setPromiseAccessible(false);
		}
	}
	
	@Override
	public void channelRead(ChannelInboundHandlerAdapter parent, NettyTCPChannelStatement statement, ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		if(StringObjects.assertEquals("h2", statement.getApplicationProtocol())) {
			_readHttp2(parent, statement, ctx, msg);
		}else {
			_readHttp1(statement, ctx, msg);
		}
	}
	

	private void _readHttp2(ChannelInboundHandlerAdapter parent, NettyTCPChannelStatement statement, ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(!HttpObject.class.isInstance(msg)) {
			ctx.fireChannelRead(msg);
			return;
		}
		
		_readHttp1(statement, ctx, msg);
	}
	
	private void _readHttp1(NettyTCPChannelStatement statement, ChannelHandlerContext ctx, Object msg)
			throws Exception {
		HttpSession session = statement.getConnection().getAttachment(ctx.channel(), HttpSession.class);
		if (session == null) {
			throw new IllegalAccessException("Http session not bound");
		}
		
		NettyHttpResponse response = session.getResponse();
		if (HttpResponse.class.isInstance(msg)) {
			HttpResponse x = (HttpResponse) msg;
			response.buffHeader(x.headers());
			response.buffStatus(x.status());
			response.buffVersion(x.protocolVersion());
			
			if(session.isDoAck()) {
				//CMBG-25303 jerremys.su 2022-02-14 10:35:28 获得http header的同时,释放ack 锁
				session.doAck();
			}
		}
		
		if (HttpContent.class.isInstance(msg)) {
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			response.readBuf(buf);
			if (io.netty.handler.codec.http.LastHttpContent.class.isInstance(content)) {
				if("close".equalsIgnoreCase(response.getHeaders().get(HttpHeaderNames.CONNECTION)) || !statement.release()) {
					ctx.close();
					if (statement != null) {
						statement.isShutdown();
					}
				}
				session.releaseLock();
			}
		}
	}
	
	@Override
	public void exceptionCaught(NettyTCPConnection connection, NettyTCPChannelStatement statement,  ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//-------------
		HttpSession session = connection.getAttachment(ctx.channel(), HttpSession.class);
		if (session == null) {
			return;
		}
		session.getResponse().buffCaughtError(cause);
		session.releaseLock();
	}
	
	@Override
	public void inactive(NettyTCPConnection connection, NettyTCPChannelStatement statement, ChannelHandlerContext ctx) throws Exception {
		//--------------
		HttpSession session = connection.getAttachment(ctx.channel(), HttpSession.class);
		if (session == null) {
			return;
		}
		session.releaseLock();
	}
	@Override
	public void active(NettyTCPConnection connection, NettyTCPChannelStatement statement, ChannelHandlerContext ctx) throws Exception {
	}
}
