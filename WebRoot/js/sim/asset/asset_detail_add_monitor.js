/**
 * ma
 * 监视对象新建
 * 由于监视对象类型不同，需要动态创建输入组件
 * @returns
 */
var ma = {
	seq:0
}
var ADDABLE = "addable" ;
var COLLECTION = "collection" ;
var PROPERTY = "property" ;
var JOIN_STR = "_" ;
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点

ma.monitorTypeClick = function(record){
	if(record.attributes&&record.attributes.dataSources){
		var value = $.map(record.attributes.dataSources,function(item){
			item.name = item.collectType ;
			item.value = item.collectType ;
			return item ;
		}) ;
		if(value.length<1){
			showAlertMessage("无法解析此类型日志，找不到对应的监视对象解析文件！") ;
			return ;
		}
		var selectCollectType = $("#ma_collectType").attr("selectValue") ; 
		var collectType0 =  selectCollectType == "" ? value[0].value : selectCollectType;//第一种收集方式
		form.fillSelect("#ma_collectType",value,true,collectType0).trigger("change");
	}
}

/**
 * 根据时间生成一个id,前辍为ma_item
 */
ma.generateId = function(){
	var itemId = "ma_item" + ma.seq++ ;
	return itemId ;
}
/**
 * 收集方式变化时重新加载配置参数
 */
ma.collectTypeChangeHandler = function(){
	ma.getCollectComponent($("#dsa_scanNodeId").val(),$("#ma_collectType").val()) ;
}
ma.getCollectComponent = function(collectNode,collectType){
	var url = '/sim/datasource/getCollectComponent?collectNode='+collectNode+'&collectType='+collectType+'&_time=' + new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.componentId){
			$("#ma_componentId").val(result.componentId) ;
			$("#ma_auditorNodeId").val(result.auditorNodeId) ;
			$("#ma_nodeId").val(result.nodeId) ;
			var collectTypeSelect = $("#ma_collectType") ;
			var selectOption = collectTypeSelect.children(":selected") ;
			var isJob = selectOption.attr("isJob") == "true"  ;
			if(isJob){
				$("#ma_pollTimeDiv").css("display","block") ;
				$("#poll_time_type").trigger("change") ;
			}else{
				$("#ma_pollTimeDiv").css("display","none") ;
			}
			$("#ma_isJob").val(isJob) ;
			$("#ma_dataObjectType").val(selectOption.attr("dataObjectType")) ;
			ma.getConfigParam(collectTypeSelect.val()) ;
		}else{
			showErrorMessage("没有找到合适的收集组件！") ;
		}
	}) ;
}
/**
 * 选择轮询时间
 */
ma.timerTypeChangeHandler = function(timerTypeList){
	var timerTypeSelect = $(timerTypeList) ;
	var showElements = timerTypeSelect.children(":selected").attr("showElements");
	var elementIds = showElements.split(",") ;
	var allInput = timerTypeSelect.siblings().each(function(){
		var elementId = $(this).attr("id") ;
		var findIndex = $.inArray(elementId,elementIds) ;
		if( findIndex > -1){//如果是要显示的元素
			$(this).css("display","block") ;
		}else{
			$(this).css("display","none") ;
		}
	}) ;
	
} 

