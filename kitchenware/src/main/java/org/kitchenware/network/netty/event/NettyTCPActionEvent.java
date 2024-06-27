package org.kitchenware.network.netty.event;

import org.kitchenware.network.netty.NettyTCPChannelStatement;
import org.kitchenware.network.netty.NettyTCPConenctionFactory;
import org.kitchenware.network.netty.NettyTCPConnection;

public class NettyTCPActionEvent {
	public static int SHUTDOWN = 0;
	public static int CONNECT = 1;
	
	private int eventID;
	private NettyTCPChannelStatement statement;
	private NettyTCPConnection connection;
	private NettyTCPConenctionFactory conenctionFactory;
	private Object attachment;
	
	public NettyTCPActionEvent(
			int eventID
			, NettyTCPChannelStatement statement
			, NettyTCPConnection connection
			, NettyTCPConenctionFactory conenctionFactory
			, Object attachment
			) {
		this.eventID = eventID;
		this.statement = statement;
		this.connection = connection;
		this.conenctionFactory = conenctionFactory;
		this.attachment = attachment;
	}
	
	public int getEventID() {
		return eventID;
	}
	
	public NettyTCPChannelStatement getStatement() {
		return statement;
	}
	
	public NettyTCPConnection getConnection() {
		return connection;
	}
	
	public NettyTCPConenctionFactory getConenctionFactory() {
		return conenctionFactory;
	}
	
	public Object getAttachment() {
		return attachment;
	}
}
