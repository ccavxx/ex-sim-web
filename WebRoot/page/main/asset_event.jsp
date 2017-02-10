<%@ page language="java" pageEncoding="utf-8"%>
<table id="top_event_table" style="min-height: 100px;" class="easyui-datagrid" 
       data-options="height:330,fitColumns:true,singleSelect:true,url:'/sim/assetdetail/event?date=today&ip=${param.ip}',
       				 pagePosition:'bottom',pagination:true,onDblClickRow:simHandler.eventRowDBClickHandler,scrollbarSize:0">
	<thead>  
		<tr>  
			<th data-options="field:'createTime',width:150">时间</th>  
			<th data-options="field:'priority',width:50,align:'center',formatter:simHandler.levelFormatter">级别</th>  
			<th data-options="field:'NAME',width:80,formatter:simHandler.eventNameFormatter">事件名称</th>  
			<th data-options="field:'SRC_ADDRESS',width:90">源地址</th>  
			<th data-options="field:'DEST_ADDRESS',width:90">目的地址</th>
			<th data-options="field:'DVC_ADDRESS',width:90">设备地址</th> 
			<th data-options="field:'DESCR',width:220,formatter:simHandler.eventDescFormatter">描述</th>
			<th data-options="field:'no_field',width:60,align:'center',formatter:simMainHandler.eventOperatorByAsset">操作</th>
			<!--  <th data-options="field:'cat1',width:110">一级分类</th>  -->
		</tr>  
	</thead>  
</table>
