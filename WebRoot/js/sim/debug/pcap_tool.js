var pcap = {
	timer:null	
}

pcap.start = function(){
	var filter = {
		nodeId:$("#pcap_node").val(),
		src:$("#pcap_filter_src").val(),
		dest:$("#pcap_filter_dest").val(),
		destPort:$("#pcap_filter_dest_port").val(),
		charset:$("#pcap_charset").combobox("getValue")
	}
	if(filter.nodeId == null || filter.nodeId == ""){
		showErrorMessage("请选择一个节点！") ;
		return ;
	}
	
	$("#pcap_start_btn").linkbutton('disable') ;
	$("#pcap_stop_btn").linkbutton('enable') ;
	$.post("/sim/pcap/start",filter,function(result){
		if(!result.success){
			showErrorMessage(result.message) ;
		}else{
			pcap.clearData() ;
			pcap.timer = window.setInterval(pcap.getPacket, 2000) ;
		}
	},"json") ;
}

pcap.stop = function(){
	$("#pcap_start_btn").linkbutton('enable') ;
	$("#pcap_stop_btn").linkbutton('disable') ;
	var param = {nodeId:$("#pcap_node").val()};
	$.post("/sim/pcap/stop",param,function(result){
		if(!result.success){
			$("#pcap_stop_btn").linkbutton('enable') ;
			showErrorMessage(result.message) ;
		}else{
			window.clearInterval(pcap.timer) ;
		}
	},"json") ;
}

pcap.getPacket = function(){
	var param = {nodeId:$("#pcap_node").val()};
	$.post("/sim/pcap/getPacket",param,function(result){
		if(result && result.length > 0){
			var rowCount = $("#pcap_data").datagrid("getRows").length ;
			for(var index in result){
				if((++rowCount) > 500){
					$("#pcap_data").datagrid("deleteRow",0) ;
				}
				$("#pcap_data").datagrid("appendRow",result[index]) ;
			}
		}
	},"json") ;
}

pcap.clearData = function(){
	$("#pcap_data").datagrid('loadData',{total:0,rows:[]});
}
window.onbeforeunload = function(){
	pcap.stop() ;
}
