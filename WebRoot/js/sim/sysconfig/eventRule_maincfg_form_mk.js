/**
 * 事件规则条件
 * @returns {EventCondition}
 */
function EventCondition() {
	this.ui_condition_data = null;
	this.fields = null;
	this.ops = null;
	this.inputVal = {};
}
/**
 * 加载规则级联数据
 */
EventCondition.prototype.load_syscfg_rule = function() {
	var _this = this;
	$.ajax({// 按id加载配置
	    type : "post",
	    url : "../../sim/sysconfig/event/jsondata",
	    dataType : "json",
	    data : {"json" : "evt_cfg_property"},
	    async : false,
	    success : function(json) {
	    	if (json && eval(json).length == 1) {
				_this.ui_condition_data = eval(json)[0];
				_this.fields = _this.ui_condition_data["fields"];
				_this.ops = _this.ui_condition_data["ops"];
			}
	    }
	});
}
/**
 * 获得select-options组合
 * @param name
 * @returns {Array}
 */
EventCondition.prototype.findShowItemByFN = function(name) {
	var _this = this,
		_d = [];
	$.each(_this.fields, function(i, field) {
		if (field["name"] == name) {
			$.each(field["showItem"], function(j, item) {
				var _item = {};
				_item["dataType"] = field["type"];
				_item["validate"] = field["validate"];
				_item["validateSize"] = field["validateSize"];
				_item["op"] = _this.findOptionByN(item);
				_d.push(_item);
			});
		}
	});
	return _d;
}
/**
 * 获得对应name的option内容
 * @param name
 * @returns
 */
EventCondition.prototype.findOptionByN = function(name) {
	for ( var i in this.ops) {
		if (this.ops[i].name == name) {
			return this.ops[i];
		}
	}
}
EventCondition.prototype.findField = function(o, _func) {
	for ( var i in this.fields) {
		if (_func(o, this.fields[i])) {
			return this.fields[i];
		}
	}
}
/**
 * 格式化字段对应的函数值
 * @param _item
 * @param input
 */
EventCondition.prototype.inputFormat = function(_item, input) {

	if(_item["dataType"] == "String") {
		if(_item["validate"] == "notNull") {
			input.attr("maxLength", 200).width(350);
			// input.attr("data-rule", "required;length[~50]");
		} else if(_item["validate"] == "IP") {
			/*input.bind("change", function(){
				var val = $(this).val();
				if(val) {
					var flag = /^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/g.test(val);
					if(!flag) {
						$(this).val("");
					}
				}
			});*/
			// input.attr("data-rule", "required;ipv4");
		} else if(_item["validate"] == "DVC_TYPE_SELECT") {
			input.combotree({
				width : 200,
				height : 24,
				editable : false,
				url : '/sim/asset/assetCategories?levelFlag=first',
				onClick : function(node) {
					input.val(node.id);
				}
			});
		}
	} else if(_item["dataType"] == "Integer" && _item["validateSize"]) {
		var min = _item["validateSize"][0],
			max = _item["validateSize"][1];
		input.width(350);
		/*input.numberbox({
			min : _item["validateSize"][0],
			max : _item["validateSize"][1]
		});*/
	} else if(_item["dataType"] == "Date") {// 日期处理
		input.addClass("Wdate").attr("onFocus", "WdatePicker({dateFmt:'HH:mm:ss'})");
		/*input.timepicker({
			  minuteStep: 1,
			  secondStep:1,
			  showMeridian: false,
			  showSeconds:true,
			  disableFocus: true
		});*/
	}
}
/**
 * 1.不指定参数将添加一条空的规则，主要作用是用在添加一条新规则用户自定规则
 * 2.指定参数将按指定的参数添加一条规则，主要作用是用在加载一条已经用户配置好的规则：
 * 如：{"field":” SRC_ADDRESS”,"function":” EQ”,” params”: [“192.168.1.1”] }
 */
