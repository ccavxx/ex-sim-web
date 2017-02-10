var userid = $("#user_management_table").datagrid("getSelections");
(function(){
	//编辑用户时，初始化表单数据
	 $("#userid").val(userid[0].USERID);
	 $("input[name='userName']").val(userid[0].USERNAME);
	 $("input[name='maxIp']").val(userid[0].MAXIP);
	 $("input[name='minIp']").val(userid[0].MINIP);
	 if(userid[0].STATUS=='1'){
		 $("input[name='status']").attr("checked","true");
	 }
	 $("input[name='roleName']").val(userid[0].ROLENAME);
	 $("#edit_description").val(userid[0].DESCRIPTION);
	 //有效期
	 $("#user_name").val(userid[0].USERNAME);
	 if(userid[0].USERNAME =="admin"){
		 $("#editExpireTime").hide();
	 }else{
		 $("#editExpireTime").show();
		 $("#datecontent").html("<input id=\"expireDate\" type=\"text\" name=\"expireTime\" class=\"Wdate cursorHand\" style=\"height:20px;width:190px;\" readonly=\"readonly\" onclick=\"WdatePicker({dateFmt:'yyyy-MM-dd',minDate:CurentTime()})\">");
	     $("#expireDate").val(userid[0].EXPIRETIME);
	 }
	 //权限
	 if(userid[0].ROLEID == "53" && userid[0].USERNAME != "operator") {
		 $.ajax({
				url:'/sim/authUser/getGroupEnable',
				type:'post',
				dataType:'json',
				success:function(data){
					if(data == false){
						$("#device_group").hide();
					}else if(data == true){
						$("#device_group").show();
					}
					$("#edit_user_perid").show();
				}
		 });
		 if(userid[0].AssetName!="") {
			 var assetNameArr=userid[0].AssetName.split(",");
		     var resourceArr=userid[0].RESOURCEID.split(";");
		     var permissionValue={};
		     var selectDataSource="";
		     for(var i = 0 ; i < assetNameArr.length ; i++) {
			     $("#assetContent").append("<option value='"+resourceArr[i]+"'>"+assetNameArr[i]+"</option>");
			     var deviceType = resourceArr[i].split(",")[4];
			     if(!permissionValue[deviceType]) {
			    	 permissionValue[deviceType] =resourceArr[i].split(",")[3];
				  }
			      selectDataSource+=resourceArr[i].split(",")[3];
			      if(i<assetNameArr.length-1){
			    	 selectDataSource+=",";
			      }
		      }
		      var index = 0;
			  var dataSource = "";
			  for(var temp in permissionValue) {
			      if(permissionValue.hasOwnProperty(temp)) {
			    	 if(index > 0) {
			    		dataSource += ",";
			    	  }
			    	  dataSource+=permissionValue[temp];
			    	  index++;
			    	}
			    }
			    $("#dataSource").val(dataSource);
			    $("#selectDataSource").val(selectDataSource);
		 }
		 var groupMap=userid[0].groupMap;
		 if(groupMap){
			for(var i = 0 ; i < groupMap.length ; i++) {
			   $("#groupContent").append("<option value='"+groupMap[i].value+"'>"+groupMap[i].name+"</option>");
			}
			$("#deviceGroup").val(userid[0].groupId);
		 }
		
	 }else {
		 $("#edit_user_perid").hide();
	 }
	  
	 $("#userDevices").val(userid[0].RESOURCEID);
	 $("#editRoleId").val(userid[0].ROLEID);
	
})();
//是否修改密码
function ifupdatePwd() {
	var checkPwd = $("input:checkbox[id='checkPwd']:checked").val();
	if(checkPwd == "0") {
		$("#againpwd").show();
		$("#newpwd").show();
	}else{
		$("#againpwd").hide();
		$("#newpwd").hide();
		$("input[name='passwordAgain']").val("");
		$("input[name='password']").val("");
	}
}
