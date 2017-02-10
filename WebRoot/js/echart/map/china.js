var currentMapType;
var chinaMap = new EchartsModel();
function initChina() {
	var chinaChartContext = ['echarts', 'echarts/chart/map'];
	require(chinaChartContext, function(echarts) {
		currentMapType = 'china';
		var chinaChart = chinaMap.buildChart(echarts, '#china', setChinaOption);
		var params = { 'mapType' : 'province', 'parentName' : '中国' }
		chinaMap.initData(chinaChart, '/sim/index/getMapData', params, updateChinaChartData);
		chinaChart.on('mapSelected', mapSelect);
		if(system.userName == "operator"){
			   chinaChart.on("click",onChinaDataClick) ;
		}
		chinaMap.onLegendSelect(function(param) {
			var options = chinaChart.getOption();
			options.legend.selected = param.selected;
			chinaChart.setOption(options, true);
		});
	});
}

function onChinaDataClick(params){
	if(params.seriesName == ""){
		return ;
	}
	var selectLegend = chinaMap.getSelectLegend()[0] ; 
	var queryField ;
	if(selectLegend == "源地址"){
		if(currentMapType == "china"){
			queryField = {name:"SRC_ADDRESS_PROVINCE",alias:"源省份",type:"String",value:params.name,operator:"等于"} ; 
		}else{
			queryField = {name:"SRC_ADDRESS_CITY",alias:"源城市",type:"String",value:params.name,operator:"等于"} ;
		}
	}else{
		if(currentMapType == "china"){
			queryField = {name:"DEST_ADDRESS_PROVINCE",alias:"目的省份",type:"String",value:params.name,operator:"等于"} ; 
		}else{
			queryField = {name:"DEST_ADDRESS_CITY",alias:"目的城市",type:"String",value:params.name,operator:"等于"} ;
		}
	}
	var handlerParam = {
		logQueryParam:{
			queryStartDate:moment(simHandler.serverTime).format("YYYY-MM-DD 00:00:00"),
			queryEndDate:moment(simHandler.serverTime).format("YYYY-MM-DD 23:59:59"),
			securityObjectType:null,
			condition:[queryField]
		}
	} ;
	openLogQueryWindow(handlerParam) ;
}

function updateChinaChartData(chinaChart, result, params){
	var options = chinaChart.getOption();
	if (result.success) {
		if(params.mapType == 'city'){
			options.series[2].markPoint.data = result.srcData;
			options.series[3].markPoint.data = result.destData;
		} else if(params.mapType == 'province'){
			options.series[0].markPoint.data = result.srcData;
			options.series[1].markPoint.data = result.destData;
		}
		options.dataRange.max = chinaMap.getDataRangeMax(result.srcData, result.destData);
		chinaChart.setOption(options, true);
	}
}

