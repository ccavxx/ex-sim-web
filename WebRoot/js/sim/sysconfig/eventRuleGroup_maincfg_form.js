/**
 * 节点数据
 */
function RuleDataNode() {
	this.$id = null;// 节点ID
	this.$type = "event";// 类型
	this.$format = null;// 表现样式
	this.$Properties = {name:null, time:null, freq:null};// 配置属性
	this.$conditon = {fields:null, propOps:null, fVals:null};// 节点配置条件
	this.dataId = null;
}
/**
 * 规则流程图类
 * @returns {RuleFlow}
 */
function RuleFlow() {
	this.ruleNodes = [];// 规则节点组
	this.ruleAssc = {}; // 节点关联组fromIdtoId:data
	this.currentRuleNode = null;// 当前选择节点
	this.currentConnection = null;// 当前选择的关联
	this.jsPlumbInstance = null;// 当前Jsplumb实例对象
	
	this.item_menu = null;// 节点菜单
	
	this.endPoint = {sourceEndpoint : null, targetEndpoint : null};// 当前流程中使用的连接点样式
	this.dynamicOperation = {};// 右键菜单添加的动态函数
	this.config_panel = $("<div></div>");
	this.sourceAnchors = ["LeftMiddle", "RightMiddle"];// 连线节点位置
	this.configTabTitles = ["告警方式","关联知识", "规则配置", "关联条件"];// tab的 title 集合（按顺序从左到右）
	this.currentSelectedTabTitle = this.configTabTitles[0];// tab的 title 集合（按顺序从左到右）
}
/**
 * init
 */
RuleFlow.prototype.init = function() {
	this.initJsPlumb();
}
/**
 * 格式化container容器的宽度
 */
RuleFlow.prototype.initContentDivW = function(nodeleng) {
	var widthTemp = $("#container_id").width();
	var countTotalNow = Math.floor(widthTemp / 200);
	if(nodeleng > countTotalNow){
		var countTemp = nodeleng - countTotalNow;
		$("#container_id").width(widthTemp + countTemp * 200);
	} else {
		var countTemp = countTotalNow - nodeleng;
		var wTemp = widthTemp - countTemp * 200;
		$("#container_id").width((wTemp <= 1150) ? 1150 : wTemp);
	}
}
/**
 * 设置第一个规则节点为默认选中
 * 选中ruleNode
 * indexVal 默认为0
 */
RuleFlow.prototype.set_default_rule = function(indexVal) {
	var ruleNodes = this.ruleNodes;
	if(ruleNodes && ruleNodes.length) {
		var indexTemp = 0;
		if(indexVal) {
			indexTemp = indexVal;
		}
		this.ruleNodeClickHandler(ruleNodes[indexTemp]);
	}
}
/**
 * 绑定规则节点事件
 */
RuleFlow.prototype.bindclickEvt = function(ruleNode) {
	var _this = this;
	var _ruleNode = ruleNode;
	_ruleNode.$format.click(function() {// 添加节点事件
		_this.ruleNodeClickHandler(_ruleNode);
	});

	_this.refreshPosition();// 刷新节点位置
	_this.addRuleDataNode(_ruleNode);// 记录当前节点数据
	$(".flow_container").append(_ruleNode.$format);// 添加节点
	_this.addEndPoint(_ruleNode);// 添加链接节点
}
/**
 * 计算节点之间间隔
 * @returns {Number}
 */
RuleFlow.prototype.layoutFactor = function() {
	var height = $(".flow_container").height(),
	  	width = $(".flow_container").width(),
	  	len = this.ruleNodes.length,
	 	_factor = (1 / (2 + len)) * width;
	return _factor;
}
/**
 * 切换设计视图参数 tab
 * @param tabId
 * @param titles
 * @param openTabTitle
 */
RuleFlow.prototype.changeTabs = function(tabId, titles, openTabTitle) {
	var _this = this;
	var $tab = $("#" + tabId);
	$tab.tabs();
	$.each(titles, function(index, title) {
		if(title === openTabTitle) {
			$tab.tabs("enableTab", title);
			$tab.tabs("select", title);
			_this.currentSelectedTabTitle = title;
		} else {
			if(title == _this.configTabTitles[2] || title == _this.configTabTitles[3]){
				$tab.tabs("disableTab", title);
			}
		}
	});
}
/**
 * 初始化Jsplumb图形中的所有的对象
 */
RuleFlow.prototype.initJsPlumb = function(){
	
	this.jsPlumbInstance = jsPlumb.getInstance({
		DragOptions : { cursor : 'pointer', zIndex : 2000},
		ConnectionOverlays : [
			["Arrow", {location : 1}],
			["Label", {location : 0.1, id : "label", cssClass : "aLabel"}]
		],
		Container:"container_id"
	});
	
	this.currentConnection = null;// 当前用户操作选中的关联线
	this.currentRuleNode = null;// 当前用户操作选中的节点
	this.initJsPlumbStyle();// 初始化使用的样式对象
	this.initJsPlumbEvt();// 初始化JsPlumb事件 :主要是节点点击事件 连线点击事件
	this.initJsPlumbMenu();// 初始化菜单样式
	this.initAsscOpt();// 关联线上的关联关系
	
	eventRuleGroup.load_syscfg_evt_cat1();// 基本信息·加载关联分析分类
}
/**
 * 初始化菜单
 */
