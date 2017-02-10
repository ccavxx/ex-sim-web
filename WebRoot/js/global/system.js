/**
 * @description 注册timer组
 */
var registTimers = new Array() ;
/**
 * @description timer收集组
 */
var timerGroup = {} ;
/**
 * @description 视图颜色组
 */
var colors =['#32c8fa','#f99049','#a4e120','#ffe666','#906bc8', '#08a4f3','#ffa03f','#99cc00','#fff558','#d040ff', '#99ccff','#ff7f3a','#labebe','#f7c43b','#ff52b0', '#6666ff','#fe8a8a','#2cb022','#ec6dff','#95cfd1'];
/**
 * @description 创建一个定时器
 * @param obj 要执行的函数或代码
 * @param interval 时间间隔
 * @param group group
 */
function createTimer(obj,interval,group){
	var timer = window.setInterval(obj, interval) ;
	registTimers.push(timer) ;
	if(group){
		addGroupTimer(timer, group) ;
	}
	return timer ;
}
/**
 * @description 将定时器加入到指定组中
 * @param timer
 * @param group
 */
function addGroupTimer(timer,group){
	var timers = timerGroup[group] ;
	if(!timers){
		timers = new Array() ;
		timerGroup[group] = timers ;
	}
	timers.push(timer) ;
}

/**
 * @description 删除定时器,注意此方法会删除registTimers中的相应的元素，registTimers的length会减小
 * @param timerId
 */
function clearTimer(timerId){
	window.clearInterval(timerId) ;
	var timerIndex = $.inArray(timerId,registTimers) ;
	if(timerIndex > -1){
		registTimers.splice(timerIndex,1) ;
	}
}
/**
 * @description 清除所有的timer
 */
function clearAllTimer(){
	var timersCount = registTimers.length ;
	for(var i=0;i<timersCount;i++){
		clearTimer(registTimers.pop()) ;
	}
	timerGroup = {} ;
}
/**
 * @description 删除一组timer
 * @param group
 */
function clearGroupTimer(group){
	var timers = timerGroup[group] ;
	if(timers){
		for(var i=0;i<timers.length;i++){
			clearTimer(timers[i]) ;
		}
		delete timerGroup[group] ;
	}
}
/*存储转为k、M、G的方法*/
function kbFormatter(kVal,scale){
	var num = arguments.length < 2 ? 2 : scale;
	if(kVal == null || kVal == "null"){
		return "" ;
	}
	var st = 0 + " KB ";
	if(kVal<1024){
		st = kVal.toFixed(num)+" KB ";
	}else if(kVal<(1024*1024)){
		st = (kVal/1024).toFixed(num)+" MB ";
	}else if(kVal<(1024*1024*1024)){
		st = (kVal/1024/1024).toFixed(num)+" GB ";
	}else{
		st = (kVal/1024/1024/1024).toFixed(num)+" TB ";
	}
	return st;
}
/*存储转为k、M、G的方法*/
function bytesFormatter(kVal,precision){

	if(kVal == null || kVal == "null"){
		return "" ;
	}
	if(precision == null || precision == undefined){
		precision = 2 ;
	}
	var st = 0 + " KB ";
	if(kVal < 1024){
		st = kVal + "B" ;
	}else if(kVal<1024*1024){
		st = (kVal/1024).toFixed(precision)+" KB ";
	}else if(kVal<(1024*1024*1024)){
		st = (kVal/1024/1024).toFixed(precision)+" MB ";
	}else if(kVal<(1024*1024*1024*1024)){
		st = (kVal/1024/1024/1024).toFixed(precision)+" GB ";
	}else{
		st = (kVal/1024/1024/1024/1024).toFixed(precision)+" TB ";
	}
	return st;
}
function countFormatter(count,precision){
	if(precision == null || precision == undefined){
		precision = 2 ;
	}
	var result = count ;
	if(count){
		if(count >= 100000000){
			result = (count/100000000).toFixed(precision) + "亿"; 
		}else if(count >= 10000){
			result = (count/10000).toFixed(precision) + "万";
		}
	}
	return result ;
}
/**
 * 自定义messager弹框
 * 默认样式右下角滑出，如果不手动关闭将保留5000ms自动关闭，宽300px，高300px
 * @param title 弹框标题（必填）
 * @param message 弹框信息（必填）
 * @param options 弹框自定义设置（可选）格式为{key1:value1,key2:value2,......}
 */
function showPopMessage(title, message, options) {
	var opts = {
			title : title,
			msg : message,
			width:300,
			height:300,
			timeout : 5000,
			showType : "slide"
		};
	if(options) {
		$.extend(opts, options);
	}
	$.messager.show(opts);
}

