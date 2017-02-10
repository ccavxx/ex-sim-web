<%@ page language="java" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div style="padding:0 3px;">
	<div>
			<a style="margin-left:87.3%;" class="easyui-linkbutton"  data-options="iconCls:'icon-word'" onclick="exportWordProductSupport();">导出</a>&emsp;
			<a class="easyui-linkbutton"  data-options="iconCls:'icon-excel'" onclick="exportExcelProductSupport();">导出</a>
	</div>
	<table class="table table-striped " style="border:1px solid #dddddd;">
		<thead>
			<tr>
				<th style="width:200px">类型</th>
				<th style="width:200px">厂商</th>
				<th>版本</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="types" items="${allTypes}">
				<c:forEach var="venders" items="${types.value}"
					varStatus="venderStatus">
					<tr>
						<c:choose>
							<c:when test="${venderStatus.index==0 }">
								<td>${types.key}</td>
							</c:when>
							<c:otherwise>
								<td></td>
							</c:otherwise>
						</c:choose>
						<td>${venders.key}</td>
						<td><c:forEach var="version" items="${venders.value}"
								varStatus="statustemp">
								<a href="javascript:void(0)"
									onclick="productsupportObj.productsupChangeMenu(event, '${typeMap[types.key]}', '${venderMap[venders.key]}')">${version}</a>
								<%-- <a href="javascript:void(0)" onclick="simHandler.changeMenu(event, encodeURI('/data/TAL_TA_logCllection_datasource_setup.html#${types.key}/${venders.key}'))">${version}</a> --%>
								<c:if test="${!statustemp.last}">，</c:if>
							</c:forEach></td>
					</tr>
				</c:forEach>
				<c:if test="${empty types.value}">
					<tr>
						<td>${types.key}</td>
						<td></td>
						<td></td>
					</tr>
				</c:if>
				<tr>
					<td colspan="3" style="background-color:#99ccff;"></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<div id="totopBox">
	<a href="javascript:void(0)" id="totop"></a>
</div>
<script>
	var productsupportObj = {
		clientHeight : $("#main_panel").height(),
		productsupChangeMenu : function(event, typesKey, vendersKey) {
			simHandler.changeMenu(event, encodeURI("/sim/productsupport/productsupportHelp?name=" + typesKey + "/" + vendersKey));
			simHandler.selectProductsupport();
		}
	}
	//首先将#totop隐藏
	$("#totopBox").hide();
	//当滚动条的位置处于距顶部100像素以下时，跳转链接出现，否则消失
	$(function() {
		$("#main_panel").scroll(function() {
			
			var scrollTop = $("#main_panel").scrollTop();
			if (scrollTop > productsupportObj.clientHeight) {
				
				$("#totopBox").fadeIn(1500);
			} else {
				
				$("#totopBox").fadeOut(1500);
			}
		});
		//当点击跳转链接后，回到页面顶部位置
		$("#totop").click(function() {
			
			$("#main_panel").animate({scrollTop : 0}, 1000);
		});
	});
	function exportWordProductSupport(){
		var url = "/sim/productsupport/exportWordProductSupport";
		window.open(url) ;
	}
	function exportExcelProductSupport(){
		var url = "/sim/productsupport/exportExcelProductSupport";
		window.open(url) ;
	}
</script>