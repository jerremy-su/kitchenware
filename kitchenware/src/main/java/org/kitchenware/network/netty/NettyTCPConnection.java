package org.kitchenware.network.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import org.kitchenware.express.annotation.NotNull;
import org.kitchenware.express.function.access.UserAccessor;
import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.express.util.StringObjects;
import org.kitchenware.network.dns.HostNamingFactory;
import org.kitchenware.network.netty.event.NettyTCPActionEvent;
import org.kitchenware.network.netty.event.NettyTCPActionListener;
import org.kitchenware.network.proxy.ProxyConfiguration;
import org.kitchenware.network.proxy.ProxyFunctionProtocol;
import org.kitchenware.network.ssl.SSLTrustStatement;
import org.kitchenware.network.tcp.SocketStrategyLayoutPolicy;
import org.kitchenware.network.tcp.TCPChannelOption;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandler;

/**
 * CMBG-22617 jerremy.su 2021-11-29 15:56:34 分离连接池,区分出默认连接池channels,强制不进行alpn握手连接池notAlpnChannels
 * @author jerremy.su
 *
 */
public class NettyTCPConnection {
	
	static {
		
		System.setProperty("java.net.useSystemProxies", "true");
		
		io.netty.util.internal.logging.InternalLoggerFactory.setDefaultFactory(
				io.netty.util.internal.logging.JdkLoggerFactory.INSTANCE
				);
	}
	
	Set<NettyTCPActionListener> nettyTCPActionListeners = Collections.synchronizedSet(new LinkedHashSet<>());
	
	static final boolean debug = BoolObjects.valueOf(System.getProperty("debug"));
	static final Logger logger = Logger.getLogger(NettyTCPConnection.class.getName());

	/**
	 * CMBG-22617 jerremy.su 2021-11-29 15:57:23 默认连接池
	 * 
	 */
	final Deque<NettyTCPChannelStatement> channels = new LinkedBlockingDeque<NettyTCPChannelStatement>();
	
	/**
	 * CMBG-22617 jerremy.su 2021-11-29 15:57:35 TLS/SSL状态下,强制不进行ALPN握手的连接池
	 */
	final Deque<NettyTCPChannelStatement> notAlpnChannels = new LinkedBlockingDeque<NettyTCPChannelStatement>();
	
	final Map<String, NettyTCPChannelStatement> statmentContext = new ConcurrentHashMap<>();
	
	final Map<String, Object> attachmentContext = new ConcurrentHashMap<String, Object>();
	
	boolean ssl;

	NettyProtocolHandler protocolHandler;

	InetSocketAddress socketAddress;

	NettyTCPConenctionFactory conenctionFactory;
	ProxyConfiguration defaultProxy;
	NettyTCPConnection(NettyTCPConenctionFactory conenctionFactory, InetSocketAddress socketAddress, ProxyConfiguration defaultProxy) {
		this.socketAddress = socketAddress;
		this.conenctionFactory = conenctionFactory;
		this.defaultProxy = defaultProxy;
	}

//	SslContext getSslContext() throws SSLException {
//		return sslContext == null
//				? sslContext = SslContextBuilder.forClient().startTls(true)
//						.trustManager(InsecureTrustManagerFactory.INSTANCE).build()
//				: sslContext;
//	}

	public ProxyConfiguration getDefaultProxy() {
		return defaultProxy;
	}
	
	public void addNettyTCPActionListener(NettyTCPActionListener l) {
		nettyTCPActionListeners.add(l);
	}
	
	public void removeNettyTCPActionListener(NettyTCPActionListener l) {
		nettyTCPActionListeners.add(l);
	}
	
	public boolean containsListener(NettyTCPActionListener l) {
		return nettyTCPActionListeners.contains(l);
	}
	
