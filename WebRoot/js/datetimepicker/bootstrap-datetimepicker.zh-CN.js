/**
 * Simplified Chinese translation for bootstrap-datetimepicker

 * Yuan Cheung <advanimal@gmail.com>
 */
var locales = {
	'zh-CN' : {
		applyLabel : '应用',
		cancelLabel : '取消',
		fromLabel : '从',
		toLabel : '到',
		weekLabel : '周',
		customRangeLabel : '自定义',
		daysOfWeek : ["日", "一", "二", "三", "四", "五", "六"],
		monthNames : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
		firstDay : 1
	}
};
;(function($){
	$.fn.datetimepicker.dates['zh-CN'] = {
				days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
			daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
			daysMin:  ["日", "一", "二", "三", "四", "五", "六", "日"],
			months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
			monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
			today: "今日",
		suffix: [],
		meridiem: []
	};
}(jQuery));
