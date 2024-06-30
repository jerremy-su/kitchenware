package org.kitchenware.spring.web.hook;

import java.net.URI;

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
				response.setReciveEndpoint(response.getReciveEndpoint());
			}
			return response;
		}
		
		public static void main(String[] args) throws Throwable{
			ServiceRPC rpc = new ServiceRPC(new URI("http://localhost:8081/test/rpc/hook/invoke"));
			TesterService shell = rpc.getService(TesterService.class);
			
			TesterRequest request = new TesterRequest()
					.setEndpoint("http://localhost")
					;
			
			TesterResponse response = shell.transport(request);
			System.out.println("Recieve URL: " + response.getReciveEndpoint());
		}
	}
}
