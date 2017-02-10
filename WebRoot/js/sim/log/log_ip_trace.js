var chart ;
var fieldList ;
$(function(){
	$.ajaxSettings.traditional = true ;
	buildIpTraceTree() ;
}) ;

function buildIpTraceTree(){
	$("#traceTreeContainer").height($(document.body).height()-$("#traceTools").height()) ;
	var params = {traceGroupFields:[]} ;
	$(".groupFields").each(function(){
		var fieldEle = $(this) ;
		if(fieldEle.val() != ""){
//			var txt = fieldEle.attr("name")+"="+fieldEle.val();
			var txt = fieldEle.val();
			params.traceGroupFields.push(txt) ;
			$('#traceGroupFields').attr('value', txt);
		}
	}) ;
	$.post('/sim/logSearchResultStat/doIpTraceStat',params,
        function(result){
			var data = result.data ;
			if(!data){
				return ;            		
        	}
			fieldList = result.fieldsInfo ;
			rebuildTraceData(data,data.otherProperties.count/result.size) ;
			var size = result.size ;
			var parentHeight = $("#logSearchResultIpTraceTree").parent().height() ;
			var treeCavansHeight = size * 14 ;
			var treeHeight = treeCavansHeight > parentHeight ? treeCavansHeight + 120 : parentHeight ; 
			$("#logSearchResultIpTraceTree").height(treeHeight) ;
        	var option = buildTreeOption(createTitle(result), result,result.reverse,treeHeight/2) ;
        	log_echarts.init("logSearchResultIpTraceTree",option,onChartCreate) ;
    },"json");   
}

function onChartCreate(chart){
	chart.on("click",onDataClick) ;
	chart.on("hover",function(params){
		params.event.target.style.cursor = params.data.searchable ? "hand" : "default" ; 
	}) ;
	chart.on('mouseout', function(param) { param.event.target.style.cursor='default'; });
}

function createTitle(result){
	var fieldsInfo = result.fieldsInfo ;
	var root = result.data;
	var title = "IP跟踪树(" ;
	title += (root && root.name != '') ? root.name : '根'; 
	$('#filterField').attr('value', root.name);
	for(var i in fieldsInfo){
		title += ">" + fieldsInfo[i].alias ; 
	}
	title += ")" ;
	return title ;
}
/**
 * 重构数据，计算数据点的大小<br>
 * 
 * @param data 节点数据
 */
function rebuildTraceData(data,avg,parent){
	data.level = parent ? parent.level + 1 : 0 ;
	data.searchable = data.level > 0 && fieldList[data.level-1].searchable ;
	if(!data.searchable){
		data.itemStyle = {normal:{color:"gray"},emphasis:{color:"gray"}} ;
	}
	if(data.otherProperties){
		data.value = data.otherProperties.count ;
		data.symbolSize = Math.min(Math.max(data.value / avg,6),40);
	}
	if(data.children){
		for(var index in data.children){
			rebuildTraceData(data.children[index],avg,data) ;
		}
	}
}

function onDataClick(params){
	var clickField = fieldList[params.data.level-1] ;
	if(!clickField.searchable){
		return ;
	}
	var queryField = {name:clickField.name,type:clickField.type,alias:clickField.alias,operator:"等于",value:params.name} ;
	var handlerParam = {
			logQueryParam:{
				securityObjectType:$("#securityObjectType").val(),
				host:$("#host").val(),
				group:$("#group").val(),
				queryStartDate:$("#queryStartDate").val(),
				queryEndDate:$("#queryEndDate").val(),
				condition:[queryField]
			}
	} ;
	openLogQueryWindow(handlerParam) ;
}

