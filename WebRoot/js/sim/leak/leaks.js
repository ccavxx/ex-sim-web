var queryParams = {};
var published_time = null;
(function(){
	var optionSet = {
			maxDate : moment(), //最大时间   
	        locale: locales['zh-CN'],
	        format: 'YYYY-MM-DD',
	        opens: 'left',
	};	
	var publishedCb = function(start,end){
		$('#published_begin_time').val(start.format('YYYY-MM-DD'));
	    $('#published_end_time').val(end.format('YYYY-MM-DD'));
	};
	
	$('#published_receipt_time').daterangepicker(optionSet,publishedCb);
	var mdfCb = function(start,end){
		$('#mdf_begin_time').val(start.format('YYYY-MM-DD'));
	    $('#mdf_end_time').val(end.format('YYYY-MM-DD'));
	};
	$('#mdf_receipt_time').daterangepicker(optionSet,mdfCb);
	
	initLeakTable();
	initCategory();
	$("#leakXMLId").cnfileupload();
})();
//初始化table列表
function initLeakTable(){
	$("#leaks_query_table_sel_id").datagrid({
		fit : true,
		fitColumns:true,
		singleSelect : true,
		pagination:true,
		pageSize:20,
		scrollbarSize:0,
//		rownumbers : true,
		view: detailview,
		detailFormatter:function(index,row){
             return '<div style="padding:2px"><div id="ddv'+ index +'"></div></div>';
         },
         onExpandRow: function(index,row){
        	 var ddv =  $("#ddv"+index);
        	 ddv.panel({
 			    border : false,
 			    content:'详情：'+row.detail
 			});
        	 $('#leaks_query_table_sel_id').datagrid('fixDetailRowHeight',index);
         },
		url:'/sim/leak/getAllLeaks', 
		columns:[[    
//		          {field:'id',title:'序号',width:20,align:'center'},
		          {field:'name',title:'名称',width:50,align:'left'},    
		          {field:'score',title:'级别',width:25,align:'center',formatter:function(value,index,row){
		        	  return "<span class='"+simHandler.getPriorityClassByCN(value)+"'></span>";
		          }},    
		          {field:'publishedTime',title:'发布时间',width:40,align:'left'},    
		          {field:'mdfTime',title:'修改时间',width:40,align:'left'},  
		          {field:'cpe',title:'cpe',width:200,align:'left',formatter:function(value,index,row){
		        	  return "<span title='"+ value +"'>"+ value.substring(0,200)+"</span>";
		          }},
		          {field:'summary',title:'描述',width:300,align:'left',formatter:function(value,index,row){
		        	  return "<span title='"+ value +"'>"+ value.substring(0,200)+"</span>";
		          }}
		      ]] ,
		      onLoadSuccess:function(data){
		      }
	});
	var pager = $("#leaks_query_table_sel_id").datagrid("getPager");
	pager.pagination({
		displayMsg:"显示{from}到{to},共{total}条"
		
	});
}

function importXMLFile() {
	var fileVal = $("#leakXMLId").val();
	$("#leakXMLForm").form("submit", {
        url: "/sim/leak/importXMLFile",
        onSubmit: function() {
        	if(!fileVal) {
        		$("#leakXMLMsgId").html("请选择要上传的文件！");
        	return false;
        	}
        	closeImportWindBtns();
       		return true;
        },
        dataType: "json",
        success: function(result) {
        	$("#leaks_query_table_sel_id").datagrid('reload');
        	initCategory();
        	closeImportWind();
        }
    });
}

function initCategory(){
	var tree_data =[];
	$.ajax({
		type : "post",
		url : "/sim/leak/getAllYears",
		success : function(data) {
			var dataArr = eval(data);
			for(var i=dataArr.length-1; i>=0;i--){
				tree_data.push({id:dataArr[i],text:dataArr[i]});
			}
			$("#leaks_menu_id").tree({
				data:tree_data,
				onSelect:doClear,
				onClick: function(node){
					published_time = node.text;
					queryParams = {
							publishedTime:node.text,
						}
					$("#leaks_query_table_sel_id").datagrid({queryParams:queryParams});
				}
			});
		}
	});
}
function doQueryLeak(){
	queryParams = {
			name:$("#name").val(),
			score:$("#score").val(),
			publishedTime:published_time,
			published_begin_time:$("#published_begin_time").val(),
			published_end_time:$("#published_end_time").val(),
			mdf_begin_time:$("#mdf_begin_time").val(),
			mdf_end_time:$("#mdf_end_time").val(),
	}
	$("#leaks_query_table_sel_id").datagrid({queryParams:queryParams});
}

function doClear(){
	$("#name").val("");
	score:$("#score").val("");
	$("#published_begin_time").val("");
	$("#published_end_time").val("");
	$("#mdf_begin_time").val("");
	$("#mdf_end_time").val("");
};

function openImportWind() {
	openImportWindBtns();
	$("#importLeakXML").window("open");
	$("#importLeakXMLMsgId").html("");
} 

function openImportWindBtns() {
	$("#importLeakXML_sourth_btns1").css("display", "none");
	$("#importLeakXML_sourth_btns2").css("display", "inline-block");
}

function closeImportWindBtns() {
	$("#importLeakXML_sourth_btns1").css("display", "block");
	$("#importLeakXML_sourth_btns2").css("display", "none");
}

function closeImportWind() {
	$("#importLeakXML").window("close");
	$("#importLeakXMLForm").form('reset');
}