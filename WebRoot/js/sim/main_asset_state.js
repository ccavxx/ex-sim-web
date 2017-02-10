function MainAssetState(){
}	
MainAssetState.prototype = {
	drawTrendChart:function(id,title,name,dataFunction){
		var width = $(id).parent().width()-25;
		var container = $(id) ;
		Highcharts.StockChart({
				chart : {
					renderTo:container.get(0),
					width:width,
					height:140,
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
					tickLength:0,
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
	},
	loadStatusList:function (ip,stateName,chartId){
		var time = new Date().getTime() ;
		$.getJSON("/sim/assetdetail/stateList?ip="+ip+"&stateName="+stateName+"&_time="+time,function(data){
			var chartData = new Array(data.length) ;
			$.each(data,function(index,record){
				var value = record[stateName] == undefined ? 0 : record[stateName] ;
				chartData[index] = new Array(record.StartTime,value) ;
			}) ;
			chartData.reverse() ;
			$(chartId).highcharts().series[0].setData(chartData) ;
			$(chartId).highcharts().redraw() ;
		}) ;
	}
}