function showMessage(title, message) {
	$.messager.show({
				title : title,
				msg : message,
				width:300,
				showType : 'fade',
				timeout : 2000,
				style : {
					right:'',
					bottom:''
				}
			});
}
/**
 * 错误消息
 * 
 * @param message
 */
function showErrorMessage(message) {
	showMessage("错误", message);
}
/**
 * 警告消息
 */
function showAlarmMessage(message) {
	showMessage("警告", message);
}
/**
 * 提示消息
 */
function showAlertMessage(message) {
	showMessage("提示", message);
}
/**
 * trim wq
 * @param str
 * @returns
 */
function LTrim(str){ 
	var i;
	for(i=0;i< str.length;i++){
		if(str.charAt(i)!=" " && str.charAt(i)!=" "&& str.charAt(i)!="　") break;
	}
	str = str.substring(i,str.length);
	return str;
}
function RTrim(str){
	var i;for(i=str.length-1;i>=0;i--){
		if(str.charAt(i)!=" " && str.charAt(i)!=" "&& str.charAt(i)!="　") break;
	}
	str = str.substring(0,i+1);
	return str;
}
function Trim(str){ 
	return LTrim(RTrim(str)); 
}
/**
 * 判断对象如果为null或者undefined则返回defaultValue
 * @param obj
 * @param defaultValue
 * @returns
 */
function nvl(obj,defaultValue){
	if(obj == null || obj == undefined){
		return defaultValue ;
	}
	return obj ;
}
/**
 * 通用加载完成处理函数，此函数会判断数据是否为0如果为0则追加一行没有找到记录的提示
 * @param data
 */
