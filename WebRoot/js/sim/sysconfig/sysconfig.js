var simSysconfig = {};
/**
 * 菜单点击触发
 */
simSysconfig.initWestNav = function(){
	$(".nav-list>.nav-header>.groupMenu").die().live("click",function(){
		var isHidden = $("#"+$(this).parent().attr("group")).is(":hidden");
		if(isHidden){
			$(this).removeClass("menuExpanded");
			$(this).addClass("menuCollapsed");
		}else{
			$(this).removeClass("menuCollapsed");
			$(this).addClass("menuExpanded");
		}
		$("#"+$(this).parent().attr("group")).slideToggle("normal");
	});
}

/**
 * 设定容器高度
 */
simSysconfig.initContainerH = function(){
	var h = $("#sysconfig_menu").height();
	var mainPanelH = $("#main_panel").height();
	//内容面板高度 = 窗口的高度-外补丁
	var wholePanelH = mainPanelH - 10;
	h = (h < wholePanelH) ? wholePanelH : h;
	// 初始化panel
	$('#sysconfig_container').panel({
		border : false,
		height : wholePanelH
	});
	$("#sysconfig_menu").height(h);
}
/**
 * 切换系统配置菜单
 */
simSysconfig.changeMenu = function(it, e, url) {
	simHandler.clearResidualDomCodes();
	$('#sysconfig_menu').children("._listGroup").children("[class!='nav-header']").removeClass('active');
	$(it).parent().addClass('active');
	clearAllTimer();
	$('#sysconfig_container').panel('refresh', url);
}
/**
 * 多级分页
 */
simSysconfig.withPramsChangePage = function(url) {
	simHandler.clearResidualDomCodes();
	clearAllTimer();
	$('#sysconfig_container').panel('refresh', url);
}
$(function(){
	simSysconfig.initWestNav();
	simSysconfig.initContainerH();

	if(simHandler.sysconfigDefaultElementId !== null){
		$("#" + simHandler.sysconfigDefaultElementId).click();
		simHandler.sysconfigDefaultElementId = null;
	}else{
		//默认显示
		$("#initMenu").click();
	}
});