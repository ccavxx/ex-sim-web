package com.topsec.tsm.sim.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.topsec.tal.base.util.EnhanceProperties;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.action.config.ActionConfiguration;
import com.topsec.tsm.action.config.CommandActionConf;
import com.topsec.tsm.action.config.IntegerActionConf;
import com.topsec.tsm.action.config.MailActionConf;
import com.topsec.tsm.action.config.MailServerConfiguration;
import com.topsec.tsm.action.config.SmsActionConf;
import com.topsec.tsm.action.config.SnmpActionConf;
import com.topsec.tsm.action.config.SoundActionConf;
import com.topsec.tsm.action.config.SoundShineActionConf;
import com.topsec.tsm.action.config.UMSGateActionConf;
import com.topsec.tsm.action.config.UMSGateActionConfiguration;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;

/**
 * 响应下发类
 * 
 * @author liuzhan
 */
public class ResponseSend {
	private static ResponseSend dispatcher = null;

	public static ResponseSend getInstance() {
		if (dispatcher == null)
			dispatcher = new ResponseSend();
		return dispatcher;
	}

	/**
	 * 短信响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendPhonemsg(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_SMSACTION, true);// HANDLER_SMSACTION
				if (component != null) {
					ActionConfiguration<SmsActionConf> smsActionConfiguration = new ActionConfiguration<SmsActionConf>();
					smsActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, smsActionConfiguration);
					smsActionConfiguration.setConfClass(SmsActionConf.class) ;
					if (smsActionConfiguration != null) {
						if ("delete".equals(type)) {
							smsActionConfiguration.getActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							SmsActionConf smsActionConf = new SmsActionConf();

							Block portBlock = config.getBlockbyKey("connectport");
							Block msgBlock = config.getBlockbyKey("msgnotify");
							Block phonelistBlock = config.getBlockbyKey("phonelist");
							
							String baudRate = portBlock.getItemValue("smsbaudrate");
							String comPort = portBlock.getItemValue("smscomport");
							String title = msgBlock.getItemValue("title");
							String msg = msgBlock.getItemValue("content");
							List<String> phonelist = phonelistBlock.getItemValueList("phonelist");
							phonelistBlock.getItemValueList("phonelist");

							smsActionConf.setBaudRate(baudRate);
							smsActionConf.setComPort(comPort);
							smsActionConf.setId(resp.getId());
							smsActionConf.setIsstart(resp.isStart());
							smsActionConf.setMsg(msg);
							smsActionConf.setPhonelist(phonelist);
							smsActionConf.setTitle(title);

							Map<String, SmsActionConf> smsActionMap = smsActionConfiguration.getActionMap();
							smsActionMap.put(resp.getId(), smsActionConf);
							smsActionConfiguration.setActionMap(smsActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, smsActionConfiguration);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行服务命令响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendExeccmd(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_COMMANDACTION, true);// HANDLER_COMMANDACTION
				if (component != null) {
					ActionConfiguration<CommandActionConf> allActionConfig = new ActionConfiguration<CommandActionConf>();
					allActionConfig = nodeMgrFacade.getSegConfigByComAndT(component, allActionConfig);
					allActionConfig.setConfClass(CommandActionConf.class) ;
					CommandActionConf commandActionConf;
					if (allActionConfig != null) {
						if ("delete".equals(type)) {
							allActionConfig.getActionMap().remove(resp.getId());// 根据id移除配置信息
						}else if ("save".equals(type) || "modify".equals(type)) {
							Block mailsvrBlock = config.getDefaultBlock();
							String exeCommand = mailsvrBlock.getItemValue("execcmd");
							commandActionConf = new CommandActionConf(resp.getId(), resp.isStart(), exeCommand) ;
							Map<String, CommandActionConf> commandActionMap = allActionConfig.getActionMap();
							commandActionMap.put(resp.getId(), commandActionConf);
							allActionConfig.setActionMap(commandActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, allActionConfig);
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 声音响应 下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendWavalert(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_SOUNDACTION, true);// HANDLER_SOUNDACTION
				if (component != null) {
					ActionConfiguration<SoundActionConf> soundActionConfiguration = new ActionConfiguration<SoundActionConf>();
					soundActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, soundActionConfiguration);
					if (soundActionConfiguration != null) {
						if ("delete".equals(type)) {
							soundActionConfiguration.getActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							Block msgBlock = config.getDefaultBlock();
							String basegrade = msgBlock.getItemValue("basegrade");
							int exectimes = StringUtil.toInt(msgBlock.getItemValue("exectimes"));
							int execinterval = StringUtil.toInt(msgBlock.getItemValue("execinterval"));
							SoundActionConf soundActionConf = new SoundActionConf(resp.getId(),resp.isStart(),basegrade,exectimes,execinterval);
							Map<String, SoundActionConf> soundActionMap = soundActionConfiguration.getActionMap();
							soundActionMap.put(resp.getId(), soundActionConf);
							soundActionConfiguration.setActionMap(soundActionMap);
						}

						nodeMgrFacade.updateComponentSegmentAndDispatch(component, soundActionConfiguration);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 声光响应 下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendWavaShinelert(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {

				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_SOUNDSHINEACTION, true);
				if (component != null) {

					ActionConfiguration<SoundShineActionConf> soundShineActionConfiguration = new ActionConfiguration<SoundShineActionConf>();
					soundShineActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, soundShineActionConfiguration);
					if (soundShineActionConfiguration != null) {
						if ("delete".equals(type)) {
							soundShineActionConfiguration.getActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							Block msgBlock = config.getDefaultBlock();
							String basegrade = msgBlock.getItemValue("basegrade");
							String shinecontent = msgBlock.getItemValue("shinecontent");

							SoundShineActionConf soundShineActionConf = new SoundShineActionConf();
							soundShineActionConf.setId(resp.getId());
							soundShineActionConf.setIsStart(resp.isStart());
							soundShineActionConf.setBasegrade(basegrade);
							soundShineActionConf.setShinecontent(shinecontent);

							Map<String, SoundShineActionConf> soundShineActionMap = soundShineActionConfiguration.getActionMap();
							soundShineActionMap.put(resp.getId(), soundShineActionConf);
							soundShineActionConfiguration.setActionMap(soundShineActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, soundShineActionConfiguration);
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	

	/**
	 * Snmp Trap 响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendSnmpTrap(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_SNMPACTION, true);// HANDLER_SNMPACTION
				if (component != null) {
					ActionConfiguration<SnmpActionConf> snmpActionConfiguration = new ActionConfiguration<SnmpActionConf>();
					snmpActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, snmpActionConfiguration);
					if (snmpActionConfiguration != null) {
						if ("delete".equals(type)) {
							snmpActionConfiguration.getActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							Block snmpBlock = config.getDefaultBlock();
							String serverip = snmpBlock.getItemValue("serverip");
							int serverport = new Integer(snmpBlock.getItemValue("serverport"));
							String snmp_community = snmpBlock.getItemValue("snmp_community");
							String snmp_ver = snmpBlock.getItemValue("snmp_ver");
							String transfer_protocol = snmpBlock.getItemValue("transfer_protocol");

							SnmpActionConf snmpActionConf = new SnmpActionConf();
							snmpActionConf.setId(resp.getId());
							snmpActionConf.setIsStart(resp.isStart());
							snmpActionConf.setServerip(serverip);
							snmpActionConf.setServerport(serverport);
							snmpActionConf.setSnmp_community(snmp_community);
							snmpActionConf.setSnmp_ver(snmp_ver);
							snmpActionConf.setTransfer_protocol(transfer_protocol);

							Map<String, SnmpActionConf> snmpActionMap = snmpActionConfiguration.getActionMap();
							snmpActionMap.put(resp.getId(), snmpActionConf);
							snmpActionConfiguration.setActionMap(snmpActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, snmpActionConfiguration);
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * UMS 响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendUMSGate(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, "Handler.UMSAction", true);
				if (component != null) {
					UMSGateActionConfiguration umsGateActionConfiguration = new UMSGateActionConfiguration();
					umsGateActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, umsGateActionConfiguration);
					EnhanceProperties configFile = PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH) ;
					String smsMsgPrefix = configFile.getProperty("sms.msg.prefix") ;
					if (umsGateActionConfiguration != null) {
						if ("delete".equals(type)) {
							umsGateActionConfiguration.getUMSGateActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							UMSGateActionConf umsGateActionConf = packUMSGateObj(config, resp);
							umsGateActionConf.setSmsMsgPrefix(smsMsgPrefix);
							Map<String, UMSGateActionConf> umsActionMap = umsGateActionConfiguration.getUMSGateActionMap();
							umsActionMap.put(resp.getId(), umsGateActionConf);
							umsGateActionConfiguration.setUMSGateActionMap(umsActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, umsGateActionConfiguration);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
	
	private UMSGateActionConf packUMSGateObj(Config config, Response resp){
		Block umsBlock = config.getBlockbyKey("base_info");
		String spCode = umsBlock.getItemValue("spCode");
		String loginName = umsBlock.getItemValue("loginName");
		String password = umsBlock.getItemValue("password");
		String messageContent = umsBlock.getItemValue("messageContent");
		Block phoneListBlock = config.getBlockbyKey("phonelist") ;
		List<String> phonelist = phoneListBlock.getItemValueList("phonelist");
		
		UMSGateActionConf umsGateActionConf = new UMSGateActionConf();
		umsGateActionConf.setId(resp.getId());
		umsGateActionConf.setSpCode(spCode);
		umsGateActionConf.setLoginName(loginName);
		umsGateActionConf.setPassword(password);
		umsGateActionConf.setMessageContent(messageContent);
		umsGateActionConf.setUserNumber(StringUtil.join(phonelist));
		umsGateActionConf.setEnabled(resp.isStart()) ;
		EnhanceProperties configFile = PropertyManager.getResource("resource/system.properties") ;
		umsGateActionConf.setSmsMsgPrefix(configFile.getProperty("sms.msg.prefix")) ;
		return umsGateActionConf;
	}
	
	/**
	 *邮件响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 */
	public void sendToMail(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_MAILACTION, true);// HANDLER_MAILACTION
				if (component != null) {
					 
					ActionConfiguration<MailActionConf> mailActionConfiguration = new ActionConfiguration<MailActionConf>();
					mailActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, mailActionConfiguration);
					MailServerConfiguration mailServerConfiguration = nodeMgrFacade.getSegmentConfigByClass(component, MailServerConfiguration.class) ;
					if(mailServerConfiguration == null){
						mailServerConfiguration = new MailServerConfiguration() ;
					}
					EventResponseService eps = (EventResponseService) SpringContextServlet.springCtx.getBean("eventResponseService") ;
					List<Response> responses = eps.getResponsesbyCfgKey("sys_cfg_mailserver") ;
					Config mailServerConfig = ObjectUtils.isNotEmpty(responses) ? null : RespCfgHelper.getConfig(responses.get(0));
					String serverIp = mailServerConfig == null ? null : mailServerConfig.getDefaultBlock().getItemValue("serverip") ;
					if(StringUtil.isNotBlank(serverIp) && !serverIp.equals(mailServerConfiguration.getIp())){
						SystemConfigDispatcher.synMailServerConfig(mailServerConfig, mailServerConfiguration) ;
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, mailServerConfiguration);
					}
					if (mailActionConfiguration != null) {
						if ("delete".equals(type)) {
							mailActionConfiguration.getActionMap().remove(resp.getId());
						}else if ("save".equals(type) || "modify".equals(type)) {
							Block msgBlock = config.getBlockbyKey("msgnotify");
							Block receiveBlock = config.getBlockbyKey("mailreceivers");
							String msg = msgBlock.getItemValue("content");
							List<String> recervers = receiveBlock.getItemValueList("mailreceivers");
							String title = msgBlock.getItemValue("title");

							MailActionConf mailActionConf = new MailActionConf();
							mailActionConf.setId(resp.getId());
							mailActionConf.setIsStart(resp.isStart());
							mailActionConf.setMsg(msg);
							mailActionConf.setRecervers(recervers);
							mailActionConf.setTitle(title);

							Map<String, MailActionConf> mailActionMap = mailActionConfiguration.getActionMap();
							mailActionMap.put(resp.getId(), mailActionConf);
							mailActionConfiguration.setActionMap(mailActionMap);
						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, mailActionConfiguration);
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * TopAnalyzer联动响应下发
	 * 
	 * @param config
	 * @param nodeMgrFacade
	 * @param resp
	 * @param string
	 */
	public void sendInteger(Config config, NodeMgrFacade nodeMgrFacade, Response resp, String type) {
/*		
		try {
			Node node = nodeMgrFacade.getNodeByNodeId(resp.getNode().getNodeId(), false, false, true, true);
			if (node != null) {
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_INTEGERACTION, true);// HANDLER_INTEGERACTION
				if (component != null) {
					IntegerActionConfiguration integerActionConfiguration = new IntegerActionConfiguration();
					integerActionConfiguration = nodeMgrFacade.getSegConfigByComAndT(component, integerActionConfiguration);
					if (integerActionConfiguration != null) {
						if ("delete".equals(type)) {
							integerActionConfiguration.getIntegerActionConf().remove(resp.getId());
						}
						if ("save".equals(type)) {
							Block snmpBlock = config.getDefaultBlock();
							String serverip = snmpBlock.getItemValue("serverip");
							String topic = snmpBlock.getItemValue("topic");
							boolean encryption = Boolean.valueOf(snmpBlock.getItemValue("encryption"));
							int serverport = new Integer(snmpBlock.getItemValue("serverport"));

							IntegerActionConf integerActionConf = new IntegerActionConf();
							integerActionConf.setId(resp.getId());
							integerActionConf.setIp(serverip);
							integerActionConf.setIsDec(encryption);
							integerActionConf.setIsStart(resp.isStart());
							integerActionConf.setPort(serverport);
							integerActionConf.setTopicdestination(topic);

							Map<String, IntegerActionConf> integerActionConfMap = integerActionConfiguration.getIntegerActionConf();
							integerActionConfMap.put(resp.getId(), integerActionConf);
							integerActionConfiguration.setIntegerActionConf(integerActionConfMap);

						}
						if ("modify".equals(type)) {
							integerActionConfiguration.getIntegerActionConf().remove(resp.getId());
							Block snmpBlock = config.getDefaultBlock();
							String serverip = snmpBlock.getItemValue("serverip");
							String topic = snmpBlock.getItemValue("topic");
							boolean encryption = Boolean.valueOf(snmpBlock.getItemValue("encryption"));
							int serverport = new Integer(snmpBlock.getItemValue("serverport"));

							IntegerActionConf integerActionConf = new IntegerActionConf();
							integerActionConf.setId(resp.getId());
							integerActionConf.setIp(serverip);
							integerActionConf.setIsDec(encryption);
							integerActionConf.setIsStart(resp.isStart());
							integerActionConf.setPort(serverport);
							integerActionConf.setTopicdestination(topic);

							Map<String, IntegerActionConf> integerActionConfMap = integerActionConfiguration.getIntegerActionConf();
							integerActionConfMap.put(resp.getId(), integerActionConf);
							integerActionConfiguration.setIntegerActionConf(integerActionConfMap);

						}
						nodeMgrFacade.updateComponentSegmentAndDispatch(component, integerActionConfiguration);
					}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
*/
	}

	/* 命令测试下发 */
	public void sendTestExeccmd(HttpServletRequest request, Node node, Config config, Response resp) {

		try {
			String[] route = NodeUtil.getRoute(node);

			Block mailsvrBlock = config.getDefaultBlock();
			String exeCommand = mailsvrBlock.getItemValue("execcmd");

			CommandActionConf commandActionConf = new CommandActionConf();
			commandActionConf.setExeCommand(exeCommand);
			commandActionConf.setId(resp.getId());
			commandActionConf.setIsStart(resp.isStart());

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_CMDCTION, (Serializable) commandActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/* 邮件测试下发 */
	public void sendTestToMail(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block msgBlock = config.getBlockbyKey("msgnotify");
			Block receiveBlock = config.getBlockbyKey("mailreceivers");
			String msg = msgBlock.getItemValue("content");
			List<String> recervers = receiveBlock.getItemValueList("mailreceivers");
			String title = msgBlock.getItemValue("title");

			MailActionConf mailActionConf = new MailActionConf();
			mailActionConf.setId(resp.getId());
			mailActionConf.setIsStart(resp.isStart());
			mailActionConf.setMsg(msg);
			mailActionConf.setRecervers(recervers);
			mailActionConf.setTitle(title);

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_MAILACTION, (Serializable) mailActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* 短信测试下发 */
	public void sendTestPhonemsg(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block portBlock = config.getBlockbyKey("connectport");
			Block msgBlock = config.getBlockbyKey("msgnotify");
			Block phonelistBlock = config.getBlockbyKey("phonelist");
			
			String baudRate = portBlock.getItemValue("smsbaudrate");
			String comPort = portBlock.getItemValue("smscomport");
			String title = msgBlock.getItemValue("title");
			String msg = msgBlock.getItemValue("content");
			List<String> phonelist = phonelistBlock.getItemValueList("phonelist");
			phonelistBlock.getItemValueList("phonelist");
			
			
			SmsActionConf smsActionConf = new SmsActionConf();
			smsActionConf.setBaudRate(baudRate);
			smsActionConf.setComPort(comPort);
			smsActionConf.setId(resp.getId());
			smsActionConf.setIsstart(resp.isStart());
			smsActionConf.setMsg(msg);
			smsActionConf.setPhonelist(phonelist);
			smsActionConf.setTitle(title);

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_SMSACTION, (Serializable) smsActionConf, 2 * 60 * 10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* snmp测试下发 */
	public void sendTestSnmpTrap(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block snmpBlock = config.getDefaultBlock();
			String serverip = snmpBlock.getItemValue("serverip");
			int serverport = new Integer(snmpBlock.getItemValue("serverport"));
			String snmp_community = snmpBlock.getItemValue("snmp_community");
			String snmp_ver = snmpBlock.getItemValue("snmp_ver");
			String transfer_protocol = snmpBlock.getItemValue("transfer_protocol");

			SnmpActionConf snmpActionConf = new SnmpActionConf();
			snmpActionConf.setId(resp.getId());
			snmpActionConf.setIsStart(resp.isStart());

			snmpActionConf.setServerip(serverip);
			snmpActionConf.setServerport(serverport);
			snmpActionConf.setSnmp_community(snmp_community);
			snmpActionConf.setSnmp_ver(snmp_ver);
			snmpActionConf.setTransfer_protocol(transfer_protocol);
			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_SNMPACTION, (Serializable) snmpActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 声音测试下发 */
	public void sendTestWavalert(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block msgBlock = config.getDefaultBlock();
			String basegrade = msgBlock.getItemValue("basegrade");
			int exectimes = new Integer(msgBlock.getItemValue("exectimes"));
			int execinterval = new Integer(msgBlock.getItemValue("execinterval"));

			SoundActionConf soundActionConf = new SoundActionConf();
			soundActionConf.setBasegrade(basegrade);
			soundActionConf.setExecinterval(execinterval);
			soundActionConf.setExectimes(exectimes);
			soundActionConf.setId(resp.getId());
			soundActionConf.setIsStart(resp.isStart());

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_SOUNDACTION, (Serializable) soundActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* 声光测试下发 */
	public void sendTestWavaShinelert(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block msgBlock = config.getDefaultBlock();
			String basegrade = msgBlock.getItemValue("basegrade");
			String shinecontent = msgBlock.getItemValue("shinecontent");

			SoundShineActionConf soundShineActionConf = new SoundShineActionConf();
			soundShineActionConf.setBasegrade(basegrade);
			soundShineActionConf.setShinecontent(shinecontent);
			soundShineActionConf.setId(resp.getId());
			soundShineActionConf.setIsStart(resp.isStart());
			

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_SOUNDSHINEACTION, (Serializable) soundShineActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* TopAnalyzer联动响应测试下发 */
	public void sendTestInteger(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);

			Block snmpBlock = config.getDefaultBlock();
			String serverip = snmpBlock.getItemValue("serverip");
			String topic = snmpBlock.getItemValue("topic");
			boolean encryption = Boolean.valueOf(snmpBlock.getItemValue("encryption"));
			int serverport = new Integer(snmpBlock.getItemValue("serverport"));

			IntegerActionConf integerActionConf = new IntegerActionConf();
			integerActionConf.setId(resp.getId());
			integerActionConf.setIp(serverip);
			integerActionConf.setIsDec(encryption);
			integerActionConf.setIsStart(resp.isStart());
			integerActionConf.setPort(serverport);
			integerActionConf.setTopicdestination(topic);

			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_INTEGERACTION, (Serializable) integerActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendTestUMSGate(HttpServletRequest request, Node node, Config config, Response resp) {
		try {
			String[] route = NodeUtil.getRoute(node);
			
			Block umsBlock = config.getBlockbyKey("base_info");
			String spCode = umsBlock.getItemValue("spCode");
			String loginName = umsBlock.getItemValue("loginName");
			String password = umsBlock.getItemValue("password");
			String messageContent = umsBlock.getItemValue("messageContent");
			Block phoneListBlock = config.getBlockbyKey("phonelist") ;
			List<String> phonelist = phoneListBlock.getItemValueList("phonelist");
			
			UMSGateActionConf umsGateActionConf = new UMSGateActionConf();
			umsGateActionConf.setId(resp.getId());
			umsGateActionConf.setSpCode(spCode);
			umsGateActionConf.setLoginName(loginName);
			umsGateActionConf.setPassword(password);
			umsGateActionConf.setMessageContent(messageContent);
			umsGateActionConf.setUserNumber(StringUtil.join(phonelist));
			umsGateActionConf.setEnabled(resp.isStart()) ;
			EnhanceProperties configFile = PropertyManager.getResource("resource/system.properties") ;
			umsGateActionConf.setSmsMsgPrefix(configFile.getProperty("sms.msg.prefix")) ;
			
			NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_TEST_UMSGATEACTION, (Serializable) umsGateActionConf, 2 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
