package org.kitchenware.spring.web.rpc;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.network.netty.http.HttpNetty;

public interface ServiceRPCConnection {

	void handleConnection(
			@NotNull final Connection connection);
	
	static Connection newConnection(
			@NotNull final HttpNetty client) {
		return new Connection(client);
	}
	
	static class Connection{
		final HttpNetty client;
		Connection(
			final HttpNetty client	
				) {
			this.client = client;
		}
		
		public HttpNetty getClient() {
			return client;
		}
	}
}
