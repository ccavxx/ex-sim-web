$(function(){
	buildIpLocationMap() ;
}) ;
function buildIpLocationMap(){
	var option = buildMapOption('IP数量分布', []) ;
	var ipLocationChart = log_echarts.init("backup_logIpLocationMap",option) ;
	//option对象中存储了查询条件
	var option = $("#backup_ipLocationMapDialog").dialog("options");
	$.getJSON('/sim/searchHistoryLog/doIpLocationStat',option.params,	
        function(result){
			ipLocationChart.setSeries([{markPoint:{data:result}}]) ;
    	}
	);   
}
function buildMapOption(title,data){
	var option = {
		    title : {
		        text: title,
		        x:'center'
		    },
		    tooltip : {
		        trigger: 'item'
		    },
		    legend: {
		        orient: 'vertical',
		        x:'left',
		        data:["数量"]
		    },
		    dataRange: {
		        min : 0,
		        max : 100,
		        calculable : true,
		        color: ['maroon','purple','red','orange','yellow','lightgreen']
		    },
		    toolbox: {
		        show : true,
		        feature : {
		            dataView : {
		            	 readOnly: true,
		            	 show:true,
						 lang : ["<label style='text-align:center;'><b>"+title+"统计列表</b></label>","返回"],
						 optionToContent: function(opt) {
							 return  createTableBackupLocation(opt.series[0].markPoint.data, title);
						 }
		            },
		            exp : {
		                show : true,
		                title : '导出',
		                icon : 'image://../../img/icons/ecxel.png',
		                onclick : function (params){
		                	exportBackupLocation('xls');
		                }
		            }
		        }
		    },
		    series : [
		        {
		            name: "数量",
		            type: 'map',
		            mapType: 'china',
		            hoverable: false,
		            roam:true,
		            data : [],
		            itemStyle:{ normal : { label : {show:true} } },
		            markPoint : {
		                symbolSize: 5,       // 标注大小，半宽（半径）参数，当图形为方向或菱形则总宽度为symbolSize * 2
		                itemStyle: {
		                    normal: {
		                        borderColor: '#87cefa',
		                        borderWidth: 1,            // 标注边线线宽，单位px，默认为1
		                        label: {
		                            show: false
		                        }
		                    },
		                    emphasis: {
		                        borderColor: '#1e90ff',
		                        borderWidth: 5,
		                        label: {
		                            show: false
		                        }
		                    }
		                },
		                data : data
		            },
		            geoCoord: cityGeoCoordMap
		        }
		    ]
		};
	return option ;
}
function createTableBackupLocation(tableData, title){
	var table = '<table style="text-align:center;width:90%;border-bottom:1px dotted #ccc;margin:15px 0px 0px 20px;">';
		table += '<tr style="border-bottom:1px dotted #ccc;">';
		table += "<td>地区</td><td>数量</td>";
		table += "</th></tr><tbody>";
	sort(tableData,"value","desc") ;
	 $.each(tableData,function(index,item){
		 table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
		 table += "<td>"+item["name"]+"</td><td>"+item["value"]+"</td>";
		 table += "</tr>";
	 });
	 table += '</tbody></table>';
	 return table;
}
function exportBackupLocation(type){
	window.location = "/sim/searchHistoryLog/exportMapLog?exportType="+type;
}