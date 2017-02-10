function CurentTime() {
    var now =simHandler.serverTime;
    var year = now.getFullYear();       // 年
    var month = now.getMonth() + 1;     // 月
    var day = now.getDate();            // 日
    var hh = now.getHours();            // 时
    var mm = now.getMinutes();          // 分
    var clock = year + "-";
    if (month < 10)
        clock += "0";
    clock += month + "-";
    if (day < 10)
        clock += "0";
    clock += day ;
    return (clock);
}
var user_dialog;
var minCount = 0;
var minUpperCount = 0;
var minLowerCount = 0;
var minNumCount = 0;
//初始化获取密码设置的个数
function user_pwd_show() {
	$.ajax({
		url : '/sim/authUser/modifyPswUI',
	    type : 'post',
	    dataType : 'json',
		async : false,
		success : function (data) {
			minCount = data.minCount;
			minUpperCount = data.minUpperCount;
			minLowerCount = data.minLowerCount;
			minNumCount = data.minNumCount;
			 
		}
	});
}
function user_add_open() {
	user_pwd_show();
	userOpen('/page/user/addUserInfo.html');
}
// 添加/编辑用户时，弹框
function userOpen(href) {
		// 计算当前面板宽度和高度
		var w = $('#user_management_panel').layout('panel','center').width();
		var h = $('#user_management_panel').layout('panel','center').height();
		var top = $("#user_management_panel").position().top;
		user_dialog = $('#user_dialog').dialog({
			href:href,
			style:{'padding':0,'border':0},
			top:top,
			left:0,
			width:w,
			height:h,
			inline:true,
			noheader:true,
			modal:false,
			border:false,
			collapsed:true,
			onCollapse:onCollapseDialog
		}).dialog('expand',true);	
}
var tempcount = 0;
// 当折叠日志查询弹出窗口后
function onCollapseDialog() {
	tempcount++;
	if(tempcount%2 == 0) {
		user_dialog.dialog('close',false);
	}
}
// 关闭弹出的窗口
function closeUserDialog() {
	user_dialog.dialog('collapse',true);
}
//根据用户角色查询用户信息
function user_search() {
	var rolevalue = $("#searchRoleId").val();
	$('#user_management_table').datagrid({
		 queryParams:{roleid:rolevalue}
	});
}
// 删除已选的权限
function deleteAsset() {
	var optObj = $("#assetContent").children("option:selected");
	var obj = optObj.length;
	if (obj > 0) {
			var optValue=optObj[0].value.split(",")[4];
			var delGroup=false;
			$("#assetContent").children("option:selected").remove();
			savePermission();
			var permissionOpt = $("#assetContent").children("option");
			for (var i = 0 ; i <permissionOpt.length ; i++) {
				 var permValue = permissionOpt[i].value.split(",")[4];
				 if(optValue == permValue) {
					delGroup=false;
					break;
				 } else {
					delGroup=true;
				 }
			 }
			if(delGroup || permissionOpt.length==0) {
				var groupOptObj = $("#groupContent").children("option");
				var groupLength = groupOptObj.length;
				for (var j = groupLength - 1; j >= 0; j--) {
					 var groupOptValue = groupOptObj[j].value.split(":")[0];
					 if(groupOptValue == optValue) {
						 groupOptObj[j].remove();
					 }
				}
			}
			
	} else {
		showAlarmMessage("请选择要删除的内容!");
	}

}
function savePermission() {
	 var opts = document.getElementById("assetContent").options;
	 var userDevices = "";
	 var dataSource = "";
	 var permissionValue = {};
     for (var i = 0 ; i <opts.length ; i++) {
    	  var opValue = opts[i].value;
    	  userDevices += opValue.substring(0,opValue.lastIndexOf(",",opValue.length-1));
    	  dataSource += opValue.split(",")[3];
    	  if(i < opts.length-1) {
    		 userDevices += ";";
    		 dataSource += ",";
    	  }
    	  var deviceType = opValue.split(",")[4];
    	  if(permissionValue[deviceType]) {
    		
		  } else {
			 permissionValue[deviceType] =opValue.split(",")[3];
			 
		  };
	  }
      var index = 0;
	  var filterDataSource = "";
	  for (var temp in permissionValue) {
	       if(permissionValue.hasOwnProperty(temp)) {
	    	  if(index > 0) {
	    		 filterDataSource+=",";
	    	  }
	    	  filterDataSource += permissionValue[temp];
	    		index++;
	    	  }
	    	
	  }
	    
     $("#userDevices").val("");
	 $("#dataSource").val("");
	 $("#selectDataSource").val();
     $("#userDevices").val(userDevices);
	 $("#dataSource").val(filterDataSource);
	 $("#selectDataSource").val(dataSource);
}
// 点击添加弹出选择权限框
function selectAsset() {
	$('#asset_tree').tree({
		url:'/sim/authUser/getAssetTree',
		checkbox:true,
		onLoadSuccess : function(node , data){
			var $_this=$(this);
			 var selectNode = $("#selectDataSource").val().split(",");
			 var node_arr = data;
			 $.each(selectNode,function(i,nodes){
				 $.each(node_arr,function(i,n){
					  var children_node = $_this.tree('find',nodes);
					  if(children_node!=null) {
						  $_this.tree('check',children_node.target);
						 var parentNode=$_this.tree('getParent',children_node.target);
						 $_this.tree('expand', parentNode.target);
						 var par_parentNode=$_this.tree('getParent',parentNode.target);
						 $_this.tree('expand', par_parentNode.target);
					  }
				  });
			 });
		}
	});
	$("#assetTree_Dialog").dialog('open').dialog('setTitle','选择权限');
}
// 关闭权限框
function closeAssetDialog() {
	$("#assetTree_Dialog").dialog('close');
}
// 选择权限
function selectPermission() {
    var allchecked = $("#asset_tree").tree("getChecked");
//    if(allchecked.length == 0){
//    	 closeAssetDialog();
//    	return;
//    }
    
    $("#assetContent").find("option").remove();
    for(var i = 0 ; i < allchecked.length ; i++) {
    	 if($("#asset_tree").tree("isLeaf",allchecked[i].target)){
    		 if(allchecked[i].type){
			   var device = allchecked[i].text+","+allchecked[i].type+","+allchecked[i].resid+","+allchecked[i].id+","+allchecked[i].attributes[0].deviceType;
			   addAssertOption('assetContent',allchecked[i].text,device);
    		 }
		 }
    }
    closeAssetDialog();
    savePermission();
    $("#deviceGroup").val("");
    $("#groupContent").find("option").remove();
    
  }
