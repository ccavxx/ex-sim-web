<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<div id="adt_disk${param.tabSeq}" style="height:125px;"/>
<script>
function loadDiskData(ip,option,myChart){
	assetChart.getChartData(ip, "disk", function(result) {
		if(result){
			if(result.status && result.result){
				var xname = [];
				var usedZone = [];
				var enableZone = [];
				var flag = false;
				$.each(result.result, function(title, data) {
					flag = true;
					xname.push(title);
					var usedPercent = data["DISK_USED_PERCENT"];
					var size = data["DISK_CAPABILITY"];
					if(usedPercent != null && (typeof usedPercent) != undefined && size) {
						var enableUse = (100 - usedPercent) * size / 100;
						enableZone.push((enableUse/(1024*1024*1024)).toFixed(1)-0);
						usedZone.push(((usedPercent * size / 100)/(1024*1024*1024)).toFixed(1)-0);
					}
				});
				if (flag) {//有数据，正常显示的场景
					option.xAxis[0].data = xname;
					option.series[0].data = usedZone;
					option.series[1].data = enableZone;
					myChart.setOption(option);
				} else {//监视对象启用时，数据为空的场景
					option.xAxis[0].data = [""];
					option.series[0].data = [""];
					option.series[1].data = [""];
					myChart.setOption(option);
				}
			}else{
				//没有数据，监视对象禁用的场景
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
            'echarts/chart/bar'  // 按需加载所需图表，如需动态类型切换功能，别忘了同时加载相应图表
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表
			myChart = ec.init(document.getElementById('adt_disk${param.tabSeq}')); 
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
			y:40,
			x2:30,
			y2:30
		},
	    title : {
	        text: '',
	        subtext: '',
	        sublink: '',
        textStyle: { // 其余属性默认使用全局文本样式，详见TEXTSTYLE
                fontWeight: 'bolder',
                fontSize: 15,
                fontStyle: 'italic'
            }
	    },
	    tooltip : {
	        trigger: 'axis',
	        axisPointer : {      // 坐标轴指示器，坐标轴触发有效
	            type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
	        },
	        formatter: function (params){
	            return params[0].name + '<br/>'
	            	   + "使用率：" + ((params[1].value*100/(params[0].value + params[1].value)).toFixed(1)-0) + "%" + '<br/>'
	                   + params[1].seriesName + ' : ' + params[1].value + 'G' + '<br/>'
	                   + params[0].seriesName + ' : ' + params[0].value + 'G';
	        }
	    },
	    legend: {
	        selectedMode: false,
	        data:['已用空间', '剩余空间']
	    },
	    calculable : true,
	    xAxis : [
	        {
	            type : 'category',
	            boundaryGap: true,
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
	            boundaryGap: [0, 0.1],
	            splitNumber: 2,
	            axisLabel : {
	            	show: true,
	                formatter: '{value} G',
	                textStyle: {
                        color: 'black',
                        fontSize: 11
                    }
	            }
	        }
	    ],
	    series : [
	        {
	            name:'已用空间',
	            type:'bar',
	            stack: 'sum',
	            barCategoryGap: '50%',
	            barMaxWidth:50,
	            barWidth:40,
	            barMinHeight:20,
	            itemStyle: {
	                normal: {
	                    color: 'tomato',
	                    barBorderColor: 'gray',
	                    barBorderWidth: 1,
	                    barBorderRadius:5,
	                    label : {
	                        show: true, position: 'insideTop'
	                    }
	                }
	            },
	            data:[]
	        },
	        {
	            name:'剩余空间',
	            type:'bar',
	            stack: 'sum',
	            itemStyle: {
	                normal: {
	                    color: '#99ccff',
	                    barBorderColor: 'gray',
	                    barBorderWidth: 1,
	                    barBorderRadius:5,
	                    label : {
	                        show: true, 
	                        position: 'top',
	                        formatter: function (params) {
	                            for (var i = 0, l = option.xAxis[0].data.length; i < l; i++) {
	                                if (option.xAxis[0].data[i] == params.name) {
	                                    return (parseFloat(option.series[0].data[i]) + parseFloat(params.value)).toFixed(1)-0;
	                                }
	                            }
	                        },
	                        textStyle: {
	                            color: 'gray'
	                        }
	                    }
	                }
	            },
	            data:[]
	        }
	    ]
	};
	
	// 为echarts对象加载数据 
	var ip = "${param.ip}";
	loadDiskData(ip,option,myChart);
	createTimer(function(){
		loadDiskData(ip,option,myChart);
	},30000,${param.tabSeq});
});
</script>