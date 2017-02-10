var adl = {
	
};
adl.loadLogData = function(params){
	$.ajax({url:"/sim/logSearch/doLogSearch",
			data:JSON.stringify(params.queryParams),
			contentType:"text/javascript",
			type:"POST",
			dataType:"json",
			success:function(result){
				params.queryCount = params.queryCount + 1 ;
				if(params.queryCount > 10){
					$("#adt_asset_log_div"+params.tabSeq).append($("<div class='alert'/>").html("查询日志超时！"));
					return ;
				}
				if(result.finished||result.displayCount>=params.pageSize){
					adl.createTable(result,params) ;
					adl.cancelQuery(params) ;
				}else{
					setTimeout(function(){
						adl.loadLogData(params) ;
					}, 1000)
				}
			}
	}) ;
};
/**
 * 取消后台的日志检索任务
 */
adl.cancelQuery = function(params){
	params.queryParams.cancel = true ;
	$.ajax({url:"/sim/logSearch/doLogSearch",
		    type:"POST",
		    dataType:"json",
		    data:JSON.stringify(params.queryParams),
		    contentType:"text/javascript"}) ;
} ;
adl.createTable = function(data,params){
	//定义列集、数据集，并初始化
	var cols = [], rows = [], colslength = {}; 
	if(!!data.maps){
		rows = $.map(data.maps,function(row){
			var _row = {};
			for(var key in row){
				_row[key] = row[key];
				if(!!colslength[key]){
					if(row[key] && row[key].length > colslength[key]){
						colslength[key] = row[key].length;
					}
				}else if(row[key]){
					colslength[key] = row[key].length;
				}
			}
			return _row;
		});
	}
	
	if(!!data.columns){
		cols = $.map(data.columns,function(col){
			var fieldName = col.dataField.toLowerCase() ;
			var columnDataLength = colslength[col.dataField];
			var formatter = null ;
			if(fieldName == "priority"){
				formatter = simHandler.cnLevelFormatter ; 
			}else if(columnDataLength > 20){
				formatter = adl.tooltipFormatter ;
			}
			//计算字段宽度，最小宽度为5
			return {field:col.dataField,title:col.headerText,formatter:formatter,width:10};
		});
	}
	adl.createLogTable(cols,rows,params);
};
adl.tooltipFormatter = function(value,row,index){
	var newValue = html2Escape(value) ;
	return "<div title=\""+newValue+"\">" + newValue + "</div>" ;
}
/**
 * 根据列集和数据集创建日志查询表格
 * @param cols 列集
 * @param rows 数据集合
 */
adl.createLogTable = function(cols,rows,params){
	if(rows.length == 0){
		$("#adt_asset_log_div"+params.tabSeq).append($("<div class='alert'/>").html("没有找到日志！"));
		return ;
	}
	$("#adt_asset_log_table"+params.tabSeq).datagrid({   
		border : false,
		fit : true,
		fitColumns:true,
		striped : true,
		//rownumbers : true,
		singleSelect : true,
	    columns : [cols],
	    data : rows
	});  		
};