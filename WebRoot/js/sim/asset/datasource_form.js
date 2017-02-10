/**
 * dsf
 * 日志源新建
 * 由于日志源类型不同，需要动态创建输入组件
 * @returns
 */
var dsf={seq:0};
var ADDABLE = "addable" ;
var COLLECTION = "collection" ;
var PROPERTY = "property" ;
var JOIN_STR = "_" ;
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点

dsf.dataSourceTypeClick = function(record){
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
		var selectCollectType = $("#dsf_collectType").attr("selectValue") ; 
		var collectType0 =  selectCollectType == "" ? value[0].value : selectCollectType;//第一种收集方式
		form.fillSelect("#dsf_collectType",value,true,collectType0).trigger("change");
		/* 过滤器、归并规则方法 */
		var deviceType = record.id;
		if(deviceType){
			/* 过滤器 */
			var currentTime = new Date().getTime() ;
			var filterUrl = "/sim/LogFilterRule/getByDeviceObjectType?deviceType="+deviceType+"&_time=" + currentTime;
			$.getJSON(filterUrl,function(simRulesJson){
				if(simRulesJson){
					simRulesJson.push({name:"请选择",value:""}) ;
					var selectRule = $("#dsf_dataSource_filter").attr("selectValue") ;
					form.fillSelect("#dsf_dataSource_filter",simRulesJson,true,selectRule);
				}
			});
			/* 归并规则 */
			var aggregatorUrl = "/sim/AggregatorRule/getByDeviceObjectType?deviceType="+deviceType+"&_time=" + currentTime;
			$.getJSON(aggregatorUrl,function(aggregatorScenesJson){
				if(aggregatorScenesJson){
					aggregatorScenesJson.push({name:"请选择",value:""}) ;
					var selectAggregator = $("#dsf_dataSource_aggregator").attr("selectValue") ;
					form.fillSelect("#dsf_dataSource_aggregator",aggregatorScenesJson,true,selectAggregator);
				}
			});
		}
		if($("#dsf_operation").val() == "edit"){
			setTimeout(function(){
				var cbt = $("#dsf_dataSourceType") ;
				cbt.combotree("setText",cbt.combotree("tree").tree("getSelected").pathName) ;
			}, 300) ;
		}
	}
}

/**
 * 根据时间生成一个id,前辍为dsf_item
 */
dsf.generateId = function(){
	var itemId = "dsf_item" + dsf.seq++ ;
	return itemId ;
}

/**
 * 收集方式变化时重新加载配置参数
 */
dsf.collectTypeChangeHandler = function(){
	dsf.getCollectComponent($("#dsf_scanNodeId").val(),$("#dsf_collectType").val()) ;
}

dsf.getCollectComponent = function(collectNode,collectType){
	var url = '/sim/datasource/getCollectComponent?collectNode='+collectNode+'&collectType='+collectType+'&_time=' + new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.componentId){
			$("#dsf_componentId").val(result.componentId) ;
			$("#dsf_auditorNodeId").val(result.auditorNodeId) ;
			$("#dsf_nodeId").val(result.nodeId) ;
			var collectTypeSelect = $("#dsf_collectType") ;
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
			$("#dsf_isJob").val(isJob) ;
			$("#dsf_dataObjectType").val(selectOption.attr("dataObjectType")) ;
			dsf.getConfigParam(collectTypeSelect.val()) ;
		}else{
			showErrorMessage("没有找到合适的收集组件！") ;
		}
	}) ;
}

