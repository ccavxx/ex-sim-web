var assetChart = {
}
/**仪表盘
 * id要渲染的div id(包含#)
 * yAxisTitle　仪表盘显示名称
 * dataFunction　创建完成后的函数
 */
assetChart.drawGaugeChart = function(id,yAxisTitle,dataFunction){
	var width = $(id).parent().width()-10;
	$(id).highcharts({
		chart : {
			type : 'gauge',
			height : 150,
			width : width,
			spacingBottom:0,
			spacingTop:0,
			zoomType:'x',
			backgroundColor:''
		},
		title : {
			text : null
		},
		credits : {
			enabled : false //禁止显示LOGO
		},
		pane : [{
				startAngle : -90,
				endAngle : 90,
				background : null,
				center : ['50%', '100%'],
				size : width*0.70
			}
		],
		yAxis : [{
				min : 0,
				max : 100,
				minorTickPosition : 'outside',
				minorTickLength : 5, //小刻度线高度
				tickLength : 6, //大刻度线高度[0,90,180,270]
				tickPosition : 'outside',
				labels : {
					enabled : false
				},
				plotBands : [{ //刻度红色部分
						from : 80,
						to : 100,
						color : '#C02316',
						innerRadius : '100%',
						outerRadius : '105%'
					}
				],
				pane : 0,
				title : {
					text : yAxisTitle,
					y : 0
				}
			}
		],

		plotOptions : {
			gauge : {
				dataLabels : {
					enabled : true,
					format:'{y}'
				},
				dial : {
					radius : '100%',
					baseWidth : 2
				}
			}
		},
		tooltip : {
        	enabled : true,
        	pointFormat: '{point.y:.0f}'+"%"
		},
		series : [{
				data : [0],
				yAxis : 0
			}
		]
	},dataFunction);		
}

assetChart.drawSolidGauge = function(id,yAxisTitle,dataFunction){
	var width = $(id).parent().width()-10;
	$(id).highcharts({
		chart: {
		        type: 'solidgauge',
		        height : 125,
				width : width,
				spacingBottom:0,
				spacingTop:15,
				backgroundColor:''
	    },
	    credits: { enabled: false },
	    title: null,
	    pane: {
	    	center: ['50%', '85%'],
	    	size: '150%',
	        startAngle: -90,
	        endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
	    },
	    tooltip: { enabled: false },
	    yAxis: {
    	  	min: 0,
	        max: 100,
	        title: {
	            text: yAxisTitle,
	            enabled:true
	        },
			stops: [
				[0.1, '#32C8FA'], // green
	        	[0.5, '#F99049'], // yellow
	        	[0.8, '#DF5353'] // red
			],
			lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 200,
            tickWidth: 0,
	        title: {
	        	text:yAxisTitle,
                y: -55
	        },
            labels: {
                y: -2
            }        
	    },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: -15,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        },
        series: [{
        	name:yAxisTitle,
	        data: [0]
	    }]
	},function(chart){
		//下面一行代码是为了解决ie下无法显示plotOptions中的问题
		$(id+" > .highcharts-container > div > .highcharts-data-labels").css("top","0px") ;
		dataFunction.call() ;
	});
}

/**
 * 趋势图
 */
assetChart.drawTrendChart = function(id,title,name,dataFunction){
	var width = $(id).parent().width()-15;
	var container = $(id) ;
	Highcharts.StockChart({
			chart : {
				renderTo:container.get(0),
				width:width,
				height:135,
				spacingBottom:5,
				spacingTop:20,
				spacingRight:0
			},
			rangeSelector:{
				enabled:false
			},
			navigator:{
				enabled:false
			},
			scrollbar:{
				enabled:false
			},
			title : {
				text : title
			},
			credits : {
				enabled : false //禁止显示LOGO
			},
			xAxis :{
				tickInterval:12,
				tickLength:10,
				labels:{
					enabled:false
				}
			},
			yAxis : {
				title:{
					text:"",
				},
				tickInterval:50,
				showLastLabel:true,
				min:0,
				max:100
			},
			legend : {
				enabled:false
			},
			threshold : null,
			tooltip : {
				valueDecimals : 2
			},
			plotOptions: {
				column: {
					stacking: 'normal',
					dataLabels: {
						enabled: true
					}
				}
			},
			series : [{
				name : name,
				data : [],
				type : 'areaspline',
				threshold : null,
                fillColor: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
							[0, Highcharts.getOptions().colors[0]],
							[1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                },
			}]
	    },dataFunction);
}
assetChart.drawColumnChart = function(id,title,categories,yAxisTitle,data,formatterFunction){
	var width = $(id).parent().width()-15;
	$(id).highcharts({
		chart: {
			type: 'column',
			height:150,
			width:width,
			spacingTop:0,
			spacingBottom:0
		},
		credits : {
			enabled : false //禁止显示LOGO
		},
		title: {
			text: title
		},
		xAxis: {
			categories: categories
		},
		yAxis: {
			min: 0,
			max:100,
			title: {
				text: yAxisTitle
			},
			stackLabels: {
				enabled: true,
				style: {
					fontWeight: 'bold'
				}
			}
		},
		legend: {
			align: 'right',
			x: 10,
			y: -99,
			floating: true,
			layout:'vertical',
			borderColor: '#CCC',
			borderWidth: 1,
			shadow: false
		},
		
		plotOptions: {
			column: {
				pointWidth:35,
				stacking: 'normal',
				dataLabels: {
					enabled: true
				}
			}
		},
		series:data
	});
}
assetChart.getChartData = function(ip,attributeId,onResult) {
	var time = new Date().getTime() ;
	$.getJSON("/sim/assetdetail/assetStatus?attributeId="+attributeId+"&ip="+ip+"&_time="+time,onResult) ;
}
assetChart.getFlowData = function(ip,onResult) {
	var time = new Date().getTime() ;
	$.getJSON("/sim/assetdetail/assetFlow?ip="+ip+"&_time="+time,onResult) ;
}