	public NettyTCPActionListener [] getNettyTCPActionListeners() {
		return nettyTCPActionListeners.toArray(new NettyTCPActionListener [0]);
	}
	
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}
	
	void handleNettyTCPActionEvent(NettyTCPActionEvent evt) {
		if (nettyTCPActionListeners.isEmpty()) {
			return;
		}
		conenctionFactory.mainExecutor
		.execute(()->{
			Arrays.stream(getNettyTCPActionListeners())
			.forEach(l -> UserAccessor.functionProcess(()->{
				l.handle(evt);
			}));
		});
	}
	
	public Map<String, NettyTCPChannelStatement> statmentEntries(){
		return new LinkedHashMap<>(statmentContext);
	}
	
	public boolean isSsl() {
		return ssl;
	}

	void setSsl(boolean ssl) {
		this.ssl = ssl;
	}
	
	public ProxyHandler proxyHandler(TCPChannelOption tcpOption) throws Exception {
		ProxyConfiguration proxy = null;
		
		proxy = tcpOption.getProxy();
		if(proxy == null || !proxy.isRemoteProxy()) {
			proxy = this.defaultProxy;
		}
		
		if(proxy != null && !proxy.isRemoteProxy()) {
			proxy = null;
		}
		
		if(proxy == null) {
			String url = SocketStrategyLayoutPolicy.getPolicy().lookupProxyURL(this.socketAddress);
			if(StringObjects.assertNotEmptyAfterTrim(url)) {
				proxy = ProxyConfiguration.build(url);
			}
		}
		
		if(proxy != null && !proxy.isRemoteProxy()) {
			return null;
		}
		InetSocketAddress address = HostNamingFactory.owner().socketAddress(proxy.getHost(), proxy.getPort());
		
		if(ProxyFunctionProtocol.HTTP.equals(proxy.getProtocol())) {
			return new HttpProxyHandler(address);
		}else if (ProxyFunctionProtocol.SOCKS4.equals(proxy.getProtocol())) {
			return new Socks4ProxyHandler(address);
		}else if (ProxyFunctionProtocol.SOCKS5.equals(proxy.getProtocol())) {
			return new Socks5ProxyHandler(address);
		}
		
		return null;
	}
	
	/**创建/获取连接池状态
	 * @param ssl
	 * @param option
	 * @return
	 * @throws Throwable
	 */
	public NettyTCPChannelStatement openTCPChannelStatement(boolean ssl, TCPChannelOption option) throws Exception {
		NettyTCPChannelStatement statement = 
				option.isAlpnInTls() ?
						readCacheStatement(this.channels) //支持alpn握手,使用默认连接池
						: readCacheStatement(this.notAlpnChannels);//不支持alpn握手,使用扩展连接池
				
		if (statement == null) {
			
			InetSocketAddress endpointAddress = SocketStrategyLayoutPolicy.getPolicy().lookupAddress(this.socketAddress);
			if(endpointAddress == null) {
				endpointAddress = this.socketAddress;
			}
			ProxyHandler proxyHandler = proxyHandler(option);
			
			EventLoopGroup eventGroup 
			= conenctionFactory.eventLoopGroup(endpointAddress);
			Bootstrap bootstrap = new Bootstrap();
			statement = new NettyTCPChannelStatement(option, eventGroup, NettyTCPConnection.this);
					
			bootstrap.group(eventGroup);
			bootstrap.channel(NioSocketChannel.class);
			//custom
			bootstrap.option(ChannelOption.SO_KEEPALIVE, option.isKeepAlive());
			bootstrap.option(ChannelOption.TCP_NODELAY, option.isTcpNoDelay());
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, option.getConnectionTimeout());
//			bootstrap.option(ChannelOption.SO_TIMEOUT, option.getSoTimeout());//CMBG-23436 netty 已经不支持这个参数
			bootstrap.option(ChannelOption.SO_REUSEADDR, option.isSo_reuseaddr());
//			bootstrap.option(ChannelOption.SO_LINGER, Integer.valueOf(0));//CMBG-33009 jerremy.su 2022-08-31 10:39:26
			
			//system
//			bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			
			bootstrap.remoteAddress(endpointAddress);
			bootstrap.handler(new NettyTCPConnectionInitializer(statement, proxyHandler, option));
			try {
				ChannelFuture future = bootstrap.connect(endpointAddress).syncUninterruptibly();
				Channel channel = future.channel();
				statement.setChannelFuture(future);
				statement.setChannel(channel);
				
				if(ssl) {
					ChannelPromise promise = channel.newPromise();
					statement.setPromise(promise);
					statement.promiseAwaitSettings(option.getConnectionTimeout(), TimeUnit.MILLISECONDS);
				}
				
				statmentContext.put(statement.getChannel().id().asLongText(), statement);
				handleNettyTCPActionEvent(new NettyTCPActionEvent(
						NettyTCPActionEvent.CONNECT
						, statement
						, this
						, conenctionFactory
						, null
						));
			} catch (Throwable e) {
				eventGroup.shutdownGracefully();
				throw new IOException(e.getMessage(), e);
			}
		}
		return statement;
	}

	

	public NettyProtocolHandler getProtocolHandler() {
		return protocolHandler;
	}

	public void setProtocolHandler(NettyProtocolHandler protocolHandler) {
		this.protocolHandler = protocolHandler;
	}

	public void attachmentRegistry(Channel channel, Object attachment) {
		attachmentContext.put(channel.id().asLongText(), attachment);
	}

