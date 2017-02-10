package com.topsec.tsm.common.message;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.common.DoCommand;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.comm.CommandHandle;
import com.topsec.tsm.node.comm.CommandListener;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;

public class CommandListenerSimulator implements CommandListener{
	
	private static final Logger logger = LoggerFactory.getLogger(CommandListenerSimulator.class);
		private NodeMgrFacade nodeMgrFacade;

		public Serializable onCommand(String command, Serializable obj,
				String[] route, CommandHandle handle){
			StringBuilder buffer = new StringBuilder();
			buffer.append("Received command [");
			buffer.append(command);
			buffer.append("] from ");
			boolean first = true;
			for(String r:route){
				if(!first) {
					buffer.append("->");
				} else {
					first = false;
				}
				buffer.append(r);
			}
			logger.debug(buffer.toString());
			
			if (command.equals(MessageDefinition.CMD_NODE_REGIST_REGISTER)){
				try {
					DoCommand.getInstance().doRegister(obj, route, nodeMgrFacade);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}					
			}else if (command.equals(MessageDefinition.CMD_NODE_REREGIST_REGISTER)){
					try {
						//当节点内部组件结构发生改变时,会向SOC发送重新获取节点配置请求.
 						DoCommand.getInstance().doReGetNodeConfiguration(obj, route, nodeMgrFacade);
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}					
			}else if(command.equals(MessageDefinition.CMD_NODE_GET_REGISTER)){//得到配置文件				
				return DoCommand.getInstance().doGetConfig(obj, route, nodeMgrFacade);
			}else if(command.equals(MessageDefinition.CMD_NODE_CHECK_COMMUNICATION)){
				return obj;
			}else if(command.equals(MessageDefinition.CMD_NODE_GET_WORKSTATUS)){
				return DoCommand.getInstance().getAgentWorkStatus(nodeMgrFacade, route);
			}else if(MessageDefinition.CMD_NODE_SYNCHRONIZE_TIME.equals(command)){
				return new java.util.Date().getTime();
			}
			
			return null;
		}

		public NodeMgrFacade getNodeMgrFacade() {
			return nodeMgrFacade;
		}

		public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
			this.nodeMgrFacade = nodeMgrFacade;
		}
		
		
}
