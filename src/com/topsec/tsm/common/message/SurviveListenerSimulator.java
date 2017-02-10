package com.topsec.tsm.common.message;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.comm.CommandDispatcher;
import com.topsec.tsm.node.comm.CommandHandle;
import com.topsec.tsm.node.comm.CommandListener;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;
import com.topsec.tsm.node.status.NodeStatusMap;
import com.topsec.tsm.sim.node.util.NodeAliveCache;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;

public class SurviveListenerSimulator {
	private static final Logger log = LoggerFactory.getLogger(SurviveListenerSimulator.class);
	
	class SurviveListener implements CommandListener {
		public Serializable onCommand(String command, Serializable obj,String[] route, CommandHandle handle) {
			if (command.equals(MessageDefinition.MSG_NODE_KEEP_ALIVE)) {// 得到配置文件
				NodeAliveCache.getInstance().putLastAliveTime(route[0],new Date().getTime());
				if(obj instanceof NodeStatusMap){
					NodeStatusMap statusMap=(NodeStatusMap)obj;
					try {
						NodeStatusQueueCache.getInstance().putNodeStatusMap(statusMap.getNamespace(), statusMap);
					} catch (Exception e) {
						log.error("com.topsec.tsm.sim.node.listener.SurviveListenerSimulator.SurviveListener.onCommand()!"+e.getMessage());
					}
				}
			}
			return null;
		}
	}

	public void init() {
		String[] cmds = new String[] { MessageDefinition.MSG_NODE_KEEP_ALIVE };

		try {

			CommandDispatcher commandChannel = ChannelGate.getCommandChannel(ChannelConstants.COMM_KEEP_ALIVE_CHANNEL);
			
		//	CommandDispatcher lucommandChannel = ChannelCenter
		//	.getCommandChannel(NodeConstants.COMM_LIVE_UPDATE_KEEP_ALIVE_CHANNEL);

			commandChannel.regist(cmds, new SurviveListener());
			
		//	lucommandChannel.regist(cmds, new SurviveListener());

		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
