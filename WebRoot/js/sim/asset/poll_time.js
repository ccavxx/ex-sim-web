/**
 *轮询时间 
 */
var pollTime = {
} 

/**
 * 选择轮询时间
 */
pollTime.typeChangeHandler = function(timerTypeList){
	var timerTypeSelect = $(timerTypeList) ;
	var showElements = timerTypeSelect.children(":selected").attr("showElements");
	var elementIds = showElements.split(",") ;
	var allInput = timerTypeSelect.siblings().each(function(){
		var elementId = $(this).attr("id") ;
		var findIndex = $.inArray(elementId,elementIds) ;
		if(findIndex > -1){//如果是要显示的元素
			$(this).css("display","block") ;
		}else if(elementId != "pollTimeErrorDiv"){
			$(this).css("display","none") ;
		}
		
	}) ;
	$("#pollTimeError").html("") ;
};
pollTime.validatePollTime = function(){
	var selectValue = $("#poll_time_type").val();
	var hour = $("#poll_time_container [name='hour']").val();
	var minute = $("#poll_time_container [name='min']").val();
	var date = $("#poll_time_container [name='date']").val();
	if(selectValue == "INTERVAL_MINUTE"){
		if(minute>59 || minute<1){
			return "分钟取值范围[1-59]。";
		}
	}else if(selectValue == "INTERVAL_HOUR"){
		if(hour>24 || hour<1){
			return "小时取值范围[1-23]。";
		}
	}else if(selectValue == "EVERY_DAY" || selectValue == "EVERY_WEEK"){
		if(hour>23 || hour<0){
			return "小时取值范围[0-23]。" ;
		}else if(minute>59 || minute<0){
			return "分钟取值范围[0-59]。" ;
		}
	}else if(selectValue == "EVERY_MONTH" || selectValue == "EVERY_YEAR"){
		if(date > 31 || date<1){
			return "日期取值范围[1-31]。" ;
		}else if(hour>23 || hour<0){
			return "小时取值范围[0-23]。";
		}else if(minute>59 || minute<0){
			return "分钟取值范围[0-59]。" ;
		}
	}
	return null;
};
$(function(){
	var timerType = $("#poll_time_type") ;
	var selectValue = timerType.attr("selectValue") ;
	if(selectValue && selectValue != ""){
		timerType.val(selectValue) ;
		switch(selectValue){
			case "INTERVAL_MINUTE" : 
				$("#poll_time_container [name='min']").val(timerType.attr("interval")) ;break ;
			case "INTERVAL_HOUR" :	
				$("#poll_time_container [name='hour']").val(timerType.attr("interval")) ;break ;
			case "EVERY_DAY" :	
				$("#poll_time_container [name='hour']").val(timerType.attr("hour")) ;
				$("#poll_time_container [name='min']").val(timerType.attr("min")) ;break ;
			case "EVERY_WEEK" :	
				$("#poll_time_container [name='day']").val(timerType.attr("week")) ;
				$("#poll_time_container [name='hour']").val(timerType.attr("hour")) ;
				$("#poll_time_container [name='min']").val(timerType.attr("min")) ;break ;
			case "EVERY_MONTH" :
				$("#poll_time_container [name='date']").val(timerType.attr("date")) ;
				$("#poll_time_container [name='hour']").val(timerType.attr("hour")) ;
				$("#poll_time_container [name='min']").val(timerType.attr("min")) ;break ;
			case "EVERY_YEAR" :
				$("#poll_time_container [name='month']").val(timerType.attr("month")) ;
				$("#poll_time_container [name='date']").val(timerType.attr("date")) ;
				$("#poll_time_container [name='hour']").val(timerType.attr("hour")) ;
				$("#poll_time_container [name='min']").val(timerType.attr("min")) ;break ;
		}
	}
}) ;