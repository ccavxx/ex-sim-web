(function($) {
	/**
	 * 默认选中第一个tab
	 * @param {Object} jq
	 * @param {Object} tabTitle
	 */
	function scrollToFirst(jq, tabTitle) {
		var tabFlag = 0;
		if(tabTitle){
			tabFlag = tabTitle;
		}
		var panel = $(jq).combo("panel");
		var tabsDom = panel.find(".horizon-comboPanel-body-tabs");
		var items = tabsDom.tabs("tabs");
		if (items.length) {
			tabsDom.tabs("select", tabFlag);
		}
	};
	/**
	 * 选中指定值对应的选项
	 * @param {Object} jq
	 * @param {Object} value
	 * @return {TypeName} 
	 */
	function selectByValue(jq, value) {
		if(!value){
			return;
		}
		var panel = $(jq).combo("panel");
		panel.find(".horizon-comboPanel-ul-li-a[value=" + value + "]").parent().addClass(
					"horizon-comboPanel-ul-li_selected");
		var options = $.data(jq, "horizonComboPanel").options;
		var data = $.data(jq, "horizonComboPanel").data;
		//重新设置combobox值（单选/多选）
		if (options.multiple) {
			var values = $(jq).combo("getValues");//获取当前值
			for ( var i = 0; i < values.length; i++) {
				if (values[i] == value) {
					return;//若指定值已经在当前值中，则不作任何处理
				}
			}
			values.push(value);//若指定值不在当前值中，则将指定值添加到combobox值数组中
			setValues(jq, values);
		} else {
			setValues(jq, [ value ]);
		}
		for ( var i = 0; i < data.length; i++) {
			var optionsTemp = data[i].options;
			
			for( var j = 0; j < optionsTemp.length; j++) {
				if (optionsTemp[j][options.valueField] == value) {
					options.onSelect.call(jq, optionsTemp[j]);
					return;
				}
			}
		}
	};
	/**
	 * 取消选择指定值的选项
	 * @param {Object} jq
	 * @param {Object} value
	 * @return {TypeName} 
	 */
	function unselectByValue(jq, value) {
		if(!value){
			return;
		}
		var panel = $(jq).combo("panel");
		panel.find(".horizon-comboPanel-ul-li-a[value=" + value + "]").parent().removeClass(
				"horizon-comboPanel-ul-li_selected");
		var options = $.data(jq, "horizonComboPanel").options;
		var data = $.data(jq, "horizonComboPanel").data;
		var values = $(jq).combo("getValues");
		for ( var i = 0; i < values.length; i++) {
			if (values[i] == value) {
				values.splice(i, 1);
				setValues(jq, values);
				break;
			}
		}
		for ( var i = 0; i < data.length; i++) {
			var optionsTemp = data[i].options;
			
			for( var j = 0; j < optionsTemp.length; j++) {
				if (optionsTemp[j][options.valueField] == value) {
					options.onUnselect.call(jq, optionsTemp[j]);
					return;
				}
			}
		}
	};
	/**
	 * 设置horizonComboPanel的值（数组）
	 * @param {Object} jq
	 * @param {Object} values
	 * @param {Object} single 是否单选（这个参数感觉没完全吃透）
	 */
	function setValues(jq, values, single) {
		var options = $.data(jq, "horizonComboPanel").options;
		var data = $.data(jq, "horizonComboPanel").data;//获取horizonComboPanel的数据
		
		var panel = $(jq).combo("panel");
		panel.find(".horizon-comboPanel-ul-li_selected").removeClass(
				"horizon-comboPanel-ul-li_selected");//去掉当前值选项的选中样式
		var vv = [], ss = [];
		for ( var k = 0; k < values.length; k++) {
			var v = values[k];
			var s = v;
			
			for ( var i = 0; i < data.length; i++) {
				var breakFlag = false;
				var optionsTemp = data[i].options;
				
				for( var j = 0; j < optionsTemp.length; j++) {
					if (optionsTemp[j][options.valueField] == v) {
						s = optionsTemp[j][options.textField];
						breakFlag = true;
						break;
					}
				}
				if(breakFlag) {
					break;
				}
			}
			vv.push(v);
			ss.push(s);
			panel.find(".horizon-comboPanel-ul-li-a[value=" + v + "]").parent().addClass(
					"horizon-comboPanel-ul-li_selected");
		}
		$(jq).combo("setValues", vv);
		if (!single) {
			$(jq).combo("setText", ss.join(options.separator));//多选，使用分隔符
		}
	};
	/**
	 * 加载数据
	 * @param {Object} jq
	 * @param {Object} data
	 * @param {Object} single 是否单选（这个参数感觉没完全吃透）
	 * @memberOf {TypeName} 
	 * @return {TypeName} 
	 */
	function loadData(jq, data, single) {
		var options = $.data(jq, "horizonComboPanel").options;
		var panel = $(jq).combo("panel");
		$.data(jq, "horizonComboPanel").data = data;//将数据赋给horizonComboPanel
		var values = $(jq).horizonComboPanel("getValues");
		panel.empty();//清空下拉面板所有选项
		
		var panelTempDiv = '<div class="horizon-comboPanel-body">';
		panelTempDiv += '<div class="horizon-comboPanel-body-tabs"></div>';
		panelTempDiv += '<div class="horizon-comboPanel-foot">';
		panelTempDiv += '<span class="horizon-comboPanel-foot-sp_right">';
		panelTempDiv += '<a class="horizon-comboPanel-foot-sp_right-btn_clear">清空</a>&nbsp;&nbsp;<a';
		panelTempDiv += ' class="horizon-comboPanel-foot-sp_right-btn_close">关闭</a>';
		panelTempDiv += '</span></div></div>';
		var item = $(panelTempDiv).appendTo(panel);//添加选项

		var tabsDom = panel.find(".horizon-comboPanel-body-tabs");
		var clearBtnDom = panel.find(".horizon-comboPanel-foot-sp_right-btn_clear");
		var closeBtnDom = panel.find(".horizon-comboPanel-foot-sp_right-btn_close");
		// 初始化tabs
		tabsDom.tabs({
			height:184,
			width:498,
			border:false,
			plain:true
		});
		// 初始化清空按钮
		clearBtnDom.linkbutton().click(function() {
			$(jq).horizonComboPanel("clear");
		});
		// 初始化关闭按钮
		closeBtnDom.linkbutton().click(function() {
			$(jq).combo('hidePanel');
		});
		// 循环数据，给下拉面板添加选项
		for ( var i = 0; i < data.length; i++) {
			
			var typeTemp = data[i]["type"];
			var optionsTemp = data[i].options;
			
			//循环数据，给下拉面板添加选项
			var ulTemp = '<ul class="horizon-comboPanel-ul">';
			for( var j = 0; j < optionsTemp.length; j++) {
				// var attributes = optionsTemp[j]["attributes"];// 扩展数据集合
				var litemp = "<li class='horizon-comboPanel-ul-li'><a class='horizon-comboPanel-ul-li-a' href='javascript:void(0);' value='" + optionsTemp[j][options.valueField] + "'>" + optionsTemp[j][options.textField] + "</a></li>";
				var v = optionsTemp[j][options.valueField];
				// 若选项定义为默认选中
				if (optionsTemp[j]["selected"] && $.inArray(v, values) == -1) {
					values.push(v);//讲默认选中的值加入到combobox值数组values中
				}
				ulTemp += litemp;
			}
			
			ulTemp += '</ul>';
			// 添加 tab 标签
			tabsDom.tabs('add', {
				title : typeTemp,
				content : ulTemp,
				closable : false
			});
		}

		//设置默认选中值数组values（单选/多选）
		if (options.multiple) {
			setValues(jq, values, single);
		} else {
			if (values.length) {
				setValues(jq, [ values[values.length - 1] ], single);
			} else {
				setValues(jq, [], single);
			}
		}
		options.onLoadSuccess.call(jq, data);// 触发onLoadSuccess事件
		// 给下拉面板选项注册hover、click事件
		$(".horizon-comboPanel-ul-li-a", panel).hover(function() {
			$(this).parent().addClass("horizon-comboPanel-ul-li_hover");
		}, function() {
			$(this).parent().removeClass("horizon-comboPanel-ul-li_hover");
		}).click(function() {
			var selectItem = $(this);//单击选中的选项
			if (options.multiple) {
				if (selectItem.parent().hasClass("horizon-comboPanel-ul-li_selected")) {
					unselectByValue(jq, selectItem.attr("value"));
				} else {
					selectByValue(jq, selectItem.attr("value"));
				}
			} else {
				selectByValue(jq, selectItem.attr("value"));
				$(jq).combo("hidePanel");//单选时，选中一次就隐藏下拉面板
			}
		});
	};
	/**
	 * 重新加载数据
	 * @param {Object} jq
	 * @param {Object} url
	 * @param {Object} paramData 参数
	 * @param {Object} single 是否单选（这个参数感觉没完全吃透）
	 * @memberOf {TypeName} 
	 * @return {TypeName} 
	 */
	function reloadData(jq, url, paramData, single) {
		var options = $.data(jq, "horizonComboPanel").options;
		if (url) {
			options.url = url;
		}

		if (!options.url) {
			return;
		}

		paramData = paramData || {};
		$.ajax( {
			type : options.method,
			url : options.url,
			dataType : "json",
			data : paramData,
			success : function(json) {
				loadData(jq, json, single);
			},
			error : function(e) {
				options.onLoadError.apply(this, arguments);
			}
		});
	};
	function create(jq) {
		var options = $.data(jq, "horizonComboPanel").options;
		$(jq).addClass("horizonComboPanel-f");
		$(jq).combo($.extend( {}, options, {
			onShowPanel : function() {
				// 切换到相应的位置·默认选中第一个tab start{
				scrollToFirst(jq, 0);
				// 切换到相应的位置 end}
				options.onShowPanel.call(jq);//响应onShowPanel事件
			}
		}));
	};

	/**
	 * 实例化combobox或方法调用
	 * @param {Object} options 若为string则是方法调用，否则实例化组件
	 * @param {Object} param 方法参数
	 * @memberOf {TypeName} 
	 * @return {TypeName} 
	 */
	$.fn.horizonComboPanel = function(options, param) {
		if (typeof options == "string") {
			var fn = $.fn.horizonComboPanel.methods[options];
			if (fn) {
				return fn(this, param);
			} else {
				return this.combo(options, param);
			}
		}
		options = options || {};
		return this.each(function() {
			var data = $.data(this, "horizonComboPanel");
			if (data) {
				$.extend(data.options, options);
			} else {
				data = $.data(this, "horizonComboPanel", {
					options : $.extend( {}, $.fn.horizonComboPanel.defaults,
							$.fn.horizonComboPanel.parseOptions(this), options)
				});
			}
			create(this);
			if (data.options.data) {
				loadData(this, data.options.data);
			}
			reloadData(this);
		});
	};
	/**
	 * 方法注册
	 * @param {Object} jq
	 * @return {TypeName} 
	 */
	$.fn.horizonComboPanel.methods = {
		options : function(jq) {
			return $.data(jq[0], "horizonComboPanel").options;
		},
		getData : function(jq) {
			return $.data(jq[0], "horizonComboPanel").data;
		},
		setValues : function(jq, values) {
			return jq.each(function() {
				setValues(this, values);
			});
		},
		setValue : function(jq, value) {
			return jq.each(function() {
				setValues(this, [ value ]);
			});
		},
		clear : function(jq) {
			return jq.each(function() {
				$(this).combo("clear");//清除值
				var panel = $(this).combo("panel");
				panel.find(".horizon-comboPanel-ul-li_selected").removeClass("horizon-comboPanel-ul-li_selected");//去掉当前值选项的选中样式
			});
		},
		loadData : function(jq, data) {
			return jq.each(function() {
				loadData(this, data);
			});
		},
		reload : function(jq, url) {
			return jq.each(function() {
				reloadData(this, url);
			});
		},
		select : function(jq, value) {
			return jq.each(function() {
				selectByValue(this, value);
			});
		},
		unselect : function(jq, value) {
			return jq.each(function() {
				unselectByValue(this, value);
			});
		}
	};
	/**
	 * class声明式定义属性转化为options
	 * @param {Object} target DOM对象
	 * @return {TypeName} 
	 */
	$.fn.horizonComboPanel.parseOptions = function(target) {
		var t = $(target);
		return $.extend( {}, $.fn.combo.parseOptions(target),$.parser.parseOptions(target,["valueField","textField","mode","method","url"]));
	};
	/**
	 * 默认参数设置
	 * @memberOf {TypeName} 
	 */
	$.fn.horizonComboPanel.defaults = $.extend( {}, $.fn.combo.defaults, {
		valueField : "value",
		textField : "text",
		panelWidth:500,
		panelHeight:222,
		mode : "local",//加载值的方式（local:本地;remote:服务）
		method : "post",
		url : null,
		data : null,
		onLoadSuccess : function() {
		},
		onLoadError : function() {
		},
		onSelect : function(record) {
		},
		onUnselect : function(record) {
		}
	});
})(jQuery);
