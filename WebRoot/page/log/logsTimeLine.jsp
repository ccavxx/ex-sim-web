<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<!--[if IE 7]><html class="ie7" lang="zh"><![endif]-->
<!--[if gt IE 7]><!-->
<html lang="zh">
<!--<![endif]-->
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<link href="/css/timeline.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div class="content">
  <div class="wrapper">
    <div class="light"><i></i></div>
    <hr class="line-left">
    <hr class="line-right">
    <div class="main">
      <h1 class="title">${traceFieldAlias }：${traceFieldValue}&nbsp;&nbsp;时间线</h1>
      <c:forEach var="dataItem" items="${datas}">
	      <div class="year">
	        <h2><a href="#">${dataItem.date}<i></i></a></h2>
	        <div class="list">
	          <ul>
	            <c:forEach var="logItem" items="${dataItem.logs}">
	              	<fmt:formatDate var="tmpHour" value="${logItem.START_TIME }" pattern="HH"/>
		            <li class="cls hour${tmpHour}">
		              <p class="date">
		                <!-- 比较当前数据与上一次数据是否是同一小时，如果非同一小时时间段，则创建一个新的小时时间段 -->
		              	<c:if test="${hour ne tmpHour}">
			              	<c:set var="hour" value="${tmpHour}"/>
			              	<a href="javascript:void(0)" onclick="toggleHourSegment(this,'${tmpHour}')"><span style="padding-right: 30px;color:#58a6fb;font-weight: bold;">${hour}时</span></a>
			              	<a href="javascript:void(0)" onclick="toggleHourSegment(this,'${tmpHour}')"><i></i></a>
		              	</c:if>
		              	<span><fmt:formatDate value="${logItem.START_TIME}" pattern="HH:mm:ss"/></span>
		              </p>
		              <p class="intro" title="源地址-->目的地址:目的端口">
		              	${logItem.SRC_ADDRESS}-->${logItem.DEST_ADDRESS}:${logItem.DEST_PORT}</p>
		              <p class="version">&nbsp;</p>
		              <div class="more">
		              	<c:forEach var="field" items="${fields }" varStatus="stats">
		              		<c:choose>
		              			<c:when test="${field.type eq 'Date' }">
		              				<span class="field${stats.index}">${field.alias}=<fmt:formatDate value="${logItem[field.name]}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
		              			</c:when>
			              		<c:otherwise>
				              		<span class="field${stats.index}">${field.alias}=${fn:escapeXml(logItem[field.name])}</span>&nbsp;&nbsp;
			              		</c:otherwise>
		              		</c:choose>
		              	</c:forEach>
		              </div>
		            </li>
	            </c:forEach>
	          </ul>
	        </div>
	      </div>
      </c:forEach>
    </div>
  </div>
</div>
<div id="fieldMenuDiv" style="position: fixed;right: 30px;z-index: 9999;background-color: #ceedfe">
    <c:forEach var="field" items="${fields }" varStatus="stats">
        <div style="padding: 5px;">
        	<div style="display: inline;">
	    		<input id="field${stats.index}Switch" type="checkbox" checked="checked" onchange="changeFieldState('${stats.index}')"/>
        	</div>
        	<div style="display: inline;color:#008000">
        		<span style="cursor: pointer;" 
        		      onclick="$('#field${stats.index}Switch').trigger('click');"
        		      onmouseover="toggleFieldHighlight('field${stats.index}')"
        		      onmouseout="toggleFieldHighlight('field${stats.index}')">${field.alias }</span>
        	</div>
    	</div>
    </c:forEach>
</div>
<script src="/js/global/jquery-1.8.3.js"></script>
<script src="/js/global/bootstrap.js"></script>
<script src="/js/global/bootstrap-switch.min.js"></script>
<script>
	//字段列表div垂直居中
	$("#fieldMenuDiv").css("top",($(window).height()-26*${fn:length(fields)})/2) ; 
	/**
	 * 改变字段内容显隐状态
	 */
	function changeFieldState(index){
		var checked = $("#field"+index+"Switch").attr("checked") ;
		if(checked){
			$(".more .field"+index).show();
		}else{
			$(".more .field"+index).hide();
		}
	}
	/**
	 * 显示和隐藏小时数据
	 */
	function toggleHourSegment(target,hour){
		var targetElement = $(target) ;
		var firstHourSegment = targetElement.parents(".hour"+hour) ; 
		var listSegment = targetElement.parents(".list") ;
		var otherHourSegments = listSegment.find("ul>li.hour"+hour+":gt(0)") ;
		var totalHeight = 0 ;
		otherHourSegments.each(function(index,domEl){
			totalHeight += $(domEl).outerHeight() ; 
		}) ;
		if(firstHourSegment.hasClass("collapsed")){
			listSegment.height(listSegment.height() + totalHeight) ;
			otherHourSegments.show() ;
		}else{
			otherHourSegments.hide() ;
			listSegment.height(listSegment.height() - totalHeight) ;
		}
		firstHourSegment.toggleClass("collapsed");
	}
	function toggleFieldHighlight(field){
		$("."+field).toggleClass("highlight");
	}
	$(".main .year .list").each(function (e, target) {
	    var $target=  $(target),
	        $ul = $target.find("ul");
	    $target.height($ul.outerHeight()), $ul.css("position", "absolute");
	}); 
	$(".main .year>h2>a").click(function (e) {
	    e.preventDefault();
	    $(this).parents(".year").toggleClass("close");
	});
	</script>
</body>
</html>
