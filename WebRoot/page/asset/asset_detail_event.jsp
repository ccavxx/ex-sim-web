<%@ page language="java" pageEncoding="utf-8"%>
<table id="adt_event_list${param.tabSeq}" class="easyui-datagrid"
       data-options="scrollbarSize:0,fitColumns:true,singleSelect:true,url:'/sim/assetdetail/event?date=week&ip=${param.ip}',
                     pagination:true,pagePosition:'top',title:'最近一周事件信息',onDblClickRow:simHandler.openEventDetailFEM,onLoadSuccess:generalLoadSuccess">
	<thead>  
		<tr>  
			<th data-options="field:'createTime',width:100">时间</th>  
			<th data-options="field:'priority',width:30,formatter:simHandler.levelFormatter">等级</th>  
			<th data-options="field:'NAME',width:80,formatter:simHandler.eventNameFormatter">事件名称</th>  
			<th data-options="field:'DVC_ADDRESS',width:80">设备地址</th>  
			<th data-options="field:'SRC_ADDRESS',width:80">源地址</th>  
			<th data-options="field:'DEST_ADDRESS',width:80">目的地址</th>  
			<th data-options="field:'cat1',width:80">一级分类</th>  
			<th data-options="field:'cat2',width:80">二级分类</th>  
			<th data-options="field:'DESCR',width:250,formatter:simHandler.eventDescFormatter">描述</th>  
		</tr>  
	</thead>  
</table>
