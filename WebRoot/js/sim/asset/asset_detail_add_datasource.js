/**
 * dsa
 * 日志源新建
 * 由于日志源类型不同，需要动态创建输入组件
 * @returns
 */
var dsa = {
	seq:0
}
var ADDABLE = "addable" ;
var COLLECTION = "collection" ;
var PROPERTY = "property" ;
var JOIN_STR = "_" ;
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点
dsa.dataSourceTypeClick = function(record){
	if(record.attributes&&record.attributes.dataSources){
		var value = $.map(record.attributes.dataSources,function(item){
			item.name = item.collectType ;
			item.value = item.collectType ;
			return item ;
		}) ;
		if(value.length<1){
			showAlertMessage("无法解析此类型日志，找不到对应的日志解析文件！") ;
			return ;
		}
		var selectCollectType = $("#dsa_collectType").attr("selectValue") ; 
		var collectType0 =  selectCollectType == "" ? value[0].value : selectCollectType;//第一种收集方式
		form.fillSelect("#dsa_collectType",value,true,collectType0).trigger("change");
		/* 过滤器、归并规则方法 */
		var deviceType = record.id;
		if(deviceType){
			/* 过滤器 */
			var currentTime = new Date().getTime() ;
			var filterUrl = "/sim/LogFilterRule/getByDeviceObjectType?deviceType="+deviceType+"&_time=" + currentTime;
			$.getJSON(filterUrl,function(simRulesJson){
				if(simRulesJson){
					simRulesJson.push({name:"请选择",value:""}) ;
					var selectRule = $("#dsa_dataSource_filter").attr("selectValue") ;
					form.fillSelect("#dsa_dataSource_filter",simRulesJson,true,selectRule);
				}
			});
			/* 归并规则 */
			var aggregatorUrl = "/sim/AggregatorRule/getByDeviceObjectType?deviceType="+deviceType+"&_time=" + currentTime;
			$.getJSON(aggregatorUrl,function(aggregatorScenesJson){
				if(aggregatorScenesJson){
					aggregatorScenesJson.push({name:"请选择",value:""}) ;
					var selectAggregator = $("#dsa_dataSource_aggregator").attr("selectValue") ;
					form.fillSelect("#dsa_dataSource_aggregator",aggregatorScenesJson,true,selectAggregator);
				}
			});
		}
		if($("#dsa_operation").val() == "edit"){
			setTimeout(function(){
				var cbt = $("#dsa_dataSourceType") ;
				cbt.combotree("setText",cbt.combotree("tree").tree("getSelected").pathName) ;
			}, 300) ;
		}
	}
}

/**
 * 根据时间生成一个id,前辍为dsa_item
 */
dsa.generateId = function(){
	var itemId = "dsa_item" + dsa.seq++ ;
	return itemId ;
}
/**
 * 收集方式变化时重新加载配置参数
 */
