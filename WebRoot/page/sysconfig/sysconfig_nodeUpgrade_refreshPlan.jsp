<%@ page language="java" pageEncoding="utf-8"%>
<style>
#nodeUpgradeRefreshForm{
	margin:20px 10px 10px;
	font-size:12px;
}
#nodeUpgradeRefreshForm select{
	height:24px;
	width:200px;
	font-size:12px;
	padding:0px;
	border-color:#8CB7C8;
}
div.pull-left{
	padding-left:10px;
}
</style>
<form id="nodeUpgradeRefreshForm" class="form-horizontal">
	<fieldset>
		<legend>升级时间</legend>
		<div style="padding-left:100px;">
		<label><select id="upgradePlanType" name="planType" >
			<option value="perTime" selected="selected">周期性</option>
			<option value="spcOne">特定时间执行一次</option>
		</select></label>
		<div class="control-group" ></div>
		<div class="control-group" id="upgradePlanType_perTime" >
			<%@include file="/page/asset/poll_time.jsp" %>
		</div>
		<div class="control-group" id="upgradePlanType_spcOne" >
			<input id="spcOneVal" name="exeTime" type="text" class="easyui-datetimebox" data-options="editable:false,width:200"/>
		</div>
		<div class="control-group">
			<div><a class="easyui-linkbutton" iconCls="icon-apply" onclick="nodeUpgradeRefresh.saveSubmit()">应用</a>　
			<a class="easyui-linkbutton" iconCls="icon-cancel" onclick="simSysConfNodeUpgrade.closeUploadFileDialog()">取消</a></div>
		</div>
		</div>
	</fieldset>
</form>
<script src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/sysconfig/sysconfig_nodeUpgradeRefreshPlan.js"></script>
<script>
	$(function(){
		var planType = "${planType}";
		if(planType) {
			$("#upgradePlanType").val(planType);
		}
		$("#upgradePlanType").focus();
		$("#upgradePlanType").change(function(){
			nodeUpgradeRefresh.displayDiv("upgradePlanType",$(this).val());
		});
		$("#upgradePlanType").trigger("change") ;
		if(planType === "spcOne"){
			var _date = "${date}";
			if(!_date) {
				_date = new Date();
			}
			$("#spcOneVal").datetimebox({
			    value: _date,
			    height:24,
			    width:200
			});
		}
		if("${timerExpression}") {
			$("#poll_time_type").trigger("change") ;
		}
	});
</script>
