package com.topsec.tsm.sim.asset.web.vtclient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationExpirationException;
import com.topsec.tsm.common.SIMConstant;

public abstract class OSCommandProxy extends ConnectionProxy {
	
	public static Set<String> unsupportCommand ;
	/**命令计数器*/
	private AtomicInteger commandCounter = new AtomicInteger(1);
	/**命令历史*/
	private TreeMap<Integer,String> historyCommand = new TreeMap<Integer,String>() ;
	static{
		String[] commands = PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH).getArray("unsupport_os_command") ;
		unsupportCommand = new HashSet<String>(commands.length) ;
		for(String cmd:commands){
			unsupportCommand.add(cmd) ;
		}
	}

	public OSCommandProxy(String sessionId, String ip, String name,String password, int port) {
		super(sessionId, ip, name, password, port);
	}

	@Override
	public CommandResult exec(String command, int timeout)throws ProxyException, CommunicationExpirationException {
		setLastAccessTimes(System.currentTimeMillis()) ;
		if(command == null){
			throw new NullPointerException("message is null") ;
		}
		if(unsupportCommand.contains(command)){
			throw new UnsupportedOperationException(command) ;
		}
		String[] subcommands = command.split("[&|]") ;//将整条命令分割为单条命令
		for(String subcommand:subcommands){
			int spaceIndex = subcommand.indexOf(" ") ;
			String commandWithoutParam = subcommand.substring(0,spaceIndex > 0 ? spaceIndex : subcommand.length()) ;
			if(unsupportCommand.contains(commandWithoutParam)){
				throw new UnsupportedOperationException(commandWithoutParam) ;
			}
		}
		if(command.equals("his")||command.equals("history")){
			StringBuffer sb = new StringBuffer() ;
			for(Map.Entry<Integer, String> hisCmd:historyCommand.entrySet()){
				sb.append(StringUtil.lpad(hisCmd.getKey().toString(),5 , ' '))
				  .append("     ")
				  .append(hisCmd.getValue()).append("\n") ;
			}
			return new StringResult(sb.toString()) ;
		}
		if(historyCommand.size() > 30){
			historyCommand.remove(historyCommand.firstKey()) ;
		}
		historyCommand.put(commandCounter.getAndAdd(1), command) ;
		command = rebuildCommand(command) ;
		return innerExec(command, timeout) ;
	}
	/**
	 * 重组命令
	 * @param command
	 * @return
	 * @throws CommandException 
	 */
	protected String rebuildCommand(String command){
		if(command.startsWith("!")){
			Integer commandSeq = StringUtil.toInteger(command.substring(1),-1) ;
			if(commandSeq < 1){
				throw new UnsupportedOperationException(command) ;
			}
			command = historyCommand.get(commandSeq) ;
			if(command == null){
				throw new CommandException("无效的命令序号："+commandSeq) ;
			}
		}
		return command ;
	}
	public abstract CommandResult innerExec(String command,int timeout)throws ProxyException, CommunicationExpirationException ;

}
