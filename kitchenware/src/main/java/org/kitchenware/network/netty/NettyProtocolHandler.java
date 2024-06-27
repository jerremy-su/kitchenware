package org.kitchenware.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

public interface NettyProtocolHandler {
	public void installChannel(NettyTCPChannelStatement statement, Channel channel, ChannelPipeline pipeline) throws Exception;
	
	public void channelRead(ChannelInboundHandlerAdapter parent, NettyTCPChannelStatement statement, ChannelHandlerContext ctx, Object msg) throws Exception;
	public void exceptionCaught(NettyTCPConnection connection, NettyTCPChannelStatement statement, ChannelHandlerContext ctx, Throwable cause) throws Exception;
	
	public void active(NettyTCPConnection connection, NettyTCPChannelStatement statement, ChannelHandlerContext ctx) throws Exception;
	public void inactive(NettyTCPConnection connection, NettyTCPChannelStatement statement, ChannelHandlerContext ctx) throws Exception;
}
