var adb = {
};
/**
 * 切换显示内容
 */
adb.toggleDisplay = function(){
	
} ;
/**
 * 为编辑按钮绑定的函数
 */
adb.editAssetProperty = function(){
	$(this).parent().css("display","none") ;
	$(this).parent().siblings().css("display","") ;

	var adt_detail_base_flag = $("#adt_detail_base_flag"+getSelectTabSeq()).html();
	if(adt_detail_base_flag!="ok"){
		$("#adt_detail_base_flag"+getSelectTabSeq()).html("ok");
	}
}
adb.moreInfo = function(ip,tabSeq){
	var table = $("#adt_base_info"+tabSeq) ;
	if(table.attr("state") == "expanded"){//目前牌展开状态
		table.find(".otherAttr").css("display","none") ;
		table.attr("state","collapsed") ;
		var a = $("#adt_moreInfo"+tabSeq)
	    .find(".l-btn-left>.l-btn-text>.l-btn-empty")
		.removeClass("easyui-accordion-collapse").addClass("easyui-accordion-expand");
		$("#showStyle"+tabSeq).height(220);
	}else{//目前牌收缩状态
		$("#showStyle"+tabSeq).css("height", "342px");
		var a = $("#adt_moreInfo"+tabSeq)
		    .find(".l-btn-left>.l-btn-text>.l-btn-empty")
			.removeClass("easyui-accordion-expand").addClass("easyui-accordion-collapse");
		if(table.attr("loadComplete") == "true"){
			table.find(".otherAttr").css("display","") ;
			table.attr("state","expanded") ;
		}else{
			$.getJSON("/sim/assetdetail/moreInfo?ip="+ip+"&_time="+new Date().getTime(),function(result){
				if(result.status){
					var attributes = result.result ;
					if(attributes.length < 1){
						table.append("<tr class='otherAttr'><td colspan='2' style='text-align:center' algin='center'>没有找到数据</td></tr>")
					}else{
						for(var i=0;i<attributes.length;i++){
							var row = $("<tr/>").addClass("otherAttr").append($("<td/>").css("width","53px").html(attributes[i].label))
							.append($("<td/>").css("width","150px").html(attributes[i].value)) ;
							table.append(row) ;
						}
					}
					table.attr("loadComplete","true") ;
					table.attr("state","expanded") ;
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}
	}
}
/**
 * 为保存按钮绑定的函数
 */
adb.editSaveAssetProperty = function(){

	var adt_detail_base_flag = $("#adt_detail_base_flag"+getSelectTabSeq()).html();
	if(adt_detail_base_flag=="ok"){
		$('#adt_form' + getSelectTabSeq()).submit();
	}
}
/**
 * 为取消按钮绑定的函数
 */
adb.editCancelAssetProperty = function(){
	var adt_detail_base_flag = $("#adt_detail_base_flag"+getSelectTabSeq()).html();
	if(adt_detail_base_flag=="ok"){
		$("#adt_form"+getSelectTabSeq()+" .editable_input>input").each(function(){
			var $it = $(this);
			if($it.hasClass("easyui-combobox")){
				$it.combobox("setValue",$it.attr("selectValue"));
			}else{
				$it.val($it.attr("inputValue"));
			}
		});
		$("#adt_detail_base_flag"+getSelectTabSeq()).html("");
	}
	$("#adt_form"+getSelectTabSeq()+" .editable_input").css("display","none");
	$("#adt_form"+getSelectTabSeq()+" .editable_input").siblings().css("display","inline-block");
}
adb.invokeTool = function(tool,ip){
	if(tool == "ping"){
		simHandler.ping(ip) ;
	}else{
		window.open(tool+"://"+ip,tool) ;
	}
}
$(function(){
	//初始化表单验证组件，并创建表单验证实例
	al.adt_form_validator = $('#adt_form' + getSelectTabSeq()).validator({
		theme: 'simple_right',
		showOk: "",
		fields:{
			name:'required;length[1~30]'
		}
	}).data("validator");
	
	//初始化基本信息
	var ip = $("#asset_ip"+getSelectTabSeq()).val();
	var table = $("#adt_base_info"+getSelectTabSeq());
	$.getJSON("/sim/assetdetail/moreInfo?ip="+ip+"&_time="+new Date().getTime(),function(result){
		if(result.status){
			var attributes = result.result ;
			if(attributes.length < 1){
				table.append("<tr class='otherAttr'><td colspan='2' style='text-align:center' algin='center'>没有找到数据</td></tr>")
			}else{
				for(var i=0;i<attributes.length;i++){
					var row = $("<tr/>").addClass("otherAttr").append($("<td/>").css("width","53px").html(attributes[i].label))
					.append($("<td/>").css("width","150px").html(attributes[i].value)) ;
					table.append(row) ;
				}
			}
		}else{
			showErrorMessage(result.message) ;
		}
	}) ;
}) ;