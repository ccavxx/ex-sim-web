var worldMap = new EchartsModel();
function initWorld() {
	var worldChartContext = ['echarts', 'echarts/chart/map'];
	require(worldChartContext, function(echarts) {
		var worldChart = worldMap.buildChart(echarts, '#world', setWorldOption);
		if(system.userName == "operator"){
		   worldChart.on("click",onWorldDataClick) ;
		}
		worldMap.onLegendSelect() ;
		var params = {'mapType' : 'country', 'parentName' : ''};
		worldMap.initData(worldChart, '/sim/index/getMapData', params, updateWorldChartData);
		worldMap.onLegendSelect() ;
	});
	
}
function onWorldDataClick(params){
	var selectLegend = worldMap.getSelectLegend()[0] ;
	var field = selectLegend == "源地址" ? 
			{name:"SRC_ADDRESS_COUNTRY",alias:"源国家",type:"String",value:params.name,operator:"等于"} : 
			{name:"DEST_ADDRESS_COUNTRY",alias:"目的国家",type:"String",value:params.name,operator:"等于"}  ;
	var handlerParam = {
		logQueryParam:{
			queryStartDate:moment(simHandler.serverTime).format("YYYY-MM-DD 00:00:00"),
			queryEndDate:moment(simHandler.serverTime).format("YYYY-MM-DD 23:59:59"),
			securityObjectType:null,
			condition:[field]
		}
	} ;
	openLogQueryWindow(handlerParam) ;
}
function updateWorldChartData(worldChart, result, params){
	var options = worldChart.getOption();
	if (result.success) {
		options.series[0].markPoint.data = result.srcData;
		options.series[1].markPoint.data = result.destData;
		options.dataRange.max = worldMap.getDataRangeMax(result.srcData, result.destData);
	}
	worldChart.setOption(options, true);
}
function setWorldOption() {
	return option = {
		tooltip : {
			trigger : 'item',
			formatter : function(param) {
				return param.name + "(" + param.data.value + "次)";
			},
		},
		toolbox : {
			show : true,
			orient : 'vertical',
			x : 'right',
			y : 'bottom',
			feature : {
				dataView : {
        	        show: true, 
        	        readOnly: true,
        	        lang : [" <label style='text-align:center;' id='world_title'></label>","返回"],
 				    optionToContent: function(opt) {
 				    	var legend = worldMap.getSelectLegend()[0] ;
 				    	$("#world_title").html("<b>"+legend+"分布情况</b>");
 				        var table = '<table style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
 				    	   table += '<tr style="border-bottom:1px dotted #ccc;">';
 				    	   table += "<td align='left'><b>地区</b></td>";
 				    	   table += "<td align='left'><b>数量</b></td></th></tr>";
 				    	   $.each(opt.series,function(i,item){
				    	  		 if(item.name == legend){
				    	  			var datas = item.markPoint.data ? item.markPoint.data.slice(0) : [] ;
				    	  			sort(datas,"value","desc") ;
				    	  			$.each(datas,function(index,result){
				    	  				table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
				    	  				table += "<td align='left'>"+ result.name + "</td>";
				    	  				table += "<td align='left'>"+ result.value + "</td></tr>";
				    	  			 });
				    	  		 }
				    	  	    });
 				    	table += '</table>';
 				    	
 				    	return   table;
 					 }
        	        }
          },
				restore : {},
				saveAsImage : {}
		},
		legend : {
			orient : 'vertical',
			x : 'left',
			selectedMode:'single',
			selected:{'源地址':false,'目的地址':true},
			data : [ '源地址', '目的地址']
		},
		dataRange : {
			min : 0,
			max : 5000,
			calculable : true,
			color : [ 'red', 'orange', 'yellow', 'lightgreen' ]
		},
		noDataLoadingOption: {
            text: '暂无数据',
            effect: 'bubble',
            effectOption: {
                effect: {
                    n: 0
                }
            }
		},
		series : [ 
		{
			name : '源地址',
			type : 'map',
			mapType : 'world',
			roam : false,
			data : [],
			nameMap : worldGeoInfo.countryNameMap,
			itemStyle : {
				normal : {
					borderColor : '#ffffff',
					borderWidth : 1,
					areaStyle : {
						color : '#c5dce8'
					}
				},
				emphasis : {
					label : {
						show : true
					}
				}
			},
			markPoint : {
				symbol : 'emptyCircle',
				symbolSize : worldMap.getSymbolSize,
				effect : {
					show : true,
					shadowBlur : 0
				},
				itemStyle : {
					normal : {
						borderColor : '#87cefa',
						borderWidth : 1, // 标注边线线宽，单位px，默认为1
						label : {
							show : false
						}
					},
					emphasis : {
						borderColor : '#1e90ff',
						borderWidth : 5,
						label : {
							show : false
						}
					}
				},
				data : []
			},
			geoCoord : worldGeoInfo.countryGeoCoordMap
		},
		{
			name : '目的地址',
			type : 'map',
			mapType : 'world',
			roam : false,
			itemStyle : {
				emphasis : {
					label : {
						show : true
					}
				}
			},
			data : [],
			nameMap : worldGeoInfo.countryNameMap,
			itemStyle : {
				normal : {
					borderColor : '#ffffff',
					borderWidth : 1,
					areaStyle : {
						color : '#c5dce8'
					}
				},
				emphasis : {
					label : {
						show : true
					}
				}
			},
			markPoint : {
				symbol : 'emptyCircle',
				symbolSize : worldMap.getSymbolSize,
				effect : {
					show : true,
					shadowBlur : 0
				},
				itemStyle : {
					normal : {
						label : {
							show : false
						}
					},
					emphasis : {
						label : {
							show : true
						}
					}
				},
				data : []
			}
//			, geoCoord : worldGeoInfo.countryGeoCoordMap
		}]
	};
}