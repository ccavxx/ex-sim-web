<%@ page language="java" pageEncoding="utf-8"%>
<div id="poll_time_container" class="form-inline">
	<select id="poll_time_type" name="timerType" 
		selectValue="${timerExpression.type}" 
		month="${timerExpression.month}" week="${timerExpression.week}" date="${timerExpression.date}" hour="${timerExpression.hrs}"
		min="${timerExpression.min}" interval="${timerExpression.interval}"
		onchange="pollTime.typeChangeHandler(this)" class="pull-left" style="width:80px;">
		<option value="INTERVAL_MINUTE" showElements="pollTime_minInput">分钟间隔</option>
		<option value="INTERVAL_HOUR" showElements="pollTime_hourInput">小时间隔</option>
		<option value="EVERY_DAY" showElements="pollTime_hourInput,pollTime_minInput">每日</option>
		<option value="EVERY_WEEK" showElements="pollTime_weekDays,pollTime_hourInput,pollTime_minInput">每周</option>
		<option value="EVERY_MONTH" showElements="pollTime_dateInput,pollTime_hourInput,pollTime_minInput">每月</option>
		<option value="EVERY_YEAR" showElements="pollTime_monthList,pollTime_dateInput,pollTime_hourInput,pollTime_minInput">每年</option>
	</select>
	<div id="pollTime_monthList" class="pull-left input-append" style="display:none;">
		<select name="month" style="width:40px;">
			<option value="1">1</option>
			<option value="2">2</option>
			<option value="3">3</option>
			<option value="4">4</option>
			<option value="5">5</option>
			<option value="6">6</option>
			<option value="7">7</option>
			<option value="8">8</option>
			<option value="9">9</option>
			<option value="10">10</option>
			<option value="11">11</option>
			<option value="12">12</option>
		</select>
		<span class="add-on">月</span>
	</div>
	<div id="pollTime_weekDays" class="pull-left input-prepend" style="display:none;">
	    <span class="add-on">周</span>
		<select name="day" style="width:50px;">
			<option value="2">一</option>
			<option value="3">二</option>
			<option value="4">三</option>
			<option value="5">四</option>
			<option value="6">五</option>
			<option value="7">六</option>
			<option value="1">日</option>
		</select>
	</div>	
	<div id="pollTime_dateInput" class="pull-left input-append" style="display:none;">
	    <input type="text" class="easyui-numberbox" name="date"  data-options="value:1" style="width:18px;">
	    <span class="add-on">日</span>
	</div>
	<div id="pollTime_hourInput" class="pull-left input-append" style="display:none;">
	    <input type="text" class="easyui-numberbox" name="hour"  data-options="value:1" style="width:18px;">
	    <span class="add-on">时</span>
	</div>
	<div id="pollTime_minInput" class="input-append pull-left" style="display:none;">
	    <input type="text" class="easyui-numberbox" name="min"  data-options="value:1" style="width:18px;">
	    <span class="add-on">分</span>
	</div>
	<div id="pollTimeErrorDiv"><span id="pollTimeError"></span></div>
</div>
