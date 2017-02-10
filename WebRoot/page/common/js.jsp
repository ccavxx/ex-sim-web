<script type="text/javascript">
var objectPath = "<%=contextPath%>";
var themeName = "<%=themeName%>";
var imgPath = objectPath+"/theme/"+themeName+"/images";
var publicImgPath = objectPath + "/theme/public_element";
var _system_comfirm_title="<tsm:i18n key="message.confirm.title"/>";
var _system_comfirm_msg="<tsm:i18n key="message.confirm.msg"/>";
var _system_alert_msg="<tsm:i18n key="message.alert.msg"/>";
var _system_alert_title="<tsm:i18n key="message.alert.title"/>";
var _system_label_show="<tsm:i18n key="label.common.show"/>";
var _system_label_hide="<tsm:i18n key="label.common.hide"/>";
var error_canNotBeSpace="<tsm:i18n key="error.common.canNotBeSpace"/>";  
var confirm_intercept="<tsm:i18n key="error.common.intercept"/>";
var error_ipBound="<tsm:i18n key="error.common.invalidIPAddress"/>,<tsm:i18n key="error.common.ipBound"/>";
var error_ipNotAllZero="<tsm:i18n key="error.common.invalidIPAddress"/>,<tsm:i18n key="error.common.ipNotAllZero"/>";
var error_ipFormat="<tsm:i18n key="error.common.invalidIPAddress"/>,<tsm:i18n key="error.common.ipFormat"/>";
var error_lawlessLetter="<tsm:i18n key="error.common.invalidIPAddress"/>,<tsm:i18n key="error.common.lawlessLetter"/>";
var error_canNotNull="<tsm:i18n key="error.common.canNotBeNull"/>";
var error_invalid="<tsm:i18n key="error.common.invalid"/>,<tsm:i18n key="error.common.lawlessLetter"/>";
var TAL_VERSION_ENTERPRISE="ENTERPRISE";
var TAL_VERSION_STANDARD="STANDARD";
var TAL_VERSION_SIMPLE="SIMPLE";
var TAL_VERSION_SIM="SIM";

/* modify by yangxuanjia at 2011-06-16 start */
var error_ip_invalidIPAddress="<tsm:i18n key="error.common.invalidIPAddress"/>";
var error_common_message_shouldLess="<tsm:i18n key="error.common.shouldLess"/>";
var error_common_message_chars="<tsm:i18n key="error.common.chars"/>";
/* modify by yangxuanjia at 2011-06-16 end */


</script>
<script type="text/javascript" src="/js/global/jquery-1.8.3.min.js"></script>

<script type="text/javascript">
var $j = jQuery;
</script>
<script type="text/javascript" src="/page/report/js/common.js"></script>
<script type="text/javascript" src="/page/report/js/ymPrompt.js"></script>
<script type="text/javascript" src="/page/report/js/winOpen.js"></script>
<script type="text/javascript" src="/page/report/js/form.js"></script>


<script type="text/javascript">

var tokenStr = "<%=session.getAttribute("org.apache.struts.action.TOKEN")%>";
var org_apache_struts_action_TOKEN="&org.apache.struts.taglib.html.TOKEN="+"<logic:present name='org.apache.struts.action.TOKEN' scope='session'><bean:write name='org.apache.struts.action.TOKEN' scope='session'/></logic:present>"; 

function getToken(){
	if(tokenStr){
		var formNodes = document.getElementsByTagName("form");
		var hiddenNode = document.createElement("input");
		hiddenNode.setAttribute("type","hidden");
		hiddenNode.setAttribute("value",tokenStr);
		hiddenNode.setAttribute("name","org.apache.struts.taglib.html.TOKEN");
		for(var i=0;i<formNodes.length;i++){
			formNodes[i].appendChild(hiddenNode);
		}
	}
}



$j(function(){
	getToken();
});
$j(function(){
   if(navigator.appVersion.indexOf("MSIE 9.0") !=-1){
     $j("legend").css({"margin-right": "100%"});
   }
});
       
</script>