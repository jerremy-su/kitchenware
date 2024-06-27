package org.kitchenware.network.netty;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.kitchenware.express.concurrent.DefaultNamingThreadFactory;
import org.kitchenware.express.concurrent.ExecutorBootstrapFactory;
import org.kitchenware.express.debug.Debug;
import org.kitchenware.express.util.BoolObjects;
import org.kitchenware.network.proxy.ProxyConfiguration;
import org.kitchenware.network.ssl.SSLTrustStatement;
import org.kitchenware.network.tcp.TCPChannelOption;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.concurrent.DefaultThreadFactory;

public class NettyTCPConenctionFactory {
	
	static final Logger LOGGER = Logger.getLogger(NettyTCPConenctionFactory.class.getName());
	
	static final NettyTCPConenctionFactory owner = new NettyTCPConenctionFactory();
	public static NettyTCPConenctionFactory getOwner() {
		return owner;
	}
	
	final Map<InetSocketAddress, NettyTCPConnection> sslTcpContext = new ConcurrentHashMap<>();
	final Map<InetSocketAddress, NettyTCPConnection> defaultTcpContext = new ConcurrentHashMap<>();
	final Map<InetSocketAddress, ThreadFactory> groupThreadFactoryContext = new ConcurrentHashMap<>();
	final Map<InetSocketAddress, Executor> executorContext = new ConcurrentHashMap<>();
	
	
	final Map<String, Boolean> alpnSupportContext = new ConcurrentHashMap<>();
	
//	final ExecutorService eventThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1
//			, new DefaultNamingThreadFactory("[NettyTCPConenctionFactory]"));
	
//	final ExecutorService eventThreadPool = new ThreadBootstrap( 
//			new DefaultNamingThreadFactory("[NettyTCPConenctionFactory]")
//			, 4
//			, 128
//			, Runtime.getRuntime().availableProcessors() << 1
//			);
	
	Executor mainExecutor;
	
//	SslContext standarSslContext;
//	SslContext notAlpnSupportSSLContext;
	
	ApplicationProtocolConfig alpnConfig;
	
	boolean opensslEnable;
	private NettyTCPConenctionFactory() {
		initialization();
	}
	
	private void initialization() {
		if(BoolObjects.valueOf(System.getProperty("thread.native"))) {
			mainExecutor = Executors.newCachedThreadPool(new DefaultNamingThreadFactory("[NettyTCPConenctionFactory]"));
		}else {
			//CMBG-25263 jerrmey.su 2022-02-17 10:03:05
			mainExecutor = ExecutorBootstrapFactory.newCachedThreadPool(new DefaultNamingThreadFactory("[NettyTCPConenctionFactory]"));
		}
		
		
		alpnConfig = new ApplicationProtocolConfig(
				Protocol.NPN_AND_ALPN
				, SelectorFailureBehavior.NO_ADVERTISE
				, SelectedListenerFailureBehavior.ACCEPT
				, ApplicationProtocolNames.HTTP_1_1, ApplicationProtocolNames.HTTP_2
				);
		
		try {
			String arch = System.getProperty("os.arch");
			if (BoolObjects.valueOf(System.getProperty("use.openssl")) || (arch != null && arch.contains("64"))) {
				NettyOpenSSLNativeRegistry.loadTcNative();
				opensslEnable = true;
			}
		} catch (Throwable e) {
			String error = String.format("Could not regist native openssl mudule : %s", e.getMessage());
			if(Debug.isDebug()) {
				LOGGER.log(Level.WARNING, error, e);
			}else {
				LOGGER.log(Level.WARNING, error);
			}
		}
	}
	
	public NettyTCPConnection openConnection(boolean ssl, InetSocketAddress address, ProxyConfiguration defaultProxy) {
		return ssl ? openSSLConnection(address, defaultProxy) : openDefaultTCPConnection(address, defaultProxy);
	}
	
	NettyTCPConnection openDefaultTCPConnection(InetSocketAddress address, ProxyConfiguration defaultProxy) {
		synchronized (defaultTcpContext) {
			NettyTCPConnection conn = defaultTcpContext.get(address);
			if (conn == null) {
				defaultTcpContext.put(address, conn = new NettyTCPConnection(this, address, defaultProxy));
			}
			return conn;
		}
	}
	
	NettyTCPConnection openSSLConnection(InetSocketAddress address, ProxyConfiguration defaultProxy) {
		synchronized (sslTcpContext) {
			NettyTCPConnection conn = sslTcpContext.get(address);
			if (conn == null) {
				sslTcpContext.put(address, conn = new NettyTCPConnection(this, address, defaultProxy));
				conn.setSsl(true);
			}
		return conn;
		}
	}
	
	EventLoopGroup eventLoopGroup(InetSocketAddress address) {
		ThreadFactory threadFactory = groupThreadFactoryContext.get(address);
		if (threadFactory == null) {
			threadFactory = new DefaultThreadFactory(String.format("[NettyTCPConnection]@%s ", address));
			groupThreadFactoryContext.put(address, threadFactory);
		}
		
		Executor executor = this.executorContext.get(address);
		if(executor == null) {
			if(BoolObjects.valueOf(System.getProperty("thread.native"))) {
				executor = Executors.newCachedThreadPool(threadFactory);
			}else {
				//CMBG-25263 jerrmey.su 2022-02-17 10:03:05
				executor = ExecutorBootstrapFactory.newCachedThreadPool(threadFactory);
			}
			this.executorContext.put(address, executor);
		}
		
		return new NioEventLoopGroup(
				1,
				executor
				);
	}
	
	public Map<InetSocketAddress, NettyTCPConnection> connectionEntrys(){
		return new LinkedHashMap<>(defaultTcpContext);
	}
	
	public Map<InetSocketAddress, NettyTCPConnection> sslConnectionEntrys(){
		return new LinkedHashMap<>(sslTcpContext);
	}

	SSLEngine buildPeerSSLEngine(TCPChannelOption option, Channel channel, String peerHost, int peerPort, SSLTrustStatement trustStatement) throws Exception {
		if(option.isAlpnInTls()) {
			return buildPeerStandarSslContext(trustStatement).newEngine(channel.alloc(), peerHost, peerPort);
		}else {
			return buildPeerNotAlpnSupportSSLContext(trustStatement).newEngine(channel.alloc(), peerHost, peerPort);
		}
	}
	
	SslContext buildPeerNotAlpnSupportSSLContext(SSLTrustStatement trustStatement) throws SSLException{
		SslContext sslContext = buildPeerSslContext(false, trustStatement);
		return sslContext;
	}

	SslContext buildPeerStandarSslContext(SSLTrustStatement trustStatement) throws SSLException{
		SslContext sslContext = buildPeerSslContext(true, trustStatement);
		return sslContext;
	}
	
	/**
	 * @param alpn
	 * @param trustStatement CMBG-23436 jerremy.su 每个链接都会创建自己证书管理
	 * @return
	 * @throws SSLException
	 */
	SslContext buildPeerSslContext(boolean alpn,  SSLTrustStatement trustStatement) throws SSLException {
		SslContextBuilder builder = SslContextBuilder.forClient();

		if (opensslEnable) {
			try {
				builder.sslProvider(SslProvider.OPENSSL);
				if(alpn) {
						builder
//						.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE	)
						.applicationProtocolConfig(this.alpnConfig)
						;
				}
			} catch (Throwable e) {
			}
		}
		
		builder.trustManager(trustStatement.getTrustManagerFactory());
		builder.startTls(true);
	
		return builder.build();
	}
}
