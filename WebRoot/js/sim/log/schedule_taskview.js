
function previewSubjectResult(chartDataParams){
	FastJson.format(chartDataParams);
	$.each(chartDataParams,function(index,result){
		var xAxis = result.data.xAxis;
		var series = result.data;
		var unit = result.data.unit ;
		var title = result.subName;
		
		if(result.diagram == "1"){//图表(柱状图)
     	    showColumnCharts(index,xAxis,series,unit,title);
     	}
 	    if(result.diagram == "5"){//图表(饼状图)
           showPieCharts(index,series,unit,title);
 	    }
 	    if(result.diagram == "6"){//图表(折线图)
 		  showSplineCharts(index,xAxis,series,unit,title);
 	    }
 	    if(result.diagram == "7"){//表格
 	    	
 	    	$("#title"+index).html(title+"统计列表");
 	    	showTable(result.data.groupFields,result.data.tabledata,result.data.statFunDesc,result.browseObject.functionName,index);
 	    }
	});
}
function showTable (groupFields,tableData,operColumn,functionName,index){
	  var tableObj = $("#table"+index);
	  var table = "";
	  var td_width = 0;
	  if(groupFields.length >1){
		  td_width = 333;
	  }else{
		  td_width = 500;
	  }
    table += '<thead style="width:550px;display:block;font-weight:bold;">';
	  table += '<tr>';
    $.each(groupFields,function(index,item){
	    table += "<td width="+td_width+">" + item.alias + "</td>";
    });
    table +="<td width="+td_width+">" + operColumn + "</td>";
    table += "</tr>";
    table += "</thead>";
	  table += "<tbody style='height:300px;width:550px;display:block;overflow-y:auto;'>";
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
function showSplineCharts(index,xaxis,data,yTitle,title){
	//设置图例数量
	var lengend_data = [];
	var seriesdata = data.seriesData;
	for(var i=0;i<seriesdata.length;i++){
		lengend_data[i] = seriesdata[i].name;
	}
	
    var option = {
    		color: simHandler.colors,
    		title:{
		        text:title+"统计图",
		        //subtext: '纯属虚构',
		        x:'center'
		       // padding:[50,0,0,0]
		     },
//		     grid:{
//		    	x:50,
//		    	y:50
//		     },
    		legend:{
    			orient: 'horizontal',
    			 x: 'center', // 'center' | 'left' | {number},
    		     y: 'bottom', 
    			data:lengend_data
    		},
		   tooltip: {
			   trigger: 'axis'
           },
           toolbox:{
        	   show:true,
               orient: 'horizontal',      // 布局方式，默认为水平布局，可选为：
               x: 'right',                // 水平安放位置，默认为全图右对齐，可选为：
			   y: 'top',
			   feature : {
				   dataView : {
					   show:true,
					  // title : '数据视图',
					   readOnly: true,
					   lang : ["<label style='text-align:center;'><b>"+title+"统计列表</b></label>","收起"],
					   optionToContent: function(opt) {
						   var groupFields = data.groupFields;
						   var tableData = data.tabledata;
						   var operColumn = data.statFunDesc;
						   var functionName = data.functionName;
						   return  createTable(groupFields,tableData,operColumn,functionName);
					   }
			   }
           }
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
            noDataLoadingOption: {
            	textStyle:{
            		fontWeight:'bold'
            	},
                text: '暂无数据',
                effect: 'bubble',
                effectOption: {
                	backgroundColor:"#f1f8fb",
                    effect: {
                        n: 0
                    }
                }
    		},
            series:[0] 
   };
    if(seriesdata && seriesdata.length > 0){
    	option.series = seriesdata;
    	
    }else{
    	var baseSeries = [{
        		type:'line',
        		data:[0]	
        	}];
        	option.xAxis[0].data.push('0');
           option.series = baseSeries;
   }
    log_echarts.init("sub_chart"+index, option);
}
//饼图
function showPieCharts(index,data,unit,title){
	//设置图例数量
	var lengend_data = [];
	var series_data = [];
	var seriesdata = data.seriesData;
	var i =0;
	$.each(seriesdata,function(index,item){
			series_data.push({name:item[0],value:item[1]});
			lengend_data[i] = item[0];
		i++;
	});
    var option = {
    		color: simHandler.colors,
    		title:{
    		        text:title+"统计图",
    		        //subtext: '纯属虚构',
    		        x:'center'
    		 },
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
		    toolbox: {
		        show : true,
		        feature : {
		           // mark : {show: true},
		            dataView:{
		            	show: true,
		            	readOnly: true,
		            	lang : ["<label style='text-align:center;'><b>"+title+"统计列表</b></label>","收起"],
		            	optionToContent: function(opt) {
							   var groupFields = data.groupFields;
							   var tableData = data.tabledata;
							   var operColumn = data.statFunDesc;
							   var functionName = data.functionName;
							  return  createTable(groupFields,tableData,operColumn,functionName);
						   }
		            }
//		            magicType : {
//		                show: true, 
//		                type: ['pie', 'funnel'],
//		                option: {
//		                    funnel: {
//		                        x: '25%',
//		                        width: '50%',
//		                        funnelAlign: 'left',
//		                        max: 1548
//		                    }
//		                }
//		            },
		           // restore : {show: true},
		           // saveAsImage : {show: true}
		        }
		    },
		    noDataLoadingOption: {
		    	textStyle:{
            		fontWeight:'bold'
            	},
                text: '暂无数据',
                effect: 'bubble',
                effectOption: {
                	backgroundColor:"#f1f8fb",
                    effect: {
                        n: 0
                    }
                }
    		},
            series :[0]
   };
    if(seriesdata && seriesdata.length > 0){
    	option.series = [{
   	     type:'pie',
         radius : '55%',
         data:series_data
         }];
    }else{
		var baseSeries = [ {
			name : 0,
			type : 'pie',
			radius : '55%',
			data : [ 0 ]
		} ];
		option.series = baseSeries;
		option.calculable = true;
    }
    log_echarts.init("sub_chart"+index, option);
}
//柱状图
function showColumnCharts(index,xaxis,data,yTitle,title){
	var seriesdata = data.seriesData;
	//设置图例数量
	var lengend_data = [];
	for(var i=0;i<seriesdata.length;i++){
		lengend_data[i] = seriesdata[i].name;
	}
	//<a class='layout-button-up'>"+"</a>
    var option = {
    		color: simHandler.colors,
    		title:{
		        text:title+"统计图",
		        //subtext: '纯属虚构',
		        x:'center'
		 },
//		 grid:{
//			 backgroundColor:'#fff'
//		 },
    		legend:{
    			orient: 'horizontal',
    			 x: 'center', // 'center' | 'left' | {number},
    		     y: 'bottom', 
    			data:lengend_data
    		},
		   tooltip: {
			   trigger: 'axis'
           },
           toolbox:{
        	   show:true,
               orient: 'horizontal',      // 布局方式，默认为水平布局，可选为：
               x: 'right',                // 水平安放位置，默认为全图右对齐，可选为：
			   y: 'top',
			   feature : {
				   dataView : {
					   show:true,
					  // title : '数据视图',
					   readOnly: true,
					   lang : ["<label style='text-align:center;'><b>"+title+"统计列表</b></label>",
					           "收起"],
					   optionToContent: function(opt) {
						   var groupFields = data.groupFields;
						   var tableData = data.tabledata;
						   var operColumn = data.statFunDesc;
						   var functionName = data.functionName;
						   return  createTable(groupFields,tableData,operColumn,functionName);
					   }
			   }
           }
           },
           xAxis :  [
                     {
                         type : 'category',
                         data : eval(xaxis),
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
           
    		noDataLoadingOption: {
    			textStyle:{
            		fontWeight:'bold'
            	},
                text: '暂无数据',
                effect: 'bubble',
                effectOption: {
                 	backgroundColor:"#f1f8fb",
                    effect: {
                        n: 0
                    }
                }
    		},
            series :[0] 
   };
    if(seriesdata && seriesdata.length > 0){
    	option.series = seriesdata;
    }else{
    	var baseSeries = {
    		type:'bar',
    		data:[0]	
    	};
    	option.xAxis[0].data.push('0');
       option.series.push(baseSeries);
    }
    log_echarts.init("sub_chart"+index, option);
	
	
}
function createTable(groupFields,tableData,operColumn,functionName){
	  var table = '<table style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
		table += '<tr style="border-bottom:1px dotted #ccc;">';
	  $.each(groupFields,function(index,item){
  		 table += "<td align='left'><b>" + item.alias + "</b></td>";
  	 });
  	 
  	 table +="<td  align='left'><b>" + operColumn + "</b></td></th></tr><tbody>";
  	 $.each(tableData,function(index,item){
  		 table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
  		 $.each(groupFields,function(groupIndex,groupItem){
  			 table += "<td align='left'>"+ item[groupItem.name] + "</td>";
  		 });
  		table += "<td align='left'>"+item[functionName]+"</td></tr>";
  		 
  	 });
  	 
	   table += '</tbody></table>';
	   return table;
}