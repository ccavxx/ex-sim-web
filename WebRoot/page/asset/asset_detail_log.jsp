<div id="adt_asset_log_div${param.tabSeq}">
	<table id="adt_asset_log_table${param.tabSeq}">
	</table>
</div>
<script src="/js/sim/asset/asset_detail_log.js"></script>
<script>
	
	var params = {} ;
	var queryParam = {} ;
	queryParam.host = '${param.ip}' ;
	queryParam.nodeId = '${nodeId}'
	queryParam.deviceType = '${deviceType}' ;
	queryParam.queryStartDate = '${startDate}' ;
	queryParam.queryEndDate = '${endDate}' ;
	queryParam.pageSize = 10 ;
	params.queryParams = queryParam ;
	params.tabSeq = ${param.tabSeq} ;
	params.queryCount = 0 ;
	$(function(){
		adl.loadLogData(params) ;
	}) ;
</script>