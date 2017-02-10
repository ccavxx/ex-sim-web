var lanChart;
var lanMap = new EchartsModel();
var lanConfig ;
var legend = "目的地址";
function initLan() {
	var lanChartContext = ['echarts', 'echarts/chart/scatter'];
	var params = {'mapType' : 'department', 'parentName' : 'src'};
	require(lanChartContext, function(echarts) {
		lanChart = lanMap.buildChart(echarts, '#lan', setLanOption);
		lanConfig = { 'mapType' : 'department', 'parentName' : 'dest' };
		if(system.userName == "operator"){
			lanChart.on("click",onLanDataClick) ;
		}
		lanMap.initData(lanChart, '/sim/index/getMapData', lanConfig, updateLanChartData);
	});
}
function onLanDataClick(params){
	var field = lanConfig.parentName == "src" ? 
			{name:"SRC_ADDRESS_NET_SEGMENT",alias:"源网段",type:"String",value:params.seriesName,operator:"等于"} : 
			{name:"DEST_ADDRESS_NET_SEGMENT",alias:"目的网段",type:"String",value:params.seriesName,operator:"等于"}  ;
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
function updateLanChartData(lanChart, result, params){
	var options = lanChart.getOption();
	if (result.success) {
		if(params.parentName == 'src'){
			options.toolbox.feature.lanSrc.icon = 'image://../../img/icons/srcall.png';
			options.toolbox.feature.lanDest.icon = 'image://../../img/icons/disable_destall.png';
		} else if(params.parentName == 'dest'){
			options.toolbox.feature.lanSrc.icon = 'image://../../img/icons/disable_srcall.png';
			options.toolbox.feature.lanDest.icon = 'image://../../img/icons/destall.png';
		}
		var sData = [];
		options.series = [];
		options.legend.data = [];
		for ( var sindex in result.srcData) {
			var temp = result.srcData[sindex];
			options.legend.data.push(sindex);
			var tempSerie = {
				name : sindex,
				type : 'scatter',
				data : temp,
				symbol:'circle',
				symbolSize: function (value){
					var size = 5;
					if(value[1] > 1000){
						size = 8;
					} else if(value[1] > 500){
						size = 7;
					} else if(value[1] > 100){
						size = 6;
					}
					return size;
				}
			};
			options.series.push(tempSerie);
		}
	} else {
		var test = {
		    name : 'test',
			type : 'scatter',
			data : []
        };
		options.series.push(test);
	}
	lanChart.setOption(options, true);
}
function formatterDate(str){
	var date1=parseInt(str);
	var startdate=new Date(date1);
	startdate=startdate.getFullYear()
	          +"-"+((startdate.getMonth()+1)<10 ? ("0"+(startdate.getMonth()+1)):(startdate.getMonth()+1))
	          +"-"+(startdate.getDate()<10 ? ("0"+startdate.getDate()):startdate.getDate())
	          +" "+(startdate.getHours()<10 ? ("0"+startdate.getHours()):startdate.getHours())
	          +":"+(startdate.getMinutes()<10 ? ("0"+startdate.getMinutes()):startdate.getMinutes())
	          +":"+(startdate.getSeconds()<10 ? ("0"+startdate.getSeconds()):startdate.getSeconds()) ;
	return startdate;
}
function setLanOption() {
	return option = {
		legend : {
			show:false
		},
		tooltip : {
			trigger : 'item',
			showContent : true,
			formatter : function(params) {
				var value = params.value ;
				return value[2] + "(" + value[1] + "次)" ;
			},
			axisPointer : {
				type : 'cross',
				lineStyle : {
					type : 'dashed',
					width : 1
				}
			}
		},
		toolbox: {
	        show : true,
	        orient: 'vertical',  		// 'horizontal' ¦ 'vertical'
	        x: 'right',                 // 'center' ¦ 'left' ¦ 'right'
	        y: -50,                   // 'top' ¦ 'bottom' ¦ 'center'
	        itemSize:80,
	        feature : {
	        	
	            lanSrc : {
	                show : true,
	                title : '',
	                icon : 'image://../../img/icons/src.png',
	                onclick : function (params){
	             		lanConfig = { 'mapType' : 'department', 'parentName' : 'src' };
	             		legend = "源地址";
	             		lanMap.initData(lanChart, '/sim/index/getMapData', lanConfig, updateLanChartData);
	                }
	            },
	           lanDest : {
	                show : true,
	                title : '',
	                icon : 'image://../../img/icons/dest.png',
	                onclick : function (params){
	                	legend = "目的地址";
	                	lanConfig = { 'mapType' : 'department', 'parentName' : 'dest' };
	                	lanMap.initData(lanChart, '/sim/index/getMapData', lanConfig, updateLanChartData);
	                }
	            },
	            dataView : {
	    	        show: true, 
	    	        icon:'image://../../img/dataview.png',
	    	        readOnly: true,
	    	        title:'',
	    	        lang : [" <label style='text-align:center;' id='lan_title'><b>"+legend + "分布情况</b></label>","返回"],
					    optionToContent: function(opt) {
					    	$("#lan_title").html("<b>"+legend+"分布情况</b>");
					        var table = '<table style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
					    	   table += '<tr style="border-bottom:1px dotted #ccc;">';
					    	   table += "<td align='left'><b>时间</b></td>";
					    	   table += "<td align='left'><b>IP地址</b></td>";
					    	   table += "<td align='left'><b>数量</b></td></th></tr>";
					    	   $.each(opt.series,function(i,item){
				    	  			 $.each(item.data,function(index,result){
				    	  				table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
				    	  				var date = formatterDate(parseInt(result[0]));
				    	  				table += "<td align='left'>"+ date + "</td>";
				    	  				table += "<td align='left'>"+ result[2] + "</td>";
				    	  				table += "<td align='left'>"+ result[1] + "</td></tr>";
				    	  			 });
				    	  	    });
					    	table += '</table>';
					    	return  table;
						 }
	    	        }
	           
	        }
	    },
		grid:{
			x:50,
			y:20,
			x2:90,
			y2:30
		},
		xAxis : [ {
			type : 'time',
			boundaryGap :[0.05, 0.05],
			axisLine: {
				show : false				
			},
			splitLine: {
				show : true,
				lineStyle:{
				    color: ['#ccc'],
				    width: 1,
				    type: 'dashed'	//'solid' | 'dotted' | 'dashed'
				}
			},
			axisLabel:{
				formatter:function(value){
					return value.Format("HH:mm") ;
				}
			}
		} ],
		yAxis : [ {
			type : 'value',
            min:0,
			splitLine: {
				show : true,
				lineStyle:{
				    color: ['#ccc'],
				    width: 1,
				    type: 'dashed'	//'solid' | 'dotted' | 'dashed'
				}				
			},
			axisLabel:{
				formatter:lanMap.labelFormatter
            }
		} ],
		noDataLoadingOption: {
            text: '暂无数据',
            effect: 'bubble',
            effectOption: {
                effect: {
                    n: 0
                }
            }
		},
		series : []
	};
}
