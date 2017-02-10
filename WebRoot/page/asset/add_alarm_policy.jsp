<%@ page language="java" pageEncoding="utf-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!-- 本页面组件id命名全部使用前辍aar(add alarm rule)统一命名 -->
<form id="aap_form" class="form-horizontal hgap3" style="margin-top: 5px;">
  <input name="id" value="${eventPolicyMonitor.id}" type="hidden">
  <input name="monitorId" value="${monitorId}" type="hidden">
  <input id="securityObjectType" name="securityObjectType" value="${securityObjectType}" type="hidden" >
  <div class="control-group">
    <label class="control-label" for="aap_name"><span style="color:red;">*</span>告警名称：</label>
    <div class="controls">
      <input type="text" id="aap_name" name="name" value="${eventPolicyMonitor.name}" maxlength="25" placeholder="告警名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="aap_priority">级别：</label>
    <div class="controls">
      <select id="aap_priority" name="priority">
      	<option value="0">非常低</option>
      	<option value="1">低</option>
      	<option value="2">中</option>
      	<option value="3">高</option>
      	<option value="4">非常高</option>
      </select>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="aap_nodes">状态：</label>
    <div class="controls">
    	<input name="start" type="checkbox" checked="checked" data-on-label="启用" data-off-label="禁用" class="switch-mini" data-on="success" data-off="danger"/>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="aap_frequency">发生次数：</label>
    <div class="controls">
    	<input id="aap_frequency" name="frequency" class="easyui-numberspinner"
    		   data-options="width:200,height:24,min:1,max:100,step:1,value:1" />  
    </div>
  </div>    
  <div class="control-group">
    <label class="control-label" for="aap_eventFrequencyType">时间窗口：</label>
    <div class="controls">
    	<div class="input-append">
	    	<input id="aap_eventFrequencyType" name="eventFrequencyType" class="easyui-numberspinner"
	    		   data-options="width:200,height:24,min:0,max:10,step:1,value:0" />
	    	<span class="add-on" style="height: 20px;line-height: 20px;margin:0px 1px;padding:0px;">分钟</span>
    	</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="aap_filterSql"><span style="color:red;">*</span>过滤器：</label>
  	<div class="controls">
  		<div class="input-append">
  			<textarea id="aap_filterSql" name="filterSql" rows="5" readonly="readonly" style="margin: 0px;padding: 0px;width: 198px;" >${eventPolicyMonitor.filterSql}</textarea>
			<div class="btn-group btn-group-vertical">
				<!-- <button class="btn" type="button" onclick="$('#aap_filterSql').val('SELECTOR(TRUE)')">默认</button> -->
				<button class="btn" type="button" onclick="aap.openFilter()">编辑</button>
			</div>
  		</div>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="aap_desc">描述：</label>
  	<div class="controls">
  		<textarea id="aap_desc" name="desc" rows="3" style="margin: 0px;padding: 0px;width: 198px;">${eventPolicyMonitor.desc}</textarea>
    </div>
  </div>       
  <div class="control-group">
    <label class="control-label" for="aap_response">通知方式：</label>
  	<div class="controls">
  		<select id="aap_response" name="responseId" style="width:200px;height:150px;overflow: auto;" multiple data-options="multiple:true,panelHeight:150">  
  			<c:forEach var="resp" items="${responses}">
  				<option value="${resp.id}" >${resp.name}</option>
  			</c:forEach>
		</select> 
    </div>
  </div>       
</form>
<script src="/js/sim/asset/add_alarm_policy.js"></script>
<script type="text/javascript">
	$('#aap_form .switch-mini').bootstrapSwitch();
	$(function(){
		if('${eventPolicyMonitor}'!=''){
			var responseIds = "${responseIds}";
			if(responseIds){
				var responseIdsArray = responseIds.split(",");
				$("#aap_response").find("option").each(function(i){
					if(responseIdsArray.indexOf(this.value)!=-1){
						$(this).attr("selected",true);
					}
				});
			}
			$("#aap_response").focus();
			$("#aap_priority").val("${eventPolicyMonitor.priority}");
			$("#aap_form .switch-mini").bootstrapSwitch("setState", "${eventPolicyMonitor.start}"==="true");
			$("#aap_frequency").val("${eventPolicyMonitor.frequency}");
			$("#aap_eventFrequencyType").val("${eventPolicyMonitor.eventFrequencyType}");
		}
		//初始化表单验证组件，并创建表单验证实例
		aap.aap_form_validator = $("#aap_form").validator({
			theme: 'simple_right',
			showOk: "",
			fields:{
				name:'required;length[1~30]',
				filterSql:'required;',
				desc:'length[~100];'
			}
		}).data("validator");
		$("#aap_form").submit(function () {
			var isValid = aap.aap_form_validator.isFormValid();
			if(isValid){
				var value = $("#aap_form [name=id]").val();
				var fromData = $("#aap_form").serialize();
				if(value) {
					$.post("/sim/monitor/editAlarmPolicy", fromData, function(result){
						if(result.status){
							$("#"+mnt.currentOpenDialog).dialog("close") ;
							getTabElement("adt_monitor").panel("refresh") ;
						} else {
							showErrorMessage(result.message) ;
						}
					},"json") ;
				} else {
					$.post("/sim/monitor/addAlarmPolicy", fromData, function(result){
						if(result.status) {
							$("#"+mnt.currentOpenDialog).dialog("close") ;
							getTabElement("adt_monitor").panel("refresh") ;
						} else {
							showErrorMessage(result.message) ;
						}
					},"json") ;
				}
			}
		});
	});
</script>