//	public void attachmentDestroy(Channel channel) {
//		attachmentContext.remove(channel.id().asLongText());
//	}
	
	public Map<String, Object> attachmentEntries(){
		return new LinkedHashMap<>(attachmentContext);
	}
	
	public <T> T getAttachment(Channel channel, Class<T> type) {
		Object attachment = attachmentContext.get(channel.id().asLongText());
		if (attachment == null || !type.isInstance(attachment)) {
			return null;
		}
		return (T) attachment;
	}

	public boolean idle(Channel channel, @NotNull NettyTCPChannelStatement statement, boolean forceClose) {
		String channelID = channel.id().asLongText();
		attachmentContext.remove(channelID);
		
		boolean result = true;
		if (forceClose || !statement.valid()) {
			statement.shutdown(true);
			result = false;
		} else {
			result = reuseChannel(statement);
		}
		
		return result;
	}

	NettyTCPChannelStatement readCacheStatement(Deque<NettyTCPChannelStatement> channels) {
		NettyTCPChannelStatement statement;
		while (channels.size() > 0) {
			statement = channels.pollLast();
			if (statement == null) {
				continue;
			}
			if (statement.isShutdown()) {
				continue;
			}
			
			if (!statement.valid()) {
				statement.shutdown(true);
				continue;
			} 
			return statement;
		}
		return null;
	}

	private boolean reuseChannel(NettyTCPChannelStatement statement) {
		if (!statement.valid()) {
			statement.shutdown(true);
			return false;
		} 
		
		{
			//CMBG-23208 jerremy.su 2021-12-09 16:26:02
			Deque<NettyTCPChannelStatement> pool;
			if(statement.getOption().isAlpnInTls()) {
				pool = this.channels;
			}else {
				pool = this.notAlpnChannels;
			}
			statement.resetActive();//CMBG-24122 jerremy.su 2022-01-06 15:14:37 重设活动时间
			pool.addFirst(statement);
		}
		
		return true;
	}

