<!-- 资产接口信息 -->
<%@ page language="java" pageEncoding="utf-8"%>
<table id="adt_interface${param.tabSeq}" class="easyui-datagrid"
		data-options="
				border:false,
				scrollbarSize:0,
				fitColumns:true,
				singleSelect:true,
				onLoadSuccess:generalLoadSuccess">
	<thead>
		<tr>
			<th data-options="field:'INTERFACENAME',width:150">接口名</th>
			<th data-options="field:'INTERFACE_IP',width:150">IP地址</th>
			<th data-options="field:'MAC',width:150">MAC地址</th>
			<th data-options="field:'BYTES_IN',width:100,formatter:bytesFormatter">接收流量</th>  
			<th data-options="field:'BYTES_OUT',width:100,formatter:bytesFormatter">发送流量</th>  
			<th data-options="field:'SPEED_IN',width:100,formatter:bytesFormatter">接收速度</th>  
			<th data-options="field:'SPEED_OUT',width:100,formatter:bytesFormatter">发送速度</th>  
			<th data-options="field:'LINK_SPEED',width:100">链路速度</th>  
			<th data-options="field:'DISCARD',width:100">丢弃包数</th>  
			<th data-options="field:'STATE',width:40,formatter:upDownFormatter">状态</th>  
		</tr>  
	</thead>  
</table>
<script>
function upDownFormatter(value,row,index){
	if(value && value.toLowerCase() == "up"){
		return "<span style='margin-left:9px' class='icon-status-online'></span>"
	}else{
		return "<span style='margin-left:9px' class='icon-status-offline'></span>"
	}
}
function loadInterfaceData(ip,gridId){
	assetChart.getChartData(ip,"interface",function(result){
		if(!result.result){
			return ;
		}
		var data =  $.map(result.result,function(record){
			return record ;
		}) ;
		data.sort(function(obj1,obj2){
			return obj1.INTERFACENAME > obj2.INTERFACENAME ? 1 : -1;
		}) ;
		$(gridId).datagrid("loadData",data) ;
	}) ;
}
$(function(){
	loadInterfaceData("${param.ip}", "#adt_interface${param.tabSeq}") ;
	createTimer(function(){
		loadInterfaceData("${param.ip}", "#adt_interface${param.tabSeq}") ;	
	},60000,${param.tabSeq}) ;
}) ;
</script>