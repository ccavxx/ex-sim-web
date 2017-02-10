<%@ page language="java" pageEncoding="utf-8"%>
<html>
  <head>
    <title>${node.alias}抓包</title>
    <link href="/css/bootstrap.css" rel="stylesheet" media="screen">
    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
    <link href="/css/system.css" rel="stylesheet" type="text/css">
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
  	<script src="/js/validator/jquery.validator.js"></script>
  	<script src="/js/global/system.js"></script>
  	<script src="/js/sim/debug/pcap_tool.js"></script>
  </head>
  
  <body>
		<div class="easyui-layout sim" data-options="fit:true">
			<div data-options="region:'north',height:40">
				<div class="alert alert-info" style="margin: 0px;">
				<span class="label label-success">1</span>&nbsp;此工具只能抓取UDP的数据包
				<span class="label label-success">2</span>&nbsp;修改源地址、目的地址、目的端口后要停止抓包再开始抓包才能生效
				<span class="label label-success">3</span>&nbsp;“下载数据”是开始抓包以后所有的数据与表格数据无关(格式:源地址Tab源端口Tab目的地址Tab目的端口Tab数据)
				<span class="label label-success">4</span>&nbsp;关闭此窗口时应先停止抓包过程
				</div>
			</div>
			<div data-options="region:'center'">
				<table id="pcap_data" class="easyui-datagrid" 
					   data-options="fitColumns:true,singleSelect:true,toolbar:'#pcap_tool_bar',fit:true,pageSize:20">  
				    <thead>  
				        <tr>  
				            <th data-options="field:'src',width:100">源地址</th>  
				            <th data-options="field:'dest',width:100">目的地址</th>  
				            <th data-options="field:'destPort',width:50">目的端口</th>  
				            <th data-options="field:'data',width:750">数据</th>  
				        </tr>  
				    </thead>  
				    <tbody>  
				    </tbody>  
				</table> 
			</div>
		</div>
		<div id="pcap_tool_bar">
			 <table style="height:28px;vertical-align: middle;">
				<tr>
					<td><a id="pcap_start_btn" class="easyui-linkbutton" data-options="iconCls:'icon-start'" onclick="pcap.start()">开始抓包</a></td>
					<td><a id="pcap_stop_btn" class="easyui-linkbutton"  data-options="iconCls:'icon-stop',disabled:true" onclick="pcap.stop()">停止抓包</a></td>
					<td><a class="easyui-linkbutton"  data-options="iconCls:'icon-remove'" onclick="pcap.clearData()">清空数据</a></td>
					<td><a class="easyui-linkbutton" href="/sim/pcap/download?nodeId=${node.nodeId}" target="_blank" data-options="iconCls:'icon-down'">下载数据</a></td>
					<td width="80" align="right"><span>字符集：</span></td>
					<td>
						<select id="pcap_charset" class="easyui-combobox" name="pcap_charset" style="width:100px;">  
						    <option value="ASCII">ASCII</option>  
						    <option value="UTF-8">UTF-8</option>  
						    <option value="GBK">GBK</option>  
						    <option value="ISO-8859-1">ISO-8859-1</option>  
						    <option value="UNICODE">UNICODE</option>  
						</select> 
					</td>
					<td width="80" align="right">源地址：</td>
					<td><input id="pcap_filter_src" style="width: 150"></td>
					<td width="100" align="right">目的地址：</td>
					<td><input id="pcap_filter_dest" style="width: 150"></td>
					<td width="100" align="right">目的端口：</td>
					<td><input id="pcap_filter_dest_port" value="514" style="width: 60"></td>
				</tr>
			</table>
			<input id="pcap_node" type='hidden' value='${node.nodeId}'>
		</div>		
  </body>
</html>
