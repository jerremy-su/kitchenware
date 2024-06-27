package org.kitchenware.network.netty.http;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.network.netty.NettyTCPChannelStatement;
import org.kitchenware.network.netty.http.async.NettyHttpAsyncCallback;
import org.kitchenware.network.tcp.TCPChannelOption;

public class DefaultNettyHttpSession {
	
	static final Logger LOGGER = Logger.getLogger(DefaultNettyHttpSession.class.getName());
	
	final CountDownLatch cdl = new CountDownLatch(1);
	final CountDownLatch ackLock = new CountDownLatch(1);
	
	final DefaultNettyHttpRequest request;
	final DefaultNettyHttpResopnse response;
	final TCPChannelOption option;
	final NettyTCPChannelStatement statement;
	boolean doAck;
	
	NettyHttpAsyncCallback asyncCallback;
	
	DefaultNettyHttpSession(
			boolean doAck
			, NettyTCPChannelStatement statement
			, TCPChannelOption option
			, DefaultNettyHttpRequest request
			, @NotNull DefaultNettyHttpResopnse response
			){
		this.doAck = doAck;
		this.statement = statement;
		this.option = option;
		this.request = request;
		this.response = response;
	}
	
	public CountDownLatch getCdl() {
		return cdl;
	}
	
	public DefaultNettyHttpResopnse getResponse() {
		return response;
	}
	
	public DefaultNettyHttpRequest getRequest() {
		return request;
	}
	
	public void releaseLock() {
		cdl.countDown();
		
		if(this.asyncCallback != null) {
			try {
				this.asyncCallback.handle(this);
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
	
	public void doAck() {
		this.ackLock.countDown();
	}
	
	public boolean isDoAck() {
		return doAck;
	}
	
	public NettyTCPChannelStatement getStatement() {
		return statement;
	}
	
	public void await() throws InterruptedException {
		if (statement.isShutdown()
				|| getStatement().getCaughtError() != null) {
			return;
		}
		cdl.await(option.getSoTimeout(), TimeUnit.MILLISECONDS);
	}
	
	public void awaitAck() throws InterruptedException {
		if (statement.isShutdown()
				|| getStatement().getCaughtError() != null) {
			return;
		}
		ackLock.await(option.getConnectionTimeout(), TimeUnit.MILLISECONDS);
	}
	
	public boolean isAckSuccessfully() {
		boolean result = this.ackLock.getCount() < 1;
		return result;
	}
	
	public DefaultNettyHttpSession asyncCallback(NettyHttpAsyncCallback handler) {
		this.asyncCallback = handler;
		return DefaultNettyHttpSession.this;
	}

}