function generalLoadSuccess(data){
	if(data.total == 0){
		var columnFileds = $(this).datagrid('getColumnFields');
		var rowData = new Object() ;
		rowData[columnFileds[0]] = '<div style="text-align:center;color:red">没有相关记录！</div>' ;
		$(this).datagrid('appendRow', rowData)
		       .datagrid('mergeCells', {index: 0, field: columnFileds[0], colspan: columnFileds.length })
	}
}
function htmlEscape(str) {
	if(str == null || str == undefined){
		return "" ;
	}
    return  str.replace(/&/g, '&amp;')
               .replace(/"/g, '&quot;')
               .replace(/'/g, '&#39;')
               .replace(/</g, '&lt;')
               .replace(/>/g, '&gt;');
}
function askYesOrNo(message,callback,width,height){
	var dialog = $('<div/>').dialog({
		title:null,
        width: 300,   
        height: 80,   
        closed: false, 
        cache: false,
        content:"<div style='margin:5px;'>"+message+"</div>",
        modal: true,
        buttons:[{text:"是",
		      handler:function(){
		    	  dialog.dialog("destroy") ;
	    		  callback.call(this,true) ;
		      }
		},{
			      text:"否",
			      handler:function(){
			    	  dialog.dialog("destroy") ;
			    	  callback.call(this,false) ;
			      }
		},{
		          text:"取消",
			      handler:function(){
			    	  dialog.dialog("destroy") ;
			      }
		}]
    });
}
/**
 * 判断对象是否为空或者null
 */
function isBlankObj(obj) {
    for(var name in obj) {
        if(obj.hasOwnProperty(name)){
            return false;
        }
    }
    return true;
}
function enterEventHandler(event, callback){
	var event = event || window.event || arguments.callee.caller.arguments[0] ;
	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;  
    if (keyCode == 13) {
    	callback.call();
    }   
}
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点
/**
 * 函数错误处理
 * @return true不在浏览器中显示错误,false在浏览器状中显示此错误
 */
window.onerror = function(msg){
	//由于chrome中返回的msg不只自定义的msg还包含系统附加的前辍，所以采用indexOf来判断
	if(msg.indexOf(ONLY_LEAF_CAN_SELECTED) > -1){
		return true ;
	}
	return false ;
}
function checkOnlyLeafSelect(node){
	var tree = $(this);
	if(tree.tree("isLeaf",node.target)){
		return true ;
	}else{
		if(node.state == "closed"){
			tree.tree("expand",node.target);
		}else{
			tree.tree("collapse",node.target);
		}
		throw new Error(ONLY_LEAF_CAN_SELECTED) ;//抛出错误，阻止下拉tree被关闭
	}
}
function newURL(url,param){
	var q = url.indexOf("?") > 0 ? "" : "?" ;
	return url + q + $.param(param) + "&_time=" + new Date().getTime() ;
}

function isValidIp(ip){
	var blocks = ip.split(".");
	if(blocks.length === 4) {
		for(var i=0;i<4;i++){
			var block = blocks[i] ;
			if(!isNaN(block) && parseInt(block) >=0 && parseInt(block) <= 255){
				continue ;
			}else{
				return false ;
			}
		}
		return true ;
	}
	return false;
}
function isInvalidIp(ip){
	return !isValidIp(ip) ;
}
function html2Escape(sHtml) {
	 return sHtml.replace(/[<>&"']/g,function(c){return {'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;','\'':'&'}[c];});
}
function isBlank(str){
	return str == null || str == undefined || Trim(str).length == 0 ;
}
function isNotBlank(str){
	return !isBlank(str) ;
}
/**
 * 打开一个模态窗口
 * @param title 窗口标题
 * @param width 宽度
 * @param height 高度
 * @param href 地址
 * @param otherConfig 其它配置,如果其它配置中也配置了title,width,height,href参数，将会覆盖传递的参数
 * @returns
 */
function openDialog(title,width,height,href,otherConfig){
	var config = {title:title,modal:true,width:width,height:height,href:href,onClose : function() {
		$(this).dialog('destroy');
	}} ;
	config = $.extend(config,otherConfig) ;
	var dialog = $('<div/>').dialog(config) ;
	return dialog ;
}
function openNewWindow(menu,url,simHandler){
	var param = {menu:menu,url:url,simHandler:simHandler} ;
	var paramString = cryptico.b256to64(encodeURIComponent(JSON.stringify(param))) ;
	window.open("/sim/index/?forwardParams="+paramString,"_blank") ;
}
function openLogQueryWindow(handlerParam){
	openNewWindow("menu_log_query", "/page/log/logQuery2Main.html", handlerParam) ;
}

function isArray(obj) { 
	return Object.prototype.toString.call(obj) === '[object Array]'; 
}

/**
 * 排序数组数据,如果数组为null或undefined返回空数组
 * 1、如果config指定为数组，表示使用多个字段来排序
 * 2、如果config为函数表示使用指定的比较函数排序
 * 3、如果config为字符串，表示使用数据指定字段进行排序
 * 4、如果config未指定，表示按照自然排序方式排序
 * @param arrayData
 * @param config
 * @param order asc 升序,desc降序
 */
function sort(arrayData,config,order){
	if(arrayData == null || arrayData == undefined){
		return new Array() ;
	}
	if(config == null || config == undefined){
		arrayData.sort() ;
	}else{
		var type = typeof config ;
		if(type == "function"){
			arrayData.sort(config) ;
		}else if(type == "string"){
			arrayData.sort(function(obj1,obj2){
				return compareFields(obj1,obj2,config) ;
			}) ;
		}else if(isArray(config)){
			arrayData.sort(function(obj1,obj2){
				return compareFields(obj1, obj2, config) ;
			}) ;
		}
	}
	if(order && order.toLowerCase() == "desc"){
		arrayData.reverse() ;
	}
	return arrayData ;
}

function compareFields(o1,o2,fields){
	if(fields == null){
		return compare(o1, o2);
	}
	if(o1 == null){
		return -1 ;
	}
	if(o2 == null){
		return 1 ;
	}
	var fieldArray = typeof fields == "string" ? new Array(fields) : fields ; 
	for(var i in fieldArray){
		var result = compare(o1[fieldArray[i]], o2[fieldArray[i]]) ;
		if(result != 0){
			return result ;
		}
	}
	return 0 ;
}

function compare(o1,o2){
	if(o1 == null || o1 == undefined){
		return -1 ;
	}
	if(o2 == null || o2 == undefined){
		return 1 ;
	}
	return o1 == o2 ? 0 : o1 > o2 ? 1 : -1 ;
}

if($.extend && $.fn.layout){
	//在layout的panle全局配置中,增加一个onCollapse处理title
	$.extend($.fn.layout.paneldefaults, {
		onCollapse : function () {
			//获取layout容器
			var layout = $(this).parents("div.layout");
			//获取当前region的配置属性
			var opts = $(this).panel("options");
			//获取key
			var expandKey = "expand" + opts.region.substring(0, 1).toUpperCase() + opts.region.substring(1);
			//从layout的缓存对象中取得对应的收缩对象
			var expandPanel = layout.data("layout").panels[expandKey];
			//针对横向和竖向的不同处理方式
			if (opts.region == "west" || opts.region == "east") {
				//竖向的文字打竖,其实就是切割文字加br
				var split = [];
				for (var i = 0; i < opts.title.length; i++) {
					split.push(opts.title.substring(i, i + 1));
				}
				expandPanel.panel("body").addClass("panel-title").css("text-align", "center").html(split.join("<br>"));
			} else {
				expandPanel.panel("setTitle", opts.title);
			}
		}
	});
}