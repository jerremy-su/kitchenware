package org.kitchenware.spring.web.hook;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kitchenware.spring.web.ServiceRPC;
import org.springframework.stereotype.Service;

public interface TesterService {

	static class TesterRequest {
		String endpoint;

		public String getEndpoint() {
			return endpoint;
		}

		public TesterService.TesterRequest setEndpoint(String endpoint) {
			this.endpoint = endpoint;
			return TesterService.TesterRequest.this;
		}

	}

	static class TesterResponse {
		String reciveEndpoint;

		public String getReciveEndpoint() {
			return reciveEndpoint;
		}

		public TesterService.TesterResponse setReciveEndpoint(String reciveEndpoint) {
			this.reciveEndpoint = reciveEndpoint;
			return TesterService.TesterResponse.this;
		}
	}
	
	TesterResponse transport(TesterRequest request) throws Exception;
	
	@Service
	static class TesterServiceImpl implements TesterService{
		
		@Override
		public TesterResponse transport(TesterRequest request) throws Exception {
			TesterResponse response = new TesterResponse();
			if(request != null) {
				response.setReciveEndpoint(request.getEndpoint());
			}
			return response;
		}
		
		public static void main(String[] args) throws Throwable{
			
			/**
			 * 初始化一个@Controller作为数据传输地址
			 * */
			URI uri = new URI("http://localhost:8081/test/rpc/hook/invoke");
			
			//初始化远程调用
			ServiceRPC rpc = new ServiceRPC(uri);
			
			//这是一个 @Service
			TesterService shell = rpc.getService(TesterService.class);
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			
			//执行远程调用
			TesterRequest request = new TesterRequest()
					.setEndpoint(String.format("[%s]:  把消息还给我", formatter.format(new Date())))
					;
			
			TesterResponse response = shell.transport(request);
			System.out.println("丢回来的消息: " + response.getReciveEndpoint());
		}
	}
}
