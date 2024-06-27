package org.kitchenware.network.ssl;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLTrustStatement implements javax.net.ssl.X509TrustManager{

	X509Certificate[] peerCertificateChain;
	X509Certificate[] clientCertificateChain;
	
	final javax.net.ssl.TrustManager[] tmCerts;
	final TrustManagerFactory trustManagerFactory;
	public SSLTrustStatement() {
		this.tmCerts = new javax.net.ssl.TrustManager[] { this};
		this.trustManagerFactory = new DefualtTrustManagerFactory();
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		clientCertificateChain = chain;
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		this.peerCertificateChain = chain;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public X509Certificate[] getPeerCertificateChain() {
		return peerCertificateChain;
	}
	
	public X509Certificate[] getClientCertificateChain() {
		return clientCertificateChain;
	}
	
	public TrustManagerFactory getTrustManagerFactory() {
		return trustManagerFactory;
	}
	
	class DefualtTrustManagerFactory extends io.netty.handler.ssl.util.SimpleTrustManagerFactory{

		@Override
		protected void engineInit(KeyStore keyStore) throws Exception {}

		@Override
		protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception {}

		@Override
		protected TrustManager[] engineGetTrustManagers() {
			return SSLTrustStatement.this.tmCerts;
		}
		
	}
	
}
