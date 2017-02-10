<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<style>
table.tableIchat tr td:first-child {text-align:right;font-weight: bold;}
table.tableIchat tr {height: 40px;}
table.tableIchat tr td {padding: 0 10px;font-size: 12px;}
</style>
<div class="easyui-panel"
	data-options="title:'产品信息',fit:true,headerCls:'sim-panel-header',bodyCls:'sim-panel-body'">
	<div style="margin:10px auto;width:360px;position:relative;">
		<table class="tableIchat">
			<tbody>
				<tr>
					<td style="width:136px;">名称:</td>
					<td style="width:150px;"><%=CommonUtils.getProductName() %></td>
				</tr>
				<tr>
					<td>版本:</td>
					<td title="${version}">V3</td>
				</tr>
				<tr>
					<td>许可型号:</td>
					<td>${type}</td>
				</tr>
                <c:if test="${show}">
					<tr>
						<td>许可类型:</td>
						<td>${haspID}</td>
					</tr>
					<tr>
						<td>过期时间(剩余天数):</td>
						<td>${expireTime}</td>
					</tr>
                </c:if>
				<tr>
					<td>许可资产数目:</td>
					<td>${max_tal_num}</td>
				</tr>
				<tr>
					<td>已使用资产个数:</td>
					<td>${used_devicetotal}</td>
				</tr>
			</tbody>
		</table>
		<c:if test="${hasOperatorRole}">
			<div style="position:absolute;bottom:10px;right:20px;width:82px;height:100px;text-align:center;">
				<a style="outline:none;" href="/sim/systemConfig/downloadAgentFile?ftpfilepath=agent.rar" ><img src="/img/downExe.png" title="代理安装程序下载" /><div>代理程序下载</div></a>
			</div>
			<div style="position:absolute;bottom:10px;right:-80px;width:82px;height:100px;text-align:center;">
				<a style="outline:none;" href="/sim/systemConfig/downloadAgentFile?ftpfilepath=action.rar" ><img src="/img/downExe.png" title="告警安装程序下载" /><div>告警程序下载</div></a>
			</div>
		</c:if>
	</div>
	<c:if test="${showImportLicence}">
		<hr>
		<div style="margin:10px auto;width:600px;">
			<form id="license_file_import_form"
				method="post" enctype="multipart/form-data">
				<table class="tableIchat">
					<tbody>
						<tr>
							<td style="width:136px;"><span style="color:red;">许可文件:</span>
							</td>
							<td>
								<input type="file" class="horizon-fileupload" name="theLicenseFile" id="theLicenseFile"/>
							</td>
						</tr>
						<tr>
							<td colspan="2">
							<a class="easyui-linkbutton" style="margin-right: 4px;" data-options="iconCls:'icon-import',plain:false"
								href="javascript:aboutUs.importSubmit();">上传</a>
							<a class="easyui-linkbutton" data-options="plain:false" href="javascript:aboutUs.cancelSubmit();"
								onclick="$('#license_file_import_form').form('reset');" style="margin-right: 2px;">清除文件</a>
							<a class="easyui-linkbutton" href="/page/sysconfig/licensegen.jsp" target="_blank">获取机器码</a>
							<a class="easyui-linkbutton" href="/sim/pcap/ui" target="_blank">抓包</a>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
	</c:if>
</div>
<script src="/js/sim/about/about_us.js"></script>
<script type="text/javascript" >
	$(function(){
		$("#theLicenseFile").cnfileupload();
	});
</script>
