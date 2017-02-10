function EchartsModel(){
	this.myChart = {};
	this.legendState = null ;
	this.configPath = '/js/echart/build/dist';
	
	/**
	 * 图表绘制函数
	 */
	this.initChart = function(){
		var configPath = this.configPath;
		//路径配置
		require.config({ 
			paths: {echarts: configPath }
		});
	};
	
	/**
	 * 图表绘制函数
	 * @param requireChart 图表所需的js数组 
	 * @param id echarts初始化容器的ID
	 * @param setOption 设置chart的option函数
	 * @param url 请求数据路径
	 * @param params 请求数据参数
	 * @param callback 请求数据回调函数
	 */
	this.buildChart = function(echarts, id, setOption){
		var myChart = echarts.init($(id)[0]); 
		myChart.setOption(setOption(), true);
		this.myChart = myChart ;
		return myChart;
	};
	
	/**
	 * 图表数据初始化
	 * @param myChart 
	 * @param url 请求路径
	 * @param params 请求参数
	 * @param callback 异步回调函数,需要myChart、result、params三个参数
	 */
	this.initData = function (myChart, url, params, callback){
		$.ajax({
			url : url,
			data : params,
			type : "post",
			dataType : 'json',
			success : function(result) {
				callback(myChart, result, params);
			}
		});
	};
	
	/**
	 * 获取DataRange最大值
	 * @param srcData 源地址数据
	 * @param descData 目的地址数据
	 */
	this.getDataRangeMax = function(srcData, descData){
		var max = 5000;
		var srcMax = 0, descMax = 0;
		if(srcData && srcData.length > 0){
			for ( var srcIndex in srcData) {
				if(srcData[srcIndex].value > srcMax){
					srcMax = srcData[srcIndex].value;
				}
			}
		}
		if(descData && descData.length > 0){
			for ( var descIndex in descData) {
				if(descData[descIndex].value > descMax){
					descMax = descData[descIndex].value;
				}
			}
		}
		if(srcMax > 0 || descMax > 0){
			var max = srcMax > descMax ? srcMax : descMax;
			max = Math.ceil(max/2000)*1000;
		}
		return max;
	}
	/**
	 * 设置Symbol大小
	 */
	this.getSymbolSize = function(val){
		var size = 5 + val / 5000;
		size = size > 10 ? 10 : size;
		return size;
	}
	/**
	 * 设置图表提示框位置
	 * @param id
	 * @param position
	 * @param xstep
	 * @param ystep
	 */
	this.setTooltipPosition = function (id, position, xstep, ystep){
//		var _left = $(id).position().left;
		var _width = $(id).width();
		var xstep = xstep? xstep : 0;
		var ystep = ystep? ystep : 0;
		var x = position[0];
		if(position[0] - xstep < _width){
			x = position[0];
		}
		if(position[0]+xstep > _width){
			x = position[0]-xstep;
		}
        return [x, position[1]+ystep];
	}
	/**
	 * 
	 * @param val
	 */
	this.labelFormatter = function(val){
		 var label;
		 if(val >= 1000000000){
			 label = (val/1000000000).toFixed(0)+'B'; 
		 } else if(val >= 1000000){
			 label = (val/1000000).toFixed(0)+'M'; 
		 } else if(val >= 1000){
			 label = (val/1000).toFixed(0)+'K'; 
		 } else {
			 label = val; 
		 }
		 return label;
	}

	this.onLegendSelect = function(listener){
		var _this = this ;
		this.legendState = null ;
		this.myChart.on("legendSelected", function (param) {
			_this.legendState = param.selected;
			if(listener && typeof listener == "function"){
				listener(param) ;
			}
		});
	}
	
	this.getSelectLegend = function(){
		var selectLegend = new Array() ;
		var allLegend = this.myChart.getOption().legend.data ;
		if(this.legendState == null){
			this.legendState = this.myChart.getOption().legend.selected ;
		}
		for(var i in allLegend){
			if(this.legendState[allLegend[i]]){
				selectLegend.push(allLegend[i]) ;
			}
		}
		return selectLegend ;
	}
}
