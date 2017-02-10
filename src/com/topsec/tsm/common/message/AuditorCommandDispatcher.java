package com.topsec.tsm.common.message;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;

/**
 * auditor节点命令下发工具类
 * @author hp
 *
 */
public class AuditorCommandDispatcher extends Thread {
	private static Logger logger = LoggerFactory.getLogger(AuditorCommandDispatcher.class) ;
	/**要下发的命令*/
	private String command;
	/**命令超时时间*/
	private long timeout = 10000 ;
	/**下发超时后尝试次数*/
	private int sendTimes = 3 ;
	private Serializable param ;
	public AuditorCommandDispatcher(String command,Serializable param) {
		super("AuditorCommandSender") ;
		this.command = command ;
		this.param = param ;
	}

	public AuditorCommandDispatcher(String command,Serializable param, long timeout,int sendTimes) {
		super("AuditorCommandSender") ;
		this.command = command;
		this.sendTimes = sendTimes;
		this.timeout = timeout;
		this.param = param ;
	}

	@Override
	public void run() {
		if(command == null){
			logger.error("命令不能为null！") ;
			return ;
		}
		boolean success = false ;
		while(!success && sendTimes-- > 0){
			try{
				String[] routes = RouteUtils.getAuditorRoutes();
				NodeUtil.dispatchCommand(routes, command, param, timeout) ;
				success = true ;
			}catch(CommunicationException e){
				logger.error("命令:{}下发超时！",command) ;
			}
		}
		if(!success){
			logger.warn("命令:{}下发失败！",command) ;
		}
	}
	
}