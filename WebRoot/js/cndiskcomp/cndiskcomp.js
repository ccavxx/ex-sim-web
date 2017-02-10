/**
 * 自定义磁盘组件样式
 * by wang_qiao
 */
(function( $ ){
	var defaults = {
			diskName:"本地磁盘",
			enableUse:"0GB",
			total:"0GB",
			warningVal:80,
			persentParam:"1px"
		}
	var updateData = function(it,options) {
		if (options) {
			$.extend(defaults, options);
		}
		var persentParamTemp = defaults["persentParam"];
		var warningClass = "";
		if(persentParamTemp == "1px"){
			persentParamTemp = "0%";
		} else {
			var perst = persentParamTemp.replace("%", "");
			if(perst > parseInt(defaults["warningVal"])) {
				warningClass = "horizon-disk-cncomp-content1-warning";
			}
		}
		
		it.find(".horizon-disk-cncomp-diskName").text(defaults["diskName"]);
		it.find(".horizon-disk-cncomp-content1").addClass(warningClass).html("<div style='width:" + defaults["persentParam"] + ";'>" + persentParamTemp + "</div>");
		it.find(".horizon-disk-cncomp-content2").html(defaults["enableUse"] + " 可用 , 共 " + defaults["total"]);
	}
	$.fn.cndiskcomp = function(options, method) {
		if (typeof options == "string") {
			return updateData(this, method);
		}
		options = options || {};

		return this.each(function(){
			var $cndiskcompElement = $(this);
			if($cndiskcompElement.attr("cndiskc") === "_cndiskcomp"){
				return true;
			}
			if (options) {
				$.extend(defaults, options);
			}
			var  $cndiskcompId = $cndiskcompElement.attr("id");
			if(!$cndiskcompId) {
				$cndiskcompId = "cn_" + new Date().getTime();
			}
			$cndiskcompElement.attr("cndiskc", "_cndiskcomp").addClass("horizon-disk-cncomp");
			var persentParamTemp = defaults["persentParam"];
			var warningClass = "";
			if(persentParamTemp == "1px"){
				persentParamTemp = "0%";
			} else {
				var perst = persentParamTemp.replace("%", "");
				if(perst > parseInt(defaults["warningVal"])) {
					warningClass = "horizon-disk-cncomp-content1-warning";
				}
			}
			var $trElementImg = $("<tr></tr>");
			var $trElementPro = $("<tr></tr>");
			var $trElementCon = $("<tr></tr>");
			$trElementImg.append("<td rowspan='3'><span class='horizon-disk-cncomp-img'></span></td>");
			$trElementImg.append("<td class='horizon-disk-cncomp-diskName'>" + defaults["diskName"] + "</td>");
			$trElementPro.append("<td><div class='horizon-disk-cncomp-content1 " + warningClass + "'><div style='width:" + defaults["persentParam"] + ";'>" + persentParamTemp + "</div></div></td>");
			$trElementCon.append("<td class='horizon-disk-cncomp-content2'>" + defaults["enableUse"] + " 可用 , 共 " + defaults["total"] + "</td>");
			$cndiskcompElement.append($trElementImg).append($trElementPro).append($trElementCon);
		});
	};
})(jQuery);