/** 获取选中节点列表 */
function getSelect(selected){
	var result = [];
	for ( var node in selected) {
		if (selected[node]) {
			result.push(node);
		}
	}
	return result;
}
/** 获取选中省份 */
function getProvince(allMapType, selectNode){
	var result = 'china';
	for( var node in selectNode){
		if($.inArray(selectNode[node], allMapType) >= 0){
			result = selectNode[node];
		}
	}
	return result;
}
/** 地图选择函数 */
function mapSelect(param) {
	var chinaChart = this ;
	if(param){
		var options = chinaChart.getOption();
		var selected = getSelect(param.selected);
		if(selected.length == 0 || selected == '南海诸岛' || selected == '台湾'){
			return;
		}
		if (currentMapType == 'china') {
			// 全国选择时指定到选中的省份
			currentMapType = getProvince(chinaMapType, selected);
			options.series[0].mapLocation = { x: 'left', y: 'top', width: '30%'};
			options.series[1].mapLocation = { x: 'left', y: 'top', width: '30%'};
			options.series[2] = {
				name : '源地址',
				type : 'map',
				mapType : currentMapType,
				data : [],
				selectedMode : 'single',
				itemStyle : {
					normal : {
//						label : {
//							show : true
//						},
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
				mapLocation: {x: '40%'},
				markPoint : {
					symbol : 'emptyCircle',
					symbolSize : chinaMap.getSymbolSize,
					// smooth:true,
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
				},
				geoCoord : cityGeoCoordMap
			};
			options.series[3] = {
					name : '目的地址',
					type : 'map',
					mapType : currentMapType,
					data : [],
					selectedMode : 'single',
					itemStyle : {
						normal : {
//							label : {
//								show : true
//							},
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
					mapLocation: {x: '40%'},
					markPoint : {
						symbol : 'emptyCircle',
						symbolSize : chinaMap.getSymbolSize,
						// smooth:true,
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
					},
					geoCoord : cityGeoCoordMap
			};
		} else {
			var currentSelect = getProvince(chinaMapType, selected);
			//selected.length=2表示当前为两个地图
			//selected.length=1表示当前为一个地图，getProvince返回china表示两个地图，省为非选中状态
			if(selected.length > 1){
				currentMapType = 'china';
				options.series[0].mapType = options.series[1].mapType = currentMapType;
				options.series[0].mapLocation = options.series[1].mapLocation = { x: 'center', y: 'top', width: '100%'};
				options.series.splice(3);
				options.series.splice(2);
			} else {
				if(currentSelect == 'china'){
					currentMapType = 'china';
					options.series[0].mapType = options.series[1].mapType = currentMapType;
					options.series[0].mapLocation = options.series[1].mapLocation = { x: 'center', y: 'top', width: '100%'};
					options.series.splice(3);
					options.series.splice(2);
				} else {
					if(currentSelect != currentMapType){
						currentMapType = currentSelect;
						options.series[2].mapType = options.series[3].mapType = currentMapType;
					}
				}
			}
		}
		chinaChart.setOption(options, true);
		if (currentMapType == 'china') {
			var params = { 'mapType' : 'province', 'parentName' : '中国' }
			new EchartsModel().initData(chinaChart, '/sim/index/getMapData', params, updateChinaChartData);
		} else {
			var params = { 'mapType' : 'city', 'parentName' : currentMapType }
			new EchartsModel().initData(chinaChart, '/sim/index/getMapData', params, updateChinaChartData);
		}
	}
}	
/** 设置地图参数 */
function setChinaOption() {
	return option = {
		loadingText:"数据读取中...",
		tooltip:{
				trigger: 'item',
				formatter:function(param){
					for(var index in this.getOption().legend.selected){
						if(this.getOption().legend.selected[index]){
							target = index;
						}
					}
					if(param.indicator){
						return param.name + "("+target+ ":" + param.data.value + ")" ;
					}else{
						var datas = this.getOption().series[1].markPoint.data ;
						var target;
						if(datas && datas.length > 0){
							for(var index in datas){
								var record = datas[index] ;
								if(record.name.indexOf(param.name) == 0){
									return param.name + "(" + target+ ":" + record.value+")" ;
								}
							}
						}
					}
					return param.name ;
				}
		},
		legend : {
			orient : 'vertical',
			x : 'left',
			selectedMode:'single',
			selected:{'源地址':false,'目的地址':true},
			data : [ '源地址', '目的地址' ]
		},
		dataRange : {
			min : 0,
			max : 5000,
			calculable : true,
			color : [ 'red', 'orange', 'yellow', 'lightgreen' ]
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
		            	        lang : [" <label style='text-align:center;' id='china_title'><b>"+legend + "分布情况</b></label>","<label id='testbtn'>返回</label>"],
		     				    optionToContent: function(opt) {
		     				    	var legend = chinaMap.getSelectLegend()[0] ;
		     				    	$("#china_title").html("<b>"+legend+"分布情况</b>");
		     				    	$("#testbtn").remove();
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
		        }
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
		series : [ {
			name : '源地址',
			type : 'map',
			mapType : 'china',
			data : [],
			selectedMode : 'single',
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
				symbolSize : chinaMap.getSymbolSize,
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
							show : false
						}
					}
				},
				data : []
			},
			geoCoord : cityGeoCoordMap
		},
		{
			name : '目的地址',
			type : 'map',
			mapType : 'china',
			data : [],
			selectedMode : 'single',
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
				symbolSize : chinaMap.getSymbolSize,
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
			},
			geoCoord : cityGeoCoordMap
		} ]
	};
	
	
}

