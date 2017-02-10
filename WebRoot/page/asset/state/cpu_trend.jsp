<%@ page language="java" pageEncoding="utf-8"%>
<div id="adt_cpu_memory_trend${param.tabSeq}" style="height:125px;"/>
<script>
function loadCpuAndMemoryTrendData(ip,option,myChart){
		var time = new Date().getTime() ;
		var startTime = [];
		var cupTrend = [];
		var memoryTrend = [];
		$.ajax({
			url : "/sim/assetdetail/getCpuAndMemoryTrend",
			async : false,
			type : "post",
			data : {ip:ip},
			dataType : "json",
			success : function (data) {
				if (data && data.length > 0) {
					$.each(data,function(index,record){
						var cpuUsed = record.CPU_USED_PERCENT == undefined ? 0 : record.CPU_USED_PERCENT;
						var memoryUsed = record.MEM_USED_PERCENT == undefined ? 0 : record.MEM_USED_PERCENT;
						startTime.push(new Date(record.START_TIME).Format("HH:mm:ss"));
						cupTrend.push(cpuUsed);
						memoryTrend.push(memoryUsed);
					});
				    option.xAxis[0].data = startTime;
					option.series[0].data = cupTrend;
					option.series[1].data = memoryTrend;
					myChart.setOption(option);
				} else {
					option.xAxis[0].data = [""];
					option.series[0].data = [""];
					option.series[1].data = [""];
					myChart.setOption(option);
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
            'echarts/chart/line'  // 按需加载所需图表，如需动态类型切换功能，别忘了同时加载相应图表
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表
			myChart = ec.init(document.getElementById('adt_cpu_memory_trend${param.tabSeq}')); 
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
		grid : {
			x:50,
			y:30,
			x2:50,
			y2:30
		},
	    title : {
	        text: '',
	        subtext: ''
	    },
	    tooltip : {
	        trigger: 'axis'
	    },
	    legend: {
	    	//x : 'right',
	    	//y : 'center',
	    	//orient : 'vertical',
	        data:['cpu使用率', '内存使用率'/* , '流量' */]
	    },
	    dataZoom : {
	        show : false,
	        start : 0,
	        end : 100
	    },
	    xAxis : [
	        {
	            type : 'category',
	            boundaryGap : false,
	            axisLabel: {
                    show: true,
                    //interval: 0,//横轴数据如果非常多，会自动隐藏一部分数据，我们可以通过属性interval来进行调整
                    textStyle: {
                        color: 'black',
                        fontSize: 11
                    }
                },
                axisTick: {
                    show: false,
                },
	            data : []
	        }
	    ],
	    yAxis : [
	        {
	            type : 'value',
	            scale: false,//显示动态区间,是否是脱离 0 值比例。
	            name : '使用率%',
	            min : 0,
	            max : 100,//设置后将忽略boundaryGap
	            boundaryGap: [0, 0.1],
	            splitNumber: 2,
	            axisLabel : {
	            	show: true,
	                textStyle: {
                        color: 'black',
                        fontSize: 11
                    }
	            }
	        }/* ,
	        {
	            type : 'value',
	            scale: true,//在设置 min 和 max 之后该配置项无效
	            name : '流量',
	            boundaryGap: [0, 0.1]
	        } */
	    ],
	    series : [
	        /* {
	            name:'流量',
	            type:'bar',
	            yAxisIndex: 1,
	            data:[]
	        }, */
	        {
	            name:'cpu使用率',
	            type:'line',
	            smooth:true,
            	itemStyle: {normal: {areaStyle: {type: 'default'}}},
	            data:[]
	        },
	        {
	            name:'内存使用率',
	            type:'line',
	            smooth:true,
            	itemStyle: {normal: {areaStyle: {type: 'default'}}},
	            data:[]
	        }
	    ]
	};
	
	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadCpuAndMemoryTrendData(ip,option,myChart);
	createTimer(function(){
		loadCpuAndMemoryTrendData(ip,option,myChart);
	},30000+Math.random()*5000,${param.tabSeq});
	
}) ;
</script>