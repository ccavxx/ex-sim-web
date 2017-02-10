/**
 * 生成机器码
 */
var licensegen = {}

$(function(){
	ZeroClipboard.setMoviePath( "/js/global/ZeroClipboard.swf" );
	licensegen.initClipboard();
	$("#companyNameId").blur(licensegen.getCompanyName);
});
/**
 * 初始化剪切板插件
 */
licensegen.initClipboard = function(){
	var clip = new ZeroClipboard.Client(); // 新建一个对象 
	clip.setHandCursor( true );
	clip.addEventListener("mouseDown", function(client) {
		var value = $.trim($("#textareaVal").val());
		var compName = $.trim($("#companyNameId").val());
		if (compName.length == 0) {
			showAlertMessage("请输入公司名称!");
		}else if(compName.length > 20){
			showAlertMessage("公司名称不能超过20字符!");
		}else if(value.length == 0){
			showAlertMessage("请再次点击“复制到粘贴板”按钮!");
		}else{
			clip.setText(value); // 设置要复制的文本。
		}
	});
	clip.addEventListener( "complete", function(client, text){
		var value = $("#textareaVal").val();
		if(($.trim(value).length != 0)&&value==text){
			showAlertMessage("服务器机器码已经复制到剪贴板上！");
		}
	});
	clip.glue("copyTextBtn"); // 和上一句位置不可调换
}
/**
 * 为公司名称添加失去焦点监听
 */
licensegen.getCompanyName = function() {
	var compName = $.trim($("#companyNameId").val());
	if(compName.length == 0){
		$("#companyNameId").focus();
		$("#textareaVal").val("");
		return;
	}
	if(compName.length > 20){
		$("#textareaVal").val("");
		return;
	}
	var time = (new Date()).valueOf();
	var url = "/page/sysconfig/licensegen.jsp?time=" + time;
	param = { paramKey : compName }
	$.post(url, param, function(data, textStatus) {
		if (textStatus == "success") {
			var end = $.trim(data.toString()).indexOf("\r\n");
			$("#textareaVal").val($.trim(data.toString().substring(0, end)));
		}
	});
}
$('#companyNameId').keydown(function(e){
	if(e.keyCode==13){
		e.preventDefault() ;
		e.stopPropagation() ;
		licensegen.getCompanyName();
	}
});