RuleFlow.prototype.initJsPlumbMenu = function() {
	var _this = this;
	this.item_menu = $('<div/>').appendTo('body');
	this.item_menu.menu().menu('appendItem', {
		text : '删除',
		iconCls : 'icon-remove',
		onclick : function(){
		 	_this.dynamicOperation["_doDeleteItem"]();// 删除节点
		}
	});
}
/**
 * 初始化JsPlumb使用到样式
 */
RuleFlow.prototype.initJsPlumbStyle = function() {
	
	var connectorPaintStyle = {
		lineWidth : 2,
		strokeStyle : "#61B7CF",
		joinstyle : "round",
		outlineColor : "white",
		outlineWidth : 1
	}, connectorHoverStyle = {
		lineWidth : 2,
		strokeStyle : "#216477",
		outlineWidth : 1,
		outlineColor : "white"
	}, endpointHoverStyle = {
		fillStyle : "#216477",
		strokeStyle : "#216477"
	};
	
	this.endPoint.sourceEndpoint = {// 规则连线节点样式 起始节点
		endpoint : "Dot",
		paintStyle : {
			strokeStyle : "#7AB02C",
			fillStyle : "transparent",
			radius : 2,
			lineWidth : 2
		},
		enabled:false,
		maxConnections : 1,
		isSource : true,
		connector : ["Flowchart", {stub : [20, 30], gap : 5, cornerRadius : 5, alwaysRespectStubs : true}],
		connectorStyle : connectorPaintStyle,
		hoverPaintStyle : endpointHoverStyle,
		connectorHoverStyle : connectorHoverStyle,
        dragOptions : {},
        overlays : [
        	["Label", {
            	location : [0.5, 1.5],
                //label : "Drag",
            	cssClass : "endpointSourceLabel",
            	events : {click : function(labelOverlay, originalEvent) {}}
            }]
        ]
	}
	this.endPoint.targetEndpoint = {// 结束节点
		endpoint:"Dot",
		enabled:false,
		paintStyle:{ fillStyle:"#7AB02C", radius:3},
		hoverPaintStyle:endpointHoverStyle,
		maxConnections:1,
		dropOptions:{ hoverClass:"hover", activeClass:"active",
			onDrop:function(e, ui) {}
		},
		isTarget:true,
        overlays : [
        	["Label", {
        		location : [0.5, -0.5],
        		// label : "Drop",
        		cssClass : "endpointTargetLabel",
        		events : {click : function(labelOverlay, originalEvent) {}}
		    }]
        ]
	}
}
/**
 * 初始化图形操作事件·链接线
 */
RuleFlow.prototype.initJsPlumbEvt = function() {
	var _this = this;
 	this.jsPlumbInstance.bind("connection", function(info) {// 链接
 		info.connection.getConnector().addClass("connHandStyle");
   		// 收集当前关系连线
   		/*var ruleAsscId = "conn_" + info.sourceId + "_to_" + info.targetId;
   		if(!_this.ruleAssc[ruleAsscId]) {
   			_this.ruleAssc[ruleAsscId] = [];
   		}
   		*/
	});
	this.jsPlumbInstance.bind("click", function(conn, originalEvent) {// 链接选择
		_this.connClickFn(conn);
	});
	this.jsPlumbInstance.bind("contextmenu", function(component, originalEvent) {// 取消链接右键菜单功能
		originalEvent.preventDefault();
	});
}
/**
 * 初始化节点属性·关联选项
 */
RuleFlow.prototype.initAsscOpt = function() {
	$.ajax({// 加载关联比较选项
		type : "post",
		url : "/sim/sysconfig/event/jsondata",
		dataType : "json",
		data : {"json":"evt_precompare_property"},// 对应的json文件名
		success : function(json) {
			if (json && eval(json).length == 1) {
				
				var _fields = json[0]['fields'],
				 	_opts = json[0]['ops'];
				
				var $cc1 = $("#_c1");
				$.each(_fields, function(fieldKey, fieldVal) {
					var _option = "<option value='" + fieldKey + "'>" + fieldVal.alias + "</option>";
					$cc1.append(_option);
				});
				
				var $f2 = $("#_f2");
				$cc1.change(function() {// 切换关联字段
					$f2.find("option").remove();// 删除关联条件
					var _val = $(this).val();
		
				 	if (_val) {// 根据选中的关联字段 切换对应的关联比较
			 			var	_showItems = _fields[_val].showItem;
			 			$.each(_showItems, function(itemIndex, itemVal) {
			 				var _foption = "<option value='" + itemVal + "'>" + _opts[itemVal] + "</option>";
				 			$f2.append(_foption);
				 		});
					 }
				});
				$cc1.change();
			}
		}
	});
}
/**
 * 规则节点创建
 */
