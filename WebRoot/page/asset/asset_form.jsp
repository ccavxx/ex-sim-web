<%@ page language="java" pageEncoding="utf-8"%>
<!-- 本页面组件id命名全部使用前辍af(asset form)统一命名 -->
<form id="af_assetForm" class="form-horizontal hgap3" style="margin-top: 5px;">
  <input type="hidden" name="id" value="${id}">
  <input type="hidden" name="operation" value="${param.operation}">
  <div class="control-group">
    <label class="control-label" for="af_ip"><span style="color:red;">*</span>IP地址</label>
    <div class="controls">
      <input id="af_ip" name="ip" type="text" value="${asset.masterIp}" ${param.operation eq "edit" ? "readonly" : ""} placeholder="IP地址">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="name"><span style="color:red;">*</span>资产名称</label>
    <div class="controls">
      <input type="text" id="af_name" name="name" value="${asset.name}" placeholder="资产名称">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_deviceType"><span style="color:red;">*</span>资产类型</label>
    <div class="controls">
      <select id="af_deviceType" name="deviceType" ${param.operation eq "edit" ? "readonly" : ""} 
      		  selectValue="${asset.deviceType}" selectText="${asset.deviceTypeName}"
      		  class="easyui-combotree"
      		  data-options="height:24,width:200,url:'/sim/asset/assetCategories',onBeforeSelect:al.beforeDeviceTypeSelect,onSelect:al.onDeviceTypeSelect,
      		  onLoadSuccess:al.setDeviceTypeSelect,onHidePanel:al.resetDeviceTypeText"/>
      <input type="hidden" id="af_deviceTypeName" name="deviceTypeName" value="${asset.deviceTypeName}">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_scanNodes"><span style="color:red;">*</span>管理节点</label>
    <div class="controls">
        <select id="af_scanNodes" class="easyui-combobox" selectValue="${asset.scanNodeId}"
      		  data-options="height:24,width:200,editable:false,url:'/sim/node/allNode',textField:'ip',valueField:'nodeId',onLoadSuccess:al.setSelect,onChange:al.setChangeValue"/>
    	<input type="hidden" id="hid_af_scanNodes" name="nodeId" value="${asset.scanNodeId}"/>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_state">状　　态</label>
    <div class="controls">
		<label class="checkbox" style="display:inline-block;width:50px;">
		  <input type="checkbox" name="state" value="1" ${empty asset ? 'checked' : (asset.enabled==1 ? 'checked' : '')} />　启用
		</label>
		<span class="msg-box" for="state"></span>
     </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_accountName">管理账号</label>
    <div class="controls">
    	<input type="text" id="af_accountName" name="accountName" autocomplete="off" value="${asset.accountName}" placeholder="管理账号">
    </div>
  </div> 
  <div class="control-group">
    <label class="control-label" for="af_accountPassword">管理密码</label>
    <div class="controls">
    	<input type="password" id="af_accountPassword" name="accountPassword" autocomplete="off" value="" placeholder="管理密码">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_confirmPassword">确认密码</label>
    <div class="controls">
    	<input type="password" id="af_confirmPassword" name="af_confirmPassword" value="" placeholder="确认密码">
    </div>
  </div>         
  <div class="control-group">
    <label class="control-label" for="os">操作系统</label>
    <div class="controls">
      	<select id="af_os" name="osName" class="easyui-combobox" selectValue="${asset.os.osName}"
    		data-options="height:24,width:200,editable:false,url:'/sim/asset/osList',valueField:'value',textField:'value',onLoadSuccess:al.setSelect"></select>
    </div>
  </div> 
  <div class="control-group">
    <label class="control-label" for="af_hostName">主机名</label>
    <div class="controls">
      <input type="text" id="af_hostName" name="hostName" value="${asset.hostName}" placeholder="主机名">
    </div>
  </div>      
  <div class="control-group">
    <label class="control-label" for="safeRank">安全等级</label>
    <div class="controls">
        <select id="af_safeRank" class="easyui-combobox" name="safeRank" selectValue="${asset.safeRank}"
      		  data-options="height:24,width:200,editable:false,url:'/sim/asset/safeRankList',valueField:'value',textField:'value',panelHeight:80,onLoadSuccess:al.setSelect""></select>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="af_linkman">联系人</label>
    <div class="controls">
      <input type="text" id="af_linkman" name="linkman" value="${asset.linkman}" placeholder="联系人">
    </div>
  </div>
</form>
<script>
$(function(){
	//初始化表单验证组件，并创建表单验证实例
	$("#af_accountPassword,#af_confirmPassword").val(rsaDecrypt("${accountPassword}")) ;
	al.asset_operate_form_validator = $('#af_assetForm').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			ip:'required;ipv4;remote[/sim/assetlist/checkUniqueIp, ip, id]',
			name:'required;length[1~30];remote[/sim/assetlist/checkUniqueName, id]',
			deviceTypeName:'required;',
			nodeId:'required;',
			state:'required;',
			accountName:'length[~50];',
			accountPassword:'管理密码:length[~50];',
			af_confirmPassword:'确认密码:match(accountPassword);',
			hostName:'length[~30]',
			linkman:'length[~10]'
		},
		valid : function(form){
			$("#al_asset_form > .dialog-button > .l-btn").first().linkbutton("disable");
			var formData = $("#af_assetForm").serializeArray();
			$.each(formData,function(index,inputItem){
				if(inputItem.name == "accountPassword" || inputItem.name == "af_confirmPassword"){
					inputItem.value = rsaEncrypt(inputItem.value) ;
				}
			}) ;
			var data = $.param(formData) ;
			$.post("/sim/assetlist/saveAsset",data,al.addResultHandler,"json") ;
		}
	}).data("validator");
})
</script>