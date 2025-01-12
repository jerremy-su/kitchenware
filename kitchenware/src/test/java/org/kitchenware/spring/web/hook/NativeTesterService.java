package org.kitchenware.spring.web.hook;

import java.util.UUID;

import org.springframework.stereotype.Service;

public interface NativeTesterService {

	String executeToken();
	
	@Service
	static class NativeTesterServiceImpl implements NativeTesterService{

		@Override
		public String executeToken() {
			return UUID.randomUUID().toString();
		}
		
	}
}