function buildTreeOption(title,result,reverse,y){
	var data = result.data;
	var option = {
		    title : { text: title,x:'left',y:'top'},
            tooltip:{
				formatter:"{b}({c})"
			},
		    toolbox: {
		        show : true,
		        feature : {
		        	dataView : {
		        		show:true,
		        		readOnly: true,
		        		lang : ["<div style='text-align:center;'><label><b>"+title+"统计列表</b></label><button style='float:right; margin-right:30px;' onclick=backPage()>返回</button></div>"," "],
		        		optionToContent: function(opt) {
		        			return  createTableTrace(opt.series, result.fieldsInfo);
		        		}
		        	},
		        	exp : {
		        		show : true,
		        		title : '导出',
		        		icon : 'image://../../img/icons/ecxel.png',
		        		onclick : function (params){
		        			exportTrace('xls');
		        		}
		        	}
		        }
		    },
		    series : [
		        {
		            name:'树图',
		            type:'tree',
		            orient: 'horizontal',  // vertical horizontal
		            direction:reverse ? "inverse" : "",
		            rootLocation: {x: reverse ? 1000 : 150,y: y ? y : 'center'}, // 根节点位置  {x: 100, y: 'center'}
		            nodePadding: 8,
		            layerPadding: 200,
		            hoverable: false,
		            roam: false,
		            symbolSize: 6,
		            itemStyle: {
		                normal: {
		                    color: "#61A0A8",
		                    label: {
		                        show: true,
		                        position: 'right',
		                        formatter: "{b}",
		                        textStyle: {
		                            color: '#000',
		                            fontSize: 12
		                        }
		                    },
		                    lineStyle: { color: '#DADADA',type: 'curve'}// 'curve'|'broken'|'solid'|'dotted'|'dashed'
		                },
		                emphasis: {
		                    color: '#61A0A8',
		                    label: {
		                        show: false
		                    },
		                    borderWidth: 0
		                }
		            },
		            data: [data] 
		        }
		    ]
		};
	return option ;
}

var root = '';
var traceTableResult = [], tempData = [],  tempHeadLen = 0, headData = [];
function createTableTrace(tableData, fieldsInfo){
	var level = 0;
	var table = '<table id="traceTable" style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
	table += '<thead><tr style="border-bottom:1px dotted #ccc;">';
	for ( var index in fieldsInfo) {
		level++;
		headData.push(fieldsInfo[index].alias);
		table += "<td align='left'><a onclick='sortField(\""+ fieldsInfo[index].alias+"\")'><b>"+fieldsInfo[index].alias+"</b></a></td>";
	}
	level++;
	headData.push('次数');
	table += "<td align='left'><a onclick='sortField(\"次数\")'><b>次数</b></a></td></th></tr></thead>";
	if(tableData && tableData[0].data && tableData[0].data.length > 0){
		var treeData = tableData[0].data[0].children;
		table += "<tbody>";
		if(traceTableResult.length == 0 || tempHeadLen != (fieldsInfo.length +1)){
			buildTableData(treeData, headData, {});
			traceTableResult = tempData;
			tempHeadLen = fieldsInfo.length+1;
		}
		table = builtTable(traceTableResult, headData, table);
	}
	table += '</tbody></table>';
	return table;
}

function buildTableData(treeData, headData, loopObj){
	$.each(treeData,function(m, item){
		if(item.level == 1){
			loopObj = {};
		}
		loopObj[headData[item.level-1]] = item.name;
		var tempArray = item.children; 
		if(tempArray && tempArray.length > 0){
			buildTableData(tempArray, headData, loopObj);
		} else {
			var len = headData.length - item.level-1;
			if(len != 0){
				for(var i = 0; i < len; i++){
					loopObj[headData[item.level+i]] = '';
				}
			} else {
				loopObj[headData[item.level]] = item.name;
			}
			loopObj[headData[headData.length-1]] = item.value;
			var loopData = [];
			for(var obj in loopObj){
				loopData[obj] = loopObj[obj];
			}
			tempData.push(loopData);
		}
	});
}
function builtTable(treeData, headData, addStr){
	$.each(treeData,function(i, item){
		var index = 0;
		addStr += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
		for(var index = 0, len = headData.length; index < len; index++){
			addStr += "<td align='left'>"+ item[headData[index]] + "</td>";
		}
		addStr += "</tr>";
	});
	return addStr;
}

function exportTrace(type){
	window.location = "/sim/logSearchResultStat/exportLogIPTrace?exportType="+type+'&'+$('#traceTools').serialize();
}
function backPage(){
	$('.echarts-dataview').fadeOut();
	tempData = [], headData = [];
}
var curField = '', curOrder = 'asc', clickCount = 0; 
function sortField(field){
	if(curField == field && clickCount % 2 == 0){
		clickCount++;
		curOrder = 'desc';
	} else {
		clickCount = 0;
		curOrder = 'asc';
	}
	curField = field;
	sort(traceTableResult, field, curOrder);
	if(traceTableResult){
		$('#traceTable tbody').html('');
		var addStr = '';
		$.each(traceTableResult,function(i, item){
			var index = 0;
			addStr += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
			for(var index = 0, len = headData.length; index < len; index++){
				addStr += "<td align='left'>"+ item[headData[index]] + "</td>";
			}
			addStr += "</tr>";
		});
		$('#traceTable tbody').html(addStr);
	}
}