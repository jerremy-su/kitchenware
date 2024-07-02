package org.kitchenware.network.netty;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.kitchenware.express.util.StringObjects;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class NettyOpenSSLNativeRegistry {
	static final Logger LOGGER = Logger.getLogger(NettyOpenSSLNativeRegistry.class.getName());
	
	static boolean isInitial;
	public synchronized static void loadTcNative() throws Exception {
		if(isInitial) return;
		isInitial = true;
		
		String os = normalizeOs(System.getProperty("os.name"));
		String arch = normalizeArch(System.getProperty("os.arch"));

		if(arch.toLowerCase().startsWith("aarch_64")) {
			arch = "aarch_64";
		}
		
		Set libNames = new LinkedHashSet();
		libNames.add("netty_tcnative_" + os + '_' + arch);
		libNames.add("libnetty_tcnative_" + os + '_' + arch);
//		if ("linux".equalsIgnoreCase(os)) {
//			libNames.add("netty_tcnative-" + os + '_' + arch + "_fedora");
//		}

		libNames.add("netty-tcnative");

		libNames.add("netty_tcnative");
		
		libNames.add("libnetty_tcnative");

		libNames.add("libnetty-tcnative");

		loadFirstAvailable(NettyOpenSSLNativeRegistry.class.getClassLoader(),
				(String[]) libNames.toArray(new String[libNames.size()]));
	}

	public static void loadFirstAvailable(ClassLoader loader, String[] names) {
		for (String name : names) {
			try {
				NettyNativeLibraryLoader.load(name, loader);
				return;
			} catch (Throwable e) {
				String err = StringObjects.format("Failed to load native lib: %s", e.getMessage());
				LOGGER.warning(err);
			}
		}
	}

	private static String normalizeOs(String value) {
		value = normalize(value);
		if (value.startsWith("aix")) {
			return "aix";
		}
		if (value.startsWith("hpux")) {
			return "hpux";
		}
		if ((value.startsWith("os400"))
				&& (((value.length() <= 5) || (!(Character.isDigit(value
						.charAt(5))))))) {
			return "os400";
		}

		if (value.startsWith("linux")) {
			return "linux";
		}
		if ((value.startsWith("macosx")) || (value.startsWith("osx"))) {
			return "osx";
		}
		if (value.startsWith("freebsd")) {
			return "freebsd";
		}
		if (value.startsWith("openbsd")) {
			return "openbsd";
		}
		if (value.startsWith("netbsd")) {
			return "netbsd";
		}
		if ((value.startsWith("solaris")) || (value.startsWith("sunos"))) {
			return "sunos";
		}
		if (value.startsWith("windows")) {
			return "windows";
		}

		return "unknown";
	}

	private static String normalize(String value) {
		return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
	}

	private static String normalizeArch(String value) {
		value = normalize(value);
		if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
			return "x86_64";
		}
		if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
			return "x86_32";
		}
		if (value.matches("^(ia64|itanium64)$")) {
			return "itanium_64";
		}
		if (value.matches("^(sparc|sparc32)$")) {
			return "sparc_32";
		}
		if (value.matches("^(sparcv9|sparc64)$")) {
			return "sparc_64";
		}
		if (value.matches("^(arm|arm32)$")) {
			return "arm_32";
		}
		if ("aarch64".equals(value)) {
			return "aarch_64";
		}
		if (value.matches("^(ppc|ppc32)$")) {
			return "ppc_32";
		}
		if ("ppc64".equals(value)) {
			return "ppc_64";
		}
		if ("ppc64le".equals(value)) {
			return "ppcle_64";
		}
		if ("s390".equals(value)) {
			return "s390_32";
		}
		if ("s390x".equals(value)) {
			return "s390_64";
		}

		return "unknown";
	}

	public static void main(String[] args) throws Throwable {
		loadTcNative();
		SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL)
				.startTls(true)
				.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	}
}
