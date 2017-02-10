$(function(){
	var sys_cfg_mailserver_id;
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey:'sys_cfg_mailserver',nodeType:'SMP'},function(res){
		if(res && res.status){
			var data = res.result;
			data.mailserver.loginpwd = rsaDecrypt(data.mailserver.loginpwd) ;
			sys_cfg_mailserver_id = data.responseId;
			$('#sysconfig_mail_form').form('load',data.mailserver);
		}
	});
	
	$('#sysconfig_mail_form').validator({ 
	    theme: 'simple_right',
	    stopOnError:true,
	    timely: 1,
	    rules: simHandler.rules,
	    showOk: "",
	    fields: {
	        "serverip": {
	            rule: "required;请输入邮件服务器IP地址;contentRule"
	        },
	        "serverport":{
	        	rule:"required;range[0~65535];integer;请输入邮件服务器端口"
	        },
	        "mailsender":{
	        	rule:"required;email;length[1~30];请输入发件人邮件地址"
	        },
	        "loginaccount":{
	        	rule:"required;length[1~30];请输入发件人用户名"
	        },
	        "loginpwd":{
	        	rule:"required;length[1~30];请输入发件人密码"
	        }
	    },
	    //验证成功
	    valid: function(form) {
	    	var formdataarray = $(form).serializeArray();
	    	var formdata = {},mailserver = {};
	    	$.map(formdataarray,function(data){
	    		var value = data.name == "loginpwd" ? rsaEncrypt(data.value) : data.value ;  
	    		mailserver[data.name] = value;
	    	});	   
	    	formdata.responseId = sys_cfg_mailserver_id;
	    	formdata.mailserver = mailserver;
	        $.ajax({
	            url: '/sim/systemConfig/modifyMailServerConfig',
	            type: 'POST',
	            data: JSON.stringify(formdata),
	            dataType:'json',
	            contentType:"text/javascript",
	            success: function(res){
	            	if(res && res.status){
	            		showAlertMessage(res.message);
	            	} else {
	            		showErrorMessage(res.message);
	            	}
	            }
	        });
	    }
	});
});