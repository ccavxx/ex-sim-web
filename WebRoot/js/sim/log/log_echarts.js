var log_echarts = {};

log_echarts.init=function(id,option,createCallback){
	require.config({
		paths : {
			echarts : '/js/echart/build/source'
		}
	});
	var chart ;
	require(
			[
			    'echarts',
				'echarts/chart/line', 
				'echarts/chart/bar',
				'echarts/chart/pie',
				'echarts/chart/tree',
				'echarts/chart/force',
				'echarts/chart/map'
				], 
			function(ec) {
				var myChart = ec.init(document.getElementById(id));
				myChart.setOption(option);
				chart = myChart ;
				if(createCallback && typeof createCallback == "function"){
					createCallback(myChart) ;
				}
			});
	return chart ;
};