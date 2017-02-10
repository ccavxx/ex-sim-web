<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.topsec.tsm.sim.util.TalVersionUtil"%>

<%
         String talVersion=TalVersionUtil.getInstance().getVersion().toUpperCase();
         String normVer=TalVersionUtil.TAL_VERSION_STANDARD.toUpperCase();
         String simVer=TalVersionUtil.TAL_VERSION_SIM.toUpperCase();
 %>
<script type="text/javascript">
var checkPackUpLocalStr = true;
var checkFtpPackUpLocalStr = true;
var checkFtpPackUpLocalStrValue = true;
function validateHtmlConfigForSubmit() {

}
/*针对不同类别的数据校验*/
function validateFormForHtmlConfig(type, notNull, minValue, maxValue, name, key) {
	var obj = document.getElementsByName(key)[0];
	obj.id = obj.name;//赋于id的值为name
	hideErr(key);//去除上次校验错误ui
	
	if (document.getElementsByName(key)[0].disabled == true) {//是否能用
		return true;
	}
	var tempmark = 0;
	
	if(Trim(document.getElementsByName(key)[0].value) =="" && notNull=='false')
		return true;
	if (notNull == 'true') {//是否允许为空
		if (!isNotEmpty(document.getElementsByName(key)[0]) || isAllBlank(document.getElementsByName(key)[0])) {
			//var receivers = document.getElementsByName("sys_cfg_sendlog.sendlog.receivers");
			//var send = document.getElementsByName("sys_cfg_sendlog.sendlog.send");
			//if (receivers && receivers[0] && !isNotEmpty(receivers[0].value) && send && send[0] && send[0].value == 'false') {
			//	tempmark = 1;
			//} else {
				showErr(key, name + "不能为空");
				return false;
			//}
		}

		//以下主要是判断textArea不能为空
		var textAreaVal = obj.value;//文本域输入框，输入不能为空
		var val = obj.value;
		while (textAreaVal.indexOf("\r\n") >= 0) {
			textAreaVal = textAreaVal.replace("\r\n", "");//如果有换行，则替换成空
		}
		if (Trim(textAreaVal).length == 0) {
			//showErr(key, name + "不能为空");
			obj.value = "";
			//return false;
		} 
     if(key=="resp_cfg_mail.msgnotify.content"){//邮件告警
        var res = true;
        var contentVal = obj.value;
        for ( var i = 0; i < contentVal.length; i++) {
          var c = contentVal.charAt(i);
                c=c.replace(/[\r\n]/g,"");//去掉回车换行
          if (c != "") {
            res = false;
            break;
          }
       }
      if(res){
      	obj.value = "";
      }
    }

	}

	switch (type) {

	case 'string': {
		if (ChkHtmlCharForConfig(document.getElementsByName(key)[0].value) && key != "sys_cfg_mailserver.mailserver.loginaccount") {
			showErr(key, name + "<tsm:i18n key='lable.config.notInvalidChar'/>");
			return false;
		}
		if (!validateMinLength(document.getElementsByName(key)[0], minValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.minLen'/>" + minValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		if (!validateLength(document.getElementsByName(key)[0], maxValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.maxLen'/>" + maxValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		break;
	}
	case 'special_str': {
		if (!validateMinLength(document.getElementsByName(key)[0], minValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.minLen'/>" + minValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		if (!validateLength(document.getElementsByName(key)[0], maxValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.maxLen'/>" + maxValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		if (name == "<tsm:i18n key='lable.config.checkPath'/>") {
			if (checkPackUpLocalStr == false) {
				return false;
			}
			hideErr(key);
			return true;
		}
		break;
	}
	case 'num': {
		if (key == 'sys_cfg_backup_auto.autoback.partitionCount') {
			if (document.getElementsByName('sys_cfg_backup_auto.autoback.autobackManner')[0].value == 'lastday') {
				if (obj.value <= 1) {
					showErr(key, name + "输入的事件天数要大于备份周期!");
					return false;
				}
			}
			if (document.getElementsByName('sys_cfg_backup_auto.autoback.autobackManner')[0].value == 'lastweek') {
				if (obj.value <= 7) {
					showErr(key, name + "输入的事件天数要大于备份周期!");
					return false;
				}
			}
			if (document.getElementsByName('sys_cfg_backup_auto.autoback.autobackManner')[0].value == 'lastmonth') {
				if (obj.value <= 31) {
					showErr(key, name + "输入的事件天数要大于备份周期!");
					return false;
				}
			}
		}
		if (document.getElementsByName(key)[0].value.length > 1) {
			if (Trim(document.getElementsByName(key)[0].value).charAt(0) == 0 || Trim(document.getElementsByName(key)[0].value).charAt(0) == "0") {
				showErr(key, name + "输入有误");
				return false;
			}
		}
		if (!isInteger(Trim(document.getElementsByName(key)[0].value))) {//只能是整数、先去除两边空格
			showErr(key, name + "<tsm:i18n key='lable.config.onlyInteger'/>");
			return false;
		}

		if ((parseInt(document.getElementsByName(key)[0].value) < parseInt(minValue))) {
			showErr(key, name + "<tsm:i18n key='lable.config.mustMoreOrEqual'/>" + minValue);
			return false;
		}

		if (!(maxValue == 2147483647)) {
			if ((parseInt(document.getElementsByName(key)[0].value) > parseInt(maxValue))) {
				showErr(key, name + "<tsm:i18n key='lable.config.mustLessOrEqual'/>" + maxValue);
				return false;
			}
		}
		break;
	}
	case 'float': {
		if (!checkFloat(document.getElementsByName(key)[0].value)) {
			showErr(key, name + "<tsm:i18n key='lable.config.onlyFloat'/>");
			return false;
		}

		if ((parseFloat(document.getElementsByName(key)[0].value) < parseFloat(minValue))) {
			showErr(key, name + "<tsm:i18n key='lable.config.mustMoreOrEqual'/>" + minValue);
			return false;
		}

		if ((parseFloat(document.getElementsByName(key)[0].value) > parseFloat(maxValue))) {
			showErr(key, name + "<tsm:i18n key='lable.config.mustLessOrEqual'/>" + maxValue);
			return false;
		}
		break;
	}
	case 'phone': {
		if (!checkPhoneNumber(document.getElementsByName(key)[0].value)) {
			showErr(key, name + "<tsm:i18n key='lable.config.notValidPhoneNum'/>");
			return false;
		}

		if ((parseInt(document.getElementsByName(key)[0].value) < parseInt(minValue))) {
			showErr(key, name + "<tsm:i18n key='lable.config.mustMoreOrEqual'/>" + minValue);
			return false;
		}

		if ((parseInt(document.getElementsByName(key)[0].value) > parseInt(maxValue))) {
			showErr(key, name + "<tsm:i18n key='lable.config.mustLessOrEqual'/>" + maxValue);
			return false;
		}
		break;
	}
	case 'ip': {
	    hideErr(key);
		if (!checkip(document.getElementsByName(key)[0])) {
			//if (tempmark == 1) {
			//} else {
				showErr(key, "<tsm:i18n key='lable.config.inputIPIsInvalid'/>");
				document.getElementsByName(key)[0].select();
				return false;
			//}
		}
		break;
	}

	case 'mail': {
		if (!validateMinLength(document.getElementsByName(key)[0], minValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.minLen'/>" + minValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		if (!validateLength(document.getElementsByName(key)[0], maxValue)) {
			showErr(key, name + "<tsm:i18n key='lable.config.maxLen'/>" + maxValue + "<tsm:i18n key='lable.config.char'/>");
			return false;
		}
		if (!checkEmail(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notValidEmail'/>");
			return false;
		}
		break;
	}
	case 'boolean': {
		if (!checkBoolean(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notTrueOrFalse'/>");
			return false;
		}
		break;
	}
	}
	hideErr(key);
	if (obj.value != null) {
		obj.value = Trim(obj.value);//去除两边的空白字符
	}

	return true;
}

/**********************************************************/

function validateFormForHtmlConfigItemType(type, name, key) {
	var obj = document.getElementsByName(key)[0];
	obj.id = obj.name;
	hideErr(key);
	if (document.getElementsByName(key)[0].disabled == true) {
		return true;
	}
	switch (type) {

	case 'string': {
		if (ChkHtmlCharForConfig(document.getElementsByName(key)[0].value)) {
			showErr(key, name + "<tsm:i18n key='lable.config.notInvalidChar'/>");
			return false;
		}
		break;
	}
	case 'special_str': {
		break;
	}
	case 'num': {
		if (!isInteger(document.getElementsByName(key)[0].value)) {
			showErr(key, "<tsm:i18n key='lable.config.onlyInteger'/>");
			return false;
		}
		break;
	}
	case 'float': {
		if (!checkFloat(document.getElementsByName(key)[0].value)) {
			showErr(key, name + "<tsm:i18n key='lable.config.onlyFloat'/>");
			return false;
		}
		break;
	}
	case 'phone': {
		if (!checkPhoneNumber(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notValidPhoneNum'/>");
			return false;
		}
		break;
	}
	case 'ip': {
		if (!checkip(document.getElementsByName(key)[0])) {
			showErr(key, "<tsm:i18n key='lable.config.inputIPIsInvalid'/>");
			document.getElementsByName(key)[0].select();
			return false;
		}
		break;
	}

	case 'mail': {
		if (!checkEmail(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notValidEmail'/>");
			//alert();
			return false;
		}
		break;
	}
	case 'boolean': {
		if (!checkBoolean(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notTrueOrFalse'/>");
			return false;
		}
		break;
	}
	}
	return true;
}
/****************************/

function validateFormForHtmlConfigListInput(notNull, name, key) {
	var obj = document.getElementsByName(key)[0];
	obj.id = obj.name;
	hideErr(obj.id);
	if (document.getElementsByName(key)[0].disabled == true) {
		return true;
	}
	if (notNull == 'true') {
		if (document.getElementsByName(key)[0].length <= 0) {
			showErr(key, name + "<tsm:i18n key='lable.config.notNull'/>");
			return false;
		}
	}
	return true;
}

function validateFormForHtmlConfigSelectInTextAreaItem(notNull, name, key) {
	var obj = document.getElementsByName(key)[0];
	//obj.id = obj.name;
	hideErr(obj.name);
	if (document.getElementsByName(key)[0].disabled == true) {
		return true;
	}
	if (notNull == 'true') {
		if (document.getElementsByName(key)[0].value.length <= 0) {
			showErr(key, name + "<tsm:i18n key='lable.config.notNull'/>");
			return false;
		}
	}
	return true;
}

function validateFormForHtmlConfigButton(notNull, name, key) {
	// document.getElementsByName(key)[0].id=document.getElementsByName(key)[0].name;
	// var id=document.getElementsByName(key)[0].id;
	// hideErr(id);
	// alert(id);
	if (document.getElementsByName(key)[0].disabled == true) {
		// hideErr(id);
		return true;
	}
	if (notNull == 'true') {
		if (!isNotEmpty(document.getElementsByName(key)[0]) || isAllBlank(document.getElementsByName(key)[0])) {
			if (ymPrompt) {
				ymPrompt.alert(name + "<tsm:i18n key='lable.config.notNull'/>", null, null, "提示");
			} else {
				alert(name + "<tsm:i18n key='lable.config.notNull'/>");
			}
			
			// showErr(id, name + "<tsm:i18n key='lable.config.notNull'/>");
			return false;
		}
	}
	//hideErr(id);
	return true;
}

/*snmp 字符 校验*/
function validateFormForHtmlConfigPassword(notNull, minValue, maxValue, name, key) {
	var obj = document.getElementsByName(key)[0];
	obj.id = key;
	hideErr(key);
	if (document.getElementsByName(key)[0].disabled == true) {
		return true;
	}


	if(Trim(document.getElementsByName(key)[0].value) =="" && notNull=='false'){
		return true;
	}
	
	if (notNull == 'true') {
		if (!isNotEmpty(document.getElementsByName(key)[0]) || isAllBlank(document.getElementsByName(key)[0])) {
			showErr(key, name + "<tsm:i18n key='lable.config.notNull'/>");
			//alert(name + "<tsm:i18n key='lable.config.notNull'/>");
			return false;
		}
		if (key == "sys_cfg_backup.ftp.password") {
			
			if (checkFtpPackUpLocalStr == false) {
				//showErr(key, "FTP服务器不正确,请核实");
				return false;
			}					
			return true;
		}
	}
	if (!validateMinLength(document.getElementsByName(key)[0], minValue)) {
		//alert(name + "<tsm:i18n key='lable.config.minLen'/>" + minValue + "<tsm:i18n key='lable.config.char'/>");
		showErr(key, "<tsm:i18n key='lable.config.minLen'/>" + minValue + "<tsm:i18n key='lable.config.char'/>");
		return false;
	}
	if (!validateLength(document.getElementsByName(key)[0], maxValue)) {
		//alert(name + "<tsm:i18n key='lable.config.maxLen'/>" + maxValue + "<tsm:i18n key='lable.config.char'/>");
		showErr(key, "<tsm:i18n key='lable.config.maxLen'/>" + maxValue + "<tsm:i18n key='lable.config.char'/>");
		return false;
	}
}
function existInput(x, baseName) {
	for ( var j = 0; j < $j("select[name="+baseName+"listInput]").children("option").size(); j++) {
		if (x.value == $j("select[name="+baseName+"listInput]").children("option")[j].text)
			return true;
	}
	return false;
}
function existPortInput(x) {
	if (document.getElementsByName('sys_cfg_port.syslog.' + 'listInput').length !=0) {
		for ( var j = 0; j < document.getElementsByName('sys_cfg_port.syslog.' + 'listInput')[0].options.length; ++j) {
			if (x.value == document.getElementsByName('sys_cfg_port.syslog.' + 'listInput')[0].options[j].text)
				return true;
		}
	}

	if (document.getElementsByName('sys_cfg_port.snmp.' + 'listInput').length !=0) {
		for ( var j = 0; j < document.getElementsByName('sys_cfg_port.snmp.' + 'listInput')[0].options.length; ++j) {
			if (x.value == document.getElementsByName('sys_cfg_port.snmp.' + 'listInput')[0].options[j].text)
				return true;
		}
	}

	if (document.getElementsByName('sys_cfg_port.netflow.' + 'listInput').length !=0) {
		for ( var j = 0; j < document.getElementsByName('sys_cfg_port.netflow.' + 'listInput')[0].options.length; ++j) {
			if (x.value == document.getElementsByName('sys_cfg_port.netflow.' + 'listInput')[0].options[j].text)
				return true;
		}
	}
	return false;
}

/*email校验*/
function addInputValid(name, type, maxValue, baseName) {
	var inputForList = document.getElementsByName(baseName + 'inputForList')[0];
	var key = inputForList.name;
	inputForList.id = key;
	hideErr(key);
	inputForList.value = Trim(inputForList.value);
	if (!isNotEmpty(inputForList) || isAllBlank(inputForList)) {
		showErr(key, name + "<tsm:i18n key='lable.config.notNull'/>");
		return false;
	}
     if (baseName == 'sys_cfg_port.syslog.' || baseName == 'sys_cfg_port.snmp.' || baseName == 'sys_cfg_port.netflow.') {
       var val=inputForList.value;
       if(val.lastIndexOf('.0')!=-1){
        val=val.substr(0,val.lastIndexOf('.0'))
        inputForList.value=val;
      }
       if(!isInteger(val)){
        showErr(key, name + "取值必须为1~"+maxValue+"之间的整数！");
        return false;
       }
       if(parseInt(val)<1||parseInt(val)>parseInt(maxValue)){
         
        showErr(key, name + "取值必须为1~"+maxValue+"之间的整数！");
        return false;
       }
     }
  
	if (!validateFormForHtmlConfigItemType(type, name, baseName + 'inputForList')) {
		return false;

	}
	if (existInput(inputForList, baseName)) {
		showErr(key, inputForList.value + "<tsm:i18n key='lable.config.isExist'/>");
		return false;
	}
	if (existPortInput(inputForList)) {
		showErr(key, "<tsm:i18n key='lable.config.isExist'/>");
		return false;
	}
	if (parseInt(inputForList.value) > parseInt(maxValue) && (baseName == 'sys_cfg_port.syslog.' || baseName == 'sys_cfg_port.snmp.' || baseName == 'sys_cfg_port.netflow.')) {
		showErr(key, name + "超过最大范围值" + maxValue);
		return false;
	}
	if (parseInt(inputForList.value) < 1 && (baseName == 'sys_cfg_port.syslog.' || baseName == 'sys_cfg_port.snmp.' || baseName == 'sys_cfg_port.netflow.')) {
		showErr(key, name + "<tsm:i18n key='lable.config.mustMoreOrEqual'/>" + 1);
		return false;
	}
	if ((document.getElementsByName(baseName + 'listInput')[0].options.length + 1) > maxValue) {
		showErr(key, "<tsm:i18n key='lable.config.canChoose'/>" + name + "<tsm:i18n key='lable.config.reachMaxNum'/>");
		return false;
	}
	//var x = new Option(inputForList.value, inputForList.value, false, true);
	//document.getElementsByName(baseName + 'listInput').add(x);
    $j("select[name="+baseName+"listInput]").append("<option value='"+inputForList.value+"'>"+inputForList.value+"</option>");
	inputForList.value = "";
	return false;

}
function delInputValid(name, minValue, baseName) {
	if (document.getElementsByName(baseName + 'listInput').selectedIndex < 0) {
		if (ymPrompt) {
			ymPrompt.alert("<tsm:i18n key='lable.config.mustSelectToDel'/>" + name, null, null, "提示");
		} else {
			showAlertMessage("<tsm:i18n key='lable.config.mustSelectToDel'/>" + name);
		}
		return;
	}
          var selectedLen=$j("select[name="+baseName+"listInput]").children("option:selected").length;//选择要删除的端口
          if(selectedLen>0){
			ymPrompt.confirmInfo("<tsm:i18n key='label.common.confirmdel'/>", null, null, "删除提示", function(res) {
			
				if (res == "ok") {
						$j("select[name="+baseName+"listInput]").children("option:selected").remove();
	                    
				} else {
					return;
				}
			});
          }else{
            if(baseName=="resp_cfg_mail.mailreceivers\." || baseName== "schedule_cfg_sysreport.reportinfo."){
			   ymPrompt.alert("请选择要删除的邮箱！", null, null, "提示");
            }else{
			   ymPrompt.alert("请选择要删除的端口！", null, null, "提示");
            }
          }
}
function setSelectListInput(name, selName) {
	var selectListInput = '';
	for ( var i = 0; i < $j("select[name='"+name+"']").children("option").size(); i++) {

		selectListInput = selectListInput + ' ' + $j("select[name='"+name+"']").children("option")[i].value;

	}

	document.getElementsByName(selName)[0].value = selectListInput;
}
function checkRadio(name, blocks) {
	var checkRadio = document.getElementsByName(name);
	for ( var i = 0; i < checkRadio.length; i++) {

		if (checkRadio[i].checked == true) {

			document.getElementById(blocks[i]).disabled = false;
			var disabledDiv = document.getElementById(blocks[i]);
			for ( var j = 0; j < disabledDiv.childNodes[0].rows.length; j++) {
				if (disabledDiv.childNodes[0].rows[j].cells[1].childNodes[0].disabled == true) {

					disabledDiv.childNodes[0].rows[j].cells[1].childNodes[0].disabled = false;

				}
			}
		} else {
			if (isDistributed == "true") {//如果是多极，则不会显示“本地路径”
				document.getElementById(blocks[i]).parentNode.style.display = "none";
			}

			document.getElementById(blocks[i]).disabled = true;
			var disabledDiv = document.getElementById(blocks[i]);
			for ( var j = 0; j < disabledDiv.childNodes[0].rows.length; j++) {
				disabledDiv.childNodes[0].rows[j].cells[1].childNodes[0].disabled = true;

			}
		}
	}
	//////
	if (document.getElementById("sys_cfg_backup.ftpBlockDiv") != null) {
		if (document.getElementsByName("backuppath")[0].checked) {
			document.getElementById('submitId').onclick = function() {
				checkPackUpLocalStr = false;

				if (document.getElementsByName('sys_cfg_backup.local.path')[0].value != "") {
					if (document.getElementsByName("backuppath")[0].checked) {
						document.getElementById('loadingImgWapper1').style.display = 'block';
						checkFileExits(document.getElementsByName('sys_cfg_backup.local.path')[0]);
					}
				}
			};
		}
	}
	if (document.getElementById("sys_cfg_backup.ftpBlockDiv") != null) {
		if (document.getElementsByName("backuppath")[1].checked) {
			document.getElementById('submitId').onclick = function() {
				checkFtpPackUpLocalStr = false;
				if (document.getElementsByName('sys_cfg_backup.ftp.serverip')[0].value != "" && document.getElementsByName('sys_cfg_backup.ftp.password')[0].value != ""
						&& document.getElementsByName('sys_cfg_backup.ftp.user')[0].value != "") {
					if (document.getElementsByName("backuppath")[1].checked) {
						checkFtpFileExits(document.getElementsByName('sys_cfg_backup.ftp.password')[0]);
					}
				}

			};
		}
	}
}

//ajax

function checkBackupPath(obj) {
	if (obj.name != null) {
		if (obj.name == "sys_cfg_store.archive.backup") {
			if (obj.value == "true") {
				var url = "<%=request.getContextPath()%>/sim/report/mgrPlanReportAction.do?method=checkExistPathValue";
				url = encodeURI(url);
				Request.sendGET(url, checkBackupPathCallBack);
			}
		}
	}
}
function checkBackupPathCallBack(response) {
	var value = response.responseText;
	if (value == "yes") {
		return true;
	} else {
		if (ymPrompt) {
			ymPrompt.alert("备份路径识别失败，请核实日志备份路径是否正确", null, null, "提示");
		} else {
			showAlertMessage("备份路径识别失败，请核实日志备份路径是否正确");
		}
		return false;
	}
}
$(function(){
         var talVersion='<%=talVersion%>';
         var normVer='<%=normVer%>';
   var tipcontent;
   tipcontent = "备份路径不能与日志存储路径在同一磁盘分区上。";
   setQtip2($("input[name='sys_cfg_backup\.local\.path']"),tipcontent);
   //系统配置--邮件服务器
   tipcontent = "设置邮件服务器IP地址。";
   setQtip2($("input[name='sys_cfg_mailserver\.mailserver\.serverip']"),tipcontent);
   
   tipcontent = "设置邮件服务器端口。";
   setQtip2($("input[name='sys_cfg_mailserver\.mailserver\.serverport']"),tipcontent);
   
   tipcontent = "设置发信方的Email地址。";
   setQtip2($("input[name='sys_cfg_mailserver\.mailserver\.mailsender']"),tipcontent);
   
   tipcontent = "发信方登录邮件系统的用户名。";
   setQtip2($("input[name='sys_cfg_mailserver\.mailserver\.loginaccount']"),tipcontent);
   
   tipcontent = "发信方登录邮件系统的口令。";
   setQtip2($("input[name='sys_cfg_mailserver\.mailserver\.loginpwd']"),tipcontent);
   
   //系统配置--备份周期
   if(talVersion != normVer){
      tipcontent = "设置自动备份的范围，支持备份1-6个月前的日志和事件,1年前的日志和事件。所有的备份都是指从备份开始时间向前推算。";
   }else{
      tipcontent = "设置自动备份的范围，支持备份1-6个月前的日志,1年前的日志。所有的备份都是指从备份当前时间向前推算。";
   }
   
   if($j("select[name='sys_cfg_backup_auto\.autoback\.autobackManner']").html() !=null){
      $j("select[name='sys_cfg_backup_auto\.autoback\.autobackManner']").qtip({
			content : tipcontent,
			position : {
				corner : {
                    target: 'middleRight',
                    tooltip: 'topLeft'
				}
			},
			style : {
			    width : 300,
				name : 'cream'
			}
	    });
   }
   
   tipcontent = "设置是否启用该自动备份策略。";
   setQtip2($("select[name='sys_cfg_backup_auto\.autoback\.enable']"),tipcontent);
   
   tipcontent = "设置安全事件在服务器中保留的天数。默认保留6个月。";
   setQtip2($("#sys_cfg_backup_auto\.autoback\.partitionCount"),tipcontent);
   
   //系统配置--索引策略
   tipcontent = "设置是否实时索引当天日志。如果设置成不自动索引，第一次查询该日志时会自动触发建立索引。当存储空间达到磁盘使用率阈值时，将停止自动索引。";
   setQtip2($("#sys_cfg_index\.index\.realtimeindex"),tipcontent);
   
   tipcontent = "设置索引保存的时间。超过设定时间没有使用的索引会被删除，释放磁盘空间。下一次查询该日志时，会自动触发建立索引。";
   setQtip2($("#sys_cfg_index\.index\.longevity"),tipcontent);
   
   tipcontent = "设置索引压缩时间。超过设定时间没有使用的索引会被压缩，释放磁盘空间，下一次查询时会自动解压激活。";
   setQtip2($("#sys_cfg_index\.index\.archive"),tipcontent);
   
   //系统配置--日志存储策略
   tipcontent = "为了避免自审计日志在系统中保存时间过久而造成占用系统大量磁盘空间的问题，该选项将只保留指定时间范围内的日志文件，超过时间范围内的日志如果没有在使用将会被系统自动删除，请谨慎操作。";
      if($j("#sys_cfg_store\.archive\.systemlongevity").html() !=null){
      $j("#sys_cfg_store\.archive\.systemlongevity").qtip({
			content : tipcontent,
			position : {
				corner : {
                    target: 'middleRight',
                    tooltip: 'topLeft'
				}
			},
			style : {
			    width : 400,
				name : 'cream'
			}
	    });
   }
   
   tipcontent = "设置日志的存储路径，由于存储日志需要大量磁盘空间，所以需要配置到大容量硬盘分区上，请谨慎操作。日志存储路径不能与备份路径在同一磁盘分区上。";
   setQtip2($("input[name='sys_cfg_store\.archive_path\.archive_path']"),tipcontent);
   
   tipcontent = "设置存储路径所在的磁盘使用率的告警上限。超过此使用率时，会产生告警。";
   setQtip2($("input[name='sys_cfg_store\.archive_path\.alert']"),tipcontent);
   
   tipcontent = "设置存储路径所在的磁盘使用率。超过此阀值时，将会删除最早的外部日志，请慎重操作。";
   setQtip2($("input[name='sys_cfg_store\.archive_path\.override']"),tipcontent);
   
   //系统配置--报表存储策略
   tipcontent = "该选项用来设置系统报表数据的保存期限，超过保存期限的统计数据将被自动删除。";
   setQtip2($("#sys_cfg_report\.reportcfg\.systemreport"),tipcontent);
   
   tipcontent = "该选项用来设置主动日志报表数据的保存期限，超过保存期限的统计数据将被自动删除。";
   setQtip2($("#sys_cfg_report\.reportcfg\.activereport"),tipcontent);
   
   tipcontent = "该选项用来设置被动日志报表数据的保存期限，超过保存期限的统计数据将被自动删除。";
   setQtip2($("#sys_cfg_report\.reportcfg\.passivereport"),tipcontent); 
   
   //系统配置--Syslog日志转发  
   tipcontent = "设置日志转发ip地址，只支持一个。";
   setQtip2($("input[name='sys_cfg_sendlog\.sendlog\.receivers']"),tipcontent); 
   tipcontent = "设置转发端口，默认端口514。";
   setQtip2($("input[name='sys_cfg_sendlog\.sendlog\.port']"),tipcontent);  
   tipcontent = "设置每秒钟转发日志的最大条数，范围：1-1000。";
   setQtip2($("input[name='sys_cfg_sendlog\.sendlog\.frequency']"),tipcontent); 
   tipcontent = "设置转发状态，包括：停止、启动。";
   setQtip2($("#sys_cfg_sendlog\.sendlog\.send"),tipcontent);
   
   //系统配置--JMS日志转发  
   tipcontent = "设置日志转发ip地址。";
   setQtip2($("input[name='sys_cfg_jmssendlog\.jmssendlog\.receivers']"),tipcontent); 
   
   tipcontent = "设置转发端口，默认端口61616。";
   setQtip2($("input[name='sys_cfg_jmssendlog\.jmssendlog\.port']"),tipcontent);  
   
   tipcontent = "设置JMS消息的Topic";
   setQtip2($("input[name='sys_cfg_jmssendlog\.jmssendlog\.topic']"),tipcontent);  
   
   tipcontent = "设置转发时是否加密。";
   setQtip2($("#sys_cfg_jmssendlog\.jmssendlog\.encrypt"),tipcontent);
   
   tipcontent = "设置每秒钟转发日志的最大条数，范围：1-5000。";
   setQtip2($("input[name='sys_cfg_jmssendlog\.jmssendlog\.frequency']"),tipcontent);
   
   tipcontent = "设置转发状态，包括：停止、启动。";
   setQtip2($('#sys_cfg_jmssendlog\.jmssendlog\.send'),tipcontent);
   

   if($j("#sys_cfg_sendlog\.sendlogBlockDiv").html()!=null){
        tipcontent = "设置要转发的Syslog日志的过滤条件。";
        setQtip2($("#filterSql"),tipcontent); 
   }else if($j("#sys_cfg_jmssendlog\.jmssendlogBlockDiv").html()!=null){
       tipcontent = "设置要转发的JMS日志的过滤条件。";
       setQtip2($("#filterSql"),tipcontent);      
   }
   
   
       
});
</script>