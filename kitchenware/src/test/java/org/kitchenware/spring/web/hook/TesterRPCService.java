package org.kitchenware.spring.web.hook;

import org.kitchenware.spring.web.annotation.Impl;
import org.kitchenware.spring.web.annotation.RPC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface TesterRPCService {

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
		
	}
}