RuleFlow.prototype.createNode = function(source) {

	var ruleDataNode = new RuleDataNode(),
		_this = this,
		// 创建节点块
		$item = $("<div class='item' style='width: 100px;height: 70px;text-align: center;'><div>"),
		// 节点id
		id = 'x_' + new Date().getTime();
      	$item.attr("id", id);
		// 创建节点名称块
		label = $("<div style='width: 100px;word-wrap:break-word;text-align: left'></div>").append(source);// 节点名称
      	$item.append(label);
      	// 为节点绑定右键事件
        $item.bind('contextmenu', function(e) {
			e.preventDefault();// 取消默认事件
			// 定义删除节点事件
			_this.dynamicOperation["_doDeleteItem"] = function() {// 删除某节点并保持节点之间的线条链接
				/*if(!_this.validateNodeOrConnData()) {
					return;
				}*/
				if(_this.ruleNodes.length == 1){
					return ;
				}
				var _id = $($item).attr("id");
				for(var i = 0; i < _this.sourceAnchors.length; i++) {
					_this.jsPlumbInstance.deleteEndpoint(_id + "_" + _this.sourceAnchors[i]);// 删除节点图形点
				}
				$($item).remove();// 删除节点图形
				// 获得节点
				var count = 0;
				for(var i = 0; i < _this.ruleNodes.length; i++) {
					if(_this.ruleNodes[i].$id == _id) {
						count = i;
						break;
					}
				}
				// 获得后节点
				var _nn = null;
				if(count + 1 < _this.ruleNodes.length){// 还有下一个节点
					// 删除下一个节点与当前节点关联的关联条件
					_nn = _this.ruleNodes[count + 1];
					_this.delRuleAsscData(_nn.$id);// 删除关联选择条件
				}
				// 获得前节点
				var _pn = null;
				if(count - 1 > -1){// 查找节点前一个节点
					_pn = _this.ruleNodes[count - 1];
				}
				_this.delRuleDataNode(_id, function(_id, node){// 删除节点数据
					if(_id == node.$id) {
						return true;
					}
					return false;
				});
				_this.delRuleAsscData(_id);// 删除关联选择条件
				
				// 重新连接被删除节点前后点
				if(_pn && _nn) {
					var connTemp = _this.jsPlumbInstance.connect({
						uuids : [_pn.$id + "_" + _this.sourceAnchors[1],
						         _nn.$id + "_" + _this.sourceAnchors[0]],
                        editable : false});
					// 节点链接选中处理
					_this.connClickFn(connTemp);
				} else if(_this.currentRuleNode && _this.currentRuleNode.$id == _id) {// 如果当前删除节点为选择节点清空
					_this.changeTabs("config_tab_id", ruleFlow.configTabTitles, ruleFlow.configTabTitles[2]);
				}
				if(_this.currentRuleNode && _this.currentRuleNode.$id == _id){
					if(_pn){
						_this.ruleNodeClickHandler(_pn) ;
					}else if(_nn){
						_this.ruleNodeClickHandler(_nn) ;
					}
				}
			}
			//配置菜单
			_this.dynamicOperation["_doConfig"] = function(){}
			_this.item_menu.menu('show', {
				left : e.pageX,
				top : e.pageY
			});
		});
        
        var len = this.ruleNodes.length;
        
        $item.css({"margin-left": _this.layoutFactor() * (len + 1) + "px",	"margin-top": "10px"});
        
        ruleDataNode.$id = id;
    	ruleDataNode.$format = $item;
        return ruleDataNode;
}
/**
 * 更新节点位置和样式 根据节点个数和节点之间的间隔
 */
RuleFlow.prototype.refreshPosition = function() {
    var len = this.ruleNodes.length;
    var _factor = this.layoutFactor();
    
    for(var i = 0; i < len; i++) {
		this.ruleNodes[i].$format.css({
			"margin-left" : _factor * (i + 1) + "px",
			"margin-top" : "10px"
		});
    }
}
/**
 * 给每一个节点div添加左中和右中两个可连接节点
 * @param ruleNode
 */
RuleFlow.prototype.addEndPoint = function(ruleNode) {
	var _this = this;
	this.jsPlumbInstance.doWhileSuspended(function() {
		_this.jsPlumbInstance.addEndpoint(ruleNode.$id, _this.endPoint.targetEndpoint, {anchor : _this.sourceAnchors[0], uuid : ruleNode.$id + "_" + _this.sourceAnchors[0]});
	    _this.jsPlumbInstance.addEndpoint(ruleNode.$id,_this.endPoint.sourceEndpoint, {anchor : _this.sourceAnchors[1], uuid : ruleNode.$id + "_" + _this.sourceAnchors[1]});
	});
}
/**
 * 记录节点实例和配置数据
 * @param ruleNode
 */
RuleFlow.prototype.addRuleDataNode = function(ruleNode) {
	this.ruleNodes.push(ruleNode);
}
/**
 * 删除节点实例和配置数据
 * @param _c
 * @param _func
 */
RuleFlow.prototype.delRuleDataNode = function(_c, _func) {
	for(var i = 0; i < this.ruleNodes.length; i++) {
		var node = this.ruleNodes[i];
		if(_func) {
			if(_func(_c, node)) {
				this.ruleNodes.splice(i, 1);
				break;
			}
		}else{
			if(ruleNode.$id == node.$id) {
				this.ruleNodes.splice(i, 1);
				break;
			}
		}
	} 
}
/**
 * 按条件删除某个关联关系
 * @param c
 * @param _func
 */
