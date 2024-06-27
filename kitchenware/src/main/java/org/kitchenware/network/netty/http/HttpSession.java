package org.kitchenware.network.netty.http;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.network.netty.NettyTCPChannelStatement;
import org.kitchenware.network.netty.http.async.HttpAsyncCallback;
import org.kitchenware.network.tcp.TCPChannelOption;

public class HttpSession {
	
	static final Logger LOGGER = Logger.getLogger(HttpSession.class.getName());
	
	final CountDownLatch cdl = new CountDownLatch(1);
	final CountDownLatch ackLock = new CountDownLatch(1);
	
	final HttpRequest request;
	final HttpResopnse response;
	final TCPChannelOption option;
	final NettyTCPChannelStatement statement;
	boolean doAck;
	
	HttpAsyncCallback asyncCallback;
	
	HttpSession(
			boolean doAck
			, NettyTCPChannelStatement statement
			, TCPChannelOption option
			, HttpRequest request
			, @NotNull HttpResopnse response
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
	
	public HttpResopnse getResponse() {
		return response;
	}
	
	public HttpRequest getRequest() {
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
	
	public HttpSession asyncCallback(HttpAsyncCallback handler) {
		this.asyncCallback = handler;
		return HttpSession.this;
	}

}
