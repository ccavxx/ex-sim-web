<%@ page language="java" pageEncoding="utf-8"%>
<div id="adt_flow_trend${param.tabSeq}" style="height:125px;"/>
<script>
function loadFlowTrendData(ip,option,myChart){
	var startTime = [];
	var flowTrend = [];
	$.getJSON("/sim/assetdetail/flowTrend?ip="+ip,function(data){
		$.each(data,function(index,record){
			startTime.push(record.time);
			flowTrend.push(record.rate);
		});
	    option.xAxis[0].data = startTime;
		option.series[0].data = flowTrend;
		myChart.setOption(option);
	});
	myChart.hideLoading();
	
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
			myChart = ec.init(document.getElementById('adt_flow_trend${param.tabSeq}')); 
		}
    );
	
	myChart.showLoading({
    	text: '正在努力的读取数据中...',
	});
	
	var option = {
		/* noDataLoadingOption: {//暂无数据时不渲染默认背景
            text: '暂无数据',
            effect: 'bubble',
            effectOption: {
                effect: {
                    n: 0
                }
            }
 		}, */
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
	    /* legend: {
	        data:['流量']
	    }, */
	    dataZoom : {
	        show : false,
	        start : 0,
	        end : 100
	    },
	    calculable : true,
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
	            scale: true,//在设置 min 和 max 之后该配置项无效
	            name : '条/秒',
	            boundaryGap: [0, 0.1],
	            splitNumber: 2,
	            axisLabel : {
	            	show: true,
	                textStyle: {
                        color: 'black',
                        fontSize: 11
                    }
	            }
	        }
	    ],
	    series : [
	        {
	            name:'流量',
	            type:'line',
	            yAxisIndex: 0,
	            symbol:'none',
	            smooth:true,
	            itemStyle: {normal: {areaStyle: {type: 'default'}}},
	            data:[]
	        }
	    ]
	};

	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadFlowTrendData(ip,option,myChart);
	createTimer(function(){
		loadFlowTrendData(ip,option,myChart);
	},60000,${param.tabSeq});
	
}) ;
</script>