RuleFlow.prototype.delRuleAsscData = function(sourceId) {
	var _this = this;
	$.each(_this.ruleAssc, function(key, val) {
		var ids = key.replace("conn_", "").split("_to_");
		if(ids.length == 2 && ids[1] == sourceId) {
			delete _this.ruleAssc[key];
		}
	});
}
/**
 * 按条件获取某个关联节点
 * @param _c
 * @param func
 * @returns
 */
RuleFlow.prototype.findRuleDataNode = function(_c, func/*条件函数*/) {
	for(var i = 0; i < this.ruleNodes.length; i++) {
		var node = this.ruleNodes[i];
		if(func) {
			if(func(_c, node)) {
				return node;
			}
		} 
	} 
	return null;
}
/**
 * 设置某个节点对应的规则的条件
 */
RuleFlow.prototype.setRuleCondition = function() {// 设置规则条件

	if(!this.currentRuleNode) {
		return;
	}
	
	var $$c2 = $("#rule_group_rule_form_id").find("select[name='c2']"),// 字段关系（大于、小于、等于....）
		c1_vals = [],// 条件字段值1
		c2_vals = [];// 条件字段值2
	
	var c1_vals_str = "",
		c2_vals_str = "";
	var fVals = [];
	$.each($$c2, function(i, c2) {
		var c2_id = $(c2).attr("id");
		var c1_id = c2_id.replace("c2_", "c1_");// 条件字段·条件
		
		var c1_val = $("#" + c1_id).horizonComboPanel("getValue");
		var c2_val = $(c2).val();
		c1_vals_str += c1_val;
		c2_vals_str += c2_val;
		
		if(i != $$c2.length - 1) {
			c1_vals_str += ",";
			c2_vals_str += ",";
		}
		var c3 = eventCondition.inputVal[c2_id];
		if(c3) {
			if(c3.length == 1) {
				fVals.push({"dataType" : c3[0].type, "val1" : $(c3[0].cp).val()});
			} else if(c3.length == 2) {
				fVals.push({"dataType" : c3[0].type, "val1" : $(c3[0].cp).val(), "val2" : $(c3[1].cp).val()});
			} else if(c3.length == 0) {
				fVals.push({"dataType":null});
			}
		}
	});
	/**
	 * 记录条件 供保存获取
	 */
	this.currentRuleNode.$conditon["fields"] = c1_vals_str;// {fields:[SRC_ADDRESS],propOps:[LT],fVals:[{"dataType":"IP","val1":"192.168.10.2"}]};
	this.currentRuleNode.$conditon["propOps"] = c2_vals_str;//
	this.currentRuleNode.$conditon["fVals"] = fVals;//
}
/**
 * 添加关联关系
 */
RuleFlow.prototype.addRuleCorr = function() {// 添加关联
	// 获取当前选择连线的两头相连的节点ID
	var sourceId = $("#formPointId").attr("pointId");
	var targetId = $("#toPointId").attr("pointId");
	
	var _f = function(_c, node) {
		if(node.$id == _c) {
			return true;
		}
		return false;
	}
	
	// 获取两头节点信息配置信息
	var source = this.findRuleDataNode(sourceId, _f),
		target = this.findRuleDataNode(targetId, _f);
	
	var sprops = source.$Properties,
		tprops = target.$Properties;
	
	var _c_selectedIndex = rule_group_assc_form.cc1.selectedIndex,// 选择的关联字段
		_f_selectedIndex = rule_group_assc_form.f2.selectedIndex,// 比较关系
		
		_c_option = rule_group_assc_form.cc1.options[_c_selectedIndex],
		_c_value = _c_option.value,
		_c_name = _c_option.text,
		
		_f_option = rule_group_assc_form.f2.options[_f_selectedIndex],
		_f_value = _f_option.value,
		_f_name = _f_option.text;
	var _textTemp = tprops.name + ":" + sprops.name + "(" + _c_name + "-" + _f_name + ")";
	var _valueTemp = targetId + "#" + _c_value + "#" + _f_value;
	var _flag = false;
	$("#linkConditionId").find("option").each(function(){
		var val = this.value;
		if(val == _valueTemp) {
			_flag = true;
			return;
		}
	});
	if(_flag) {
		return;
	}
	//拼接关系显示 在保存配置时回获取该关系配置
	var _l_option = new Option(_textTemp, _valueTemp);// 节点ID + "#" + 比较关系 + "#" + 关联字段
	_l_option.title = _textTemp;
	rule_group_assc_form.linkCondition.add(_l_option);// 添加到select显示中
}
/**
 * 刷新节点之间的连线
 */
RuleFlow.prototype.refreshConnection = function() {
	var ruleNodes = this.ruleNodes,
		_len = ruleNodes.length;
	for(var i = 1; i < _len ; i++) {
		var c = ruleNodes[i],
			p = ruleNodes[i - 1];

		this.jsPlumbInstance.connect({
			uuids : [p.$id + "_" + this.sourceAnchors[1],
			         c.$id + "_" + this.sourceAnchors[0]],
		    editable : false
		});
	}
}
/**
 * 节点事件处理·点击节点会保存上一次操作节点的信息，并将当前选中的节点的配置信息回显到页面
 * @param _ruleNode
 */