EventCondition.prototype.addCondition = function() {
	var _num = $("#syscfg_evt_div_id").find(".divC1C2").length;
	if(_num >= 5) {// 限制最多添加5条规则
		showAlarmMessage( '最多添加5条规则!');
		return;
	}
	var timeStampTemp = new Date().getTime();
	var  _this = this,
	     fields = this.ui_condition_data.fields,
		 $row = $("<div class='divC1C2 margint5'></div>"),// 规则行
		 $c1p = $("<span class='marginl5'></span>"),// 域选择列
		 $c1 = $("<select name='c1' id='c1_" + timeStampTemp + "'></select>"),
		 $c2p = $("<span class='marginl5'></span>"),// 函数列
		 $c2 = $("<select name='c2' class='w120' id='c2_" + timeStampTemp + "'></select>"),
		 $c3p = $("<span class='marginl5'></span>"),// 函数值
		 $c4p = $("<span class='marginl5'></span>");// 规则行操作
	
	/**
	 * 初始化c2事件
	 */
	var bindingOptEvent = function() {
		$c2.change(function() {// c2绑定选择时间
			delete _this.inputVal["c2_" + timeStampTemp];// 事件切换去掉初始值
			
			var c2Val = $(this).val();
			if(c2Val) {
				var c1Val = $("#c1_" + timeStampTemp).horizonComboPanel("getValue");
				
				if (c1Val) {
					var _ditem = _this.findShowItemByFN(c1Val);
					$.each(_ditem, function(i, item) {
						if (c2Val == item["op"].name) {
							addParam(item);
							return;
						}
					});
				}
			}
		});
	},
	/**
	 * 函数列操作·添加函数列
	 */
	addfunc = function() {
		// ie不兼容  清除重建 c2
		$c2p.empty();
		$c2 = $("<select name='c2' class='w120' id='c2_" + timeStampTemp + "'></select>");
		$c2p.append($c2);
		
		// 重新绑定事件
		bindingOptEvent();
		if(arguments.length == 1) {// 默认只有一个参数
			$.each(arguments[0], function(i, item) {
				var $op2 = $("<option value=" + item["op"].name + ">"+ item["op"].alias + "</option>");
				$c2.append($op2);
				if (i == 0) {
					addParam(item);
				}
			});
		}
		if(arguments.length == 2) {// 多个参数
			var _c = arguments[1];
			$.each(arguments[0], function(i, item) {
				_func_cascade_customize(item, _c);
			});
		}
	},
	/**
	 * 参数操作
	 */
	addParam = function() {
		$c3p.empty();
		inputField = [];// 规则行参数值，在删除行时需要同时删除掉参数值
		var item = arguments[0];
		_this.inputVal["c2_" + timeStampTemp] = inputField;
		if (item["op"].showInput) {
			var showItems = item["op"].showItem,
				showLabels = item["op"].showLabel,
				$showItem = null;
			
			for ( var k = 0; k < showItems.length; k++) {
				if (k < showLabels.length) {
					$c3p.append(showLabels[k]);//添加说明
				}
				$showItem = $("<input type='text' name='" + showItems[k] + "' />");
				
				if(arguments.length > 1) {
					var itemValues = arguments[1];
					if(itemValues) {
						$showItem.val(itemValues[k]);// 初始化值
					}
				}
				inputField.push({"cp":$showItem,"type":item.dataType});
				$c3p.append($showItem);
				_this.inputFormat(item, $showItem);
			}
		}
	},
	/**
	 * 函数列定制处理
	 */
	_func_cascade_customize = function() {
		var item = arguments[0],
			c = arguments[1],
			$op2 = $("<option value=" + item["op"].name + ">"+ item["op"].alias + "</option>");
		$c2.append($op2);
		if(c["function"] && item["op"].name == c["function"] && c["params"]) {
			$op2.attr("selected", "selected");
			addParam(item, c["params"]);
		}
	};
	// 初始化c2事件
	bindingOptEvent();
	// 判断c1是否有设定值
	var horizonComboPanelC = null;
	if(arguments.length == 1) {// 此处为给当前函数addCondition指定了输入值如{"field":"SRC_ADDRESS","function":"EQ","params":["192.168.1.1"]}
		// 编辑时重置数据
		horizonComboPanelC = arguments[0];
	}
	$c1p.append($c1);
	$c2p.append($c2);
	
	var $o4p = $("<a href='#' class='easyui-linkbutton' style='margin-left:5px;'>删除</a>");
	$o4p.linkbutton({
	    iconCls: 'icon-remove'
	});
	$o4p.bind('click', function() {// 执行行删除
		delete _this.inputVal["c2_" + timeStampTemp];
		$row.remove();
    });
	
	$c4p.append($o4p);
	
	$row.append($c1p).append($c2p).append($c3p).append($c4p);
	$("#syscfg_evt_div_id").append($row);
	
	$("#c1_" + timeStampTemp).horizonComboPanel({
		height : 24,
		width : 120,
		editable : false,
		valueField : "id",
		textField : "text",
		url : "/js/cnComboPanel/data.json",
		onChange : function(newValue, oldValue) {// c1绑定选择事件
			if(newValue) {
				delete _this.inputVal["c2_" + timeStampTemp];// 事件切换去掉初始值
				var _ditem = _this.findShowItemByFN(newValue);
				if(horizonComboPanelC != null) {
					addfunc(_ditem, horizonComboPanelC);
					horizonComboPanelC = null;
				} else {
					addfunc(_ditem);
				}
			}
		},
		onLoadSuccess:function() {
			if(horizonComboPanelC) {// 此处为给当前函数addCondition指定了输入值如{"field":"SRC_ADDRESS","function":"EQ","params":["192.168.1.1"]}
				$("#c1_" + timeStampTemp).horizonComboPanel("setValue", horizonComboPanelC.field);
			} else {// 无指定参数值 需要用户自定义选择当前规则各字段、函数和值
				var field = fields[0];
				$("#c1_" + timeStampTemp).horizonComboPanel("setValue", field["name"]);
			}
		}
	});
}
/**
 * ====================
 * run                =
 * ====================
 */
var eventCondition = new EventCondition();
(function() {
	// 初始化规则配置
	eventCondition.load_syscfg_rule();
})();
