<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<div class="easyui-panel"  data-options="fit:true,title:'系统备份配置',headerCls:'sim-panel-header',bodyCls:'sim-panel-body'" style="padding:5px;">
	<div class="control-group">
		<div class="alert alert-info">
			<strong>描述 </strong>
			这里是系统配置备份，点击“备份”按钮开始备份，备份成功后，点击 “确定”，可以下载备份文件。
		</div>
	  	<div id="showExportDiv" style="text-align: center;">
	         <img style="margin:20px" src="/img/loading_withoutstate.jpg" />
	         <div style="padding:0 0 16px;">正在备份，请稍等。。。 </div>
	    </div>
	    <br/>
	    <div class="control-group">
	    	<div class="controls pl200" >
	      		<a class="easyui-linkbutton" iconCls="icon-backup" onclick="startBackup()">备份</a>
	    	</div>
	  	</div>
	</div>
</div>
<script src="/js/sim/sysconfig/sysconfig_backupFile.js"></script>
