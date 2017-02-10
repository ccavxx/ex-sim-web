<%@page import="com.topsec.tsm.common.SIMConstant"%>
<%@page import="com.topsec.tsm.util.encrypt.RSAUtil"%>
<%@page import="com.topsec.tsm.sim.util.CommonUtils"%>
<%@page contentType="text/html; charset=utf8" pageEncoding="utf8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "
http://www.w3.org/TR/html4/strict.dtd">
<html lang="zh-CN">
  <head>
    <meta charset="utf-8">
    <title><%=CommonUtils.getProductName() %></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Bootstrap -->
    <link href="/css/bootstrap.css" rel="stylesheet" media="screen">
    <!-- EasuUI -->
    <link href="/js/jquery-easyui/themes/bootstrap/easyui.css" rel="stylesheet" type="text/css">
    <link href="/js/jquery-easyui/themes/icon.css" rel="stylesheet" type="text/css">
    <!-- sim-icon -->
    <link href="/css/sim-icon.css" rel="stylesheet" type="text/css">
    <!-- Validation -->
    <link href="/js/validator/jquery.validator.css" rel="stylesheet" type="text/css">
    <!-- datetimepicker -->
    <link href="/js/datetimepicker/bootstrap-datetimepicker.min.css" rel="stylesheet">
    <!-- daterangepicker -->
    <link href="/js/datetimepicker/daterangepicker-bs2.css" rel="stylesheet">
    <!-- datepicker -->
    <link href="/js/datetimepicker/bootstrap-datepicker.css" rel="stylesheet">
    <!-- timepicker -->
    <link href="/js/datetimepicker/bootstrap-timepicker.min.css" rel="stylesheet">
    <link href="/css/system.css" rel="stylesheet">
    <link href="/css/report.css" rel="stylesheet"/>
	<script src="/js/global/base64.js" type="text/javascript"></script>
	<script src="/js/global/cryptico.js" type="text/javascript"></script>
    <script src="/js/sim/init.js"></script>
    <script type="text/javascript">
		system.token = "${sessionScope.session_token}" ;
		system.locked = ${locked} ;
		system.hasOperatorRole = ${hasOperatorRole} ;
		system.isOperator = ${isOperator} ;
		system.init("<%=RSAUtil.getB64CachedPublicKey(SIMConstant.COMMON_KEY_PAIR_CACHE_ID,"|")%>",<%=RSAUtil.DEFAULT_KEY_LENGTH%>) ;
		system.forwardParams = "${param.forwardParams}" ;
		system.userName = '${userName}';
    </script>
  </head>
  <body style="padding-top: 88px; overflow: hidden;">
    <div class="navbar navbar-fixed-top sim-header">
      <div class="navbar-inner sim-sys-state">
        <div class="logo" style="background: url('<%=CommonUtils.getCompanyLogo()%>') no-repeat;" href="javascript:void(0)"></div>
        <ul class="nav pull-right">
          <li class="sim-sys-state-ctnr-left" style="background: none;"/>
          <li class="sim-sys-state-ctnr" style="background: none;">
            <div style="top: 4px;">
              <div style="margin-top: 2px;"><a style="cursor:default;" href="javascript:void(0)" title="今日日志数量"><span id="logCount" class="label label-normal" style="width:85px;">日志:加载中...</span></a></div>
              <div><a style="cursor: ${hasOperatorRole ? 'pointer' : 'default'}" href="javascript:void(0)" onclick="simHandler.showRecentEvent();" title="今日事件数量"><span id="eventCount" class="label label-normal" style="width:85px;"></span></a></div>
            </div>
          </li>
          <li class="sim-sys-state-ctnr" style="background: none;"><div id="log_flow" style="padding-top:2px;width:150px;height:38px;"></div></li>
          <li class="sim-sys-state-ctnr" style="background: none;"><div id="top_cpu_mem_container" style="top:0px;width: 270px; height: 45px;"></div></li>
		  <li class="sim-sys-state-ctnr-right" style="background: none;"/>
      	</ul>
      </div>
      <div class="navbar-inner sim-menu" id="header-nav">
      	  <ul id="topMenu" class="nav navbar-left">
      	  		<li class="sim-menu-left"/>
	      	  	<li><div style="width:80px;"></div></li>
	      	  	<c:forEach var="sysMenu" items="${setSysTreeMenus}">
		      	  	
		      	  	<li class="${sysMenu.menuAttributes.liClass }">
		      	  		
			            <c:choose>
			      	  		<c:when test="${!empty sysMenu.menuUrl}">
			      	  			<a href="javascript:void(0)" class="${sysMenu.menuAttributes.aClass }" onclick="simHandler.changeMenu(event,'${sysMenu.menuUrl}')">
			      	  			<i class="${sysMenu.menuAttributes.iClass }" ></i>${sysMenu.menuName}<b class="${sysMenu.menuAttributes.bClass }"></b></a>
			      	  		</c:when>
			      	  		<c:otherwise>
			      	  			<a href="javascript:void(0)" class="${sysMenu.menuAttributes.aClass }">
			      	  			<i class="${sysMenu.menuAttributes.iClass }" ></i>${sysMenu.menuName}<b class="${sysMenu.menuAttributes.bClass }"></b></a>
			      	  		</c:otherwise>
			      	  	</c:choose>
			            
			            <c:if test="${!empty sysMenu.children}">
		      	  			<ul class="${sysMenu.menuAttributes.ulClass }">
				              <c:forEach var="childrenMenu" items="${ sysMenu.children}">
				              	<c:choose>
				              		<c:when test="${childrenMenu.menuName eq '日志摘要' && not sessionScope.isDefaultUser}">
				              			
				              		</c:when>
				              		<c:otherwise>
							            <li><a href="javascript:void(0)" id="${childrenMenu.menuLiId}" onclick="simHandler.changeMenu(event,'${childrenMenu.menuUrl}')">${childrenMenu.menuName}</a></li>
				              		</c:otherwise>
				              	</c:choose>
				              </c:forEach>
				            </ul>
	      	  			</c:if>
			            
	          		</li>
	      	  	</c:forEach>
	      	  	<c:if test="${sessionScope.debug}">
	      	  		<li class="dropdown">
	      	  			<a href="javascript:void(0)" class="dropdown-toggle" onclick="simHandler.changeMenu(event,'/page/debug/debug.jsp')">调试</a>
	      	  		</li>
	      	  	</c:if>
      	  </ul>
	      
	      <ul class="nav pull-right navbar-right">
            <li><div id="server_time"></div></li>
	      	<li class="sim-menu-join"/>
            <li class="sim-menu-right-bg" style="width:100px;">
             <div class="user-info"><i class="icon-user hand" id="showOnlineUsersId"></i><span>${sessionScope.sid.userName}</span></div>
            </li>
            <li class="sim-menu-right-bg"><a href="javascript:void(0)" title="关于" onclick="simHandler.changeMenu(event,'/sim/systemConfig/aboutUsView')" ><span class="sim-icon-info"></span></a></li>
            <!-- <li class="sim-menu-right-bg"><a href="javascript:void(0)" title="帮助"><span class="sim-icon-help"></span></a></li> -->
            <li class="sim-menu-right-bg"><a href="/sim/userLogin/logout" title="退出"><span class="sim-icon-exit"></span></a></li>
            <li class="sim-menu-right"/>
	      </ul>
		</div>
    </div>
    <div id="main_panel" style="overflow-y:auto;"></div>
    <div class="footer"><span class="footer-left"></span><span class="footer-text">版权所有：<%=CommonUtils.getCompanyName() %></span><span class="footer-right"></span></div>
    <script src="/js/global/jquery-1.8.3.js"></script>
    <script src="/js/global/bootstrap.js"></script>
	<script type="text/javascript" src="/js/global/highstock.js"></script>
	<script type="text/javascript" src="/js/global/highcharts-more.js"></script>
	<script type="text/javascript" src="/js/global/highcharts-3d.js"></script>
	<script type="text/javascript" src="/js/global/highcharts.config.js"></script>
	<script type="text/javascript" src="/js/global/solid-gauge.js"></script>
	<script type="text/javascript" src="/js/global/jquery.sparkline.min.js"></script>    
  	<script src="/js/jquery-easyui/jquery.easyui.js"></script>
    <script src="/js/jquery-easyui/datagrid-filter.js"></script>
    <script src="/js/jquery-easyui/datagrid-detailview.js"></script>
  	<script src="/js/jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
    <script src="/js/validator/jquery.validator.js"></script>
    <script src="/js/validator/local/zh_CN.js"></script>
    <script src="/js/datetimepicker/bootstrap-datetimepicker.min.js"></script>
    <script src="/js/datetimepicker/bootstrap-datetimepicker.zh-CN.js"></script>
    <script src="/js/datetimepicker/moment.min.js"></script>
    <script src="/js/datetimepicker/daterangepicker.js"></script>
    <script src="/js/datetimepicker/bootstrap-datepicker.js"></script>
    <script src="/js/datetimepicker/bootstrap-timepicker.min.js"></script>
    <script src="/js/global/jquery.qtip-1.0.0-rc3.min.js"></script>
    <script src="/js/global/ajaxfileupload.js"></script>
    <script src="/js/global/system.js"></script>
    <script src="/js/global/json2.js"></script>
    <script src="/js/echart/echarts.js"></script>
    <script src="/js/sim/index.js"></script>
    <script type="text/javascript" src="/js/My97DatePicker/WdatePicker.js"></script>
    <script type="text/javascript" src="/js/cnfileupload.js"></script>
  </body>
</html>