// 判断权限是否已存在
function isOptionExist(selectObj, optionValue) {
	for ( var i=0;i<selectObj.length;i++) {
		if (selectObj[i].text == optionValue) {
			return true;
		}
	}
	return false;
}
// 向权限select添加项
function addAssertOption(id,value,content) {
	var selectObj = $("#"+id).children();
	if (isOptionExist(selectObj, value)) {
		return;
	} else {
		$("#"+id).append("<option value='" + content + "'>" + value + "</option>");
	}
}
// 关闭列集框
function closeDeviceTypeDialog() {
	$("#deviceTypeTree_Dialog").dialog('close');
}
// 点击添加选择列集
function selectGroup() {
	var dataSource = $("#dataSource").val();
	$("#deviceType_tree").tree({
		url : '/sim/authUser/getGroupListByDeviceType?deviceType='+dataSource,
		checkbox : true,
		onLoadSuccess : function(node, data) {
			var $it = $(this);
			var groupArr = $("#deviceGroup").val().split(";");
		    for(var i = 0; i < groupArr.length; i++){
		    	var opValue=groupArr[i];
		    	if(opValue){
			    	 var deviceId = opValue.split(":")[0];
			    	 var groupId = opValue.split(":")[1].split("#");
			    	 
			    	 var nodeDevice = $it.tree('find', deviceId);
			    	 var nodeChildrens = $it.tree('getChildren', nodeDevice.target);
			    	 
			    	 $.each(nodeChildrens, function(index, nodeTemp){
			    		 var nodeTempId = nodeTemp.id;
			    		 if($.inArray(nodeTempId, groupId) != -1){
			    			 $it.tree('check', nodeTemp.target);
			    		 }
			    	 });
		    	}
			}
			
		}
	});
	$("#deviceTypeTree_Dialog").dialog('open').dialog('setTitle','选择列集');
}
// 获取选择的列集并且显示在select中
function selectDeviceGroup() {
	var allchecked = $("#deviceType_tree").tree("getChecked");
    if(allchecked.length ==0){
    	showErrorMessage("请选择列集!");
    	return;
    }
    $("#groupContent").find("option").remove();
    for(var i = 0 ; i < allchecked.length ; i++){
    	 if($("#deviceType_tree").tree("isLeaf",allchecked[i].target)){
			 var parentNode = $("#deviceType_tree").tree("getParent",allchecked[i].target);
    		 var content = parentNode.text+"-->"+allchecked[i].text;
    		 var value = parentNode.id+":"+allchecked[i].id;
    		 addAssertOption('groupContent',content,value);
		 };
    }
    closeDeviceTypeDialog();
    saveGroupValue();
}
// 获取已选的列集并且存储于隐藏域
function saveGroupValue() {
	 var options = document.getElementById("groupContent");
	 var groupValue = {};
	    for(var i = 0 ; i < options.options.length ; i++){
	    	var opValue = options.options[i].value;
	    	var deviceType = opValue.split(":")[0];
	    	var groupId = opValue.split(":")[1];
	    	if(groupValue[deviceType]) {
	    		 groupValue[deviceType] += ("#" + groupId);
			 } else {
				 groupValue[deviceType] =deviceType+":"+groupId;
				 
			 };
		}
	    var index = 0;
	    var optValue = "";
	    for(var temp in groupValue) {
	    	if(groupValue.hasOwnProperty(temp)) {
	    		if(index > 0) {
	    			optValue+=";";
	    		}
	    		if(groupValue[temp]) {
		    		optValue += groupValue[temp];
		    		index++;
	    		}
	    	}
	    	
	    }
	    $("#deviceGroup").val("");
	    $("#deviceGroup").val(optValue);
}
// 删除已选的列集
function deleteGroup() {
	var obj = $("#groupContent").children("option:selected").length;
	if (obj > 0) {
		$("#groupContent").children("option:selected").remove();
		saveGroupValue();
	} else {
		showAlarmMessage("请选择要删除的内容!");
	}

}
// 根据角色显示隐藏权限
function showHidePermission() {
	var rolevalue = $("#rolename option:selected").val();
	if(rolevalue == "53") {
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
				$("#add_user_perid").show();
			}
		});
	}else {
		$("#add_user_perid").hide();
	}
}
// 添加用户操作
function saveUserInfo() {
	 // 验证ip地址
	if(!validateIP('ipText')) {
		return false;
	}
	 // 日期验证
	if(!validateDate()) {
		return false;
	}
	 // 验证密码
	 if(!validatePwd('passwordText')) {
		 return false;
	 }
	 // 确认密码
	 if(!validatePwdAgain('passwordAgainText')) {
		 return false;
	 }
	 // 验证角色
	 if(!validateRole()) {
		 return false;
	 }
	 var rolevalue=$("#rolename option:selected").val();
	 if(rolevalue=="53") {
		 // 验证权限
// if(!validatePermission('add_permission_tree','permissionText')){
// return false;
// }
		}
    // 验证描述
	if(!validateDesc('description','descText')) {
		return false;
	}
	$("#saveButton").attr("disabled","true");
	$('#saveButton').linkbutton('disable');
	 var dataArray=$("#addUserForm").serializeArray();
	 var formdata = {};
 	 $.map(dataArray,function(data){
 		 if(data.name == "password" || data.name == "passwordAgain"){
 			 formdata[data.name] = rsaEncrypt(data.value) ;
 		 }else{
 			 formdata[data.name] = data.value;
 		 }
 	 });	
	 $.ajax({
		 url:'/sim/authUser/addUser',
		 type: 'POST',
         data: JSON.stringify(formdata),
         dataType:'json',
         contentType:"text/javascript",
		 success : function(data , status) {
			if(data.result == "success") {
				$("#searchRoleId").val("");
				 user_search();
				closeUserDialog();
			}else{
				showErrorMessage("提示","创建失败!");
				return false;
			}
		}
	 });
}
// 验证用户描述
function validateDesc(id,text) {
	var flag = true;
	var description = $("#"+id).val();
	if(description.length > 50 ) {
		 $("#"+text).html("<span class='cancel-icon'></span><span class='msg'>长度不能超过50个字符!</span>");
		 flag = false;
		 return false;
	}else if(description.length == 0) {
		 $("#"+text).html("");
		 flag = true;
	}else {
		 $("#"+text).html("<span class='ok-icon'></span>");
		 flag = true;
	}
	return flag;
}
// 验证用户权限
function validatePermission(treeid,text){
	var flag=true;
	var permission_value=$("#"+treeid).combobox("getValues");
	if(permission_value==""){
		 $("#"+text).html("<span class='cancel-icon'></span><span class='msg'>权限不能为空,请选择!</span>");
		 flag=false;
		 return false;
	}else{
		 $("#"+text).html("<span class='ok-icon'></span>");
		 flag=true;
	}
	return flag;
}
// 验证角色
function validateRole(){
	var flag=true;
	var rolevalue=$("#rolename option:selected").val();
	if(rolevalue==""){
		 $("#roleText").html("<span class='cancel-icon'></span><span class='msg'>所属角色不能为空，请选择!</span>");
		 flag=false;
		 return false;
	}else{
		 $("#roleid").val(rolevalue);
		 $("#roleText").html("<span class='ok-icon'></span>");
		 flag=true;
	}
	return flag;
}
// 验证日期
function validateDate(){
	var flag=true;
	 var expireTime=$("#expireTime").val();
	 if(expireTime==""){
		 $("#expireTimeText").html("<span class='cancel-icon'></span><span class='msg'>有效日期不能为空，请选择!</span>");
		 flag=false;
		 return false;
	 }
	 else{
		 $("#expireTimeText").html("<span class='ok-icon'></span>");
		 flag=true;
	 }
	 return flag;
}
// 验证用户是否存在
function validateUserName(isSubmit){
	var username=$("#username").val().replace(/^\s*/,"");
	username = $.trim(username);
	if(username==""){
		$("#userNameText").html("<span class='cancel-icon'></span><span class='msg'>用户名不能为空，请输入!</span>");
		return false;
	}else if(username.length >10){
		$("#userNameText").html("<span class='cancel-icon'></span><span class='msg'>用户名长度不允许超过10个字符!</span>");
		return false;
		
	}else{
		// 判断输入的是否为中文
		if(!/^[\w]+$/g.test(username)){    
			$("#userNameText").html("<span class='cancel-icon'></span><span class='msg'>只能输入字母、数字和下划线!</span>");
			return false;
			
		}else{
			var result = true;
			$.ajax({
				url:'/sim/authUser/checkLoginName?loginName='+username,
				type:'post',
				dataType:'json',
				async:false,
				success:function(data,status){
					if(data.result=="false"){
						$("#userNameText").html("<span class='cancel-icon'></span><span class='msg'>用户名已存在，请重新输入!</span>");
						result = false;
					}else{
						$("#userNameText").html("<span class='ok-icon'></span>");
						if(isSubmit && isSubmit === 'submit'){
							saveUserInfo();
						}
					}
				}
			});
			return result;
		}
	}
}
// 验证确认密码
function validatePwdAgain(msg){
	var flag=true;
	var pwdagain=$("input[name='passwordAgain']").val().replace(/^\s*/,"");
	var pwd=$("input[name='password']").val().replace(/^\s*/,"");
	if(pwdagain==""){
		$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>确认密码不能为空，请输入！</span>");
		flag=false;
		return false;
	}
	else{
		if(pwdagain!=pwd){
			$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码不一致，请重新输入！</span>");
			flag=false;
			return false;
		} else {
			$("#"+msg).html("<span class='ok-icon'></span>");
			flag = true;
		}
	}
	return flag;
}
// 验证密码
function validatePwd(msg) {
	 var flag=true;
	 var passwordObj=$("input[name='password']");
	 if(passwordObj.val().replace(/^\s*/,"")=="") {
	    $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码不能为空，请输入！</span>");
	    flag=false;
	    return false;
	 }else {
	    if (passwordObj.val().length < parseInt(minCount)) {
		    $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码长度必须至少为"+minCount+" 个字符！</span>");
	        flag=false;
		    return false;
	     }
	    if(parseInt(minUpperCount) > 0) {
	       if(!checkUpperCaseNum(passwordObj,parseInt(minUpperCount))){
	    	 $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码长度必须至少含有 "+minUpperCount+" 个大写字母！</span>");
	         flag=false;
	    	 return false; 
	       }
	    }
	    if(parseInt(minLowerCount) > 0) {
	       if(!checkLowerCaseNum(passwordObj,parseInt(minLowerCount))){
	    	 $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码长度必须至少含有 "+minLowerCount+" 个小写字母！</span>");
	         flag=false;
	         return false; 
	       }
	    }
	    if(parseInt(minNumCount) > 0) {
	       if(!checkNumCaseNum(passwordObj,parseInt(minNumCount))) {
	    	  $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码长度必须至少含有 "+minNumCount+" 个数字！</span>");
	    	  flag=false;
	    	  return false; 
	       }
	    }
	    if (passwordObj.val().length > 20) {
		   $("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>密码长度必须少于 20 个字符！</span>");
	       flag=false;
		   return false;
	    }
	    
		var _userId = $("#userid").val();
		if(_userId) {
			var resultFlag = false;
			$.ajax({
				url:'/sim/authUser/checkLoginPwd',
				type:'post',
				data:{'pwd' : rsaEncrypt(passwordObj.val()), 'userId': _userId},
				dataType:'json',
				async:false,
				success:function(data,status){
					if(data.result=="false"){
						resultFlag = false;
					} else {
						resultFlag = true;
					}
				}
			});
			if(!resultFlag){
				$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>新密码不能与旧密码相同，请重新设置</span>");
				return false;
			}
		}
	    
	    $("#"+msg).html("<span class='ok-icon'></span>");
	    flag=true;
	 }
	    return flag;
}
// 判断字符中str出现大写字母是否至少出现num次
function checkUpperCaseNum(str,num){
		 var len=str.val().length;
		 var m=0;
		 for(var i=0;i<len;i++){
		    var c=str.val().charAt(i);
		    if(c>='A' && c<='Z'){
		      m++;
		    }
		 }
		 if(m>=num){
		    return true;
		 }
		  return false;
}
// 判断字符中str出现小写字母是否至少出现num次
function checkLowerCaseNum(str,num){
	 var len=str.val().length;
	 var m=0;
	 for(var i=0;i<len;i++){
	   var c=str.val().charAt(i);
	   if(c>='a' && c<='z'){
	     m++;
	   }
	 }
	 if(m>=num){
	   return true;
	 }
	 return false;
}

