<!-- 交换机背板使用率信息 -->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<div id="adt_backboard${param.tabSeq}" style="height:125px;"/>
<script>
function loadBackboardData(ip,option){
	assetChart.getChartData(ip,"backboard",function(result){
		if(result){
			if(result.status && result.result){
				option.series[0].data[0].value = result.result.toFixed(1) - 0;
			}else{
				option.series[0].data[0].value = 0;
			}
		}
	});
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
            'echarts/chart/gauge'  // 按需加载所需图表，如需动态类型切换功能，别忘了同时加载相应图表
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表
			myChart = ec.init(document.getElementById('adt_backboard${param.tabSeq}')); 
		}
    );
	
	var option = {
		tooltip : {
			formatter: "{a} : {c}%"
		},
		series : [
	        {
	            name:'背板使用率',
	            type:'gauge',
	            radius: '90%',
	            splitNumber:2,
	            center : ['50%', '55%'],
	            axisLine: {            // 坐标轴线
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    width: 10
	                }
	            },
	            axisTick: {            // 坐标轴小标记
	                length :15,        // 属性length控制线长
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    color: 'auto'
	                }
	            },
	            splitLine: {           // 分隔线
	                length :20,         // 属性length控制线长
	                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
	                    color: 'auto'
	                }
	            },
	            pointer: {
	                width:5
	            },
	            title : {
	            	offsetCenter: [0, '-20%'], // x, y，单位px
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 12,
	                    fontStyle: 'italic'
	                }
	            },
	            detail : {
	            	formatter:'{value}%',
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 18
	                }
	            },
	            data:[{value: 0, name: '背板'}]
	            
	        }
	    ]
	};
	
	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadBackboardData(ip,option);
	myChart.setOption(option);
	createTimer(function(){
		loadBackboardData(ip,option);
		myChart.setOption(option);
	},10000+Math.random()*5000,${param.tabSeq});
	
}) ;
</script>