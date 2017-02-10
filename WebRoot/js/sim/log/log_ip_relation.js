var globalChart ;
$(function(){
	buildIpRelationTree() ;
}) ;

function buildIpRelationTree(){
	$.getJSON('/sim/logSearchResultStat/doIpRelationStat?_time='+new Date().getTime(),
        function(result){
			var data = result.data ;
			if(!data){
				return ;            		
        	}
        	var option = buildRelationTreeOption('源主机>目的主机:目的端口关系图', data,result.count) ;
        	var size = option.series[0].size;
        	var parentHeight = $("#logSearchResultIpRelationTree").parent().height() ; 
        	if(size >= parentHeight * 1.6){
        		$("#logSearchResultIpRelationTree").height(size/1.6) ;
        	}
        	log_echarts.init("logSearchResultIpRelationTree",option,onChartCreate) ;
    });   
}
function onChartCreate(chart){
	globalChart = chart ;
	globalChart.on("click",onDataClick) ;
	globalChart.on("hover",function(params){
		if(params.data.category != undefined || params.data.source != undefined){
			params.event.target.style.cursor='hand'
		}
	}) ;
	globalChart.on('mouseout', function(param) { param.event.target.style.cursor='default'; });
}
function onDataClick(params){
	params.event.stopImmediatePropagation() ;
	var isNode = params.data.category != undefined ? true : false ;
	var conditions ;
	var series = globalChart.getOption().series[0] ;
	var allCategories = series.categories ;
	var allNodes = series.nodes ;
	
	if(isNode){
		conditions = [
              {name:"DEST_ADDRESS",type:"ip",alias:"目的地址",operator:"等于",value:params.data.text},
              {name:"DEST_PORT",type:"int",alias:"目的端口",operator:"等于",value:allCategories[params.data.category].name}
		]
	}else{
		var sourceNode = allNodes[params.data.source] ;
		var targetNode = allNodes[params.data.target] ;
		conditions = [
		      {name:"SRC_ADDRESS",type:"ip",alias:"源地址",operator:"等于",value:sourceNode.text},        
              {name:"DEST_ADDRESS",type:"ip",alias:"目的地址",operator:"等于",value:targetNode.text},
              {name:"DEST_PORT",type:"int",alias:"目的端口",operator:"等于",value:allCategories[targetNode.category].name},
		]
	}
	var handlerParam = {
			logQueryParam:{
				securityObjectType:$("#securityObjectType").val(),
				host:$("#host").val(),
				queryStartDate:$("#queryStartDate").val(),
				queryEndDate:$("#queryEndDate").val(),
				group:$("#group").val(),
				condition:conditions
			}
	} ;
	openLogQueryWindow(handlerParam) ;
}
function buildRelationTreeOption(title,data,dataCount){
	var categories = new Array(data.categories.length) ;
	for(var i in data.categories){
		var categoryName = data.categories[i] ;
		categories[i] = {
	            name: categoryName,
	            keyword: {},
	            base: categoryName
		} ;
	}
	var size = computeSize(dataCount) ;
	var legendData = data.categories.length > 20 ? data.categories.slice(0,20) : data.categories;
	var option = {
		    title : {
		    	text:title,
		        x:'center',
		        y:'top'
		    },
		    toolbox: {
		        show : true,
		        feature : {
		            dataView : {
		            	 readOnly: true,
		            	 show:true,
						 lang : ["<div style='text-align:center;'><label><b>"+title+"统计列表</b></label><button style='float:right; margin-right:30px;' onclick=backPage()>返回</button></div>"," "],
						 optionToContent: function(opt) {
							 return  createTableRelation(opt.series, title);
						 }
		            },
		            exp : {
		                show : true,
		                title : '导出',
		                icon : 'image://../../img/icons/ecxel.png',
		                onclick : function (params){
		                	exportRelation('xls');
		                }
		            }
		        }
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter : function(params,ticket,callback){
		        	var itemData = params.data ;
		        	if(params.indicator){//indicator为源节点
		        		var sourceNode = params.series.nodes[itemData.source] ;
		        		var targetNode = params.series.nodes[itemData.target] ;
		        		var category = this.getOption().legend.allLegendData[targetNode.category] ;
		        		return sourceNode.text + ">" + (targetNode.text + ":" + category) + "(" + itemData.count + ")" ;  
		        	}else{
		        		var category = this.getOption().legend.allLegendData[itemData.category] ;
		        		var text = itemData.text ;
		        		if(itemData.value){
		        			text += ":" + category + "("+itemData.value+")";
		        		}
		        		return text ;
		        	}
		        }
		    },
		    legend : {
		        data : legendData,
		        allLegendData:data.categories,
		        orient : 'vertical',
		        x : 'left'
		    },
		    noDataEffect: 'none',
		    series :[{
	            type: 'force',
	            name: title,
	            itemStyle: {
	                normal : {
	                    linkStyle : {
	                        opacity : 0.5
	                    }
	                }
	            },
	            linkSymbol:'arrow',
	            categories: categories,
	            nodes: data.nodes,
	            links: data.links,
	            minRadius: 5,
	            maxRadius: 10,
	            gravity: 1.5,
	            scaling: 1.5,
	            steps: 15,
	            large: true,
	            useWorker: true,
	            coolDown: 0.995,
	            ribbonType: false,
	            size:size,
	            roam:false
	            
	        }],
		};
	return option ;	
}
/**
 * 计算图的直径大小
 * @param count
 * @returns
 */
function computeSize(count){
	var unitNodes = 200 ;//一个单位半径可以显示的节点数
	var unitSize = 500 ;//一个单位半径的大小
	var size = count / unitNodes * unitSize ;
	return Math.max(Math.min(size,2000),1000) ;
}
function createTableRelation(tableData, title){
	var table = '<table style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
	   table += '<tr style="border-bottom:1px dotted #ccc;">';
	   table += "<td align='left'><b>源地址</b></td>";
	   table += "<td align='left'><b>目的地址</b></td>";
	   table += "<td align='left'><b>目的端口</b></td>";
	   table += "<td align='left'><b>次数</b></td></th></tr>";
	var nodes = tableData[0].nodes;
	var links = tableData[0].links;
	var categories = tableData[0].categories;
	var count = 0;
	var target = "";
	var source = "";
	var category = "";
	sort(links,"count","desc") ;
	$.each(links,function(i,linkItem){
		source = nodes[linkItem.source].text;
		target = nodes[linkItem.target].text;
		var categoryIndex = nodes[linkItem.target].category;
		category = categories[categoryIndex].name;
		count = linkItem.count;
		if(count >0){
			table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
			table += "<td align='left'>"+ source + "</td>";
			table += "<td align='left'>"+ target + "</td>";
			table += "<td align='left'>"+ category + "</td>";
			table += "<td align='left'>"+ count + "</td></tr>";
		}
	});
	table += '</table>';
	return table;
}
function exportRelation(type){
	window.location = "/sim/logSearchResultStat/exportLogIPRelation?exportType="+type+'&'+$('#exportExcel_IPRelation').serialize();
}
function backPage(){
	$('.echarts-dataview').fadeOut();
}