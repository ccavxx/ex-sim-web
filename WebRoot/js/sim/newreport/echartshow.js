/**
 * @author
 */
var echartshow = {};
var regroup={};
Date.prototype.format=function(format){
	var o={
			"M+":this.getMonth()+1,
			"d+":this.getDate(),
			"h+":this.getHours(),
			"m+":this.getMinutes(),
			"s+":this.getSeconds(),
			"q+":Math.floor((this.getMonth()+3)/3),
			"s+":this.getMilliseconds()
	};
	if(/(y+)/.test(format)){
		format=format.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length));
	}
	for(var k in o){
		if(new RegExp("("+k+")").test(format)){
			format=format.replace(RegExp.$1,RegExp.$1.length==1?o[k]:("00"+o[k]).substr((""+o[k]).length));
		}
	}
	return format;
};
echartshow.getFormatDateByLong=function(lval,pattern){
	return echartshow.getFormatDate(new Date(lval),pattern);
};

echartshow.getFormatDate=function(date,pattern){
	if(undefined==date){
		date=new Date();
	}
	if(undefined==pattern){
		pattern="yyyy-MM-dd hh:mm:ss";
	}
	return date.format(pattern);
};

echartshow.init=function(mainId,option){
	require.config({
		paths : {
			echarts : '/js/echart/build/dist'
		}
	});
	
	require(
			[
			'echarts',
			'echarts/chart/line', 
			'echarts/chart/bar',
			'echarts/chart/scatter', 
			'echarts/chart/k', 
			'echarts/chart/pie',
			'echarts/chart/radar', 
			'echarts/chart/force', 
			'echarts/chart/chord',
			'echarts/chart/gauge', 
			'echarts/chart/funnel',
			'echarts/chart/eventRiver', 
			'echarts/chart/venn',
			'echarts/chart/treemap', 
			'echarts/chart/tree',
			'echarts/chart/wordCloud', 
			'echarts/chart/heatmap' ], 
			function(ec) {
				var myChart = ec.init(document.getElementById(mainId));
				myChart.setOption(option);
				
				echartshow.queryDrill(myChart,option);
			});
	
};
echartshow.queryDrill=function(chart,option){
	
	if(!option.queryObject)return;
	var ecConfig = require('echarts/config');
	var queryType=option.queryObject.queryType;
	function pctclick(param) {
		if (typeof param.seriesIndex != 'undefined') {
			
			var queryObj=option.queryObject.queryObjects[param.seriesIndex][param.dataIndex];
			for(var prop in queryObj){
				if('LOG_QUERY'==queryType){

					if(null == queryObj[prop])return;
					var queryParam=newreport.formatReportLogQueryObj(queryObj[prop]);
					window.open('/page/forward.jsp?'+queryParam, "_blank");
					
				}else if('EVENT_QUERY'==queryType){

					newreport.reportEventQuery('/sim/eventQuery/basicEventQuery?',queryObj[prop]);
				}
				break;
			}
			
		}
	}
	chart.on(ecConfig.EVENT.CLICK, pctclick);
	chart.on('hover', function(param) {
		if (param.type == 'hover' && param.event.target) {
			param.event.target.style.cursor='default'; 
		}
	});
	if(null != option.queryObject.queryObjects
		&& null != option.queryObject.queryObjects[0]
		&& 0 < option.queryObject.queryObjects[0].length){
		
		chart.on('hover', function(param) {
			if (param.type == 'hover' && param.event.target) {
				param.event.target.style.cursor='pointer'; 
			}
		});
		chart.on('mouseout', function(param) {
			if (param.type == 'mouseout' && param.event.target) {
				param.event.target.style.cursor='default'; 
			}
		});
	}
	
};

