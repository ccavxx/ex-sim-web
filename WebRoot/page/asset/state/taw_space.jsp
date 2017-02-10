<!-- taw　空间使用情况-->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<table id="adt_taw_space${param.tabSeq}" class="easyui-datagrid" data-options="url:'/sim/assetdetail/assetAttrGroupStatus?ip=${param.ip}&group=taw',fitColumns:true">
   <thead>  
       <tr>  
           <th data-options="field:'label',width:150">名称</th>  
           <th data-options="field:'value',width:150">使用率</th>  
       </tr>  
   </thead>  	
</table>
<script>

</script>