// 判断字符中str出现数字是否至少出现num次*
function checkNumCaseNum(str,num){
		 var len=str.val().length;
		 var m=0;
		 for(var i=0;i<len;i++){
		   var c=str.val().charAt(i);
		   if(c>=0 && c<=9){
		     m++;
		   }
		 }
		 if(m>=num){
		   return true;
		 }
		 return false;
}
// 判断比较两个IP地址,,如果大于返回1，等于返回0，小于返回-1
function compareIP(ipBegin, ipEnd) {   
    var temp1;   
    var temp2;     
    temp1 = ipBegin.split(".");   
    temp2 = ipEnd.split(".");      
    for (var i = 0; i <4; i++)   
    {   
        if (parseInt(temp1[i])>parseInt(temp2[i]))   
        {   
            return 1;   
        }   
        else if (parseInt(temp1[i])<parseInt(temp2[i]))   
        {   
            return -1;   
        }   
    }   
    return 0;      
}  
// 验证第一个ip地址
function validateFirstIp(msg) {
	var flag=true;
	var validate=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
	var minIp=$("input[name='minIp']").val().replace(/^\s*/,"");
	if(minIp==""){
		$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>IP地址不能为空，请输入！</span>");
		flag=false;
		return false;
	}else{	
		if(!validate.test(minIp)){
			$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>请输入合法的IP地址！</span>");
			flag=false;
			return false;
		}else{
			$("#"+msg).html("");
			flag=true;
		}
	}
	return flag;
	
}
// 验证第二个ip地址
function validateIP(msg){
	var flag=true;
	var validate=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
	var minIp=$("input[name='minIp']").val().replace(/^\s*/,"");
	var maxIp=$("input[name='maxIp']").val().replace(/^\s*/,"");
	if(minIp==""){
		$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>IP地址不能为空，请输入！</span>");
		flag=false;
		return false;
	}else{	
		if(!validate.test(minIp)){
			$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>请输入合法的IP地址！</span>");
			flag=false;
			return false;
		}else{
			$("#"+msg).html("<span class='ok-icon'></span>");
			flag=true;
		}
	}
	if(maxIp==""){
		$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>IP地址不能为空，请输入！</span>");
		flag=false;
		return false;
	}else{	
		if(!validate.test(maxIp)){
			$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>请输入合法的IP地址！</span>");
			flag=false;
			return false;
		}else{
			$("#"+msg).html("<span class='ok-icon'></span>");
			flag=true;
		}
	}
	if(compareIP(minIp,maxIp)=='1'){
		$("#"+msg).html("<span class='cancel-icon'></span><span class='msg'>第二个IP地址应大于第一个IP地址！</span>");
		flag=false;
		return false;
	}else{
		$("#"+msg).html("<span class='ok-icon'></span>");
		flag=true;
	}
	return flag;
}
// 删除用户
function delUser(userid) {
	 $.messager.confirm('提示','确认删除所选信息吗?',function(r) {   
	    if (r) {   
	    	var user = $("#user_management_table").datagrid("getSelections");
			$.ajax({
				   url:'/sim/authUser/deleteUser?id='+user[0].USERID,
				   type:'post',
				   dataType:'json',
				   success : function (data,status) {
					  if(data.result == "success") {
						  $("#user_management_table").datagrid("reload");
						  showAlertMessage("删除成功");
					  }else{
						  showErrorMessage("删除失败！");
						  return false;
					  }
				   }
			 });   
	     }   
	 });
}
// 编辑用户时弹框
function getEditUserInfo(row) {
		 user_pwd_show();
		 // 跳转编辑用户信息页面
		 userOpen('/page/user/editUserInfo.html');
}
// 编辑用户
function editUserInfo() {
	if(!validateIP('editIpText')) {
		return false;
	}
	var username = $("#user_name").val();
	if(username== "operator" || username == "auditor" || username == "admin") {
	 
	}else {
		var expireDate = $("#expireDate").val();
		if(expireDate == "") {
		   $("#editExpireTimeText").html("<span class='cancel-icon'></span><span class='msg'>有效日期不能为空，请选择!</span>");
		   return false;
		}else {
		   $("#editExpireTimeText").html("<span class='ok-icon'></span>");
		}
	 }
	
	var checkPwd = $("input:checkbox[id='checkPwd']:checked").val();
	if(checkPwd == "0"){
		if(!validatePwd('newPwdText')) {
			return false;
		}
		if(!validatePwdAgain('newPwdAgainText')) {
			return false;
		}
	}
// if($("#editRoleId").val()=="53"){
// var permission=$("#add_permission_tree").combotree("getValues");
// if(permission=="" || permission=="undefined"){
// $("#editPermissionText").html("<span class='cancel-icon'></span><span
// class='msg'>权限不能为空,请选择!</span>");
// return false;
// }else{
// $("#editPermissionText").html("<span class='ok-icon'></span>");
// }
// }
	// 验证描述
	if(!validateDesc('edit_description','edit_descText')) {
		return false;
	}
	var dataArray = $("#editUserForm").serializeArray();
	var formdata = {};
 	$.map(dataArray,function(data){
 		 if(data.name == "password" || data.name == "passwordAgain"){
 			 formdata[data.name] = rsaEncrypt(data.value) ;
 		 }else{
 			 formdata[data.name] = data.value;
 		 }
 	});	
	$.ajax({
		 url:'/sim/authUser/modifyUser',
		 type: 'POST',
         data: JSON.stringify(formdata),
         dataType:'json',
         contentType:"text/javascript",
		 success : function(data , status) {
			if(data.result == "success") {
				showAlertMessage("修改成功!");
				$("#user_management_table").datagrid('reload');
				closeUserDialog();
			}else{
				showErrorMessage("编辑失败" + data.msg);
				return false;
			}
		}
	});
}
function formatterUserAction(value,row,index){
	var editAndDel =  '<a title=\'编辑\' style=\'width:16px;height:16px;cursor: pointer;\'  class=\'icon-edit\' onclick=\'getEditUserInfo()\'></a>&nbsp;&nbsp;<a  title=\'删除\' style=\'width:16px;height:16px;cursor: pointer;\' class=\'icon-remove\' onclick=\'delUser()\'></a>';
	var edit = '<a title=\'编辑\' style=\'width:16px;height:16px;cursor: pointer;\'  class=\'icon-edit\' onclick=\'getEditUserInfo()\'></a>';
	if(value == "edit"){
		return edit;
	}
	if(value == "editAndDel"){
		return editAndDel;
    }
}