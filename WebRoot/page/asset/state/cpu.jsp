<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<div id="adt_cpu${param.tabSeq}" style="height:125px;"/>
<script>
function loadCpuData(ip,option){
	assetChart.getChartData(ip,"cpu",function(result){
		if(result){
			if(result.status && result.result){
				option.series[0].data[0].value = result.result.toFixed(1) - 0;
			}else{
				option.series[0].data[0].value = 0;
			}
		}
	});
	assetChart.getChartData(ip,"memory",function(result){
		if(result){
			if(result.status && result.result){
				option.series[1].data[0].value = result.result.toFixed(1) - 0;
			}else{
				option.series[1].data[0].value = 0;
			}
		}
	});
	assetChart.getFlowData(ip,function(result){
		if(result){
			if(result.flow){
				if (result.flow > option.series[2].max) {
					//流量仪表盘默认500，当值超过500时动态调整仪表盘最大值
					if (result.flow <= 1000) {
						option.series[2].max = 1000;
					} else if (result.flow <= 5000) {
						option.series[2].max = 5000;
					} else {
						option.series[2].max = 20000;
					}
				}
				option.series[2].data[0].value = result.flow.toFixed(1) - 0;
			}else{
				option.series[2].data[0].value = 0;
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
			myChart = ec.init(document.getElementById('adt_cpu${param.tabSeq}')); 
		}
    );
	
	var option = {
		tooltip : {
			formatter: function(params){
				if (params.seriesName=="cpu使用率") {
					return params.seriesName + "：" + params.data.value + "%";
				} else if (params.seriesName=="内存使用率") {
					return params.seriesName + "：" + params.data.value + "%";
				} else {
					return params.seriesName + "：" + params.data.value;
				}
			}
		},
		series : [
	        {
	            name:'cpu使用率',
	            type:'gauge',
	            radius: '90%',
	            splitNumber:4,
	            center : ['50%', '55%'],
	            axisLine: {            // 坐标轴线
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    width: 2
	                }
	            },
	            axisTick: {            // 坐标轴小标记
	                length :15,        // 属性length控制线长
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    color: 'auto'
	                }
	            },
	            splitLine: {           // 分隔线
	                length :15,         // 属性length控制线长
	                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
	                    color: 'auto'
	                }
	            },
	            pointer: {
	                width:5
	            },
	            title : {
	            	offsetCenter: [0, '-25%'],       // x, y，单位px
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 15,
	                    //fontStyle: 'italic'
	                }
	            },
	            detail : {
	            	formatter:'{value}%',
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 18
	                }
	            },
	            data:[{value: 0, name: 'cpu'}]
	            
	        },
	        {
	            name:'内存使用率',
	            type:'gauge',
	            radius: '80%',
	            center : ['30%', '52%'],
	            startAngle:232,
	            endAngle:45,
	            splitNumber:2,
	            axisLine: {            // 坐标轴线
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    width: 2
	                }
	            },
	            axisLabel: {           // 坐标轴文本标签
	                show: true,
				    formatter: '{value}',
				    textStyle: {
				        color: 'auto'
				    }
	            },
	            axisTick: {            // 坐标轴小标记
	                length :12,        // 属性length控制线长
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    color: 'auto'
	                }
	            },
	            splitLine: {           // 分隔线
	                length :15,        // 属性length控制线长
	                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
	                    color: 'auto'
	                }
	            },
	            pointer: {
	            	length : '70%',
	                width:4
	            },
	            title : {
	                offsetCenter: [0, '-50%'],       // x, y，单位px
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    //fontWeight: 'bolder',
	                    fontSize: 10,
	                    //fontStyle: 'italic'
	                }
	            },
	            detail : {
	            	formatter:'{value}%',
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 15
	                }
	            },
	            data:[{value: 0, name: '内存'}]
	        },
	        {
	            name:'流量',
	            type:'gauge',
	            min:0,
	           	max:500,
	            radius: '80%',
	            center : ['70%', '52%'],
	            startAngle:135,
	           	endAngle:-52,
	           	splitNumber:1,
	            axisLine: {            // 坐标轴线
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    width: 2
	                }
	            },
	            axisTick: {            // 坐标轴小标记
	                length :12,        // 属性length控制线长
	                lineStyle: {       // 属性lineStyle控制线条样式
	                    color: 'auto'
	                }
	            },
	            splitLine: {           // 分隔线
	                length :15,         // 属性length控制线长
	                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
	                    color: 'auto'
	                }
	            },
	            pointer: {
	            	length : '70%',
	                width:4
	            },
	            title : {
	                offsetCenter: [0, '-50%'],       // x, y，单位px
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    //fontWeight: 'bolder',
	                    fontSize: 10,
	                    //fontStyle: 'italic'
	                }
	            },
	            detail : {
	            	formatter:'{value}',
	                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	                    fontWeight: 'bolder',
	                    fontSize: 15
	                }
	            },
	            data:[{value: 0, name: '流量'}]
	        }
	    ]
	};
        
	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadCpuData(ip,option);
	myChart.setOption(option);
	createTimer(function(){
		loadCpuData(ip,option);
		myChart.setOption(option);
	},5000,${param.tabSeq});
}) ;

</script>
