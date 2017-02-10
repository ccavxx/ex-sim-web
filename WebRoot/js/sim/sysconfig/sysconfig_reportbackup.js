var simSysConfLogBackup = {};
$(function(){
	/**
	 * 初始化表单数据
	 */
	var sys_cfg_report_id;
	$.getJSON('/sim/systemConfig/getCfgResponse?_time='+new Date().getTime(),{cfgKey:'sys_cfg_report',nodeType:'ReportService'},function(res){
		if(res && res.status){
			var formdata = res.result;
			sys_cfg_report_id = res.result.responseId;
			$("select[name='systemreport']").val(formdata.reportcfg.systemreport);// 审计系统报表保存期
			$("select[name='activereport']").val(formdata.reportcfg.activereport);// 主动日志报表保存期
			$("select[name='passivereport']").val(formdata.reportcfg.passivereport);// 被动日志报表保存期
		}
	});
	
	
	//提交表单事件
	$('#sysconfig_reportbackup_form').submit(function() {
		//表单数据
		var formdata = {responseId : sys_cfg_report_id, reportcfg : {}};
		formdata.reportcfg.systemreport = $("select[name='systemreport'] option:selected").val();
		formdata.reportcfg.activereport = $("select[name='activereport'] option:selected").val();
		formdata.reportcfg.passivereport = $("select[name='passivereport'] option:selected").val();
        $.ajax({
            url: '/sim/systemConfig/modifyReportBackupConfig',
            type: 'POST',
            data: JSON.stringify(formdata),
            dataType:'json',
            contentType:"text/javascript",
            success: function(res){
            	if(res && res.status) {
            		showAlertMessage(res.message);
            	} else {
            		showErrorMessage(res.message);
            	}
            }
        });	
        return false;
	});
});