RuleFlow.prototype.ruleNodeClickHandler = function(_ruleNode) {
	// 保存上节点数据
	if(!this.saveNodeOrConnData()) {
		return;
	}
	this.changeTabs("config_tab_id", this.configTabTitles, this.configTabTitles[2]);// 展开节点 tab、显示一个空属性设置窗口
	this.currentRuleNode = _ruleNode;// 调整当前规则节点
	// 清除规则列表
	$("#syscfg_evt_div_id").empty();
	eventCondition.inputVal = {};// 清除规则列表
	
	// 获取当前节点的规则配置信息
	var c1_vals_str = _ruleNode.$conditon["fields"],
		c2_vals_str = _ruleNode.$conditon["propOps"],
		fVals = _ruleNode.$conditon["fVals"];
	
	// 分解配置信息并添加到配置显示界面中
	if(c1_vals_str) {
		var fields = c1_vals_str.split(','),
			funcs = c2_vals_str.split(',');
		for (var i = 0; i < fields.length; i++) {
			var _rl = {"field" : fields[i], "function" : funcs[i], "params" : []},
			  	_fval = fVals[i];
			if(_fval.val1) {
				_rl.params.push(_fval.val1);
			}
			if(_fval.val2) {
				_rl.params.push(_fval.val2);
			}
			eventCondition.addCondition(_rl);
		}
	}
	// 把当前规则节点的基本信息显示到界面中  （名称，事件，次数）
	this.setRuleAttrs2Form();
	$.each(this.ruleNodes, function(i, rn) {// 节点图形显示样式
        rn.$format.css({"background-color": "white"});
    });
	_ruleNode.$format.css({"background-color" : "#86BBD1"});// 设置当前节点为选中样式
}
/**
 * =================================================
 * 节点、连接线属性设置								   =
 * =================================================
 */
/**
 * 渲染节点规则属性到表单
 */
RuleFlow.prototype.setRuleAttrs2Form = function() {
	var ruleNode = this.currentRuleNode;
	if(ruleNode) {
		var properties = ruleNode.$Properties;
		rule_group_rule_form.event_name.value = properties.name != null ? properties.name : "名称";
		rule_group_rule_form.time.value = properties.time != null ? properties.time : 5;
		rule_group_rule_form.count.value = properties.freq !=null ? properties.freq : 1;
	}
}
/**
 * 保存节点规则属性
 */
RuleFlow.prototype.setRuleAttrs = function() {

	var rule_group_rule_form_id_validation = $('#rule_group_rule_form_id').data("validator");
	$('#rule_group_rule_form_id').trigger("validate");
	flag = rule_group_rule_form_id_validation.isFormValid();
	if(flag === false) {
		return false;
	}
	var ruleNode = this.currentRuleNode;
	if(ruleNode) {
		
		var eventName = rule_group_rule_form.event_name.value;// 名称
		var properties = ruleNode.$Properties;
		properties["name"] = eventName;
		properties["time"] = rule_group_rule_form.time.value;
		properties["freq"] = rule_group_rule_form.count.value;
		ruleNode.$format.empty();
		var label = $("<div style='width:100px;word-wrap:break-word;text-align:left'></div>").append(eventName);
		ruleNode.$format.append(label);
	} else {
		return false;
	}
	return true;
}
/**
 * 渲染连线表单数据到表单、并切换tab标签
 */
RuleFlow.prototype.changeConnFormData = function(conn) {
	var _this = this;
	var _f = function(_c, node) {
		if(node.$id == _c) {
			return true;
		}
		return false;
	}
	var source = _this.findRuleDataNode(conn.sourceId, _f),
		target = _this.findRuleDataNode(conn.targetId, _f);
	if(source && target) {
		$("#formPointId").text(source.$Properties.name).attr("pointId", source.$id);
		$("#toPointId").text(target.$Properties.name).attr("pointId", target.$id);
	}
	var ruleAsscId = "conn_" + conn.sourceId + "_to_" + conn.targetId;
	var propertiesTemp = _this.ruleAssc[ruleAsscId] || [];
	$("#linkConditionId").empty();
	$.each(propertiesTemp, function(index, temp){
		var optionTemp = "<option title='" + temp.text + "' value='" + temp.value + "'>" + temp.text + "</option>";
		$("#linkConditionId").append(optionTemp);
	});
	
	// 定义当前链接、打开 tab 标签
	_this.currentConnection = {sourceId : conn.sourceId, targetId : conn.targetId}
	ruleFlow.changeTabs("config_tab_id", _this.configTabTitles, _this.configTabTitles[3]);
}
/**
 * 保存规则连接线属性
 */
RuleFlow.prototype.setRuleAttrConn = function() {
	var _options = rule_group_assc_form.linkConditionId.options;
	/*if(!_options || _options.length == 0) {
		$('#rule_group_assc_form_id').validator('showMsg', '#linkConditionId',{type:"error", msg:"关联条件不能为空"});
		showAlarmMessage('关联条件不能为空');
		return false;
	} else {
		$('#rule_group_assc_form_id').validator('hideMsg', '#linkConditionId');
	}*/
	var sourceId = $("#formPointId").attr("pointId");
	var targetId = $("#toPointId").attr("pointId");
	if(sourceId && targetId) {
		var fromToId = "conn_" + sourceId + "_to_" + targetId;
		if(!this.ruleAssc[fromToId]) {
			this.ruleAssc[fromToId] = [];
		}
		var properties = this.ruleAssc[fromToId];
		if(properties.length > 0) {
			properties.splice(0, properties.length);
		}
		for(var i = 0; i < _options.length; i++) {
			var _option_value = _options[i].value;
			var _option_text = _options[i].text;
			properties.push({value:_option_value, text:_option_text});
		}
	} else {
		showAlarmMessage('关联条件不能为空');
		return false;
	}
	return true;
}
/**
 * 保存节点或连接线的表单数据
 */
