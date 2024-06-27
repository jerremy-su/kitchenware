package org.kitchenware.network.netty.http;

import java.net.URI;

import org.kitchenware.express.util.StringObjects;

public class HttpNettyTester {

	public static void main(String[] args) throws Throwable{
		String url = "https://www.baidu.com";
		HttpResopnse response = HttpNetty
		.doGet(new URI(url))
		.invoke();
		
		System.out.println(StringObjects.toUTF8String(response.getContent()));
		
		System.exit(0);
	}
}
