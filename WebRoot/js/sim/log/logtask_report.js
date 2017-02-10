(function(){
	viewTaskResult();
})();
function viewTaskResult(){
	var taskId = simHandler.log_stat.taskId;
	 $.ajax({
        url: '/sim/logReport/viewTaskResult?taskid='+taskId,
        type: 'POST',
        dataType:'json',
        success: function(result){
        	$("#result_title").panel({title:result.subTitle+"统计结果"});
        	var host=result.host;
        	 if(!host){
        		 host="全部";
        	 }
        	 var content="<div>设备："+host+"<span style='margin-right:20px;'></span>时间：" + result.interval + 
        	 "(" + result.logIntervalStart + "至" + result.logIntervalEnd + ")";
        	 var searchCondition=result.conditionName;
        	 if(searchCondition!=""){
        		 content+="<span style='margin-right:20px;'></span>过滤条件："+searchCondition+"";
        	 }
        	 content += "</div>";
        		//表头列
            	 var col=[];
            	 $.each(result.chartData.groupFields,function(index,item){
            		 col.push({field:item.name,title:item.alias,width:80});
            	 });
    	         col.push({field:result.browseObject.functionName,title:result.chartData.statFunDesc,width:80});
           		$("#log_chart_query").html("");
           		$("#log_chart_query").html(content);
    	        $("#report_chart").show();
		   		$('#log_table_query').datagrid({   
		   			fit : false,
		   			fitColumns : true,
		   			singleSelect : true,
		   		    columns : [col],
		   	        data : result.chartData.tabledata
		   		}); 
		   		if(result.diagram != "7"){
		   			$("#chart_div").show();
           	    	$("#table_div").hide();
		   		}else{
		   			$("#chart_div").hide();
           	    	$("#table_div").show();
		   		}
           	    if(result.diagram=="1"){//图表(柱状图)
           	      showColumnCharts(result.chartData.xAxis,result.chartData.seriesData,result.chartData.unit);
           	    }
           	    if(result.diagram=="5"){//图表(饼状图)
       	           showPieCharts(result.chartData.seriesData);
           	    }
           	    if(result.diagram=="6"){//图表(折线图)
           		   showSplineCharts(result.chartData.xAxis,result.chartData.seriesData,result.chartData.unit);
           	    }
           	    if(result.diagram == "7"){
           	    	showTable(result.chartData.groupFields,result.chartData.tabledata,result.chartData.statFunDesc,result.browseObject.functionName);
           	    }
       }
  });
}
function showTable (groupFields,tableData,operColumn,functionName){
		  var tableObj = $("#table_result");
		  var table = "";
		  var td_width = 0;
		  if(groupFields.length >1){
			  td_width = 333;
		  }else{
			  td_width = 500;
		  }
	      table += '<thead style="width:1000px;display:block;background-color:#F8F8F8;font-weight:bold;">';
		  table += '<tr>';
	      $.each(groupFields,function(index,item){
		    table += "<td width="+td_width+">" + item.alias + "</td>";
	      });
	      table +="<td width="+td_width+">" + operColumn + "</td>";
	      table += "</tr>";
	      table += "</thead>";
	 	  table += "<tbody style='height:365px;width:1000px;display:block;overflow-y:auto;'>";
		  $.each(tableData,function(index,item){
			 table += '<tr>';
			 $.each(groupFields,function(groupIndex,groupItem){
				 table += "<td width="+td_width+">"+ item[groupItem.name] + "</td>";
			 });
			 table += "<td width="+td_width+">"+item[functionName]+"</td></tr>";
			 
		  });
	 
	   table += '</tbody>';
	   tableObj.html(table);
	   return tableObj;
}
//曲线图
function showSplineCharts(xaxis,seriesdata,yTitle){
	//设置图例数量
	var lengend_data = [];
	for(var i=0;i<seriesdata.length;i++){
		lengend_data[i] = seriesdata[i].name;
	}
    var option = {
    		color: simHandler.colors,
    		legend:{
    			orient: 'horizontal',
    			 x: 'center', // 'center' | 'left' | {number},
    		     y: 'bottom', 
    			data:lengend_data
    		},
		   tooltip: {
			   trigger: 'axis'
           },
           xAxis :  [
                     {
                         type : 'category',
                         data :eval(xaxis)
                     }
                 ],
           yAxis : [
                {
                    type : 'value',
                    axisLabel:{
		            	 formatter:function(val){
		            		 return val + yTitle;
		            	 }
		             }
                }
            ],
            series :seriesdata 
   };
    log_echarts.init("container", option);

}
//饼图
function showPieCharts(seriesdata){
	//设置图例数量
	var lengend_data = [];
	var series_data = [];
	var i =0;
	$.each(seriesdata,function(index,item){
			series_data.push({name:item[0],value:item[1]});
			lengend_data[i] = item[0];
		i++;
	});
    var option = {
    		color: simHandler.colors,
    		legend:{
    			orient: 'horizontal',
    			 x: 'center', // 'center' | 'left' | {number},
    		     y: 'bottom', 
    			data:lengend_data
    		},
    		 tooltip : {
    		        trigger: 'item',
    		        formatter: "{a} <br/>{b} : {c} ({d}%)"
    		    },
             
            series :[{
            	     type:'pie',
                     radius : '55%',
                     data:series_data
                     }]
   };
    log_echarts.init("container", option);
	
}
//柱状图
function showColumnCharts(xaxis,seriesdata,yTitle){
	//设置图例数量
	var lengend_data = [];
	for(var i=0;i<seriesdata.length;i++){
		lengend_data[i] = seriesdata[i].name;
	}
    var option = {
    		color: simHandler.colors,
    		legend:{
    			orient: 'horizontal',
    			 x: 'center', // 'center' | 'left' | {number},
    		     y: 'bottom', 
    			data:lengend_data
    		},
		   tooltip: {
			   trigger: 'axis'
           },
           xAxis :  [
                     {
                         type : 'category',
                         data :eval(xaxis)
                     }
                 ],
           yAxis : [
                {
                    type : 'value',
                    axisLabel:{
		            	 formatter:function(val){
		            		 return val + yTitle;
		            	 }
		             }
                }
            ],
            series :seriesdata 
   };
    log_echarts.init("container", option);

}