RuleFlow.prototype.saveNodeOrConnData = function() {
	var preTabTitle = this.currentSelectedTabTitle;// 保存前一个展开tab的数据
	if(preTabTitle == this.configTabTitles[2]) {
  		var flag = this.setRuleAttrs();// 保存上一节点属性数据
  		if(flag == false){
  			showAlarmMessage('参数填写不完整！');
  			return false;
  		}
  		this.setRuleCondition();// 保存上一节点规则条件
  		return true;
	} else if(preTabTitle == this.configTabTitles[3]) {
		return this.setRuleAttrConn();// 保存关联节点数据
	} else {
		return true;
	}
}
/**
 * 验证节点或连接线的表单数据
 */
RuleFlow.prototype.validateNodeOrConnData = function() {
	var preTabTitle = this.currentSelectedTabTitle;// 保存前一个展开tab的数据
	if(preTabTitle == this.configTabTitles[2]) {
		var rule_group_rule_form_id_validation = $('#rule_group_rule_form_id').data("validator");
		$('#rule_group_rule_form_id').trigger("validate");
		flag = rule_group_rule_form_id_validation.isFormValid();

		if(flag === false) {
			showAlarmMessage('参数填写不完整！');
			return false;
		}
	} else if(preTabTitle == this.configTabTitles[3]) {
		var _options = rule_group_assc_form.linkConditionId.options;
		if(!_options || _options.length == 0) {
			$('#rule_group_assc_form_id').validator('showMsg', '#linkConditionId',{type:"error", msg:"关联条件不能为空"});
			showAlarmMessage('关联条件不能为空');
			return false;
		} else {
			$('#rule_group_assc_form_id').validator('hideMsg', '#linkConditionId');
		}
	}
	return true;
}
/**
 * 切换连接线为选中状态样式
 */
RuleFlow.prototype.clickConnStyle = function(conn) {
	var connList = this.jsPlumbInstance.getConnections();
	$.each(connList, function(index, connTemp) {
		connTemp.removeClass("connClickStyle");
	});
	conn.getConnector().addClass("connClickStyle");
}
/**
 * 连接线参数处理 click 组合函数
 */
RuleFlow.prototype.connClickFn = function(conn) {
	// 保存上个节点的数据
	if(!this.saveNodeOrConnData()) {
		return;
	}
	// 渲染连接线样式
	this.clickConnStyle(conn);
	// 渲染将要打开的conn样式数据·获取两头节点信息配置信息
	this.changeConnFormData(conn);
}
/**
 * 编辑·当条节点规则配置设置收集
 */
RuleFlow.prototype.set_base_rule = function() {
	var _this = this;
	if(eventRuleGroup.currentGroupDispatch) {
		 // 按照从后台获取的规则配置 组织规则节点显示信息
		 var rules = eventRuleGroup.currentGroupDispatch.rule;
		 $.each(rules, function(i, rule) {
			ruleFlow.initContentDivW(i);
			var ruleNode = _this.createNode(rule.name),
			 	 _fVals = [],
			     _c1_vals = [],
			     _c2_vals = [];
			 
			 // 设置规则节点规则配置
			 for(var j = 0; j < rule.columns.length; j++) {
				 var rc = rule.columns[j];
				 _c1_vals.push(rc.field);
				 _c2_vals.push(rc["function"]);
				 var params = rc.params;
				 var field = eventCondition.findField(rc, function(o,_field){//name
					 if(o.field == _field.name) {
						 return true;
					 }
					 return false;
				 }),
				 _data_type = field.type;
				 
				 if(params) {
					 if(params.length == 1) {
						 _fVals.push({"dataType" : _data_type, "val1" : params[0]});
					 } else if(params.length == 2) {
						 _fVals.push({"dataType" : _data_type, "val1" : params[0], "val2" : params[1]});
					 } else if(params.length == 0) {
						 _fVals.push({"dataType" : null});
					 }
				 }
			 }
			 
			 var _c1_vals_str = "",
			 	 _c2_vals_str = "";
			 for(var j = 0; j < _c1_vals.length; j++) {
				 _c1_vals_str += _c1_vals[j];
				 _c2_vals_str += _c2_vals[j];
				 if(j != _c1_vals.length - 1) {
					 _c1_vals_str += ",";
					 _c2_vals_str += ",";
				 }
			 }
			 // dataId
			 ruleNode.dataId = rule.ruleId;
			 ruleNode.$conditon["fields"] = _c1_vals_str;// {fields : null, propOps : null, fVals : null};
			 ruleNode.$conditon["propOps"] = _c2_vals_str;//
			 ruleNode.$conditon["fVals"] = _fVals;
			 // 设置规则节点基本信息 名称、时间、次数
			 var properties = ruleNode.$Properties;
			 properties["name"] = rule.name;
			 properties["time"] = rule.time;
			 properties["freq"] = rule.count;
			 // 给节点绑定事件信息
			 _this.bindclickEvt(ruleNode);
		 });
	 } else {
		 _this.changeTabs("config_tab_id", _this.configTabTitles, _this.configTabTitles[0]);
	 }
}
/**
 * 编辑·设置关联配置到配置页面收集
 */
