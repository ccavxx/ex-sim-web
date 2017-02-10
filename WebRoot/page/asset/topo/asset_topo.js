var isFF = navigator.userAgent.toLowerCase().indexOf('firefox');
var mousewheel = function(event) {
	var event = event ? event : window.event;
	var obj = event.srcElement ? event.srcElement : event.target;
	if (obj.type == "application/x-shockwave-flash" || obj.type == "") {
		var delta ;
		if (isFF > 0) {
			delta = event.detail ;
			event.preventDefault();
			event.stopPropagation();
		} else {
			delta = event.wheelDelta ;
		}
		if(delta < 0){
			swfobject.getObjectById("AssetTopoBrowse").zoomOutTopo() ;
		}else{
			swfobject.getObjectById("AssetTopoBrowse").zoomInTopo() ;
		}
		return false;
	}
}
function onloaded() {
	if (isFF > 0)
		document.body.addEventListener("DOMMouseScroll", mousewheel, false);
	else
		document.body.onmousewheel = mousewheel;
}
window.onload = function() {
	onloaded();
}
function viewAssetDetail(ip){
	parent.simMainHandler.showAssetDetailTp(ip) ;
}
function viewAssetLog(ip){
	parent.simMainHandler.showAssetLog(ip) ;
}
function viewAssetAlarm(ip){
	parent.simMainHandler.showAssetAlarm(ip) ;
}
function viewAssetEvent(ip){
	parent.simMainHandler.showAssetEvent(ip) ;
}
function viewDataSourceLog(securityObjectType,id,nodeId,deviceIp){
	parent.simHandler.viewDataSourceLog(securityObjectType,id,nodeId,deviceIp) ;
}
function viewReport(securityObjectType,id,nodeId,deviceIp){
	parent.simHandler.viewReport(securityObjectType,id,nodeId,deviceIp) ;
}
function viewAssetStatus(ip){
	parent.simMainHandler.showAssetStatus(ip) ;
}
function ping(ip){
	parent.simHandler.ping(ip) ;
}
function invokeAssetTool(tool,ip){
	if(tool == "mstsc"){
		mstsc(ip) ;
	}else{
		var url = "/sim/assetvt/openClient?type="+tool+"&ip="+ip ;
		window.open(url) ;
	}
}
function mstsc(ip){
	var userAgent = navigator.userAgent;
	var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1;
	if(isIE){
		var reIE = userAgent.match(/MSIE ([\d.]+)/);  
		var fIEVersion = parseFloat(reIE[1]);
		if(fIEVersion >= 8){
			try{
			   	var objShell = new ActiveXObject("wscript.shell");      
			   	objShell.Run("mstsc /v:" + ip);    
			   	return ;
			}catch(err){
				var width = $(window).width() ;
				var height = $(window).height() - 70;
				var url = "/sim/assetvt/openClient?type=mstsc&ip="+ip+"&width="+width+"&height="+height ;
				window.open(url) ;
			}
		}else{
			parent.showAlertMessage("此功能只支持IE8及以上浏览器！") ;
		}
	}else{
		parent.showAlertMessage("此功能只有在IE浏览器下可用，且浏览器版本最低为IE8！") ;
	}
}	