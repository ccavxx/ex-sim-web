/**
 * 耦合文件system，jquery，cndiskcomp.js，耦合函数assetChart.getChartData
 */
var adtDiskFn = (function($, assetChart){
	/**
	 * 加载数据
	 */
	function loadDiskData(ip, chartId){
		
		assetChart.getChartData(ip, "disk", function(result) {
			if($(chartId).length == 0) {
				return;
			}
			// 清理内容
			$(chartId).html("");
			if(result && result.status) {
				var createSuccess = createDiskView(chartId, result.result);
				if(!createSuccess) {
					createDiskExceptionView(ip, chartId, null);
				}
			} else {
				var msg = null;
				if(result && result.message != null) {
					msg = result.message;
				}
				createDiskExceptionView(ip, chartId, msg);
			}
		});
	}
	/**
	 * 创建磁盘视图异常
	 */
	function createDiskExceptionView(ip, chartId, message) {
		var msg = '未取得磁盘信息！';
		if(message) {
			msg = message;
		}
		var warningElement = '<li class="alert-li"><div class="alert alert-warning" role="alert"><h4>提示</h4>';
		warningElement += msg;
		warningElement += '</div></li>';
		if($(chartId).length > 0) {
			$(chartId).html(warningElement);
			setTimeout(function() {
				loadDiskData(ip, chartId) ;
			},5000);
		}
	}
	/**
	 * 创建磁盘视图
	 */
	function createDiskView(containerId, datas) {
		var successFlag = true;
		$.each(datas, function(title, data) {
			var $liElement = $("<li></li>");
			var $tableElement = $("<table class='cndiskcomp'></table>");
			$liElement.append($tableElement);
			$(containerId).append($liElement);
			var optionstemp = {diskName : title};
			
			var usedPercent = data["DISK_USED_PERCENT"];
			var size = data["DISK_CAPABILITY"];

			if(usedPercent != null && (typeof usedPercent) != undefined && size) {
				var enableUse = (100 - usedPercent) * size / 100;
				var total = size;
				optionstemp.enableUse = bytesFormatter(enableUse, 2);
				optionstemp.total = bytesFormatter(total, 2);
				optionstemp.persentParam = usedPercent + "%";
				optionstemp.warningVal = "70";
			} else {
				successFlag = false;
				return true;
			}
			
			$tableElement.cndiskcomp(optionstemp);
		});
		return successFlag;
	}
	return {loadDiskData:loadDiskData};
})(jQuery, assetChart);