var eventQuery=new EventQuery();
var loadPie = function(_id, _values, _selection_addition_callback) {
	require(['echarts', 'echarts/chart/pie'], function(echarts) {
		var eventNameChart = new EchartsModel().buildChart(echarts, _id, function(){
			return option = {
				tooltip : {
					trigger: 'item'
				},
				legend : {
					x:'center',
					y:'bottom',
					data:_values.categories
				},
				series : [{
					radius:[0, '70%'],
		       		type : 'pie',
		       		itemStyle : {
		                normal : {
		                    label : {
		                        show : false
		                    },
		                    labelLine : {
		                        show : false
		                    }
		                }
		            },
		       		data : _values.data
				}]
			};
		});
		eventNameChart.on('click',function(param){
			_selection_addition_callback(param.name);
		});
	});
};
/**
 * 验证values值组是否为空，或者内部全部为0
 * @param values
 * @returns
 */
var arrayIsEmpty = function(values) {
	var isEmptyFlag = true;
	if (values && values.length > 0) {
		$.each(values, function(ids, val) {
			if (val != 0) {
				isEmptyFlag = false;
				return false;
			}
		});
	}
	return isEmptyFlag;
};
var loadMTrand = function(_id, _values, _selection_addition_callback) {
	require(['echarts', 'echarts/chart/eventRiver'], function(echarts) {
		var eventTrandChart = new EchartsModel().buildChart(echarts, _id, function(){
			var option = {
				tooltip : {
					trigger: 'item',
					formatter:function(params){
						var temp = params.series.data[0].evolution;
						var title = temp[0].time+'至'+ temp[temp.length -1].time + '<br/>';
						title += params.series.name+'事件共';
//						+ '等级' + params.series.priority;
						title +=params.series.data[0].count + '条';
						return title;
					}, 
					position : function(params) {
						return eventModel.setTooltipPosition(_id, params, 300, 0);
					}
				},
				 xAxis: [{
		             type: 'time',
		             boundaryGap: [0.05,0.1]
			     }],
			     grid:{
			    	x:30,
			    	y:30,
			    	x2:30,
			    	y2:80
			    },
			     legend: {
			    	 x : 'center',
			    	 y : 'bottom',
				    data:[]
			     },
				series : []
			};
			var count = 0;
			$.each(_values, function(m, node) {
				count++;
				if(node.length > 1) {
					option.legend.data.push(m);
					var sum = 0;
					var temp = {
							name : m,
							type : 'eventRiver',
							weight : node[0].weight,
							priority : node[0].priority,
							itemStyle : {
								normal: {
									label: {
										textStyle : {
											color: '#000'
										}
									}
								}
							},
							data : [{
								name: m, 
								count :0,
								weight: node[0].weight, 
								evolution: []
							}]
					};
					$.each(node, function(m, element) {
						temp.data[0].evolution.push({
							time: element.time,
							value:element.value, 
							detail: {
								text: element.detail, 
							}
						});
						sum += element.value;
					});
					temp.data[0].count = sum;
					option.series.push(temp);
				}
			});
			if(count > 10){
				option.legend.show = false;
			}
			return option;
		});
		eventTrandChart.on('click', function(param) {
			_selection_addition_callback(param.name);
		});
		eventTrandChart.on('hover', function(param) {
			if (param.type == 'hover' && param.event.target) {
				param.event.target.style.cursor='pointer'; 
			}
		});
		eventTrandChart.on('mouseout', function(param) {
			if (param.type == 'mouseout' && param.event.target) {
				param.event.target.style.cursor='default'; 
			}
		});
	});
};

var loadColums=function (_id,_values,_selection_addition_callback) {
	require(['echarts', 'echarts/chart/bar'], function(echarts) {
		var eventNameChart = new EchartsModel().buildChart(echarts, _id, function(){
			return option = {
					tooltip : {
						trigger: 'item',
	   					position : function(p) {
	   						return [p[0]-60, p[1]-16];
	   			        }
					},
					 xAxis: [{
			             type: 'category',
			             show: false,
//			             splitLabel:{
//			            	 show:false,
//			            	 lineStyle:{
//			            		 type: 'dashed'	//
//			            	 }
//			             },
			             data: _values.categories
				     }],
				     yAxis: [{
			             type: 'value',
			             axisLine:{show: false},
			             splitLine: {lineStyle:{type:'dashed'}},
			             axisLabel:{
			            	 formatter:function(val){
			            		 var label;
			            		 if(val >= 1000000){
			            			 label = (val/1000000)+'m'; 
			            		 } else if(val >= 1000){
			            			 label = (val/1000)+'k'; 
			            		 } else {
			            			 label = val; 
			            		 }
			            		 return label;
			            	 }
			             }
				     }],
				     grid:{
				    	x:50,
				    	y:50,
				    	x2:30,
				    	y2:30
				    },
					series : [{
				         type: 'bar',
				         barMaxWidth:50,
				         itemStyle: {
			                normal: {
			                    color: function(params) {
			                        return simHandler.colors[params.dataIndex];
			                    },
			                    label: {
			                        show: true,
			                        position: 'top',
			                        formatter: '{b}\n{c}'
			                    }
			                }
			            },
				         data:_values.data
					}]
				};
		});
		eventNameChart.on('click', function(param) {
			_selection_addition_callback(param.name);
		});
	});
};			

var loadTrendLine=function (_id,_values,_selection_addition_callback) {
	require(['echarts', 'echarts/chart/line'], function(echarts) {
		var eventTrendLineChart = new EchartsModel().buildChart(echarts, _id, function(){
			var option = {
					tooltip : {
						formatter:function(params){
							var dateStr = params.data[0];
							var title = dateStr.getFullYear()+"-"+(dateStr.getMonth()+1)+"-"+dateStr.getDate()+" "
								+dateStr.getHours()+":"+dateStr.getMinutes()+":"+dateStr.getSeconds() + '<br/>';
							title += params.seriesName+'共';
							title +=params.data[1] + '条';
							return title;
						}
					},
					 xAxis: [{
			             type: 'time',
//			             show: false,
			             splitNumber:10
				     }],
				     yAxis: [{
			             type: 'value'
				     }],
				     grid:{
				    	x:50,
				    	y:30,
				    	x2:30,
				    	y2:80
				    },
				     legend: {
				    	 x : 'center',
				    	 y : 'bottom',
					    data:_values.categories
				     },
					series : _values.data
				};
			if(_values.categories.length > 10){
				option.legend.show = false;
			}
			return option;
		});
		eventTrendLineChart.on('click', function(param) {
			_selection_addition_callback(param.seriesName);
		});
	});
};	