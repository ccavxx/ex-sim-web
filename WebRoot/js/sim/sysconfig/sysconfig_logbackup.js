 var simSysConfLogBackup = {loadLogbackupLocalpathTreeVal:null};
$(function(){
	// 开启Local验证
	function setLocalFieldForOn(){
		setBackupEnable();
		sysconfig_logbackup_form_validation.setField({
			local_path:'required',
			serverip:null,
			user:null,
			password:null
		});
	}
	// 关闭Local部分验证
	function setLocalFieldForOff(){
		setBackupEnable();
		sysconfig_logbackup_form_validation.setField({
			local_path:null,
			serverip:null,
			user:null,
			password:null
		});
	}
	// 开启Ftp验证
	function setFtpFieldForOn(){
		setBackupEnable();
		sysconfig_logbackup_form_validation.setField({
			local_path:null,
			serverip:'required;ipv4;',
			user:'required;',
			password:'required;'
		});
	}
	// 关闭Ftp部分验证
	function setFtpFieldForOff(){
		setBackupEnable();
		sysconfig_logbackup_form_validation.setField({
			local_path:null,
			serverip:'required;ipv4;',
			user:'required',
			password:'required'
		});
	}
	// 为是否启用绑定事件
	$("#sysconfig_logbackup_form [name=enable]:checkbox").change(function() {
		var ftpDisplay = $('.ftp').css("display");
		var localDisplay = $('.local').css("display");
		var enableVal = this.checked;
		if (ftpDisplay !== "none") {
			if(enableVal) {
				setFtpFieldForOn();
			}else{
				// clear warning
				setFtpFieldForOff();
			}
		} else if (localDisplay !== "none") {
			if(enableVal) {
				setLocalFieldForOn();
			} else {
				// clear warning
				setLocalFieldForOff();
			}
		}
	}) ;
	
	var sys_cfg_backup_id,
		sys_cfg_backup_auto_id;
	
    function setBackupEnable(){
    	if($("input[name='enable']").is(':checked')){
			$("#backupLogRange").show() ;
			$("#backupEventRange").show() ;
		}else{
			$("#backupLogRange").hide() ;
			$("#backupEventRange").hide() ;
		}
    }
	// 初始化表单数据
	function initFormData(){
		$.getJSON('/sim/systemConfig/getCfgResponse?time='+new Date().getTime(),{cfgKey:'sys_cfg_backup',nodeType:'SMP'},function(res){
			if(res && res.status){
				var formdata = res.result;
				sys_cfg_backup_id = formdata.responseId;
				if(!formdata.isSoft){
					$("#local_radio").hide();
					$("#local_remark").hide();
					$("#backup_ftp").click();
					$("input[value ='ftp']").attr("checked","checked");
					$('.local').hide();
					$('.ftp').show();
					if(formdata.ftp){
						fillFtpData(formdata) ;
					}
					setFtpFieldForOn();
					setLocalFieldForOff();
				}else{
					if(!!formdata.local) {
						$('#local_path').val(formdata.local.path);
						$("#backup_local").click();
						$('.local').show();
						$('.ftp').hide();
						$("input[value ='local']").attr("checked","checked");
						setFtpFieldForOff();
						setLocalFieldForOn();
					}else {
						fillFtpData(formdata) ;
						$("input[value ='ftp']").attr("checked","checked");
						$('.local').hide();
						$('.ftp').show();
						setFtpFieldForOn();
						setLocalFieldForOff();
					}
				}
				
			}
		});
	}
	function fillFtpData(formdata){
		$("input[name='serverip']").val(formdata.ftp.serverip);// 服务器IP
		$("input[name='user']").val(formdata.ftp.user);// 用户名
		$("input[name='password']").val(rsaDecrypt(formdata.ftp.password));// 密码
		$("select[name='encoding']").val(formdata.ftp.encoding);//编码
	}
	$.getJSON('/sim/systemConfig/getCfgResponse?time='+new Date().getTime(),{cfgKey:'sys_cfg_backup_auto',nodeType:'SMP'},function(res){
		
		if(res && res.status){
			var formdata = res.result;
			sys_cfg_backup_auto_id = formdata.responseId;
			$("select[name='autobackManner']").val(formdata.autoback.autobackManner);// 备份范围
			$("select[name='partitionCount']").val(formdata.autoback.partitionCount);// 备份天数
			$("input[name='enable'][value='"+formdata.autoback.enable+"']").attr('checked',true);// 是否启用
			initFormData();
		}
	});
	// 初始化表单验证组件，并创建表单验证实例
	var sysconfig_logbackup_form_validation = $('#sysconfig_logbackup_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules,
		fields:{
			local_path:'required;'
		}
	}).data("validator");
	
	// 点击备份方式-本地备份
	$("#backup_local").bind('click', function() {
		$('.local').show();
		$('.ftp').hide();
		simSysConfLogBackup.loadLogbackupLocalpathTree();
		var enable;
		$("#sysconfig_logbackup_form [name=enable]:checkbox").each(function(){
			if (this.checked) {
				enable = this.value;
			}
		}) ;
		if (enable === "true") {
			setLocalFieldForOn();
		} else {
			setLocalFieldForOff();
		}
	});
	// 点击备份方式-FTP备份
	$("#backup_ftp").bind('click', function() {
		$('.ftp').show();
		$('.local').hide();
		var enable;
		$("#sysconfig_logbackup_form [name=enable]:checkbox").each(function(){
			if(this.checked){
				enable = this.value;
			}
		}) ;
		if (enable === "true") {
			setFtpFieldForOn();
		} else {
			setFtpFieldForOff();
		}
	});
	// 点击提交
	$("#sysconfig_logbackup_form_submit_button").bind('click', function() {
		$("#sysconfig_logbackup_form").submit();
	});
	
	// 点击本地备份下拉树节点
	simSysConfLogBackup.onClick = function(node){
		if(node){
			var path = node.attributes.path;
			$('#sysconfig_logbackup_localpath').combotree('setText',path);
			$('#local_path').val(path);
		}
	}
	// 展开树节点之前事件
	simSysConfLogBackup.onBeforeExpand = function(node){
		if(node) {
			var path = node.attributes.path;
			var t = $('#sysconfig_logbackup_localpath').combotree('tree');
			t.tree('options').url = '/sim/systemConfig/getLocalFileDirectory?parentDir='+path;
		}
	}
	// 首次下拉树选框加载完毕后调整此
	simSysConfLogBackup.onLoadSuccess = function(node, data){
		var path = $('#local_path').val();
		if(path) {
			$('#sysconfig_logbackup_localpath').combotree('setText',path);
		}
	}
	// 加载本地路径tree
	simSysConfLogBackup.loadLogbackupLocalpathTree = function(){
		simSysConfLogBackup.loadLogbackupLocalpathTreeVal = $("#sysconfig_logbackup_localpath").combotree({
			url:'/sim/systemConfig/getLocalFileDirectory',
			height:24,
			width:200,
			onBeforeExpand:simSysConfLogBackup.onBeforeExpand,
			onClick:simSysConfLogBackup.onClick,
			onLoadSuccess:simSysConfLogBackup.onLoadSuccess
		});
	}

	// 提交表单事件
	$('#sysconfig_logbackup_form').submit(function() {
		var backup = $('#sysconfig_logbackup_form').find("input[name='backup']:checked").val();
		if(backup=='local'){
			var path = $("#sysconfig_logbackup_localpath").combotree("getText");
			if(path.length==0){
				$('#local_path').val("");
				$('#local_path').blur();
				return;
			}
		}
		// 验证表单
		var valid = sysconfig_logbackup_form_validation.isFormValid();
		if (!valid)
			return;
		// 表单数据
		var formdata = {};
		formdata.sys_cfg_backup_id = sys_cfg_backup_id;
		formdata.sys_cfg_backup_auto_id = sys_cfg_backup_auto_id;
		formdata.autoback = {};
		formdata.autoback.autobackManner = $("select[name='autobackManner'] option:selected").val();// 备份范围
		formdata.autoback.partitionCount = $("select[name='partitionCount'] option:selected").val();// 备份天数
		formdata.autoback.enable = $("input[name='enable']:checked").val();// 是否启用
		var backupType = $("input[name='backup']:checked").val();// 备份方式
		if (backupType == 'local') {
			formdata.local = {};
			formdata.local.path = $('#local_path').val();// 备份路径
		} else {
			formdata.ftp = {};
			formdata.ftp.serverip = $("input[name='serverip']").val();// 服务器IP
			formdata.ftp.user = $("input[name='user']").val();// 用户名
			formdata.ftp.password = rsaEncrypt($("input[name='password']").val());// 密码
			formdata.ftp.encoding = $("select[name='encoding'] option:selected").val();//编码
		}
        $.ajax({
            url: '/sim/systemConfig/modifyLogBackupConfig',
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
        return;
	});
});