RuleFlow.prototype.set_base_assc = function() {
	 if(eventRuleGroup.currentGroupDispatch) {
		 var _this = this;
		 var assc = eventRuleGroup.currentGroupDispatch.assc;
		 if(assc) {
			 var ruleNodes = ruleFlow.ruleNodes,
			 	 _len = ruleNodes.length;

			 ruleFlow.refreshConnection();
			 var connList = this.jsPlumbInstance.getConnections();
			 $.each(connList, function(index, connTemp) {
				 var sourceId = connTemp.sourceId;
				 var targetId = connTemp.targetId;
				 var fromToId = "conn_" + sourceId + "_to_" + targetId;
				 if(!ruleFlow.ruleAssc[fromToId]) {
					 ruleFlow.ruleAssc[fromToId] = [];
				 }
				 var properties = ruleFlow.ruleAssc[fromToId];
				 var _len_assc = assc.length;
				 var _f = function(_c, node) {
					 if(node.$id == _c) {
						 return true;
					 }
					 return false;
				 }
				 var source = _this.findRuleDataNode(sourceId, _f),
					 target = _this.findRuleDataNode(targetId, _f);
				 
				 for(var i = 0; i < _len_assc; i++) {
					 var c = assc[i],_cmps = c["cmps"];
					 if(c.ruleId == target.dataId && _cmps && _cmps.length != 0) {
						 $.each(_cmps, function(j, _cmp) {
							 var _f_opt = eventCondition.findOptionByN(_cmp.funcName);
							 var _c_field = eventCondition.findField(_cmp.field, function(o, field) {
								 return o == field.name;
							 });
							 if(!_c_field || !_f_opt){
								 return true;
							 }
							 var _option_text = target.$Properties["name"] + ":" + source.$Properties["name"] + "(" + _c_field.alias + "-" + _f_opt.alias + ")";
							 var _option_value = target.$id + "#" + _cmp.field + "#" + _cmp.funcName;
							 properties.push({value:_option_value, text:_option_text});
						 });
					 }
				 }
			 });
		}
	}
	// 关联连线属性绑定双击删除事件
	$("#linkConditionId").dblclick(function() {// 双击删除关联规则
		var _lc_selectedIndex = rule_group_assc_form.linkCondition.selectedIndex;
		rule_group_assc_form.linkCondition.options.remove(_lc_selectedIndex);
	});
}
RuleFlow.prototype.setRuleNode = function() {
	var ruleNode = this.currentRuleNode;
	ruleNode.$format.empty();
	var eventName = rule_group_rule_form.event_name.value;// 名称
	var label = $("<div style='width:100px;word-wrap:break-word;text-align:left'></div>").append(eventName);
	ruleNode.$format.append(label);
}
/**
 * ==================================================
 * run												=
 * ==================================================
 */
var ruleFlow = new RuleFlow();
$(function() {
	ruleFlow.init();// 初始化整个图形
});
/**
 * 页面添加节点按钮
 */
function createRule() {
	if(!ruleFlow.validateNodeOrConnData()) {
		return;
	}
	// 创建新节点
	var name = "名称";
	var ruleNode = ruleFlow.createNode(name);
	// 绑定点击事件
	ruleFlow.bindclickEvt(ruleNode);
	
	// 将当前添加的节点与图形中的最后一个节点连接
	var ruleNodes = ruleFlow.ruleNodes;
	var ruleNodesLength = ruleNodes.length;
	if(ruleNodesLength > 1) {
		ruleFlow.initContentDivW(ruleNodesLength);
		var c = ruleNodes[ruleNodes.length - 1],
			p = ruleNodes[ruleNodes.length - 2];
		ruleFlow.jsPlumbInstance.connect({uuids:[p.$id + "_" + ruleFlow.sourceAnchors[1],
		                                         c.$id + "_" + ruleFlow.sourceAnchors[0]],
		                                  editable:false});
	}
	// 将当前创建的节点设置为默认节点
	ruleFlow.ruleNodeClickHandler(ruleNode);
}
/**
 * 关联分析规则配置数据提交
 */
