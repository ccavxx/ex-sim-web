<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<div id="asset_port${param.tabSeq}" style="height:1000px;width:95%"/>
<script>
function loadPortData(ip,option,myChart){
	var time = new Date().getTime();
	$.getJSON("/sim/assetdetail/port?ip="+ip+"&tabSeq=${param.tabSeq}&_time="+time,function(data){
		if (data) {
			var size = data.children.length;
			if (size < 10) {
				$("#asset_port"+${param.tabSeq}).height(250);
				option.series[0].rootLocation.y = 125;
			} else {
				if (size*23 > 1000) {
					$("#asset_port"+${param.tabSeq}).height(1000);
					option.series[0].rootLocation.y = 500;
				} else {
					$("#asset_port"+${param.tabSeq}).height(size*23);
					option.series[0].rootLocation.y = size*23/2;
				}
			}
			option.series[0].data[0] = data;
		}
	    myChart.setOption(option);
	    myChart.on("click",portTreeClickHandler) ;
	});
}
function portTreeClickHandler(params){
	if(params.name.indexOf("cpe:/") > -1){
		openDialog(params.name,800,600,"/page/knowledge/cve_list.jsp?ip=${param.ip}&cpe="+params.name) ;
	}
}

$(function(){
	var myChart;
	require.config({
        paths: {
            echarts: '/js/echart/build/dist'
        }
    });
    require(
        [
            'echarts',
            'echarts/chart/tree'  // 按需加载所需图表，如需动态类型切换功能，别忘了同时加载相应图表
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表
			myChart = ec.init(document.getElementById('asset_port${param.tabSeq}')); 
		}
    );
    
	var option = {
		noDataLoadingOption: {//暂无数据时渲染默认背景
            text: '暂无数据',
            effect: 'bubble',//'spin' | 'bar' | 'ring' | 'whirling' | 'dynamicLine' | 'bubble'
            effectOption: {
                effect: {
                    n: 15
                }
            }
 		},
	    title : {
	        text: ''
	    },
	    tooltip : {
			formatter: function(params){
				return params.name;
			}
		},
	    series : [
	        {
	            name:'树图',
	            type:'tree',
	            orient: 'horizontal',  // vertical horizontal
	            rootLocation: {x: 50,y: 120}, // 根节点位置  {x: 100, y: 'center'}
	            nodePadding: 10,
	            layerPadding: 250,
	            hoverable: false,
	            roam: 'move',
	            symbol : 'circle',
	            symbolSize: 12,
	            itemStyle: {
	                normal: {
	                    color: '#4883b4',
	                    label: {
	                        show: true,
	                        position: 'right',
	                        formatter: "{b}",
	                        textStyle: {
	                            color: '#000',
	                            fontSize: 12
	                        }
	                    },
	                    lineStyle: {
	                        color: '#ccc',
	                        type: 'curve' // 'curve'|'broken'|'solid'|'dotted'|'dashed'
	
	                    }
	                },
	                emphasis: {
	                    color: 'orange',
	                    label: {
	                        show: true
	                    },
	                    borderWidth: 1
	                }
	            },
	            
	            data: []
	        }
	    ]
	};
	
	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadPortData(ip,option,myChart);
});
</script>