echartshow.pictureDrill=function(jdat,top,option,statistical,regroupdat){
	var queryObject = null;
	if('true' == jdat.needReGroup && null !=regroupdat)
		queryObject=echartshow.getReGroupQueryObject(jdat,statistical,top,regroupdat);
	else queryObject=echartshow.getQueryObject(jdat,statistical,top);
	
	if(null !=queryObject && queryObject.queryObjects.length>0)
		option.queryObject=queryObject;
		
};
echartshow.getReGroupQueryObject = function(jdat,statistical,top,regroupdat){
	var queryObject={};
	var queryObjectArr=[];
	var queryType=jdat.queryType;
	var queryCondition=jdat.queryCondition;
	var queryConditionsObj=jdat.queryConditionsObj;
	if(null == regroupdat) return queryObject;
	var serindex=0;
	
	for(var st=0;st<statistical.catename.length;st++){
		
		for(var j=0;j<=regroupdat.length;j++){
			if(null == regroupdat[j])continue;
			queryObjectArr[serindex]=[];
			for(var k=0;k<regroupdat[j].length;k++){
//				if(0 == regroupdat[j][k][statistical.catename[st]])continue;
				var unitObj=echartshow.getQueryUnitObject(queryType,queryCondition,regroupdat[j][k],queryConditionsObj,jdat.roleDs);
				if(null == unitObj)break;
				
				if(null != unitObj[statistical.catename[st]])
					queryObjectArr[serindex][k]=unitObj;
				else queryObjectArr[serindex][k]=null;
				
			}
			serindex++;
		}
	}

	queryObject.queryType=queryType;
	queryObject.queryObjects=queryObjectArr;
	
	return queryObject;
};
echartshow.getQueryObject = function(jdat,statistical,top){
	var queryObject={};
	var queryObjectArr=[];
	var bodyList = jdat.data[0];
	var queryType=jdat.queryType;
	var queryCondition=jdat.queryCondition;
	var queryConditionsObj=jdat.queryConditionsObj;
	
	var len = Math.min(top,bodyList.length);
	
	for(var st=0;st<statistical.catename.length;st++){
		queryObjectArr[st]=[];
		for(var i=0;i<len;i++){
			
			var unitObj=echartshow.getQueryUnitObject(queryType,queryCondition,bodyList[i],queryConditionsObj,jdat.roleDs);
			if(null == unitObj)break;
			
			if(null != unitObj[statistical.catename[st]])
				queryObjectArr[st][i]=unitObj;
			else queryObjectArr[st][i]=null;
			
		}
		
	}
	
	queryObject.queryType=queryType;
	queryObject.queryObjects=queryObjectArr;
	
	return queryObject;
};
echartshow.standardOption=function(subname,resultdata,topn,imgtype){
	var structDesc=resultdata.dataStructureDesc;
	
	var categorys=structdescribe(structDesc,'categorys');
	var statistical=structdescribe(structDesc,'statistical');
	var unit=getUnitByJsonDat(resultdata,statistical,topn);
	var xdata=getSinglexAxisData(resultdata,categorys,topn);
	
	var arraydat=getArrayyAxisData(resultdata,statistical,topn,unit);
	
	var seriesdat=getSeriesDat(imgtype,arraydat,topn);
	var legenddat=getlegend(arraydat,topn);
	if('' != unit)unit='('+unit+')';
	var option =  {
		    title : {
		    	x:'center',
				text : subname,
				subtext : resultdata.describe
		    },
		    tooltip : {
		        trigger: 'axis'
		    },
		    legend: {
		    	y:'bottom',
		        data:legenddat
		    },
		    toolbox: {
		        show : true,
		        y: 'top',
		        feature : {
//		            mark : {show: true},
		            dataView : {
		            	show : true,
		            	title :false,
						readOnly : true,
						optionToContent : function(option1){
							var table=echartshow.filldatviewTablecontent (resultdata,categorys,statistical,topn,'column');
							var divt=$('<div/>').append(table);
							return  divt.html();
						},
						lang: [echartshow.filldatviewTitle(subname,resultdata.describe).html()]
		            },
		            magicType : {show: true, type: ['line', 'bar']},//,'stack', 'tiled'
		            restore : {show: false},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : true,
		    xAxis : [
		        {
		            type : 'category',
		            data : xdata.length==0?[0]:xdata
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value',
		            max : '(%)'==unit?100:'{value}',
		            axisLabel : {
		                formatter: '{value} '+unit
		            }
		        }
		    ],
		    series : seriesdat
		};
	echartshow.pictureDrill(resultdata,topn,option,statistical);
	return option;
};

echartshow.pieOption=function(subname,resultdata,topn,imgtype){
	var structDesc=resultdata.dataStructureDesc;
	
	var categorys=structdescribe(structDesc,'categorys');
	var statistical=structdescribe(structDesc,'statistical');
	var seriespara=structdescribe(structDesc,'series');
	var xdata=getSinglexAxisData(resultdata,categorys,topn);
	
	var seriesdat=getPieSeriesUnit(resultdata,categorys,statistical,seriespara,topn);
	
	var option = {
		    title : {
		        text: subname,
		        subtext: resultdata.describe,
		        x:'center'
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter: "{a} <br/>{b} : {c} ({d}%)"
		    },
		    legend: {
		        orient : 'vertical',
		        x : 'left',
		        data:xdata
		    },
		    toolbox: {
		        show : true,
		        y: 'top',
		        feature : {
//		            mark : {show: true},
		            dataView : {
		            	show : true,
						readOnly : true,
						optionToContent : function(option1){
							var table=echartshow.filldatviewTablecontent (resultdata,categorys,statistical,topn,'column');
							var divt=$('<div/>').append(table);
							return  divt.html();
						},
						lang: [echartshow.filldatviewTitle(subname,resultdata.describe).html()]
		            },
		            magicType : {
		                show: true, 
		                type: ['pie', 'funnel'],
		                option: {
		                    funnel: {
		                        x: '25%',
		                        width: '50%',
		                        funnelAlign: 'left',
		                        max: 1548
		                    }
		                }
		            },
		            restore : {show: false},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : true,
		    series : seriesdat
		};
	echartshow.pictureDrill(resultdata,topn,option,statistical);
	return option;
};

echartshow.rainbowOption=function(subname,resultdata,topn,imgtype){
	var structDesc=resultdata.dataStructureDesc;
	
	var categorys=structdescribe(structDesc,'categorys');
	var statistical=structdescribe(structDesc,'statistical');
	var seriesdat=structdescribe(structDesc,'series');
	var unit=getUnitByJsonDat(resultdata,statistical,topn);
	var xdata=getSinglexAxisData(resultdata,categorys,topn);
	var ydata=getSingleyAxisData(resultdata,statistical,topn,unit);
	var seriesN=getseries(resultdata,seriesdat);
	if('' != unit)unit='('+unit+')';
	var option = {
			title : {
				x : 'center',
				text : subname,
				subtext : resultdata.describe,
				link : '/sim/index/'
			},
			tooltip : {
				trigger : 'item'
			},
			toolbox : {
				show : true,
				y: 'top',
				feature : {
					dataView : {
						show : true,
						readOnly : true,
						optionToContent : function(option1){
							var table=echartshow.filldatviewTablecontent (resultdata,categorys,statistical,topn,'column');
							var divt=$('<div/>').append(table);
							return  divt.html();//table.prop('outerHTML');
						},
						lang: [echartshow.filldatviewTitle(subname,resultdata.describe).html()]
					},
					restore : {
						show : false
					},
					saveAsImage : {
						show : true
					}
				}
			},
			calculable : true,
			grid : {
				borderWidth : 0,
				y : 80,
				y2 : 60
			},
			xAxis : [ {
				type : 'category',
				show : true,
				data : xdata.length==0?[0]:xdata
			} ],
			yAxis : [ {
				type : 'value',
				axisLabel : {
	                formatter: '{value} '+unit
	            },
				show : true
			} ],
			series : [ {
				name : seriesN[0],
				type : imgtype,
				barMaxWidth:28,
				itemStyle : {
					normal : {
						color : function(params) {
							// build a color map as your need.
							var colorList = [ '#FF7F50','#87CEFA','#C1232B', '#B5C334', '#FCCE10',
									'#E87C25', '#27727B', '#FE8463', '#9BCA63',
									'#FAD860', '#F3A43B', '#60C0DD', '#D7504B',
									'#C6E579', '#F4E001', '#F0805A', '#26C0C0' ];
							return colorList[params.dataIndex];
						},
						label : {
							show : false,
							position : 'top',
							formatter : '{b}\n{c}'
						}
					}
				},
		        markPoint : {
		        	clickable:false,
		            data : [
		                {type : 'max', name: '最大值'},
		                {type : 'min', name: '最小值'}
		            ]
		        },
		        markLine : {
		            data : [
		                {type : 'average', name: '平均值'}
		            ]
		        },
				data : ydata.length==0?[0]:ydata
			} ]
		};
	echartshow.pictureDrill(resultdata,topn,option,statistical);
	return option;
};

echartshow.groupPieBarOption=function(subname,resultdata,topn,imgtype){
	var structDesc=resultdata.dataStructureDesc;
	
	var categorys=structdescribe(structDesc,'categorys');
	var statistical=structdescribe(structDesc,'statistical');
	var seriespara=structdescribe(structDesc,'series');
	
	var seriesUnitObject=getGroupPieBarSeriesUnitObject(resultdata,categorys,statistical,seriespara,topn,imgtype);
	var seriesdat=seriesUnitObject.seriesUnit;
	var xdata=arrayLegendToOne(seriesUnitObject.xlegendDat);//getSinglexAxisData(resultdata,categorys,topn);
	var names=[''];
	
	if('RESOURCE_ID' == resultdata.reGroupCol)
		names=getAllResourseNames(resultdata.roleDs);
	else
		names=seriesUnitObject.groupParams[resultdata.reGroupCol];
	var legenddat=getLegendByMoreStatisicResource(names,statistical,false);
	legenddat=('pie' ==imgtype)?xdata:legenddat;

	var unit='' == seriesUnitObject.unit ?'':'('+seriesUnitObject.unit+')';
	var option = {
		    title : {
		        text: subname,
		        subtext: resultdata.describe,
		        x:'center'
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter: function (params,ticket,callback) {
		            
		            return params.name+'<br/>'+params.seriesName+'【 '+params.value+unit+' 】';
		        }
		    },
		    toolbox: {
		        show : true,
		        y: 'top',
		        feature : {
		            mark : {show: false},
		            dataView : {
		            	show : true,
						readOnly : true,
						optionToContent : function(option1){
							var table=echartshow.filldatviewTablecontent (resultdata,categorys,statistical,topn,'column');
							var divt=$('<div/>').append(table);
							return  divt.html();
						},
						lang: [echartshow.filldatviewTitle(subname,resultdata.describe).html()]
		            },
		            /*magicType : {
		            	show: true, 
		            	type: ['line', 'bar', 'stack', 'tiled'],
		                option: {
		                    funnel: {
		                        x: '25%',
		                        width: '50%',
		                        funnelAlign: 'left',
		                        max: 1548
		                    }
		                }
		            },*/
		            restore : {show: false},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : true,
		    legend: {
//		    	orient : 'vertical',
		    	padding: [-11, 10, 10, 5],
		    	y : 'bottom',
		        data:legenddat
		    },
		    xAxis : [{
		    	type : 'category',
		        splitLine : {show : false},
		        data : xdata.length==0?[0]:xdata
		    }],
		    yAxis : [{
		    	type : 'value',
		    	max : '(%)'==unit?100:'{value}',
		        position: 'right',
				axisLabel : {
	                formatter: '{value} '+unit
	            }
		    }],
		    
		    series : seriesdat
		};
	/*if('pie' ==imgtype){
		option.xAxis=null;
		option.yAxis=null;
		option.legend= {
	    	orient : 'vertical',
	    	x : 'left',
	        data:legenddat
	    };
		option.tooltip = {
	        trigger: 'item',
	        formatter: "{a} <br/>{b} : {c} ({d}%)"
	    };
	}*/
	
	echartshow.pictureDrill(resultdata,topn,option,statistical,seriesUnitObject.regroupdat);
	
	return option;
};
echartshow.eventRiverOption=function(subname,resultdata,topn,imgtype){
	var structDesc=resultdata.dataStructureDesc;
	
	var categorys=structdescribe(structDesc,'categorys');
	var statistical=structdescribe(structDesc,'statistical');
	var seriespara=structdescribe(structDesc,'series');
//	var xdata=getSinglexAxisData(resultdata,categorys,topn);
	var seriesdat=getEventRiverSeriesUnit(resultdata,categorys,statistical,seriespara,topn,imgtype);
	var legenddat=getEventRiverLegend(resultdata,categorys,statistical,topn);//getLegendByMoreStatisicResource(getAllResourseNames(resultdata.roleDs),statistical);
	
	option = {
			title : {
				text: subname,
		        subtext: resultdata.describe,
		        x:'center'
			},
			tooltip : {
				trigger : 'item',
				enterable : true,
				formatter : function(params){
					var showtitle=params.data.name;
					var evolution=params.data.evolution;
					
					if(evolution.length==1)
						return showtitle+" : "+evolution[0].value;
					var total=0;
					for(var ev=0;ev<evolution.length;ev++){
						total+=evolution[ev].value;
					}
					return showtitle+" : "+total;
				}
			},
			legend : {
//				orient : 'vertical',
		        y : 'bottom',
				data : legenddat
			},
			toolbox : {
				show : true,
				y: 'top',
				feature : {
					mark : {
						show : false
					},
					restore : {
						show : false
					},
					saveAsImage : {
						show : true
					}
				}
			},
			xAxis : [{
					type : 'time',
					boundaryGap : [0.05, 0.1]
				}
			],
			series : seriesdat
	};
	
//	echartshow.pictureDrill(resultdata,topn,option,statistical);
	return option;
};
echartshow.linkQueryInit=function(queryCondition){
	var linkProperties={};
	
	linkProperties.statisticalProps=[];
	linkProperties.particularlyQueryContents=[];
	linkProperties.shareQueryContents=[];
	
	var qcont=0;
	
	if(null == queryCondition || queryCondition.length<=2){
		return null;
	}

	var conditions=queryCondition.split('#SHARE@');
	for(var i=0;i<conditions.length;i++){
		if(0 == i){
			var countlinks =conditions[i].split(',');
			for(var j=0;j<countlinks.length;j++){
				
				var clinks=countlinks[j].split('$');
				
				linkProperties.statisticalProps[j]=clinks[0];
				linkProperties.particularlyQueryContents[j]=[];
				
				var qcs=clinks[1].split('&');
				for(var k=0;k<qcs.length;k++){
					linkProperties.particularlyQueryContents[j][k]=qcs[k];
				}
				
			}
		}else{
			var qcds=conditions[i].split('&');
			for(var k=0;k<qcds.length;k++){
				linkProperties.shareQueryContents[qcont++]=qcds[k];
			}
		}
	}
	return linkProperties;
};

echartshow.getUnitVal=function(val){
	if(val>=1024*1024*1024*1024){
		return ((val/(1024*1024))/(1024*1024)).toFixed(3) + ' (Tb)';
	}else if(val>=1024*1024*1024){
		return (val/(1024*1024*1024)).toFixed(3) + ' (Gb)';
	}else if(val>=1024*1024){
		return (val/(1024*1024)).toFixed(3) + ' (Mb)';
	}else if(val>=1024){
		return (val/1024).toFixed(3) + ' (Kb)';
	}else{
		return val + ' (B)';
	}
};

echartshow.getUnitType=function(val){
	if(val>=1024*1024*1024*1024){
		return 'Tb';
	}else if(val>=1024*1024*1024){
		return 'Gb';
	}else if(val>=1024*1024){
		return 'Mb';
	}else if(val>=1024){
		return 'Kb';
	}else{
		return 'B';
	}
};

echartshow.getUnitByType=function(val,types){
	if(null == val) return 0.0;
	if('Tb' == types){
		return parseFloat(((val/(1024*1024))/(1024*1024)).toFixed(3));
	}else if('Gb' == types){
		return parseFloat((val/(1024*1024*1024)).toFixed(3));
	}else if('Mb' == types){
		return parseFloat((val/(1024*1024)).toFixed(3));
	}else if('Kb' == types){
		return parseFloat((val/1024).toFixed(3));
	}else{
		return parseFloat(val);
	}
};

function fillSingleLinkDat(queryContent,linedat,roleDs){
	var querySingleObj={};
	var datType=null;
	var datContent=null;
	if(queryContent.indexOf('~')>-1){
		var conds=queryContent.split('~');
		datContent=conds[0];
		datType=conds[1];
	}else{
		datContent=queryContent;
		datType='String';
	}
	
	var operatorSymbol=['!=','>=','<=','>','<','='];
	var operatorCNSymbol=['不等于','大于等于','小于等于','大于','小于','等于'];
	var queryContent=null;
	for(var i=0;i<operatorSymbol.length;i++){
		var symbol=operatorSymbol[i];
		if(datContent.indexOf(symbol)>-1){
			var datps=datContent.split(symbol);
			var conditionName=datps[0];
			
			if(datps[1].indexOf('?')<0){
				queryContent=datps[1];
			}else{
				if('GROUP' == conditionName && null == linedat[conditionName]){
					queryContent=1;
				}else if('DVC_ADDRESS' == conditionName 
						&& null == linedat[conditionName] 
						&& null != linedat['RESOURCE_ID']
						&& null != roleDs){
					var resource=getResourseById(linedat['RESOURCE_ID'],roleDs);
					queryContent=resource.deviceIp;
				}else{
					queryContent=linedat[conditionName];
				}
			}
			
			var operator=operatorCNSymbol[i];
			querySingleObj.queryType=datType;
			querySingleObj.conditionName=conditionName;
			querySingleObj.queryContent=queryContent;
			querySingleObj.operator=operator;
			
			break;
		}
	}
	return querySingleObj;
}

function fillLogQuerydat(queryCondition,linedat,queryConditionsObj,roleDs){
	
	var linkPts=echartshow.linkQueryInit(queryCondition);
	
	var partiConts=linkPts.particularlyQueryContents;
	var shareConts=linkPts.shareQueryContents;
	var statistiConts=linkPts.statisticalProps;
	if(statistiConts.length != partiConts.length){
		return null;
	}
	var queryObjects={};
	
	var shareconditionName='';
	var sharequeryType='';
	var sharequeryContent='';
	var shareoperatort='';
	var isQuhost=false;
	var queryHost=null;
	var isGroup=false;
	var queryGroup=null;
	for(var j=0;j<shareConts.length;j++){
		var qSgOj=fillSingleLinkDat(shareConts[j],linedat,roleDs);
		if('DVC_ADDRESS' == qSgOj.conditionName){
			queryHost=qSgOj.queryContent;
			isQuhost=true;
			continue;
		}
		if('GROUP' == qSgOj.conditionName){
			isGroup=true;
			queryGroup=qSgOj.queryContent;
			continue;
		}
		shareconditionName=shareconditionName+qSgOj.conditionName+',';
		sharequeryType=sharequeryType+qSgOj.queryType+',';
		sharequeryContent=sharequeryContent+qSgOj.queryContent+',';
		shareoperatort=shareoperatort+qSgOj.operator+',';
	}
	
	for(var i=0;i<partiConts.length;i++){
		var queryObj={};
		
		var conditionName='';
		var queryType='';
		var queryContent='';
		var operatort='';
		
		for(var pa=0;pa<partiConts[i].length;pa++){
			
			var qSgObj=fillSingleLinkDat(partiConts[i][pa],linedat,roleDs);
			
			if('DVC_ADDRESS' == qSgObj.conditionName){
				queryObj.host=qSgObj.queryContent;
				continue;
			}
			if('GROUP' == qSgObj.conditionName){
				queryObj.group=qSgObj.queryContent;
				continue;
			}
			conditionName=conditionName+qSgObj.conditionName+',';
			queryType=queryType+qSgObj.queryType+',';
			queryContent=queryContent+qSgObj.queryContent+',';
			operatort=operatort+qSgObj.operator+',';
		}
		if(shareconditionName.length>1){
			conditionName=conditionName+shareconditionName;
			queryType=queryType+sharequeryType;
			queryContent=queryContent+sharequeryContent;
			operatort=operatort+shareoperatort;
		}
		if(conditionName.length>1){
			conditionName=conditionName.substring(0, conditionName.length-1);
			queryType=queryType.substring(0, queryType.length-1);
			queryContent=queryContent.substring(0, queryContent.length-1);
			operatort=operatort.substring(0, operatort.length-1);
		}
		queryObj.conditionName=conditionName;
		queryObj.queryType=queryType;
		queryObj.queryContent=queryContent;
		queryObj.operator=operatort;
		
		if(isQuhost && null != queryHost){
			queryObj.host=queryHost;
		}
		if(isGroup && null != queryGroup){
			queryObj.group=queryGroup;
		}
		queryObj.deviceType=queryConditionsObj.securityObjectType;
		queryObj.queryStartDate=queryConditionsObj.stime;
		var queryEndDate=queryConditionsObj.endtime;
		var date = moment(queryEndDate).subtract('seconds',1).toDate();
		queryObj.queryEndDate = date.Format("yyyy-MM-dd HH:mm:ss");
		if(null == queryHost &&('ALL_ROLE_ADDRESS' == queryConditionsObj.dvcAddress || 'ONLY_BY_DVCTYPE' == queryConditionsObj.dvcAddress)){
			queryObj.host='';
		}
		
		queryObjects[statistiConts[i]]=queryObj;
		
	}
	
	return queryObjects;
}

function fillEventQuerydat(queryCondition,linedat,queryConditionsObj,roleDs){
	
	var linkPts=echartshow.linkQueryInit(queryCondition);
	var partiConts=linkPts.particularlyQueryContents;
	var shareConts=linkPts.shareQueryContents;
	var statistiConts=linkPts.statisticalProps;
	if(statistiConts.length != partiConts.length){
		return null;
	}
	var queryObjects={};
	var sharequeryObj={};
	for(var j=0;j<shareConts.length;j++){
		var qSgOj=fillSingleLinkDat(shareConts[j],linedat,roleDs);
		setEventObj(sharequeryObj,qSgOj.conditionName,qSgOj.queryContent);
	}
	for(var i=0;i<partiConts.length;i++){
		var queryObj={};
		for(var pa=0;pa<partiConts.length;pa++){
			var qSgObj=fillSingleLinkDat(partiConts[i][pa],linedat,roleDs);
			setEventObj(queryObj,qSgObj.conditionName,qSgObj.queryContent);
		}
		setObjToObj(sharequeryObj,queryObj);
		queryObj.startTime=queryConditionsObj.stime;
		queryObj.endTime=queryConditionsObj.endtime;
		
		queryObjects[statistiConts[i]] = queryObj;
	}
	
	return queryObjects;
}

function setEventObj(queryObj,conditionName,queryContent){
	if(conditionName === "NAME" || conditionName === "EVENT_NAME"){
		queryObj.query_event_Name=queryContent;
	}else if(conditionName === "SRC_ADDRESS"){ 
		queryObj.srcIp=queryContent;
	}else if(conditionName === "DVC_ADDRESS"){
		queryObj.deviceIp=queryContent;
	}else if(conditionName === "DEST_ADDRESS"){
		queryObj.destIp=queryContent;
	}else if(conditionName === "PRIORITY"){
		queryObj.priority=queryContent;
	}else if(conditionName === "CAT1_ID"){
		queryObj.category1=queryContent;
	}else if(conditionName === "CAT2_ID"){
		queryObj.category2=queryContent;
	}else if(conditionName === "endTime"){
		queryObj.endTime=queryContent;
	}
}

function setObjToObj(fromObj,toObj){
	if(null == fromObj)return;
	if(null == toObj){
		toObj={};
	}
	for(prop in fromObj){
		toObj[prop]=fromObj[prop];
	}
}

function getSinglexAxisData(json,categorys,topn){
	
	var xdata=[];
	var cr=0;
	var data=json.data[0];
	var len = Math.min(topn,data.length); 
	for(var xd=0;xd<len;xd++){
		for(var i=0;i<categorys.catename.length;i++){
			var property=categorys.catename[i];
			var xval=data[xd][property];
			if('PRIORITY' == property || 'RISK' == property)xval=riskCnName(xval);
			if('START_TIME' == property )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
			xdata[cr++]=xval;
		}
	}
	return xdata;
}

function getArrayxAxisData(json,categorys,topn,type){
	type=(null ==type)?"default":type;
	
	var data=json.data[0];
	var len = Math.min(topn,data.length); 
	if("default" == type){
		var xAxisArray=[];
		for(var xd=0;xd<len;xd++){
			var catunit=[];
			for(var i=0;i<categorys.catename.length;i++){
				var property=categorys.catename[i];
				var xval=data[xd][property];
				if('PRIORITY' == property || 'RISK' == property)xval=riskCnName(xval);
				if('START_TIME' == property )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
				catunit[i]=xval;
			}
			xAxisArray[xd]=catunit;
		}
		return xAxisArray;
	}
	var arrayXdat={};
	for(var i=0;i<categorys.catename.length;i++){
		var xdata=[];
		var property=categorys.catename[i];
		for(var xd=0;xd<len;xd++){
			var xval=data[xd][property];
			if('PRIORITY' == property || 'RISK' == property)xval=riskCnName(xval);
			if('START_TIME' == property )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
			xdata[xd]=xval;
		}
		arrayXdat[property]=xdata;//categorys.cateAlaxname[i]
	}
	
	return arrayXdat;
}

function getSingleyAxisData(json,statistical,topn,unit){
	
	var ydata=[];
	var cr=0;
	var data=json.data[0];
	var len = Math.min(topn,data.length); 
	for(var xd=0;xd<len;xd++){
		for(var i=0;i<statistical.catename.length;i++){
			var yunitval=data[xd][statistical.catename[i]];
			yunitval=yunitval==null?0:yunitval;
			if('' !=unit)yunitval=echartshow.getUnitByType(yunitval,unit);
			ydata[cr++]=yunitval;
		}
	}
	return ydata;
}

//if('FLOW_NO' == statistical.cateUnit[i]){ydatval=echartshow.getUnit(ydatval);}
function getArrayyAxisData(json,statistical,topn,unit){
	
	var arrayYdat={};
	
	var data=json.data[0];
	var len = Math.min(topn,data.length); 
	for(var i=0;i<statistical.catename.length;i++){
		var ydata=[];
		var statProperty=statistical.catename[i];
		for(var xd=0;xd<len;xd++){
			var yunitval=data[xd][statProperty];
			yunitval=yunitval==null?0:yunitval;
			if('' !=unit)yunitval=echartshow.getUnitByType(yunitval,unit);
			ydata[xd]=yunitval;
		}
		arrayYdat[statistical.cateAlaxname[i]]=ydata;
	}
	
	return arrayYdat;
}

function getseries(json,series){
	
	var sdata=[];
	var cr=0;
	for(var i=0;i<series.catename.length;i++){
		sdata[cr++]=series.cateAlaxname[i];
	}
	return sdata;
}

function initSeriesUnit(imgtype,sername,valdat){
	var ismooth='line'==imgtype?true:false;
	var seriesUnit={
        name:sername,
        type:imgtype,
        smooth:ismooth,
        data:valdat,
        symbol: 'none',
        markPoint : {
        	clickable:false,
            data : [
                {type : 'max', name: '最大值'},
                {type : 'min', name: '最小值'}
            ]
        },
        markLine : {
            data : [
                {type : 'average', name: '平均值'}
            ]
        },
        barMaxWidth:28,
        itemStyle: {normal: {areaStyle: {type: 'default'}}}
    };
	return seriesUnit;
}
function analysisParams(paramstring){
	var paramsObj={};
	if(paramstring.indexOf(';', 0)>-1){
		var paramArr=paramstring.split(';');
		paramsObj.length=paramArr.length;
		for(var i=0;i<paramArr.length;i++){
			var unitArr=paramArr[i].split('=');
			if(2==unitArr.length){
				if(unitArr[1].indexOf(',', 0)>-1){
					var valueArr=unitArr[1].split(',');
					paramsObj[unitArr[0]]=valueArr;
				}else{
					paramsObj[unitArr[0]]=[unitArr[1]];
				}
			}
		}
	}
	return paramsObj;
	
}

function getGroupPieBarSeriesUnitObject(json,categorys,statistical,series,topn,imgtype){
	if(null== imgtype || '' ==imgtype)imgtype='bar';
	var seriesUnitObject={};
	var seriesdat=[];
	var unit=getUnitByJsonDat(json,statistical,topn);
	var data=json.data[0];
	
	seriesUnitObject.unit=unit;
	if(0 == data.length){
		var seriesU=groupPieBarSeriesUnit('','',imgtype,[]);
		seriesdat[0]=seriesU;
		seriesUnitObject.seriesUnit=seriesdat;
		seriesUnitObject.xlegendDat=[];
		return seriesUnitObject;
	}

	/**
	 * 多维标记
	 */
	var isMore=false;
	var finalGroupObject=getLastSortGroup(json,categorys,statistical,topn);
	var finalGroup=finalGroupObject.group;
	var groupName=finalGroupObject.groupName;
	
	var serieslen=0;
	var xdata=null;
	if(('pie' == imgtype))xdata=getxlegendDat(json,categorys,finalGroup.length*topn);//getSinglexAxisData(json,categorys,topn);
	var seriesBarTmpdat=[];
	var tmplen=0;
	for(var slen=0;slen<statistical.catename.length;slen++){
		
		var sername=isMore?statistical.cateAlaxname[slen]:'';
		
		for(var res=0;res<finalGroup.length;res++){
			var stackGroupName=groupName[res].value;
			
			if(null ==finalGroup[res][0]){
				
				var seriesUnit=groupPieBarSeriesUnit(stackGroupName,sername,imgtype,[]);
				seriesdat[serieslen++]=seriesUnit;
			}else{
				var piedata=[];
				var sedata=[];
				for(var resunit=0;resunit<finalGroup[res].length;resunit++){
					
					var val=finalGroup[res][resunit][slen];
					val=val==null?0:val;
					if('' !=unit)val=echartshow.getUnitByType(val,unit);
					sedata[resunit]=val;
					if(('pie' == imgtype)){
						var pdat={
								name:xdata[resunit],
								value:val
						};
						piedata[resunit]=pdat;
					}
					
				}
				
//				sedata=('pie' == imgtype)?piedata:sedata;
				var seriesUt=null;
				if('pie' == imgtype){
					seriesUt=groupPieBarSeriesUnit(stackGroupName,sername,imgtype,piedata,finalGroup.length,res);
					seriesdat[serieslen++]=seriesUt;
					seriesUt=groupPieBarSeriesUnit(stackGroupName,sername,'bar',sedata,finalGroup.length,res);
					seriesBarTmpdat[tmplen++]=seriesUt;
				}else{
					seriesUt=groupPieBarSeriesUnit(stackGroupName,sername,imgtype,sedata,finalGroup.length,res);
					seriesdat[serieslen++]=seriesUt;
				}
			}
			
		}
		
	}
	var regroupdat=finalGroupObject.regroupdat;
	var ret=0;
	if(tmplen > 0){
		regroupdat=[];
		for(var tim=0;tim<2;tim++)
			for(var prop in finalGroupObject.regroupdat)
				regroupdat[ret++]=finalGroupObject.regroupdat[prop];
		for(var tm=0;tm<tmplen;tm++)
			seriesdat[serieslen++]=seriesBarTmpdat[tm];
	}
	
	seriesUnitObject.seriesUnit=seriesdat;
	seriesUnitObject.xlegendDat=finalGroupObject.xlegendDat;
	seriesUnitObject.groupParams=finalGroupObject.groupParams;
	seriesUnitObject.regroupdat=regroupdat;
	return seriesUnitObject;
}
function groupPieBarSeriesUnit(stackGroupName,sername,imgtype,sdata,total,res){
	var seriesUnit=null;
	var radiusMy=null;
	if('pie' == imgtype){
		if(null != res)radiusMy=radiusArea(total,res);
		seriesUnit={
            name:stackGroupName,
            type:'pie',
            center : [200, 160],
            radius : null == radiusMy ? 1:radiusMy,
            itemStyle : {
                normal : {
                    label : {
                        position : 'inner',
                        formatter : function (params) {                         
                          return (params.percent - 0).toFixed(0) + '%';
                        }
                    },
                    labelLine : {
                        show : false
                    }
                },
                emphasis : {
                    label : {
                        show : true,
                        formatter : "{b}\n{d}%"
                    }
                }
                
            },
            data:sdata.length==0?[0]:sdata
        };
		return seriesUnit;
	}
	seriesUnit={
            name:stackGroupName+sername,
            type:imgtype,
            symbol: 'none',
            barMaxWidth:28,
//            itemStyle: {normal: {areaStyle: {type: 'default'}}},
            radius : '55%',
            center: ['50%', '60%'],
            tooltip : {trigger: 'item'},
            data:sdata.length==0?[0]:sdata
    };
	if('bar' == imgtype)
		seriesUnit.stack= sername;
	return seriesUnit;
}
function radiusArea(total,val){
	var maxWidth=120;
	var argwidth=maxWidth/total;
	var startwidth=Math.round(argwidth/10)*10+10;
	if(null == val || 0 == val)return startwidth;
	
	var interval = Math.floor((maxWidth - startwidth)/(total-1));
	var spaceInterval = Math.floor(interval/4);
//	var blackInterval = Math.floor(interval - spaceInterval);
	
	var sval=interval*(val-1);
	
	return [startwidth+sval+spaceInterval,startwidth+sval+interval];
}
function getLegendByMoreStatisicResource(names,statistical,isMore){
	var legendArr=[];
	if(null == names || null == statistical)return legendArr;
	var out=0;
	for(var i=0;i<statistical.catename.length;i++){
		var sername=isMore?statistical.cateAlaxname[i]:'';
		for(var j=0;j<names.length;j++){
			
			legendArr[out++]=names[j]+sername;
		}
	}
	return legendArr;
}

function getEventRiverLegend(json,categorys,statistical,topn){
	var legenddat=[];
	if('true' ==json.needReGroup){
		var finalGroupObject=getLastSortGroup(json,categorys,statistical,topn);
		var groupName=finalGroupObject.groupName;
		for(var res=0;res<finalGroup.length;res++){
			var stackGroupName=groupName[res].value;
			legenddat[res]=stackGroupName;
		}
	}else{
		var resource=getResourseById(json.queryConditionsObj.dvcAddress,json.roleDs);
		var resName=null==resource?null:resource.resourceName;
		legenddat[0]=resName;
	}
	return legenddat;
}

function getEventRiverSeriesUnit(json,categorys,statistical,series,topn,imgtype){
	if(null== imgtype || '' ==imgtype)imgtype='eventRiver';
	var seriesdat=[];
	
	var data=json.data[0];
	
	var seriesU=groupEventRiverSeriesUnit('',[],imgtype,[[]]);
	if(0 == data.length || series.catename.length != 1){
		seriesdat[0]=seriesU;
		return seriesdat;
	}
	var resource=getResourseById(json.queryConditionsObj.dvcAddress,json.roleDs);
	var resName=null==resource?null:resource.resourceName;
	if('true' ==json.needReGroup){
		var finalGroupObject=getLastSortGroup(json,categorys,statistical,topn);
		var finalGroup=finalGroupObject.group;
		var groupName=finalGroupObject.groupName;
		
		if(null == finalGroup){
			seriesdat[0]=seriesU;
			return seriesdat;
		}
		var serieslen=0;
		
		for(var slen=0;slen<statistical.catename.length;slen++){
			
			var sername=statistical.cateAlaxname[slen];
			
			for(var res=0;res<finalGroup.length;res++){
				var evolutiondats=[];
				var sernams=[];
				var pcount=0;
				var stackGroupName=groupName[res].value;
				
				if(null ==finalGroup[res][0]){
					var seriesUnit=groupEventRiverSeriesUnit(stackGroupName,[sername],imgtype,[[]]);
					seriesdat[serieslen++]=seriesUnit;
				}else{
					var evolutiondata=[];
					var out=0;
					for(var resunit=0;resunit<finalGroup[res].length;resunit++){
						var val=finalGroup[res][resunit][slen];
						var datUnit={
								"time" : categorys.cateAlaxname[0],
								"value" : val,
								"detail" : {
									"link" : "/sim/index",
									"text" : val
								}
							};
						evolutiondata[out++]=datUnit;
						
					}
					evolutiondats[pcount]=evolutiondata;
					sernams[pcount]=sername;
					pcount++;
				}
				var seriesUt=groupEventRiverSeriesUnit(stackGroupName,sernams,imgtype,evolutiondats);
				seriesdat[serieslen++]=seriesUt;
			}
			
		}
		
	}else{
		
		var len = data.length;
		var sameCates=samePropPutInCollection(categorys,statistical,series.catename[0],data,len);
		formatSameDatForEventRiver(sameCates);
		var serieslen=0;
		var evolutiondatas=[];
		var sernames=[];
		var propcount=0;
		for(var prop in sameCates){
			
			for(var slen=0;slen<statistical.catename.length;slen++){
				
				var sername=statistical.cateAlaxname[slen];
				var evolutiondata=[];
				var out=0;
				for(var ulen=0;ulen<sameCates[prop].length;ulen++){
					var datUnit={
							"time" : sameCates[prop][ulen].categorys[0],
							"value" : sameCates[prop][ulen].statistical[slen],
							"detail" : {
								"link" : "/sim/index",
								"text" : sameCates[prop][ulen].statistical[slen]
							}
						};
					evolutiondata[out++]=datUnit;
				}
				evolutiondatas[propcount]=evolutiondata;
				sernames[propcount]=prop+sername;
				propcount++;
			}
			
		}
		var seriesUt=groupEventRiverSeriesUnit(resName,sernames,imgtype,evolutiondatas);
		seriesdat[serieslen++]=seriesUt;
		
	}
	return seriesdat;
}
function getUnitByJsonDat(jdat,statistical,len){
	var maxval=-999;
	for(var i=0;i<statistical.catename.length;i++){
		if('FLOW_NO' == statistical.cateUnit[i]){
			var tmpval=getMaxMinDat(jdat,statistical.catename[i],'max',len);
			if(null!= tmpval && maxval<tmpval)maxval=tmpval;
		}else if('PERCENT_NO'== statistical.cateUnit[i])
			return '%';
	}
	var unit='';
	if(-999 !=maxval){
		unit=echartshow.getUnitType(maxval);
	}
	return unit;
}
function samePropPutInCollection(categorys,statistical,seriesproperty,data,len){
	
	var sameCate=[];
	
	for(var i=0;i<len;i++){
		var xval=data[i][seriesproperty];
		if('PRIORITY' == seriesproperty || 'RISK' == seriesproperty)xval=riskCnName(xval);
		if('START_TIME' == seriesproperty )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
		
		sameCate[xval]=[];
	}
	var salen=0;
	for(var prop in sameCate){
		var saprop=0;
		for(var i=0;i<len;i++){
			
			var xval=data[i][seriesproperty];
			if('PRIORITY' == seriesproperty || 'RISK' == seriesproperty)xval=riskCnName(xval);
			if('START_TIME' == seriesproperty )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
			
			if(xval == prop){
				
				var sameunit={};
				var cates=[];
				var caout=0;
				for(var j=0;j<categorys.catename.length;j++){
					if(categorys.catename[j] != seriesproperty){
						var caval=data[i][categorys.catename[j]];
						if('PRIORITY' == categorys.catename[j] || 'RISK' == categorys.catename[j])caval=riskCnName(caval);
						if('START_TIME' == categorys.catename[j] )caval=echartshow.getFormatDateByLong(caval,'MM-dd hh:mm');
						
						cates[caout++]=caval;
					}
				}
				sameunit.categorys=cates;
				
				var seris=[];
				var seout=0;
				for(var j=0;j<statistical.catename.length;j++){
					seris[seout++]=data[i][statistical.catename[j]];
				}
				sameunit.statistical=seris;
				
				sameCate[prop][saprop++]=sameunit;
			}
		}
		salen++;
	}
	sameCate['length']=salen;
	return sameCate;
}
function formatSameDatForEventRiver(dat){
	
	if(0 == dat['length'])return ;
	
	var timetmp=null;
	for(var prop in dat){
		timetmp=dat[prop][0].categorys[0];
		break;
	}
	
	for(var prop in dat){
		
		for(var ulen=0;ulen<dat[prop].length;ulen++){
			if(timetmp != dat[prop][ulen].categorys[0])
				return ;
		}
	}
	for(var prop in dat){
		var plen=dat[prop].length;
		dat[prop][plen]=copyObj(dat[prop][0]);
		var repval=dat[prop][plen].categorys[0];
		var val=parseInt(repval.substring(repval.length-2))+1;
		val = val<10?'0'+val:(val>59?'00':val);
		repval=repval.substring(0,repval.length-2)+val;
		
		dat[prop][plen].categorys[0]=repval;
		for(var i=0;i<dat[prop][plen].statistical.length;i++){
			dat[prop][plen].statistical[i]=0;
		}
		return;
	}
}
function getLastSortGroup(json,categorys,statistical,topn){
	var finalGroupObject={};
	finalGroupObject.group=null;
	finalGroupObject.groupName=null;
	var finalGroup=null;
	
	var reGroupCol=json.reGroupCol;
	var groupColArr=[];
	if(reGroupCol.indexOf(',', 0)>-1){
		groupColArr=reGroupCol.split(',');
	}else{
		groupColArr[0]=reGroupCol;
	}
	var prarr=analysisParams(json.queryConditionsObj.params);
	prarr = paramsFromDat(groupColArr,prarr,json);
	var needGroupObj=getNeedGroupObj(groupColArr,prarr);
	
	var allgroup=regroup.getGroup(needGroupObj.value);
	if(null == allgroup)return finalGroupObject;
	var data=json.data[0];
	var xleglen=topn*allgroup.length;
	var len = Math.min(topn*(allgroup.length),data.length);
	if('TREND' == json.showType){
		len=data.length;
		xleglen=data.length;
	}
	
	var groupMapper={};
	groupMapper.roleDs=json.roleDs;
	
	var groupObject=getInitialGroup(statistical,allgroup,data,needGroupObj,len,groupMapper);
	var group=groupObject.group;
	
	var xArrayAxisDat=getArrayxAxisData(json,categorys,xleglen);
	var position=regroup.rexDatArrPosition(xArrayAxisDat);
	
	var xlegendDat=regroup.rexAxis(xArrayAxisDat,position);
	position=regroup.xArrPosition(xlegendDat,xArrayAxisDat);
	
	finalGroup=initAndGetGroup(statistical,allgroup,xlegendDat.length);
	
	var regroupdat=[];
	
	for(var propg in group){
		//此参数是为了下钻的时候把查询条件重新绑定
		var redat=[];
		for(var i=0;i<len;i++){
			
			//下钻条件字段重新设置
			reObj=copyObj(data[i]);
			
			var allzero=false;
			for(var j=0;j<statistical.catename.length;j++){
				
				if(0 != group[propg][i][j])break;
				allzero=true;
			}
			
			if(!allzero){
				finalGroup[propg][position[i]]=group[propg][i];
			}else{
				for(var rj=0;rj<statistical.catename.length;rj++){
					reObj[statistical.catename[rj]]=0;
				}
				rebuildDatByGroup(reObj,propg,needGroupObj);
			}
			redat[position[i]]=reObj;
		}
		regroupdat[propg]=redat;
	}
	finalGroupObject.group=finalGroup;
	finalGroupObject.groupName=groupObject.groupName;
	finalGroupObject.regroupdat=regroupdat;
	finalGroupObject.xlegendDat=xlegendDat;
	finalGroupObject.groupParams=prarr;
	return finalGroupObject;
}
function rebuildDatByGroup(reObj,propg,needGroupObj){
	var groupiName=needGroupObj.prop[0];
	for (var i = 0; i < needGroupObj.value[0].length; i++) {
		
		if (needGroupObj.length == 1) {
			if(propg == i){
				reObj[groupiName]=needGroupObj.value[0][i];
			}
		
		}else{
			var groupjName=needGroupObj.prop[1];
			for (var j = 0; j < needGroupObj.value[1].length; j++) {
				
				if (needGroupObj.length == 2) {
					
					if(propg == i+''+j){
						reObj[groupiName]=needGroupObj.value[0][i];
						reObj[groupjName]=needGroupObj.value[1][j];
					}
					
				}else{
					var groupkName=needGroupObj.prop[2];
					for (var k = 0; k < needGroupObj.value[2].length; k++){
						
						if (needGroupObj.length == 3) {
							if(propg == i+''+j+''+k){
								reObj[groupiName]=needGroupObj.value[0][i];
								reObj[groupjName]=needGroupObj.value[1][j];
								reObj[groupkName]=needGroupObj.value[2][k];
							}
						
						}else{
							var grouppName=needGroupObj.prop[3];
							for (var p = 0; p < needGroupObj.value[3].length; p++){
								if (needGroupObj.length == 4) {
									if(propg == i+''+j+''+k+''+p){
										reObj[groupiName]=needGroupObj.value[0][i];
										reObj[groupjName]=needGroupObj.value[1][j];
										reObj[groupkName]=needGroupObj.value[2][k];
										reObj[grouppName]=needGroupObj.value[3][p];
									}
								}
							}
							
						}
					}
				}
			}
		}
	}
}
function getxlegendDat(json,categorys,len){
	var xArrayAxisDat=getArrayxAxisData(json,categorys,len);
	var position=regroup.rexDatArrPosition(xArrayAxisDat);
	var xlegendDat=regroup.rexAxis(xArrayAxisDat,position);
	var out=0;
	var singlelegend=[];
	for(var i=0;i<xlegendDat.length;i++){
		if(xlegendDat[i] instanceof Array){
			for(var j=0;j<xlegendDat[i].length;j++){
				singlelegend[out++]=xlegendDat[i][j];
			}
		}else{
			singlelegend[i]=xlegendDat[i];
		}
	}
	return singlelegend;
}
function initAndGetGroup(statistical,allgroup,len){
	var unitStat=[];
	for(var u=0;u<statistical.catename.length;u++){
		unitStat[u]=0;
	}
	
	var group=[];
	for(var prop in allgroup){
		if('length' == prop)continue;
		group[prop]=[];
		for(var x=0;x<len;x++){
			group[prop][x]=unitStat;
		}
	}
	return group;
}
function getInitialGroup(statistical,allgroup,data,needGroupObj,len,mapper){
	var groupObject={};
	var group=initAndGetGroup(statistical,allgroup,len);
	var groupName=[];
	for(var xd=0;xd<len;xd++){
		var groupiName=needGroupObj.prop[0];
		for (var i = 0; i < needGroupObj.value[0].length; i++) {
			
			if (needGroupObj.length == 1) {
				if(data[xd][groupiName] == needGroupObj.value[0][i]){
					var unitstati=[];
					for(var stai=0;stai<statistical.catename.length;stai++){
						var statiProperty=statistical.catename[stai];
						var yival=data[xd][statiProperty];
						unitstati[stai]=yival;
					}
					group[i][xd]=unitstati;
				}

				groupName[i]={};
				groupName[i].prop=needGroupObj.value[0][i];
				groupName[i].value=getGroupAliasName(groupiName,needGroupObj.value[0][i],mapper);
			
			}else{
				var groupjName=needGroupObj.prop[1];
				for (var j = 0; j < needGroupObj.value[1].length; j++) {
					
					if (needGroupObj.length == 2) {
						if(data[xd][groupiName] == needGroupObj.value[0][i] 
						&& data[xd][groupjName] == needGroupObj.value[1][j]){
							var unitstatj=[];
							for(var staj=0;staj<statistical.catename.length;staj++){
								var statjProperty=statistical.catename[staj];
								var yjval=data[xd][statjProperty];
								unitstatj[staj]=yjval;
							}
							group[i+''+j][xd]=unitstatj;
						}
						groupName[i+''+j]={};
						groupName[i+''+j].prop=needGroupObj.value[0][i]+''+needGroupObj.value[1][j];
						groupName[i+''+j].value=getGroupAliasName(groupiName,needGroupObj.value[0][i],mapper)+getGroupAliasName(groupjName,needGroupObj.value[1][j],mapper);
					
					}else{
						var groupkName=needGroupObj.prop[2];
						for (var k = 0; k < needGroupObj.value[2].length; k++){
							
							if (needGroupObj.length == 3) {
								if(data[xd][groupiName] == needGroupObj.value[0][i] 
								&& data[xd][groupjName] == needGroupObj.value[1][j] 
								&& data[xd][groupkName] == needGroupObj.value[2][k]){
									var unitstatk=[];
									for(var stak=0;stak<statistical.catename.length;stak++){
										var statkProperty=statistical.catename[stak];
										var ykval=data[xd][statkProperty];
										unitstatk[stak]=ykval;
									}
									group[i+''+j+''+k][xd]=unitstatk;
								}
								
								groupName[i+''+j+''+k]={};
								groupName[i+''+j+''+k].prop=needGroupObj.value[0][i]+''+needGroupObj.value[1][j]+''+needGroupObj.value[2][k];
								groupName[i+''+j+''+k].value=getGroupAliasName(groupiName,needGroupObj.value[0][i],mapper)+getGroupAliasName(groupjName,needGroupObj.value[1][j],mapper)+getGroupAliasName(groupkName,needGroupObj.value[2][k],mapper);
							
							}else{
								var grouppName=needGroupObj.prop[3];
								for (var p = 0; p < needGroupObj.value[3].length; p++){
									if (needGroupObj.length == 4) {
										if(data[xd][groupiName] == needGroupObj.value[0][i] 
										&& data[xd][groupjName] == needGroupObj.value[1][j] 
										&& data[xd][groupkName] == needGroupObj.value[2][k] 
										&& data[xd][grouppName] == needGroupObj.value[3][p]){
											var unitstatp=[];
											for(var stap=0;stap<statistical.catename.length;stap++){
												var statpProperty=statistical.catename[stap];
												var ypval=data[xd][statpProperty];
												unitstatp[stap]=ypval;
											}
											group[i+''+j+''+k+''+p][xd]=unitstatp;
											
										}
										groupName[i+''+j+''+k+''+p]={};
										groupName[i+''+j+''+k+''+p].prop=needGroupObj.value[0][i]+''+needGroupObj.value[1][j]+''+needGroupObj.value[2][k]+''+needGroupObj.value[3][p];
										groupName[i+''+j+''+k+''+p].value=getGroupAliasName(groupiName,needGroupObj.value[0][i],mapper)+getGroupAliasName(groupjName,needGroupObj.value[1][j],mapper)+getGroupAliasName(groupkName,needGroupObj.value[2][k],mapper)+getGroupAliasName(grouppName,needGroupObj.value[3][p],mapper);
									}
								}
								
							}
						}
					}
				}
			}
		}
	}
	
	groupObject.group=group;
	groupObject.groupName=groupName;
	return groupObject;
}

function getGroupAliasName(groupType,value,dat){
	if(null == value || null ==dat)return '';
	if('RESOURCE_ID' == groupType){
		var resource=getResourseById(value,dat.roleDs);
		var resName=null==resource?null:resource.resourceName;
		return resName;
	}
	
	return value;
}

/**
 * 得到需要重新分组的分组类对象
 * @param groupColArr
 * @returns {___anonymous36746_36747}
 */
function getNeedGroupObj(groupColArr,prarr){
	var needGroupObj={};
	
	needGroupObj.prop=[];
	needGroupObj.value=[];
	var groupTotal=1;
	var nodat=0;
	for(var reg=0;reg<groupColArr.length;reg++){
		var groupCol=groupColArr[reg];
		var needGroup=prarr[groupCol];
		if(null == needGroup){
			nodat++;
			continue;
		}
		needGroupObj[groupCol]=needGroup;
		needGroupObj.prop[reg]=groupCol;
		needGroupObj.value[reg]=needGroup;
		
		groupTotal *= needGroup.length;
	}
	needGroupObj.length=groupColArr.length-nodat;
	groupTotal = groupColArr.length == nodat ? 0 : groupTotal;
	needGroupObj.groupTotal=groupTotal;
	
	return needGroupObj;
}
function paramsFromDat(groupColArr,prarr,json){
	var hasvalue = false;
	for(var reg=0;reg<groupColArr.length;reg++){
		var groupCol=groupColArr[reg];
		var needGroup=prarr[groupCol];
		if(null == needGroup){
			continue;
		}
		hasvalue = true;
		break;
	}
	if(!hasvalue){
		var dat = json.data[0];
		if(null==dat || dat.length == 0)
			return prarr;
		
		for(var re=0;re<groupColArr.length;re++){
			var groupCo=groupColArr[re];
			var count = 0;
			var varr=[];
			for(var di=0;di<dat.length;di++){
				var gval=dat[di][groupCo];
				var hasthis=false;
				if(null != gval && [] !=varr)
					for(var i=0;i<varr.length;i++)
						if(varr[i] == gval)
							hasthis=true;
				if(!hasthis)
					varr[count++] = gval;
			}
			if(varr.length>0){
				prarr[groupCo]=varr;
				prarr.length=prarr.length+1;
			}
				
		}
		
	}
	return prarr;
}
function groupEventRiverSeriesUnit(stackGroupName,sernames,imgtype,evolutiondatas){
	var seriesUnit=null;
	var weight=79;
	var datArr=groupEventRiverDatArr(weight,sernames,evolutiondatas);
	seriesUnit={
			"name" : stackGroupName,
			"type" : imgtype,
			"weight" : weight,
			"data" : datArr
		};
	return seriesUnit;
}

function groupEventRiverDatArr(weight,sernames,evolutiondatas){
	var datarr=[];
	for(var i=0;i<sernames.length;i++){
		var datunit=groupEventRiverDatUnit(weight,sernames[i],evolutiondatas[i]);
		datarr[i]=datunit;
	}
	return datarr;
}

function groupEventRiverDatUnit(weight,sername,evolutiondata){
	var datunit={
			"name" : sername,
			"weight" : weight,
			"evolution" : evolutiondata
			};
	return datunit;
}

/**
 * 返回分组字段的线性分组键值对集合
 */
regroup.getGroup=function(arrays){
	if (null == arrays || arrays.length==0) {
		return null;
	}
	
	var group={};
	
	for (var i = 0; i < arrays[0].length; i++) {
		
		if (arrays.length == 1) {
			group[i]=arrays[0][i];
		}else {
			for (var j = 0; j < arrays[1].length; j++) {
				
				if (arrays.length == 2) {
					group[i+''+j]=arrays[0][i]+''+arrays[1][j];
				}else{
					
					for (var k = 0; k < arrays[2].length; k++) {
						if (arrays.length == 3) {
							group[i+''+j+''+k]=arrays[0][i]+''+arrays[1][j]+''+arrays[2][k];
						}else{
							for(var p=0;p<arrays[3].length;p++){
								if (arrays.length == 4) {
									group[i+''+j+''+k+''+p]=arrays[0][i]+''+arrays[1][j]+''+arrays[2][k]+''+arrays[3][p];
								}
							}
						}
						
					}
					
				}
				
			}
		}
		
	}
	var propLength=0;
	for(var prop in group){
		if(null !=prop)
		propLength++;
	}
	group.length=propLength;
	return group;
};
/**
 * 找出数据中相同的cartgroy 往哪里放
 */
regroup.xPosition=function(xdatlegend,xdata){
	var position={};
	for(var i=0;i<xdata.length;i++){
		for(var j=0;j<xdatlegend.length;j++){
			if(xdata[i]==xdatlegend[j])
				position[i]=j;
		}
	}
	return position;
};
/**
 * 去掉xdata 中重复展示的数据
 */
regroup.rexAxis=function(xdata,position){

	var xdat=[];
	var pos=0;
	for(var i=0; i<xdata.length;i++){
		if(null == position[i])
		xdat[pos++]=xdata[i];
	}
	return xdat;
};
/**
 * 找出xdata 中数据哪些是重复的并且记录下来位置
 */
regroup.rexDatPosition=function(xdata){
	var position={};
	for(var i=0;i<xdata.length;i++){
		for(var j=i+1;j<xdata.length;j++){
			if(xdata[j] == xdata[i])
				position[j]=i;
		}
	}
	return position;
};

/**
 * 找出xArrdata多维中相同的cartgroy 往哪里放
 */
regroup.xArrPosition=function(xdatlegend,xArrdata){
	var position={};
	for(var i=0;i<xArrdata.length;i++){
		for(var j=0;j<xdatlegend.length;j++){
			var allEq=false;
			for(var k=0;k<xArrdata[i].length;k++){
				if(xArrdata[i][k] != xdatlegend[j][k])
					break;
				allEq=true;
			}
			if(allEq)position[i]=j;
				
		}
	}

	return position;
};

/**
 * 找出xArrdata多维 中数据哪些是重复的并且记录下来位置
 */
regroup.rexDatArrPosition=function(xArrdata){
	var position={};
	for(var i=0;i<xArrdata.length;i++){
		for(var j=i+1;j<xArrdata.length;j++){
			var allEq=false;
			for(var k=0;k<xArrdata[i].length;k++){
				if(xArrdata[j][k] != xArrdata[i][k])
					break;
				allEq=true;
			}
			if(allEq)position[j]=i;
			
		}
	}
	for(var p in position){
		for(var pr in position){
			if(p != pr && position[pr] ==p){
				position[pr]=position[p];
			}
		}
	}
	
	return position;
};

function getResourseById(resourceId,roleDs){
	for(var i=0;i<roleDs.length;i++){
		if(roleDs[i].resourceId == resourceId)
			return roleDs[i];
	}
	if('ONLY_BY_DVCTYPE' == resourceId || 'ALL_ROLE_ADDRESS' == resourceId)
		return {deviceIp:'',resourceName:'全部设备'};
	return null;
}

function getAllResourseNames(roleDs){
	if(null == roleDs) return null;
	var names=[];
	for(var i=1;i<roleDs.length;i++){
		names[i-1]=roleDs[i].resourceName;
	}
	return names;
}

function getPieSeriesUnit(json,categorys,statistical,series,topn){
	
	var data=json.data[0];
	var len = Math.min(topn,data.length); 
	var sdata=[];
	var property=categorys.catename[0];
	var statProperty=statistical.catename[0];
	for(var xd=0;xd<len;xd++){
		var seunit={};
		var xval=data[xd][property];
		if('PRIORITY' == property || 'RISK' == property)xval=riskCnName(xval);
		if('START_TIME' == property )xval=echartshow.getFormatDateByLong(xval,'MM-dd hh:mm');
		
		var yval=data[xd][statProperty];
		seunit.name=xval;
		seunit.value=yval;
		sdata[xd]=seunit;
	}
	var sername=series.cateAlaxname[0];
	var seriesdat=[];
	var seriesUnit={
            name:sername,
            type:'pie',
            radius : '55%',
            center: ['50%', '60%'],
            data : sdata.length==0?[0]:sdata
        };
	seriesdat[0]=seriesUnit;
	return seriesdat;
}

function getSeriesDat(imgtype,arraydat,topn){
	if(null == arraydat)return null;
	if(null == imgtype || '' == imgtype)imgtype='bar';
	var seriesdat=[];
	var out=0;
	for(var prop in arraydat){
		if(out >=topn)return seriesdat;
		var seriesUnit=initSeriesUnit(imgtype,prop,arraydat[prop]);
		seriesdat[out++]=seriesUnit;
	}
	return seriesdat;
}

function getlegend(arraydat,topn){
	if(null == arraydat)return null;
	var legenddat=[];
	var out=0;
	for(var prop in arraydat){
		if(out >=topn)return legenddat;
		legenddat[out++]=prop;
	}
	return legenddat;
}

/**
 * 
 * @param json
 * @param property
 * @param type
 * @returns
 */
function getMaxMinDat(json,property,type,topn){
	var data=json.data[0];
	var len=data.length;
	if(0==len) return 0;
	if(null ==topn || '' ==topn){
		topn=len;
	}else{
		len=len<=topn?len:topn;
	}
	var max=data[0][property];
	var min=data[0][property];
	
	for(var xd=1;xd<len;xd++){
		var valnow=data[xd][property];
		max=(valnow>max)?valnow:max;
		min=(valnow<min)?valnow:min;
	}
	if('min'==type)return min;
	if('max'==type)return max;
	return -1;
}

function structdescribe(structDesc,type){
	//{categorys:{PRIORITY~危险级别},series:{PRIORITY~危险级别},statistical:{OPCOUNT~计数}}
	var categorys={};
	categorys.catename=[];
	categorys.cateAlaxname=[];
	categorys.cateUnit=[];
	
	var series={};
	series.catename=[];
	series.cateAlaxname=[];
	series.cateUnit=[];
	
	var statistical={};
	statistical.catename=[];
	statistical.cateAlaxname=[];
	statistical.cateUnit=[];
	
	structDesc=structDesc.replace(/ /g, '');
	structDesc=structDesc.substring(structDesc.indexOf('{', 0)+1, structDesc.lastIndexOf('}', structDesc.length-1));
	var properties=structDesc.split(',', 3);
	for(var i=0;i<properties.length;i++){
		var props=properties[i].split(':', properties[i].length);
		
		var vt=props[1].substring(1,props[1].length-1);
		var ts=vt.split(";");
		for(var j=0;j<ts.length;j++){
			
			var tprop=ts[j].split('~');
			
			if('categorys'==props[0]){
				categorys.catename[j]=tprop[0];
				categorys.cateAlaxname[j]=tprop[1];
				categorys.cateUnit[j]=tprop[2];
			}else if('series'==props[0]){
				series.catename[j]=tprop[0];
				series.cateAlaxname[j]=tprop[1];
				series.cateUnit[j]=tprop[2];
			}else if('statistical'==props[0]){
				statistical.catename[j]=tprop[0];
				statistical.cateAlaxname[j]=tprop[1];
				statistical.cateUnit[j]=tprop[2];
			}
		}
	}
	if('categorys'==type){
		return categorys;
	}else if('series'==type){
		return series;
	}else if('statistical'==type){
		return statistical;
	}
}

function riskCnName(val){
	if(0 == val)return '无危险';
	else if(1 == val)return '低危险';
	else if(2 == val)return '中危险';
	else if(3 == val)return '高危险';
	else if(4 == val)return '非常危险';
	return val;
}

function objToUrlString(queobj){
	var queString='';
	if(null == queobj){
		return queString;
	}
	for(var prop in queobj){
		if(queobj.hasOwnProperty(prop) && null != queobj[prop]){
			queString+=(prop+'='+queobj[prop]+',');
		}
	}
	if(queString.length>1){
		queString=queString.substring(0, queString.length-1);
	}
	return queString;
}

function arrayLegendToOne(arr){
	var ret=[];
	if(null == arr )return ret;
	
	for(var i=0;i<arr.length;i++){
		ret[i]='';
	}
	for(var i=0;i<arr.length;i++){
		if(arr[i] instanceof Array){
			for(var prop in arr[i])ret[i] += arr[i][prop];
		}else{
			ret[i]=arr[i];
		}
	}
	return ret;
}
echartshow.datViewProps=function(categorys,statistical,typeName){
	
	if(null == categorys || null == statistical)return null;
	
	if('headCnName' == typeName){
		var headername=[];
		var hcout=0;
		for(var h=0;h<categorys.cateAlaxname.length;h++){
			headername[hcout++]=categorys.cateAlaxname[h];
		}
		for(var hs=0;hs<statistical.cateAlaxname.length;hs++){
			headername[hcout++]=statistical.cateAlaxname[hs];
		}
		return headername;
	}
	if('bodyColName' == typeName){
		var bodyColName=[];
		var bcout=0;
		for(var b=0;b<categorys.catename.length;b++){
			bodyColName[bcout++]=categorys.catename[b];
		}
		for(var bs=0;bs<statistical.catename.length;bs++){
			bodyColName[bcout++]=statistical.catename[bs];
		}
		return bodyColName;
	}
	
	if('bodyColUnit' == typeName){
		var bodyColUnit=[];
		var ucout=0;
		for(var u=0;u<categorys.cateUnit.length;u++){
			bodyColUnit[ucout++]=categorys.cateUnit[u];
		}
		for(var us=0;us<statistical.cateUnit.length;us++){
			bodyColUnit[ucout++]=statistical.cateUnit[us];
		}
		return bodyColUnit;
	}
	
};

echartshow.filldatviewTitle=function(titletext,subtitle){
	var titleDiv=$('<div/>').css('text-align','center');
	var titleH4=$('<h4/>').addClass('parent-title').html(titletext);
	var titlefont=$('<font/>').addClass('sub-title').html(subtitle);
	
	var hr=$('<hr style="width:90%;height:1px;padding:5px 0;margin:0 0 5px 15px;border:none;border-top:1px solid #eee;" />');
	//padding:8px 0;margin:0 0 10px 0;border-bottom:1px solid #eee
	titleDiv.append(titleH4);
	titleDiv.append(titlefont);
	titleDiv.append(hr);
	return $('<div/>').append(titleDiv);
};
/**
 * 
 */
echartshow.filldatviewTablecontent = function(jdat,categorys,statistical,top,type){
	
	if(null == jdat)return '';
	var table = echartshow.addTable('dataview-table','center');
	var headCnName=echartshow.datViewProps(categorys,statistical,'headCnName');
	var sheader =echartshow.addThead(headCnName,'');
	var bodyColName=echartshow.datViewProps(categorys,statistical,'bodyColName');
	var bodyColUnit=echartshow.datViewProps(categorys,statistical,'bodyColUnit');
	
	var sbody = echartshow.addTbody(jdat,bodyColName,bodyColUnit,top,type);
	table.append(sheader).append(sbody);
	$('.echarts-dataview p').remove();
	return table;
};

/**
 * 添加表格头部信息
 * @param header 头部信息
 */
echartshow.addThead = function(header,csstype){
	var tr = echartshow.addTr("left","tableHead");
	for(var i=0;i<header.length;i++){
		tr.append(echartshow.addTh(csstype,header[i]));
	}
	var head = $("<thead/>");
	head.append(tr);
	tr=null;
	return head;
};

/**
 * 添加表格内容信息
 * @param data 表格列集内容
 * @param fields 表格列集
 */
echartshow.addTbody = function(jdat,bodyColName,bodyColUnit,top,type){
	var tbody=$("<tbody/>");
	var clazz= "sub-tabletdborder";
	var bodyList = jdat.data[0];
	var queryType=jdat.queryType;
	var queryCondition=jdat.queryCondition;
	var queryConditionsObj=jdat.queryConditionsObj;
	
	var len = Math.min(top,bodyList.length);
	if('TREND' == jdat.showType)len=bodyList.length;
	var drill=('DRILL'===jdat.reportType)?true:false;
	for(var i=0;i<len;i++){
		
		var tr = echartshow.addTrContext(drill,bodyList[i],bodyColName,bodyColUnit,queryConditionsObj,queryType,queryCondition,clazz,jdat.roleDs) ;
		tbody.append(tr);
		
	}
	if(len<bodyList.total){
//		tbody.append(echartshow.addMergeTd(bodyList.fields.length,tdata.moreUrl));
	}
	
	tdata=null;
	top=null;
	return tbody ;
};

echartshow.getQueryUnitObject = function(queryType,queryCondition,linedat,queryConditionsObj,roleDs){
	var queryObject=null;
	if(null != queryType && '' != queryType){
		if('LOG_QUERY' == queryType){
			queryObject=fillLogQuerydat(queryCondition,linedat,queryConditionsObj,roleDs);
		}else if('EVENT_QUERY'==queryType){
			queryObject=fillEventQuerydat(queryCondition,linedat,queryConditionsObj,roleDs);
		}
	}
	return queryObject;
};
/**
 * 添加表格一行内容
 * @param data 一行列集内容
 * @param fields 列集
 * @param url url地址
 * @param tdClazz td样式
 */
echartshow.addTrContext = function(drill,linedat,fields,bodyColUnit,queryConditionsObj,queryType,queryCondition,trClazz,roleDs){
	var trStr= echartshow.addTr("",trClazz);
	var tdClazz='';
	var drillUrl='';
	var logQueryUrl='';
	var eventQueryUrl='/sim/eventQuery/basicEventQuery?';
	
	var queryObjects=echartshow.getQueryUnitObject(queryType,queryCondition,linedat,queryConditionsObj,roleDs);
		
	for(var i =0;i<fields.length;i++){
		var td =null;
		var property=fields[i];
		var datvalue=linedat[property];
		var datunit=bodyColUnit[i];
		if('FLOW_NO' == datunit){datvalue=echartshow.getUnitVal(datvalue);}
		if('PRIORITY' == property || 'RISK' == property){datvalue=riskCnName(datvalue);}
		if('START_TIME' == property ){datvalue=echartshow.getFormatDateByLong(datvalue,'yyyy-MM-dd hh:mm:ss');}
		if('RESOURCE_ID' == property ){
			var resource=getResourseById(datvalue,roleDs);
			datvalue = null==resource?null:resource.resourceName;
		}
		/*此处逻辑需要许许改一下
*/
		if(i==0 && drill==true){
			var queryObj=null;
			td = echartshow.addHrefTdJS(tdClazz, datvalue, "ablue", drillUrl,queryObj);
			
		}else if('NO_QUERY' != queryType){
			var queryObj=null;
			if(null != queryObjects){
				try{
					queryObj=queryObjects[property];
				}catch(e){
					queryObj=null;
				}
			}
			if(null != queryObj){
				if('LOG_QUERY'==queryType){
					td = echartshow.addLogQuery(tdClazz, datvalue, "aorange", logQueryUrl,queryObj);
				}else if('EVENT_QUERY'==queryType){
					td = echartshow.addEventQueryJS(tdClazz, datvalue, "ared", eventQueryUrl,queryObj);
				}
			}else{
				td = echartshow.addTd(tdClazz,datvalue);
			}
		}else{
			td = echartshow.addTd(tdClazz,datvalue);
		}
		trStr.append(td) ;
	}
	return trStr ;
};

/**
 * 添加一个带有超链接的td内容
 * @param clazz td样式
 * @param innerHTM td里面的内容
 * @param aclazz 超链接的样式
 * @param url url地址
 */
echartshow.addHrefTd = function(clazz,innerHTML,aclazz,url){
	return echartshow.addTd(clazz,"").append($("<a/>").attr({"title":'下钻报表'}).bind("click",function(){newreport.drillReport(url,conditionObject);}).addClass(aclazz).css("cursor","pointer").html(innerHTML)) ;
};
/**
 * 此方法需要 解决bind不能 绑定的问题，暂时在此处有点问题，以后找到原因后可以直接替换echartshow.addHrefTdJS()方法，暂时用 echartshow.addHrefTdJS()方法
 */
echartshow.addHrefTdJS = function(clazz,innerHTML,aclazz,url,drillQueryObject){
	var drillstring=objToUrlString(drillQueryObject);
	var a=$("<a onClick='newreport.drillReport(\""+url+"\",\""+drillstring+"\")'/>").attr({"title":'下钻报表'}).addClass(aclazz).css("cursor","pointer").html(innerHTML);
	return echartshow.addTd(clazz,"").append(a) ;
};

echartshow.addLogQuery = function(clazz,innerHTML,aclazz,url,logQueryObject){
	var logQueryParams=newreport.formatReportLogQueryObj(logQueryObject);
	return echartshow.addTd(clazz,"").append($("<a/>").attr({"title":'查询日志',target:"_blank",href:'/page/forward.jsp?'+logQueryParams}).addClass(aclazz).css("cursor","pointer").html(innerHTML)) ;
};
/**
 * 此方法需要 解决bind不能 绑定的问题，暂时在此处有点问题，以后找到原因后可以直接替换echartshow.addEventQueryJS()方法，暂时用 echartshow.addEventQueryJS()方法
 */
echartshow.addEventQuery = function(clazz,innerHTML,aclazz,url,eventQueryObject){
	return echartshow.addTd(clazz,"").append( $("<a/>").bind("click",function(){newreport.reportEventQuery(url,eventQueryObject);}).attr({"title":'查询事件'}).addClass(aclazz).css("cursor","pointer").html(innerHTML) ) ;
};
echartshow.addEventQueryJS = function(clazz,innerHTML,aclazz,url,eventQueryObject){
	var rptqryevtstring=objToUrlString(eventQueryObject);
	var a=$("<a onClick='newreport.reportStringQueryEvent(\""+url+"\",\""+rptqryevtstring+"\")'/>").attr({"title":'查询事件'}).addClass(aclazz).css("cursor","pointer").html(innerHTML);
	return echartshow.addTd(clazz,"").append(a) ;
};

echartshow.addMergeTd = function(column,moreUrl){
	var trStr= echartshow.addTr("right","tableOddTd");
	trStr.append($("<td/>").attr("colspan",column).append($("<a/>").bind("click",function(){report.moreReport(moreUrl);}).addClass("").css("cursor","pointer").html("更多") ));
	return trStr;
};

echartshow.addSubSummarizeTd = function(column,content){
	var trStr= echartshow.addTr("left","tableOddTd");
	trStr.append($("<td/>").attr("colspan",column).css("cursor","pointer").html(content));
	return trStr;
};

echartshow.addTable = function(clazz,align){
	return $("<table/>").addClass(clazz).attr({"align":align,"cellpadding":0,"cellspacing":0}) ;
};

/**
 * 添加一个tr
 * @param align tr对齐方式
 * @param clazz 样式
 */
echartshow.addTr = function(align,clazz){
	return $("<tr/>").addClass(clazz).attr("align",align) ;
};

/**
 * 添加一个td
 * @param clazz 样式
 * @param innerHTML 
 */
echartshow.addTd = function(clazz,innerHTML){
	return $("<td/>").addClass(clazz).html(innerHTML) ;
};

/**
 * 添加一个th
 * @param clazz 样式
 * @param innerHTML 
 */
echartshow.addTh = function(clazz,innerHTML){
	var span = $("<span/>").html(innerHTML);
	var div = $("<div/>").append(span);
	return $("<th/>").addClass(clazz).append(div);
};

function copyObj(fromObj){
	var toObj=null;
	
	if(null != fromObj && (typeof fromObj == "object") ){
		toObj={};
		for(var pro in fromObj){
			
			if(typeof fromObj[pro] == "object"){
				if(fromObj[pro] instanceof Array){
					toObj[pro]=[];
					for(var i=0;i<fromObj[pro].length;i++){
						if(typeof fromObj[pro][i] == "object"){
							var tosubao=null;
							tosubao=copyObj(fromObj[pro][i],tosubao);
							toObj[pro][i]=tosubao;
						}else{
							toObj[pro][i]=fromObj[pro][i];
						}
					}
				}else{
					var tosub=null;
					tosub=copyObj(fromObj[pro],tosub);
					toObj[pro]=tosub;
				}
				
			}else{
				toObj[pro]=fromObj[pro];
			}
		}
	}
	return toObj;
}
