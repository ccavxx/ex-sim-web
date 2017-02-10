<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<table class="easyui-datagrid"
       data-options="scrollbarSize:0,fitColumns:true,singleSelect:true,url:'/sim/leak/getByCpe?cpe=${param.cpe}'">
	<thead>  
		<tr>  
			<th data-options="field:'name',width:60">名称</th>  
			<th data-options="field:'score',width:20">评分</th>  
			<th data-options="field:'publishedTime',width:65">发布时间</th>  
			<th data-options="field:'mdfTime',width:65">修改时间</th>  
			<th data-options="field:'summary',width:200,formatter:simHandler.eventDescFormatter">描述</th>  
		</tr>  
	</thead>  
</table>