dsa.collectTypeChangeHandler = function(){
	dsa.getCollectComponent($("#dsa_scanNodeId").val(),$("#dsa_collectType").val()) ;
}
dsa.getCollectComponent = function(collectNode,collectType){
	var url = '/sim/datasource/getCollectComponent?collectNode='+collectNode+'&collectType='+collectType+'&_time=' + new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.componentId){
			$("#dsa_componentId").val(result.componentId) ;
			$("#dsa_auditorNodeId").val(result.auditorNodeId) ;
			$("#dsa_nodeId").val(result.nodeId) ;
			var collectTypeSelect = $("#dsa_collectType") ;
			var selectOption = collectTypeSelect.children(":selected") ;
			var isJob = selectOption.attr("isJob") == "true"  ;
			if(isJob){
				$("#durationDiv").css("display","none") ;
				$("#speedLimitDiv").css("display","none") ;
				$("#pollTimeDiv").css("display","block") ;
				$("#poll_time_type").trigger("change") ;
			}else{
				$("#pollTimeDiv").css("display","none") ;
				$("#durationDiv").css("display","block") ;
				$("#speedLimitDiv").css("display","block") ;
			}
			$("#dsa_isJob").val(isJob) ;
			$("#dsa_dataObjectType").val(selectOption.attr("dataObjectType")) ;
			dsa.getConfigParam(collectTypeSelect.val()) ;
		}else{
			showErrorMessage("没有找到合适的收集组件！") ;
		}
	}) ;
}
dsa.beforeDataSourceSelect = function(node){
	var tree = $("#dsa_dataSourceType").combotree("tree") ;
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
 * 获取日志源配置参数
 * @param securityObjectType 分类
 * @param collectType 收集方式
 */
dsa.getConfigParam = function(collectType){
	var securityObjectType = $("#dsa_dataSourceType").combotree("getValue") ;
	var operation = $("#dsa_operation") ;
	var url ;
	//在编辑时，第一次页面加载时的初始化会使用日志源的id加载相关的日志源配置信息
	if(operation.val()=="edit" && operation.attr("initComplete") == "false"){
		operation.attr("initComplete","true") ;
		var dataSourceId = $("#dsa_dataSourceId").val() ;
		url = "/sim/datasource/getConfigParam?dataSourceId="+dataSourceId + "&_time=" + new Date().getTime() ;
	}else{
		var ip = $("#dsa_ip").val() ;
		url = "/sim/datasource/getConfigParam?securityObjectType="+securityObjectType+
		      "&collectType="+collectType+ "&ip=" + ip +
		      "&_time=" + new Date().getTime() ;
	}
	$.getJSON(url,function(result){
		var properties = result.properties;
		//dsa.displayControl = result.displayControl ;
		$("#dsa_configParamDiv :input").each(function(index,el){
			dsa.dsa_form_validator.setField(el.name,null) ;
		}) ;
		$("#dsa_configParamDiv").children().remove() ;
		$.each(properties,function(index,configParam){
			if(configParam.type == PROPERTY){//普通配置参数
				configParam.name = PROPERTY + JOIN_STR + configParam.name ;
				dsa.createPropertyItem(configParam) ;
			}else if(configParam.type == COLLECTION){//集合配置参数
				dsa.createCollectionItem(configParam) ;
			}else if(configParam.type == ADDABLE){//可扩展的配置的参数
				dsa.createAddableItem(configParam) ;
			}
		}) ;
	});
}
/**
 * 创建动态输入项
 * @param configParam　配置参数
 */
dsa.createPropertyItem = function(configParam,parent){
	if(!parent){
		parent = $("#dsa_configParamDiv") ;
	}
	var bizType = configParam.bizType ? configParam.bizType : "String";
	var dataRule = ds_mt.getDataRule(configParam) ;
	if(dataRule){
		dsa.dsa_form_validator.setField(configParam.name,dataRule) ;
	}
	if(bizType=="String"||bizType=="NonNegativeInteger"||bizType=="IP"){
		form.createFormItem("text",parent,configParam.alias+"：",dsa.generateId(),configParam.name,null,configParam.value,null) ;
	}else if(bizType=="Password"){
		form.createFormItem("password",parent,configParam.alias+"：",dsa.generateId(),configParam.name,null,rsaDecrypt(configParam.value),null) ;
	}else if(bizType=="Select"||bizType=="Boolean"){
		form.createFormItem("select",parent,configParam.alias+"：",dsa.generateId(),configParam.name,configParam.options,configParam.value,null) ;
	}
}

/**
 * 根据集合参数创建集合输入项
 * @param configParam
 */
dsa.createCollectionItem = function(configParam){
	$.each(configParam.properties,function(index,pt){
		if(pt.bizType == COLLECTION){
			pt.name = configParam.name + JOIN_STR + pt.name  ; 
			dsa.createCollectionItem(pt) ;
		}else{
			pt.name = COLLECTION + JOIN_STR + configParam.name + JOIN_STR + pt.name ;
			dsa.createPropertyItem(pt) ;
		}
	}) ;
}
/**
 * 创建addable选项
 * @param configParam
 */
dsa.createAddableItem = function (configParam){
	var addableSelectData = $.map(configParam.schemas,function(item){
		var valueStr = JSON.stringify(item).replace(/"/g,"'") ;
		var item = {name:item.alias,value:valueStr} ;
		return item ;
	}) ;
	var addableItemId = dsa.generateId() ;
	var btn = $("<button/>").attr("type","button").html("新建")
							.addClass("btn")
							.bind("click",function(){dsa.addAddableItems(addableItemId)});
	var addableSelector = form.createSelect(addableItemId, "addableSelector", addableSelectData).attr("addableName",configParam.name);
	var appendDiv = $("<div/>").addClass("input-append")
	                           .append(addableSelector)
	                           .append(btn);
	form.createFormItem(appendDiv,"dsa_configParamDiv","配置选项：");
	var addableProperties = configParam.properties ;
	if(addableProperties && addableProperties.length > 0){
		for(var i=0;i<addableProperties.length;i++){
			var properties = $.map(addableProperties[i].properties,function(pt){
				pt.name = ADDABLE + JOIN_STR + configParam.name + JOIN_STR + addableProperties[i].name + JOIN_STR + pt.name ;
				return pt ;
			}) ;
			dsa.createGroupItems(properties) ;
		}
	}
}
/**
 * template中addable项目点击事件
 */
dsa.addAddableItems = function(addableItemId){
	var addableSelect = $("#"+addableItemId) ;
	var collection = $.parseJSON(addableSelect.val().replace(/'/g,'"')) ;
	var properties = $.map(collection.properties,function(pt){
		pt.name = ADDABLE + JOIN_STR + addableSelect.attr("addableName") + JOIN_STR + collection.name + JOIN_STR + pt.name ;
		return pt ;
	}) ;
	dsa.createGroupItems(properties) ;
}
/**
 * 创建addable中的一组项目
 * @param itemArray
 */
dsa.createGroupItems = function (itemArray){
	var div = $("<div/>").addClass("alert") ;
	var closeButton = $("<button/>").addClass("close").attr("type","button").attr("data-dismiss","alert").html("&times;")
	.bind("click",function(){
		$(this).parent().find(":input").each(function(){
			var input = $(this) ;
			var inputName = input.attr("name") ;
			if(inputName){
				var sameNameInput = $("#dsa_configParamDiv [name='"+inputName+"']") ;
				if(sameNameInput.size() < 2){
					dsa.dsa_form_validator.setField(inputName,null) ;
				}
			}
		}) ;
			
	});
	div.append(closeButton) ;
	$.each(itemArray,function(index,configParam){
		dsa.createPropertyItem(configParam,div) ;
	}) ;
	$("#dsa_configParamDiv").append(div) ;
}
$(function(){
	//初始化表单验证组件，并创建表单验证实例
	dsa.dsa_form_validator = $('#dsa_form').validator({
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
	$("#dsa_form").submit(function () {
		var validResult = pollTime.validatePollTime() ;
		if(validResult != null){
			dsa.dsa_form_validator.showMsg("#pollTimeError",validResult) ;
			return ;
		}else{
			$("#pollTimeError").html("") ;
		}
		var isValid = dsa.dsa_form_validator.isFormValid();
		if(isValid){
			var formId = "#dsa_form" ;
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
			$("#"+ds.currentOpenDialog+" > .dialog-button > .l-btn").first().linkbutton("disable");
			$.post("/sim/datasource/save",data,function(result){
				$("#"+ds.currentOpenDialog+" > .dialog-button > .l-btn").first().linkbutton("enable");
				if(result.status){
					 $("#"+ds.currentOpenDialog).dialog("close");
					 getTabElement("adt_datasource").panel("refresh") ;
				}else if(result.result == "timer"){
					showErrorMessage(result.message) ;
				}else{
					dsa.dsa_form_validator.showMsg($("#dsa_name"), {type: "error",msg: result.message});
					$("#"+ds.currentOpenDialog).panel("body").children(".panel").children(".panel-body").scrollTop(0)
					//showErrorMessage(result.message) ;
				}
			},"json") ;
		}
	});
});