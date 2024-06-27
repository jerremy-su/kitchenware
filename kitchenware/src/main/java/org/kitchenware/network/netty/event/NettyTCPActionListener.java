package org.kitchenware.network.netty.event;

import java.io.IOException;

public interface NettyTCPActionListener {
	void handle(NettyTCPActionEvent evt) throws IOException;
}
