// 表单提交
function submitSafetyInfo() {
	
	var validate = /^\+?[0-9][0-9]*$/;
	
    //验证最少字符数
	var minCount=$("#minCount").val();
	if(minCount!="" && minCount!=null){
    	if(!validate.test(minCount)){
    		 $("#minCount").focus();
    		 $("#minCountText").html("<font color='red'>取值范围在8-20之间正整数</font>");
    		return false;
    	}else{
    		if(parseInt(minCount)<8  || parseInt(minCount)>20){
    			$("#minCount").focus();
    			$("#minCountText").html("<font color='red'>取值范围在8-20之间正整数</font>");
    			return false;
    		}else{
    			$("#minCountText").html("");
    		}
    	}
	}else{
		    $("#minCount").focus();
		    $("#minCountText").html("<font color='red'>该项不能为空请输入！</font>");
			return false;
	}
	//验证最少包含大写字母数
	var minUpperCount=$("#minUpperCount").val();
	if(minUpperCount!="" && minUpperCount!=null){
    	if(!validate.test(minUpperCount)){
    		 $("#minUpperCount").focus();
    		 $("#minUpperCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    		return false;
    	}else{
    		if(parseInt(minUpperCount)<0  || parseInt(minUpperCount)>20){
    			$("#minUpperCount").focus();
    			$("#minUpperCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    			return false;
    		}else{
    			$("#minUpperCountText").html("");
    		}
    	}
	}
	//验证最少包含小写字母数
	var minLowerCount=$("#minLowerCount").val();
	if(minLowerCount!="" && minLowerCount!=null){
    	if(!validate.test(minLowerCount)){
    		 $("#minLowerCount").focus();
    		 $("#minLowerCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    		return false;
    	}else{
    		if(parseInt(minLowerCount)<0  || parseInt(minLowerCount)>20){
    			$("#minLowerCount").focus();
    			$("#minLowerCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    			return false;
    		}else{
    			$("#minLowerCountText").html("");
    		}
    	}
	}
	//验证最少包含多少数字
	var minNumCount=$("#minNumCount").val();
	if(minNumCount!="" && minNumCount!=null){
    	if(!validate.test(minNumCount)){
    		 $("#minNumCount").focus();
    		 $("#minNumCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    		 return false;
    	}else{
    		if(parseInt(minNumCount)<0  || parseInt(minNumCount)>20){
    			$("#minNumCount").focus();
    			$("#minNumCountText").html("<font color='red'>取值范围在0-20之间正整数</font>");
    			return false;
    		}else{
    			$("#minNumCountText").html("");
    		}
    	}
	}
	var totalcount=parseInt(minNumCount)+parseInt(minLowerCount)+parseInt(minUpperCount);
	if(totalcount>20){
		showAlertMessage("大写字母、小写字母、数字包含个数之和不能大于20个");
		return false;
	}else if(totalcount>minCount){
		showAlertMessage("大写字母、小写字母、数字包含个数之和不能大于最少字符数");
		return false;
	}
	//验证用户密码更新周期默认10
	var pwdModifyCycle = $("#pwdModifyCycle").numberspinner('getValue');
	if(pwdModifyCycle != "" && pwdModifyCycle != null) {
		$("#pwdModifyCycleText").html("");
	} else {
		$("#pwdModifyCycleText").html("<font color='red'>用户密码更新周期最小为1天（默认为30天）</font>");
		return false;
	}
	//验证登录失败多少次被锁住
	var fialcount=$("#failCount").val();
	if(fialcount!="" && fialcount!=null){
		if(!validate.test(fialcount)){
			 $("#failCount").focus();
    		 $("#failCountText").html("<font color='red'>取值范围在3-20之间正整数</font>");
    		return false;
		}else{
			if(parseInt(fialcount)<3  || parseInt(fialcount)>20){
    			$("#failCount").focus();
    			$("#failCountText").html("<font color='red'>取值范围在3-20之间正整数</font>");
    			return false;
    		}else{
    			$("#failCountText").html("");
    		}
		}
	}else{
		$("#failCount").focus();
		$("#failCountText").html("<font color='red'>不允许为空，取值范围在3-20之间正整数</font>");
		return false;
	}
	//多长时间不操作自动退出登录
	var lostTime= $("#lostTime").val();
	if(lostTime!="" && lostTime!=null){
		if(!validate.test(lostTime)){
			$("#lostTime").focus();
			$("#lostTimeText").html("<font color='red'>取值范围应为正整数</font>");
			return false;
		}
		else{
			if(parseInt(lostTime)<1  || parseInt(lostTime)>1440){
    			$("#lostTime").focus();
    			$("#lostTimeText").html("<font color='red'>取值范围在1-1440之间正整数</font>");
    			return false;
    		}else{
    			$("#lostTimeText").html("");
    		}
		}
	}else{
		$("#lostTime").focus();
		$("#lostTimeText").html("<font color='red'>不允许为空，取值范围在1-1440之间正整数</font>");
		return false;
	}
	
	var dataArray = $("#savaForm").serializeArray();
	var formdata = {};
	$.map(dataArray, function(data) {
		if(data.value == "") {
			if(data.name != "pwdModifyCycle"){
				data.value = 0;
		    } else {
		    	data.value = 10;
		    }
	    }
	    formdata[data.name] = data.value;
 	});
	$.ajax({
		 url:'/sim/safeMgr/edit',
		 type: 'POST',
         data: JSON.stringify(formdata),
         dataType:'json',
         contentType:"text/javascript",
		 success:function(data,status){
		       if(data.result=="success"){
		    	   showAlertMessage("修改成功！");
		       }else{
		    	   showErrorMessage("修改失败！");
		    	   return false;
		    	  
		       }
		}
	});
}
// 初始化数据
function initLoadSafetyMessagementData(){
	$.ajax({
		url:'/sim/safeMgr/editUI',
	    type:'post',
	    dataType:'json',
		async : false,
		success : function(data) {
			 $("input[name='minCount']").val(data.result.minCount);
			 $("input[name='minUpperCount']").val(data.result.minUpperCount);
			 $("input[name='minLowerCount']").val(data.result.minLowerCount);
			 $("input[name='minNumCount']").val(data.result.minNumCount);
			 $("input[name='failCount']").val(data.result.failCount);
			 $("input[name='lostTime']").val(data.result.lostTime);
			 $("input[name='securityCheck']").attr('checked',data.result.securityCheck);
			 var pwdModifyCycle = data.result.pwdModifyCycle;
			 $("#pwdModifyCycle").numberspinner({
				 height:24,
				 value:10,
				 min:1,
				 max:1000,
				 onChange:function(newValue, oldValue) {
					if(!newValue) {
						$(this).numberspinner('setValue', 30);
					}
				 }
			 }).numberspinner('setValue',pwdModifyCycle ? pwdModifyCycle : 30).tooltip({
					position: 'right',
					content: function(){
						var opts = $(this).validatebox('options');
						return '<div style="width:180px;">'+opts.prompt+'</div>';
					},
					onShow: function(){
						$(this).tooltip('tip').css({
							color: '#000',
							borderColor: '#CC9933',
							backgroundColor: '#FFFFCC'
						});
					}
			});
//			$("input[name='failCount']").val(data.result.failCount);
//			$("#failCount").numberspinner({
//				onChange:function(newValue,oldValue){
//					alert(11);
//				},
//				 height:24,
//				 min:3,
//				 max:20
//			});
//			$("input[name='lostTime']").val(data.result.lostTime);
//			$("#lostTime").numberspinner({
//				 height:24,
//				 min:1,
//				 max:1440
//			});
			 
		}
	});
}
$(function(){
	initLoadSafetyMessagementData();
	
	$('input.easyui-validatebox').validatebox({
	}).tooltip({
		position: 'right',
		content: function(){
			var opts = $(this).validatebox('options');
			return '<div style="width:180px;">'+opts.prompt+'</div>';
		},
		onShow: function(){
			$(this).tooltip('tip').css({
				color: '#000',
				borderColor: '#CC9933',
				backgroundColor: '#FFFFCC'
			});
		}
	});
});
