package org.kitchenware.network.netty;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import org.kitchenware.network.netty.event.NettyTCPActionEvent;
import org.kitchenware.network.ssl.SSLTrustStatement;
import org.kitchenware.network.tcp.TCPChannelOption;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;

public class NettyTCPChannelStatement {
	static final Logger LOGGER = Logger.getLogger(NettyTCPChannelStatement.class.getName());
	
	final EventLoopGroup eventLoopGroup;
	final NettyTCPConnection connection;
	
	ChannelFuture channelFuture;
	Channel channel;
	boolean shutdown;
	
	SSLSession sslSession;
	
	SSLTrustStatement sslTrustStatement;
	
	String applicationProtocol;
	ChannelPromise promise;
	
	boolean promiseAccessible;
	ChannelPromise responsePromise;
	/**
	 * CMBG-23208 jerremy.su 
	 */
	TCPChannelOption option;
	
	/**
	 * CMBG-24122 jerremy.su
	 * 每次回池都设置一次
	 */
	long activeTime;
	
	Throwable caughtError;
	
	NettyTCPChannelStatement(TCPChannelOption option, EventLoopGroup eventLoopGroup, NettyTCPConnection connection){
		this.option = option;
		this.eventLoopGroup = eventLoopGroup;
		this.connection = connection;
		resetActive();
	}
	
	public NettyTCPConnection getConnection() {
		return connection;
	}
	
	public ChannelFuture getChannelFuture() {
		return channelFuture;
	}
	
	void setChannelFuture(ChannelFuture channelFuture) {
		this.channelFuture = channelFuture;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public boolean release() {
		boolean b = connection.idle(channelFuture.channel(), this, false);
		if (isShutdown()) {
			return false;
		}
		return b;
	}
	
	public boolean isShutdown() {
//		return eventGroup.isShutdown();
		return shutdown;
	}
	
	Object shutdown(boolean performEvent) {
		if (shutdown) {
			return null;
		}
		shutdown = true;
		String channelID = channel.id().asLongText();
		Object attachment = connection.attachmentContext.get(channelID);
		try {
			this.channel.closeFuture();
			this.eventLoopGroup.shutdownGracefully();
//			channel.close().cancel(true);
//			channel.closeFuture().cancel(true);
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}finally {
			connection.attachmentContext.remove(channelID);
			connection.statmentContext.remove(channelID);
//			eventGroup.shutdownGracefully();
			if (performEvent) {
				connection.handleNettyTCPActionEvent(new NettyTCPActionEvent(
						NettyTCPActionEvent.SHUTDOWN
						, this
						, connection
						, this.connection.conenctionFactory
						, attachment
						)
						);
			}
		}
		return attachment;
	}
	
//	EventLoopGroup getEventGroup() {
//		return eventGroup;
//	}
	
	public TCPChannelOption getOption() {
		return option;
	}
	
	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public Object disconnect(boolean performEvent) {
		return shutdown(performEvent);
	}
	
	public SSLSession getSslSession() {
		return sslSession;
	}
	
	public void setSslSession(SSLSession sslSession) {
		this.sslSession = sslSession;
	}
	
	public SSLTrustStatement getSslTrustStatement() {
		return sslTrustStatement;
	}
	
	public void setSslTrustStatement(SSLTrustStatement sslTrustStatement) {
		this.sslTrustStatement = sslTrustStatement;
	}
	
	public String getApplicationProtocol() {
		return applicationProtocol;
	}
	
	public void setApplicationProtocol(String applicationProtocol) {
		this.applicationProtocol = applicationProtocol;
	}
	
	public ChannelPromise getPromise() {
		return promise;
	}
	
	public void setPromise(ChannelPromise promise) {
		this.promise = promise;
	}
	
	public void promiseAwaitSettings(long timeout, TimeUnit unit) throws IOException{
		if(this.promise == null) {
			throw new IOException("Channel promise not bound");
		}
		
		if(!promise.awaitUninterruptibly(timeout, unit)) {
			throw new IOException("Connection timeout");
		}
		
		if(!promise.isSuccess()) {
			Throwable error = promise.cause();
			if(error == null) {
				throw new IOException("Connection setting failed.");
			}
			throw new IOException(error.getMessage(), error);
			
		}
	}
	
	public void channelWrite(TCPChannelOption option, Object msg) throws IOException{
		
		if(!isPromiseAccessible()) {
			channel.writeAndFlush(msg);
			return;
		}
		
		
		//supported HTTP/2
		ChannelFuture writerFuture = this.channel.write(msg);
		ChannelPromise promise = this.channel.newPromise();
		
		this.channel.flush();
		
		if(!writerFuture.awaitUninterruptibly(option.getConnectionTimeout(), TimeUnit.MILLISECONDS)) {
			throw new IOException("Write data timeout.");
		}
		
		if(!writerFuture.isSuccess()) {
			Throwable error = writerFuture.cause();
			if(error == null) {
				throw new IOException("Write data failed.");
			}
			throw new IOException(error.getMessage(), error);
		}
		

		setResponsePromise(promise);
		
		if(!promise.awaitUninterruptibly(option.getSoTimeout(), TimeUnit.MILLISECONDS)) {
			throw new IOException("Read TCP Response time out");
		}
		
		if(!promise.isSuccess()) {
			Throwable error = promise.cause();
			if(error == null) {
				throw new IOException("Read TCP Response data failed.");
			}
			throw new IOException(error.getMessage(), error);
		
		}
	}
	
	public ChannelPromise getResponsePromise() {
		return responsePromise;
	}
	
	public void setResponsePromise(ChannelPromise responsePromise) {
		this.responsePromise = responsePromise;
	}
	
	public void responsePromiseSuccess() {
		if(this.responsePromise != null) {
			if(!responsePromise.isSuccess() && !responsePromise.isDone()) {
				this.responsePromise.setSuccess();
			}
		}
	}
	
	public Throwable getCaughtError() {
		return caughtError;
	}
	
	public void responsePromiseFailed(Throwable cause) {
		if(this.responsePromise != null) {
			this.responsePromise.setFailure(cause);
		}
		this.caughtError = cause;
	}
	
	public boolean isPromiseAccessible() {
		return promiseAccessible;
	}
	
	/**设置是否同步写入,使用netty channel.newPromise机制
	 * @param promiseAccessible
	 */
	public void setPromiseAccessible(boolean promiseAccessible) {
		this.promiseAccessible = promiseAccessible;
	}
	
	/**
	 * 重设
	 */
	public void resetActive() {
		this.activeTime = System.currentTimeMillis();
	}
	
	/**检查是否可用
	 * @return
	 */
	public boolean valid() {
		
		boolean b = !getChannelFuture().isCancelled()
				&& getChannelFuture().isSuccess()
				&& getChannelFuture().channel().isActive()
				&& (System.currentTimeMillis() - this.activeTime) < this.option.getSoTimeout();
		
		return b;
	}
}