ma.beforemonitorSelect = function(node){
	var tree = $("#ma_monitorType").combotree("tree") ;
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
/**
 * 获取监视对象配置参数
 * @param securityObjectType 分类
 * @param collectType 收集方式
 */
ma.getConfigParam = function(collectType){
	var securityObjectType = $("#ma_monitorType").combotree("getValue") ;
	var operation = $("#ma_operation") ;
	var url ;
	//在编辑时，第一次页面加载时的初始化会使用日志源的id加载相关的日志源配置信息
	if(operation.val()=="edit" && operation.attr("initComplete") == "false"){
		operation.attr("initComplete","true") ;
		var monitorId = $("#ma_monitorId").val() ;
		url = "/sim/monitor/getConfigParam?monitorId="+monitorId + "&_time=" + new Date().getTime();
	}else{
		var ip = $("#ma_ip").val() ;
		url = "/sim/monitor/getConfigParam?securityObjectType="+securityObjectType+
		      "&collectType="+collectType+
		      "&ip="+ip+
		      "&_time=" + new Date().getTime() ;
	}
	$.getJSON(url,function(result){
		var properties = result.properties;
		//ma.displayControl = result.displayControl ;
		$("#ma_configParamDiv :input").each(function(index,el){
			ma.ma_form_validator.setField(el.name,null) ;
		}) ;
		$("#ma_configParamDiv").children().remove() ;
		$.each(properties,function(index,configParam){
			if(configParam.type == PROPERTY){
				configParam.name = PROPERTY + JOIN_STR + configParam.name ;
				ma.createPropertyItem(configParam) ;
			}else if(configParam.type == COLLECTION){
				ma.createCollectionItem(configParam) ;
			}else if(configParam.type == ADDABLE){
				ma.createAddableItem(configParam) ;
			}
		}) ;
	});
}
/**
 * 创建动态输入项
 * @param configParam　配置参数
 */
ma.createPropertyItem = function(configParam,parent){
	if(!parent){
		parent = $("#ma_configParamDiv") ;
	}
	var bizType = configParam.bizType ? configParam.bizType : "String";
	var inputType = undefined ;
	var dataRule = ds_mt.getDataRule(configParam) ;
	if(dataRule){
		ma.ma_form_validator.setField(configParam.name,dataRule) ;
	}
	if(bizType=="String"||bizType=="NonNegativeInteger"||bizType=="IP"){
		form.createFormItem("text",parent,configParam.alias+"：",ma.generateId(),configParam.name,null,configParam.value,configParam.realName) ;
	}else if(bizType=="Password"){
		form.createFormItem("password",parent,configParam.alias+"：",ma.generateId(),configParam.name,null,rsaDecrypt(configParam.value),configParam.realName) ;
	}else if(bizType=="Select"||bizType=="Boolean"){
		form.createFormItem("select",parent,configParam.alias+"：",ma.generateId(),configParam.name,configParam.options,configParam.value,configParam.realName) ;
	}
} 
/**
 * 根据集合参数创建集合输入项
 * @param configParam
 */
ma.createCollectionItem = function(configParam){
	$.each(configParam.properties,function(index,pt){
		if(pt.bizType == COLLECTION){
			pt.name = configParam.name + JOIN_STR + pt.name  ; 
			ma.createCollectionItem(pt) ;
		}else{
			pt.name = COLLECTION + JOIN_STR + configParam.name + JOIN_STR + pt.name ;
			ma.createPropertyItem(pt) ;
		}
	}) ;
}
/**
 * 创建addable选项
 * @param configParam
 */
ma.createAddableItem = function (configParam){
	var addableSelectData = $.map(configParam.schemas,function(item){
		var valueStr = JSON.stringify(item).replace(/"/g,"'") ;
		var item = {name:item.alias,value:valueStr} ;
		return item ;
	}) ;
	var addableItemId = ma.generateId() ;
	var btn = $("<button/>").attr("type","button").html("新建")
							.addClass("btn")
							.bind("click",function(){ma.addAddableItems(addableItemId)});
	var addableSelector = form.createSelect(addableItemId, "addableSelector", addableSelectData).attr("addableName",configParam.name) ;
	var appendDiv = $("<div/>").addClass("input-append")
	                           .append(addableSelector)
	                           .append(btn);
	form.createFormItem(appendDiv,"ma_configParamDiv","配置选项：");
	var addableProperties = configParam.properties ;
	if(addableProperties && addableProperties.length > 0){
		for(var i=0;i<addableProperties.length;i++){
			var properties = $.map(addableProperties[i].properties,function(pt){
				pt.name = ADDABLE + JOIN_STR + configParam.name + JOIN_STR + addableProperties[i].name + JOIN_STR + pt.name ;
				return pt ;
			}) ;
			ma.createGroupItems(properties) ;
		}
	}
}
/**
 * template中addable项目点击事件
 */
ma.addAddableItems = function(addableItemId){
	var addableSelect = $("#"+addableItemId) ;
	var collection = $.parseJSON(addableSelect.val().replace(/'/g,'"')) ;
	var properties = $.map(collection.properties,function(pt){
		pt.name = ADDABLE + JOIN_STR + addableSelect.attr("addableName") + JOIN_STR + collection.name + JOIN_STR + pt.name ;
		return pt ;
	}) ;
	ma.createGroupItems(properties) ;
}
/**
 * 创建addable中的一组项目
 * @param itemArray
 */
ma.createGroupItems = function (itemArray){
	var div = $("<div/>").addClass("alert") ;
	var closeButton = $("<button/>").addClass("close").attr("type","button").attr("data-dismiss","alert").html("&times;")
	.bind("click",function(){
		$(this).parent().find(":input").each(function(){
			var input = $(this) ;
			var inputName = input.attr("name") ;
			if(inputName){
				var sameNameInput = $("#ma_configParamDiv [name='"+inputName+"']") ;
				if(sameNameInput.size() < 2){
					ma.ma_form_validator.setField(inputName,null) ;
				}
			}
		}) ;
			
	});
	
	div.append(closeButton) ;
	$.each(itemArray,function(index,configParam){
		ma.createPropertyItem(configParam,div) ;
	}) ;
	$("#ma_configParamDiv").append(div) ;
}
/**
 * 根据收集方式，获取所有可用的收集节点
 */
ma.getAvailableNodes = function(collectType){
	var url = '/sim/monitor/getAvailableNodes?collectType='+collectType +"&_time=" + new Date().getTime();
	$.getJSON(url,function(result){
		var nodes = $.map(result,function(item){
			item.name = item.nodeName != "" ? item.nodeName : item.ip ;
			item.value = item.nodeId ;
			return item ;
		}) ;
		var selectNode = $("#ma_nodes").attr("selectValue") ;
		var selectValue = selectNode == "" ? nodes[0] : selectNode ;
		form.fillSelect("#ma_nodes",nodes,true,selectValue) ;
		$("#ma_nodes").trigger("change") ;
	}) ;
}
$(function(){
	//初始化表单验证组件，并创建表单验证实例
	ma.ma_form_validator = $('#ma_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules:{
			specialcharAndPoint:simHandler.rules.specialcharAndPoint
		},
		fields:{
			name:'required;length[1~30];specialcharAndPoint',
			collectType:'required;'
		}
	}).data("validator");
	$("#ma_form").submit(function () {
		var validResult = pollTime.validatePollTime() ;
		if(validResult != null){
			ma.ma_form_validator.showMsg("#pollTimeError",validResult) ;
			return ;
		}else{
			$("#pollTimeError").html("") ;
		}
		var isValid = ma.ma_form_validator.isFormValid();
		if(isValid){
			var formId = "#ma_form" ;
			var formData = $(formId).serializeArray();
			$(formId + " :password").each(function(index,element){
				$.each(formData,function(index,inputItem){
					if(inputItem.name == element.name){
						inputItem.value = rsaEncrypt(inputItem.value) ;
						return false ;
					}
				}) ;
			}) ;
			var data = $.param(formData) ;
			$.post("/sim/monitor/save",data,function(result){
				if(result.status){
					 $("#"+mnt.currentOpenDialog).dialog("close");
					 getTabElement("adt_monitor").panel("refresh") ;
				}else{
					showErrorMessage(result.message) ;
				}
			},"json") ;
		}
	});
});