dsf.beforeDataSourceSelect = function(node){
	var tree = $("#dsf_dataSourceType").combotree("tree") ;
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
dsf.getConfigParam = function(collectType){
	var securityObjectType = $("#dsf_dataSourceType").combotree("getValue") ;
	var operation = $("#dsf_operation") ;
	var url ;
	//在编辑时，第一次页面加载时的初始化会使用日志源的id加载相关的日志源配置信息
	if(operation.val()=="edit" && operation.attr("initComplete") == "false"){
		operation.attr("initComplete","true") ;
		var dataSourceId = $("#dsf_dataSourceId").val() ;
		url = "/sim/datasource/getConfigParam?dataSourceId="+dataSourceId + "&_time=" + new Date().getTime() ;
	}else{
		var ip = $("#dsf_ip").combo("getValue");
		url = "/sim/datasource/getConfigParam?securityObjectType="+securityObjectType+
		      "&collectType="+collectType+ "&ip=" + ip +
		      "&_time=" + new Date().getTime() ;
	}
	$.getJSON(url,function(result){
		var properties = result.properties;
		//dsf.displayControl = result.displayControl ;
		$("#dsf_configParamDiv :input").each(function(index,el){
			dsf.dsf_form_validator.setField(el.name,null) ;
		}) ;
		$("#dsf_configParamDiv").children().remove() ;
		$.each(properties,function(index,configParam){
			if(configParam.type == PROPERTY){//普通配置参数
				configParam.name = PROPERTY + JOIN_STR + configParam.name ;
				dsf.createPropertyItem(configParam) ;
			}else if(configParam.type == COLLECTION){//集合配置参数
				dsf.createCollectionItem(configParam) ;
			}else if(configParam.type == ADDABLE){//可扩展的配置的参数
				dsf.createAddableItem(configParam) ;
			}
		}) ;
	});
}

/**
 * 创建动态输入项
 * @param configParam　配置参数
 */
dsf.createPropertyItem = function(configParam,parent){
	if(!parent){
		parent = $("#dsf_configParamDiv") ;
	}
	var bizType = configParam.bizType ? configParam.bizType : "String";
	var dataRule = ds_mt.getDataRule(configParam) ;
	if(dataRule){
		dsf.dsf_form_validator.setField(configParam.name,dataRule) ;
	}
	if(bizType=="String"||bizType=="NonNegativeInteger"||bizType=="IP"){
		form.createFormItem("text",parent,configParam.alias+"：",dsf.generateId(),configParam.name,null,configParam.value,null) ;
	}else if(bizType=="Password"){
		form.createFormItem("password",parent,configParam.alias+"：",dsf.generateId(),configParam.name,null,rsaDecrypt(configParam.value),null) ;
	}else if(bizType=="Select"||bizType=="Boolean"){
		form.createFormItem("select",parent,configParam.alias+"：",dsf.generateId(),configParam.name,configParam.options,configParam.value,null) ;
	}
}

/**
 * 根据集合参数创建集合输入项
 * @param configParam
 */
dsf.createCollectionItem = function(configParam){
	$.each(configParam.properties,function(index,pt){
		if(pt.bizType == COLLECTION){
			pt.name = configParam.name + JOIN_STR + pt.name  ; 
			dsf.createCollectionItem(pt) ;
		}else{
			pt.name = COLLECTION + JOIN_STR + configParam.name + JOIN_STR + pt.name ;
			dsf.createPropertyItem(pt) ;
		}
	}) ;
}

/**
 * 创建addable选项
 * @param configParam
 */
dsf.createAddableItem = function (configParam){
	var addableSelectData = $.map(configParam.schemas,function(item){
		var valueStr = JSON.stringify(item).replace(/"/g,"'") ;
		var item = {name:item.alias,value:valueStr} ;
		return item ;
	}) ;
	var addableItemId = dsf.generateId() ;
	var btn = $("<button/>").attr("type","button").html("新建")
							.addClass("btn")
							.bind("click",function(){dsf.addAddableItems(addableItemId)});
	var addableSelector = form.createSelect(addableItemId, "addableSelector", addableSelectData).attr("addableName",configParam.name);
	var appendDiv = $("<div/>").addClass("input-append")
	                           .append(addableSelector)
	                           .append(btn);
	form.createFormItem(appendDiv,"dsf_configParamDiv","配置选项：");
	var addableProperties = configParam.properties ;
	if(addableProperties && addableProperties.length > 0){
		for(var i=0;i<addableProperties.length;i++){
			var properties = $.map(addableProperties[i].properties,function(pt){
				pt.name = ADDABLE + JOIN_STR + configParam.name + JOIN_STR + addableProperties[i].name + JOIN_STR + pt.name ;
				return pt ;
			}) ;
			dsf.createGroupItems(properties) ;
		}
	}
}

/**
 * template中addable项目点击事件
 */
dsf.addAddableItems = function(addableItemId){
	var addableSelect = $("#"+addableItemId) ;
	var collection = $.parseJSON(addableSelect.val().replace(/'/g,'"')) ;
	var properties = $.map(collection.properties,function(pt){
		pt.name = ADDABLE + JOIN_STR + addableSelect.attr("addableName") + JOIN_STR + collection.name + JOIN_STR + pt.name ;
		return pt ;
	}) ;
	dsf.createGroupItems(properties) ;
}

/**
 * 创建addable中的一组项目
 * @param itemArray
 */
dsf.createGroupItems = function (itemArray){
	var div = $("<div/>").addClass("alert") ;
	var closeButton = $("<button/>").addClass("close").attr("type","button").attr("data-dismiss","alert").html("&times;")
	.bind("click",function(){
		$(this).parent().find(":input").each(function(){
			var input = $(this) ;
			var inputName = input.attr("name") ;
			if(inputName){
				var sameNameInput = $("#dsf_configParamDiv [name='"+inputName+"']") ;
				if(sameNameInput.size() < 2){
					dsf.dsf_form_validator.setField(inputName,null) ;
				}
			}
		}) ;
			
	});
	div.append(closeButton) ;
	$.each(itemArray,function(index,configParam){
		dsf.createPropertyItem(configParam,div) ;
	}) ;
	$("#dsf_configParamDiv").append(div) ;
}

/**
 * 帮助
 */
dsf.deviceTypeHelp = function(){
	var dataSourceType = $("#dsf_dataSourceType").combo("getValue");
	var name = "";
	dataSourceType = dataSourceType ? dataSourceType : "OS/Microsoft";
	
	var dataTypes = dataSourceType.split("/");
	if(dataTypes && dataTypes.length >= 2) {
		name =  ("&name=" + dataTypes[0] + "/" + dataTypes[1]);
	}
	window.open(encodeURI("/sim/productsupport/productsupportHelp?asset=asset" + name), '_blank');
}

dsf.displayPathName = function(){
	var cbt = $(this) ; 
	var selectNode = cbt.combotree("tree").tree("getSelected") ;
	cbt.combotree("setText",selectNode.pathName) ;
}

dsf.ipClick = function(){
	$("#dsf_dataSourceType").combo("reset");
	$("#dsf_collectType").empty();
	var ip = $("#dsf_ip").combo("getValue");
	$.getJSON("/sim/datasource/getDeviceType?ip=" + ip, function(data){
		if(data){
			$("#dsf_dataSourceType").combotree('reload', '/sim/datasource/dataSourceTree?deviceType='+data.deviceType);
			$("#dsf_scanNodeId").val(data.scanNodeId);
		}
	});
}

$(function(){
	
	//初始化表单验证组件，并创建表单验证实例
	dsf.dsf_form_validator = $('#dsf_form').validator({
		theme: 'simple_right',
		showOk: "",
		rules:{
			specialcharAndPoint:simHandler.rules.specialcharAndPoint
		},
		fields:{
			name:'required;length[1~30];specialcharAndPoint;remote[/sim/datasource/checkExistName, name, id, operation];',
			collectType:'required;'
		},
		valid : function(form){
			var ip = $("#dsf_ip").combo("getValue");
			if (ip == '' || ip == null) {
				showErrorMessage("IP地址不能为空");
				return;
			}
			var validResult = pollTime.validatePollTime() ;
			if(validResult != null){
				dsf.dsf_form_validator.showMsg("#pollTimeError",validResult) ;
				return ;
			}else{
				$("#pollTimeError").html("") ;
			}
			var isValid = dsf.dsf_form_validator.isFormValid();
			if(isValid){
				var formId = "#dsf_form" ;
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
				$("#dl_dialog"+" > .dialog-button > .l-btn").first().linkbutton("disable");
				$.post("/sim/datasource/save",data,function(result){
					$("#dl_dialog"+" > .dialog-button > .l-btn").first().linkbutton("enable");
					if(result.status){
						 $("#dl_dialog").dialog("close");
					}else if(result.result == "timer"){
						showErrorMessage(result.message) ;
					}else{
						dsf.dsf_form_validator.showMsg($("#dsf_name"), {type: "error",msg: result.message});
						$("#dl_dialog").panel("body").children(".panel").children(".panel-body").scrollTop(0)
					}
				},"json") ;
			}
		}
	}).data("validator");
});