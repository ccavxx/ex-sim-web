package com.topsec.tsm.common.message;

import java.util.List;

import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.comm.CommandDispatcher;
import com.topsec.tsm.node.comm.CommandListener;
import com.topsec.tsm.node.main.ChannelConstants;
import com.topsec.tsm.node.main.ChannelGate;

public class CommandListenerRegister {

	public void init() {
		/*
		 * System.out.println("commands"+this.commands);
		 * System.out.println("listeners"+this.listeners);
		 */
		/*
		 * String[] cmds = new String[this.commands.size()]; int i=0; for(String
		 * cmd :commands){ cmds[i]=cmd; i++; }
		 */
		String[] cmds = new String[] {
				MessageDefinition.CMD_NODE_REGIST_REGISTER,
				MessageDefinition.CMD_NODE_REREGIST_REGISTER,
				MessageDefinition.CMD_NODE_GET_REGISTER,
				MessageDefinition.CMD_NODE_CHECK_COMMUNICATION,
				MessageDefinition.CMD_NODE_GET_WORKSTATUS,
				MessageDefinition.CMD_NODE_SYNCHRONIZE_TIME,
				MessageDefinition.CMD_NODE_GET_PATCHLIST,
				MessageDefinition.CMD_NODE_BACK_STATE
				};
		try {
			CommandDispatcher commandChannel = ChannelGate
					.getCommandChannel(ChannelConstants.COMM_BASE_COMMAND_CHANNEL);
			if (listeners != null) {
				for (CommandListener listener : listeners) {
					commandChannel.regist(cmds, listener);
				}
			}

		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private List<CommandListener> listeners;

	private List<String> commands;

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public List<CommandListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<CommandListener> listeners) {
		this.listeners = listeners;
	}
}
