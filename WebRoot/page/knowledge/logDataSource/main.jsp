<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title><%=CommonUtils.getProductName() %></title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="/css/bootstrap.css" rel="stylesheet" media="screen">
<link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
<link href="/js/jquery-easyui/themes/icon.css" rel="stylesheet" type="text/css">
<link href="/css/system.css" rel="stylesheet">
<script src="/js/global/jquery-1.8.3.js"></script>
<script src="/js/jquery-easyui/jquery.easyui.js"></script>
</head>
<body>
<style>
.staticHtml a{
	font-size:16px;
	font-weight:bolder;
	color:rgb(84, 120, 233);
}
a:hover{
	text-decoration: none;
}
</style>
<div class="easyui-layout" data-options="fit:true">
    <div data-options="region:'west',iconCls:'icon-reload',title:'目录'" class="w330">
    	<ul id="logDataSourceDirIndexTree"></ul>
    </div>
    <div data-options="region:'center'" >
    	<div class="easyui-panel p5" id="logDataSourceId" data-options="title:'指导手册',fit:true,border:false,tools:'#logDataSourceTool'"></div>
    </div>
</div>
<div id="logDataSourceTool">
<c:if test="${empty asset}">
	<a href="javascript:void(0);" class="icon-cancel" title="返回" onclick="gobackProductsupport(event);"></a>
</c:if>
</div>
<script>
	$(function(){
		$("#logDataSourceDirIndexTree").tree({
			lines:true,
			url:"/data/logDataSource/dirIndexTree.json",
			onSelect: function(node){
				var $it = $(this);
				var urlTemp = node.url;
				if(urlTemp) {
					var pText = node.pText;
					if(!pText) {
						var parent = $(this).tree("getParent", node.target);
						pText = parent.pText;
					}
					$("#logDataSourceId").panel("setTitle", pText).panel("refresh", "/page/knowledge/logDataSource" + urlTemp);
				} else {
					var stateTemp = node.state;
					if(stateTemp == "closed") {
						$it.tree("expand", node.target);
					} else {
						$it.tree("collapse", node.target);
					}
				}
			},
			onLoadSuccess:function(node, data) {
				var welcomeFlag = true;
				var name = "${name}";
				if(name) {
					var $it = $(this);
					var nodeTemp = $it.tree("find", name.toUpperCase());
					if(nodeTemp) {
						var parentNodeTemp = $it.tree("getParent", nodeTemp.target);
						$it.tree("expand", parentNodeTemp.target);
						$it.tree("select", nodeTemp.target);
						welcomeFlag = false;
						var $nodeTemp = $(nodeTemp.target);
						var $treeBody = $("#logDataSourceDirIndexTree").parent();
						var height = $treeBody.outerHeight();
	
						var top = $nodeTemp.position().top - height;
						if (top >= 0) {
							$treeBody.scrollTop($treeBody.scrollTop() + top + 50);
						}
					}
				}
				if(welcomeFlag) {
					$("#logDataSourceId").panel("setTitle", "指导手册").panel("refresh", "/page/knowledge/logDataSource/welcome.html");
				}
			}
		});
	});
	function gobackProductsupport(event) {
		simHandler.changeMenu(event, "/sim/productsupport/showUI");
		simHandler.selectProductsupport();
	}
</script>
</body>
</html>