function doEventGroupConfig() {
	// 基本信息 from 表单
	var rule_group_prop_form_id_validation = $('#rule_group_prop_form_id').data("validator");
	$('#rule_group_prop_form_id').trigger("validate");
	var flag = rule_group_prop_form_id_validation.isFormValid();
	
	if(flag == false) {
		return false;
	}
	var ruleName = rule_group_prop_form.rule_group.value ;
	if(ruleName.indexOf(" ") != -1 || ruleName.indexOf("%") != -1 || ruleName.indexOf("$") != -1){
		showAlarmMessage('规则名称不允许包含空格、%、$等特殊字符！');
		return false ;
	}
	if(rule_group_prop_form._r_cat1.value == "-1"){
		showAlarmMessage("一级分类不许为空，请选择！");
		return false;
	}
	// 关联规则属性信息验证、关联关系信息验证、提交表单确保最后一个节点数据被取到
	if(!ruleFlow.saveNodeOrConnData()) {
		return;
	}
	if(!ruleFlow.ruleNodes || ruleFlow.ruleNodes.length == 0) {
		showAlarmMessage('未填写关联规则！');
		return;
	}
	var _ruleAsscs = ruleFlow.ruleAssc;
	if(!_ruleAsscs || _ruleAsscs.length == 0) {
		showAlarmMessage('规则条件填写不完全');
		return;
	} else {
		var _flagRuleAsscs = false;
		var connList = ruleFlow.jsPlumbInstance.getConnections();
		$.each(connList, function(index, connTemp) {
			var fromToStr = "conn_" + connTemp.sourceId + "_to_" + connTemp.targetId;
			var val = _ruleAsscs[fromToStr];
			if(!val || val.length == 0) {
				showAlarmMessage('关联条件不能为空');
				_flagRuleAsscs = true;
				return;
			}
		});
		if(_flagRuleAsscs) {
			return;
		}
	}
	var egcfg = {};
	// 收集基本信息
	egcfg.groupName = rule_group_prop_form.rule_group.value;// 名称
	egcfg.timeout = rule_group_prop_form.rule_timeout.value;// 超时
	egcfg.priority = rule_group_prop_form.level.value;// 级别
	egcfg.alarmState = 0;
	egcfg.cat1id = rule_group_prop_form._r_cat1.value;// 一级分类
	egcfg.cat2id = rule_group_prop_form._r_cat2.value;// 二级分类
	egcfg.status = rule_group_prop_form.status.checked ? 1 : 0;// 是否启用
	egcfg.desc = rule_group_prop_form.descContent.value;// 描述
	var sumTime = 0;
	var _flagRuleFVals = false;
	// 规则配置
	$.each(ruleFlow.ruleNodes, function(i, ruleNode) {
		
		egcfg["eventRuleConfigs["+i+"].eventName"] = ruleNode.$Properties["name"];
		egcfg["eventRuleConfigs["+i+"].count"] = ruleNode.$Properties["freq"];
		egcfg["eventRuleConfigs["+i+"].time"] = ruleNode.$Properties["time"];
		sumTime += parseInt(ruleNode.$Properties["time"]);
		
		var fields = ruleNode.$conditon["fields"];
		var propOps = ruleNode.$conditon["propOps"];
		var fVals = ruleNode.$conditon["fVals"];
		if(!fVals || fVals.length == 0) {
			showAlarmMessage('规则条件不能为空！');
			_flagRuleFVals = true;
			return;
		}
		// 单规则
		egcfg["eventRuleConfigs[" + i + "].fields"] = fields;
		egcfg["eventRuleConfigs[" + i + "].propOps"] = propOps;
		egcfg["eventRuleConfigs[" + i + "].fVals"] = fVals;
		// 关联字段
		var id = ruleNode.$id,
		    _combinations = [];
		$.each(_ruleAsscs, function(key, val) {
			var ids = key.replace("conn_", "").split("_to_");
			if(ids.length == 2 && ids[1] == id) {
				$.each(val, function(index, valueTemp) {
					var _val_terms = valueTemp.value.split('#');
					_combinations.push({id : _val_terms[0], field : _val_terms[1], func : _val_terms[2]});
				});
			}
		});
 		egcfg["eventRuleConfigs[" + i + "].combinations"] = _combinations;
	});
	if(_flagRuleFVals) {
		return;
	}
	if(egcfg.timeout < sumTime) {
		showAlarmMessage('超时时间应该大于等于各个关联规则的时间和，请重新设置时间参数!');
		return;
	}
	// 收集关联知识库信息
	var _knowledgelist = knowledge._plugin_set['associateList'];
	if(_knowledgelist) {
		$.each(_knowledgelist, function(i, kl) {
			egcfg["knowledgeId[" + i + "]"] = kl;
		});
	}
	// 收集关联告警方式信息
	var _rp_options = event_resp_choice_form.responseIds.options;
	if(_rp_options) {
		$.each(_rp_options, function(i, option) {
			egcfg["responseIds[" + i + "]"] = option.value;
		});
	}
	if(eventRuleGroup.currentGroupDispatch == null || eventRuleGroup.cloneEventGroup) {// 无加载数据 添加
		
		$.ajax({// 按id加载配置
			type : "post",
			url : "/sim/sysconfig/event/addEventCorrRule",
			dataType : "json",
			data : egcfg,
			async : true,
			success : function(json) {
				if(json.error) {
					showAlarmMessage(json.error);
					return;
				}
				eventRuleGroup.closeAddOrEditDialog();
				eventRuleGroup.currentGroupDispatch = null;
				eventRuleGroup.showEventRuleGroupListReload();
			}
		});
		
	} else {// 更新
		egcfg.groupId = eventRuleGroup.currentGroupDispatch.group.groupId;// 更新数据
		
		$.ajax({// 按id加载配置
			type : "post",
			url : "/sim/sysconfig/event/editEventCorrRule",
			dataType : "json",
			data : egcfg,
			async : true,
			success : function(json) {
				if(json.error) {
					showAlarmMessage(json.error);
					return;
				}
				eventRuleGroup.closeAddOrEditDialog();
				eventRuleGroup.currentGroupDispatch = null;
				eventRuleGroup.showEventRuleGroupListReload();
			}
		});
	}
}
