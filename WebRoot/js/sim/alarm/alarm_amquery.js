/**
 * 告警查询，表格数据
 * @returns {AlarmQuery}
 */

function AlarmQuery(){
	this._BASE_ALARM_QUERY_URL = "/sim/alarm/queryAlarm";
}
AlarmQuery.prototype.doAlarmQuery = function(){
	queryParams = {
		"deviceIp" : _alarmQueryForm.deviceIp.value,
		"startTime" :_alarmQueryForm.startTime.value,
		"endTime" : _alarmQueryForm.endTime.value,
		"priority" : _alarmQueryForm.level.value,
		"eventName" : _alarmQueryForm.eventName.value,
		"srcIp" : _alarmQueryForm.srcIp.value,
		"destIp" : _alarmQueryForm.destIp.value,
		"category3" : _alarmQueryForm.category.value,
		"ip" : _alarmQueryForm.ip.value
	};
	$("#alarm_query_table_id").datagrid("load",queryParams);
}
var alarmQuery = new AlarmQuery() ;