//	boolean channelDisable(NettyTCPChannelStatement statement) {
//		return statement.getChannelFuture().isCancelled()
//				|| !statement.getChannelFuture().isSuccess()
//				|| !statement.getChannelFuture().channel().isActive()
//				|| !statement.valid() //CMBG-24122 jerremy.su 2022-01-06 15:19:00
//				;
//	}
	
	class NettyTCPConnectionInitializer extends ChannelInitializer<Channel> {

		final NettyTCPChannelStatement statement;
		final ProxyHandler proxyHandler;
		final TCPChannelOption option;
		NettyTCPConnectionInitializer(NettyTCPChannelStatement statement, ProxyHandler proxyHandler, TCPChannelOption option) {
			this.statement = statement;
			this.proxyHandler = proxyHandler;
			this.option = option;
		}
		
		@Override
		protected void initChannel(Channel channel) throws Exception {
			if(proxyHandler != null) {
				channel.pipeline().addLast(proxyHandler);
			}
			
			if (getProtocolHandler() == null) {
				channel.close().cancel(true);
				throw new NullPointerException("Cannot initial connection channel : protocol handler not bound.");
			}
			
			if (ssl) {
				String host = socketAddress.getHostString();
				int port = socketAddress.getPort();
				//CMBG-23436 jerremy.su 2021-12-20 13:16:21
				SSLTrustStatement trustStatement = new SSLTrustStatement();
				this.statement.setSslTrustStatement(trustStatement);
				SSLEngine engine = conenctionFactory
						.buildPeerSSLEngine(option, channel, host, port, trustStatement);
//				if(this.option.isAlpnInTls()) {
//					engine = conenctionFactory.getStandarSslContext().newEngine(channel.alloc(), host, port);
//				}else {
//					engine = conenctionFactory.getNotAlpnSupportSSLContext().newEngine(channel.alloc(), host, port);
//				}
				SslHandler sh = new SslHandler(engine);
				channel.pipeline().addLast(sh);
				javax.net.ssl.SSLSession sslSession =  engine.getSession();
				this.statement.setSslSession(sslSession);
				channel.pipeline().addLast(new AlpnHandler(channel, this.statement));
			}else {
				getProtocolHandler().installChannel(this.statement, channel, channel.pipeline());
				channel.pipeline().addLast(new NettyTCPConnectionHandler(statement));
				
			}
			
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			ctx.close();
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
			
			IOException error = new IOException(cause.getMessage(), cause);
			ctx.fireExceptionCaught(error);
		}
	}

	class AlpnHandler extends ApplicationProtocolNegotiationHandler{
		Channel channel;
		NettyTCPChannelStatement statement;
		protected AlpnHandler(Channel channel, NettyTCPChannelStatement statement) {
			super("");
			this.statement = statement;
			this.channel = channel;
		}

		@Override
		protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
			statement.setApplicationProtocol(protocol);
			Channel channel = ctx.channel();
			
			ChannelPipeline pipeline = ctx.pipeline();
			getProtocolHandler().installChannel(this.statement, channel, pipeline);
			pipeline.addLast(new NettyTCPConnectionHandler(statement));
		}
		
	}
	
	class NettyTCPConnectionHandler extends ChannelInboundHandlerAdapter {

		final NettyTCPChannelStatement statement;
		NettyTCPConnectionHandler(NettyTCPChannelStatement statement) {
			this.statement = statement;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			
			if (getProtocolHandler() == null) {
				ctx.close();
				ctx.channel().close().cancel(true);
				throw new NullPointerException("Cannot read data: protocol handler not bound.");
			}
			getProtocolHandler().channelRead(this, statement, ctx, msg);
			statement.responsePromiseSuccess();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			idle(ctx.channel(), statement, true);
			super.channelInactive(ctx);
			ctx.close();
			try {
				getProtocolHandler().inactive(NettyTCPConnection.this, statement, ctx);
			} catch (Throwable e) {
				if(debug) {
					logger.logp(Level.WARNING, getClass().getName(), "channelInactive::resolve-protocol-handler", e.getMessage(), e);
				}
			}
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			try {
				getProtocolHandler().active(NettyTCPConnection.this, statement,  ctx);
			} catch (Exception e) {
				if(debug) {
					logger.logp(Level.WARNING, getClass().getName(), "channelActive::resolve-protocol-handler", e.getMessage(), e);
				}
			}
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			idle(ctx.channel(), statement, true);
			ctx.close();
			statement.responsePromiseFailed(cause);
			if(debug) {
				logger.logp(Level.WARNING, getClass().getName(), "exceptionCaught",
						String.format("[%s] an connection exception caught : %s", cause.getMessage()));
			}
			try {
				getProtocolHandler().exceptionCaught(NettyTCPConnection.this, statement, ctx, cause);
			} catch (Throwable e) {
				if(debug) {
					logger.logp(Level.WARNING, getClass().getName(), "exceptionCaught::resolve-protocol-handler", e.getMessage(), e);
				}
			}
		}
	}
}
