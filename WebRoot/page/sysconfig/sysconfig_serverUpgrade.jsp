<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<div class="easyui-panel" data-options="fit:true,title:'服务器升级配置',headerCls:'sim-panel-header',bodyCls:'sim-panel-body'">
	<form id="theServerUpgradeForm" class="form-horizontal" style="font-size: 15px" enctype="multipart/form-data" method="post">
		<fieldset>
			<div id="legend" style="padding:20px;">
			</div>
			<div class="control-group">
				<label class="control-label">上传升级包：</label>
				<div class="controls">
					<input class="input-file horizon-fileupload" type="file" name="theFile" id="theServerFile" />
				</div>
			</div>
			<div class="control-group" style="font-size: 12px">
				<label class="control-label"></label>
				<div class="controls">当前版本：${version}</div>
			</div>
			<div class="control-group">
				<label class="control-label"></label>
				<div class="controls"><a class="easyui-linkbutton" iconCls="icon-apply" onclick="serverUpgrade.save_submit()">应用</a></div>
			</div>
		</fieldset>
	</form>
</div>
<script src="/js/sim/sysconfig/sysconfig_serverUpgrade.js"></script>
<script type="text/javascript" >
	$(function(){
		$("#theServerFile").cnfileupload();
	});
</script>
