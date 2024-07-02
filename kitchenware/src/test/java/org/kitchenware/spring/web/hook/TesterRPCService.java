package org.kitchenware.spring.web.hook;

import java.net.URI;

import org.kitchenware.spring.web.annotation.Impl;
import org.kitchenware.spring.web.annotation.RPC;
import org.kitchenware.spring.web.nativebean.NativeBeanFactory;
import org.kitchenware.spring.web.rpc.ServiceRPC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface TesterRPCService {
	
	void executeToken() throws Exception;

	@Service
	static class TesterRPCServiceImpl implements TesterRPCService{
		
		/**
		 * @Impl 纯业务service 使用本地开发模式, 指向实现类即可使用
		 */
		@Impl(
				org.kitchenware.spring.web.hook.NativeTesterService.NativeTesterServiceImpl.class)
		@Autowired
		private NativeTesterService nativeTesterService;
		
		/**
		 * @RPC 闭源接口, 操作数据库接口, 使用远程调用的方式
		 */
		@RPC
		@Autowired
		private RemoteService remoteService;

		@Override
		public void executeToken() throws Exception{
			String token = this.nativeTesterService.executeToken();
			RemoteService.TesterRequest request = new RemoteService.TesterRequest()
					.setEndpoint(token)
					;
			RemoteService.TesterResponse response = this.remoteService.transport(request);
			System.out.println("丢回来的消息: " + response.getReciveEndpoint());
		}
		
		
		public static void main(String[] args) throws Throwable{
			/**
			 * 初始化一个@Controller作为数据传输地址
			 * */
			URI uri = new URI("http://localhost:8081/test/rpc/hook/invoke");
			
			//初始化远程调用
			ServiceRPC rpc = new ServiceRPC(uri);
			
			NativeBeanFactory beanFactory = new NativeBeanFactory(rpc);
			
			TesterRPCService shell = beanFactory
					.typeOf(TesterRPCService.TesterRPCServiceImpl.class)
					.getBean();
			
			shell.executeToken();
			
			System.exit(0);
		}
	}
}
