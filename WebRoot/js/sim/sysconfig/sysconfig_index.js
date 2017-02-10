$(function(){
	/**
	 * 初始化表单数据
	 */
	$.getJSON('/sim/systemConfig/getSysconfig?_time='+new Date().getTime(),{configId:'index_config',r:new Date().getTime()},function(res){
		if(res && res.status){
			var formdata = res.result;
			$("input[name='enable'][value='"+formdata.enable+"']").attr('checked',true);// 实时索引当天日志
			$("select[name='indexSaveTime']").val(formdata.indexSaveTime);// 索引保存时间
			$("select[name='indexSmallTime']").val(formdata.indexSmallTime);// 索引压缩时间
		}
	});
	

	
	//提交表单事件
	$('#sysconfig_index_form').submit(function() {
		//表单数据
		var formdata = {};
		formdata.enable = $("input[name='enable']:checked").val() == "true" ? true : false;// 实时索引当天日志
		formdata.indexSaveTime = $("select[name='indexSaveTime'] option:selected").val();// 索引保存时间
		formdata.indexSmallTime = $("select[name='indexSmallTime'] option:selected").val();// 索引压缩时间
		
        $.ajax({
            url: '/sim/systemConfig/modifyindexConfig?configId=index_config',
            type: 'POST',
            data: JSON.stringify(formdata),
            dataType:'json',
            contentType:"text/javascript",
            success: function(res){
            	if(res && res.status){
            		$('#sysconf_index_res_msg').empty().removeClass().addClass('alert alert-success').append(res.message).fadeIn(500).delay(3000).fadeOut(500);
            	}else{
            		$('#sysconf_index_res_msg').empty().removeClass().addClass('alert alert-error').append(res.message).fadeIn(500).delay(3000).fadeOut(500);
            	}
            }
        });	 
	return false;
	});
});