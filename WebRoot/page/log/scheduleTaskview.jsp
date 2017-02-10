<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/page/common/taglib.jsp" %>
<script src="/js/sim/log/log_echarts.js"></script>
<style  type="text/css">
  .btn{
      padding: 2px 15px;
      background-color: none;
  }
#backDiv{
       position:fixed;
       bottom:50px;
       right:40px;
}
</style>
<div>
  <c:forEach items="${subjectList}" var="sub" varStatus="status">
        <div id="sub_chart${status.index}" style="height:400px;width:49%;float: left;padding-top:20px;">
          <h4 id="title${status.index}" style="text-align:center"></h4>
          <table id="table${status.index}" class="table" style="height:300px;width:60%;margin-left:80px;border:1px solid #ccc;" ></table>
         <c:if test="${(status.index+1)%2==0} "> 
         </div><div id="sub_chart${status.index}" style="height:430px;width:48%;float: left;padding-top:20px;">
         <h4 id="title${status.index}" style="text-align:center"></h4>
          <table id="table${status.index}" class="table" style="height:300px;width:60%;margin-left:80px;border:1px solid #ccc;" ></table>
        </c:if> 
        
		</div>
  </c:forEach>
</div>
<div id="backDiv"> 
     <a class="btn" onclick="closeScheduleDialog()">返回</a>
</div>
<script src="/js/sim/log/schedule_taskview.js"></script>
<script type="text/javascript" src="/js/global/FastJson.js"></script>

<script>
	$(function(){
		var chartData = ${subjectList} ;
		previewSubjectResult(chartData) ;
	}) ;
</script>

