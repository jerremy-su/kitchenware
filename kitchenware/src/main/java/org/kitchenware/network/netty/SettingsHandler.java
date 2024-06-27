package org.kitchenware.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

public class SettingsHandler extends SimpleChannelInboundHandler<Object>{

	private final ChannelPromise promise;
	public SettingsHandler(ChannelPromise promise) {
		this.promise = promise;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		promise.setSuccess();
		
		ctx.pipeline().remove(this);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		promise.setSuccess();
		
		ctx.pipeline().remove(this);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		promise.setFailure(cause);
		
		ctx.pipeline().remove(this);
	}
	
}
