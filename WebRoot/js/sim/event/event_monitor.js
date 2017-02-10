/**
 * 事件监控
 * @returns {RefreshEvent}
 */
function RefreshEvent() {
	this._EVENT_DATA_URL = "../../sim/eventMonitor/eventdata";
	this._COLUMNS_SET_URL = "../../sim/eventMonitor/columnSet";
	this.freq = 1000 * 5;
	this.intervalTag = -1;
	this.columns = [];
	this.priority = null;
	this.critical_event_data_url = this._EVENT_DATA_URL;
	this.param = {};
	this.cmenu = null;
	this.status = false;
}

/**
 * 加载数据
 * NAME:事件名
 * END_TIME:结束时间
 * CAT1_ID:事件分类1
 * CAT2_ID:事件分类2
 * PRIORITY:优先级
 * TRANS_PROTOCOL:传输协议
 * SRC_ADDRESS:源地址
 * SRC_PORT:源端口
 * DEST_ADDRESS:目的地址
 * DEST_PORT:目的端口
 */
RefreshEvent.prototype.getEventData = function() {
	var _this = this;
	// 如果清空未结束则不执行取值任务
	if(_this.status) {
		return;
	}
	$.ajax({
		type : "post",
		url : _this.critical_event_data_url,
		dataType : "json",
		data : _this.param,
		async : false,
		success : function(eventRows) {
			if (eventRows) {
				
				var $eventMonitorTable = $('#event_monitor_table_id');
				$.each(eventRows, function(i, n) {
					var	rows = $eventMonitorTable.datagrid("getRows");

				    // 将数据逆序插入
					$eventMonitorTable.datagrid("insertRow", {index:0, row:n});
					if(rows.length > 25) {// 清除页面部分老数据防止页面内存溢出
						
						var _c = function() {
							rows = $eventMonitorTable.datagrid("getRows");
							if(rows.length > 25) {
								$eventMonitorTable.datagrid("deleteRow", 25);
								_c();
							}
						};
						_c();
					}
				});
			}
		}
	});
}

/**
 * 启动循环刷新
 */
RefreshEvent.prototype.start = function() {
	var _this = this,
		f = function(){
			_this.getEventData();
		};
	_this.intervalTag = createTimer(f, _this.freq);
};

/**
 * 关闭循环刷新
 */
RefreshEvent.prototype.stop = function() {
	var _this = this;
	clearTimer(_this.intervalTag);
	_this.intervalTag = -1;
};

/**
 * 锁定按钮
 */
RefreshEvent.prototype.lockTheBtn = function(domId) {
	$("#" + domId).linkbutton('disable');
};

/**
 * 激活按钮
 */
RefreshEvent.prototype.startupTheBtn = function(domId) {
	$("#" + domId).linkbutton('enable');
};

/**
 * datagrid 表头右键菜单
 */
RefreshEvent.prototype.createColumnMenu = function() {
	var _this = this;
	var $eventMonitorTable = $('#event_monitor_table_id');
	
	_this.cmenu = $('<div/>').appendTo('body');
	_this.cmenu.menu({
		onClick : function(item) {
			
			if (item.iconCls == 'icon-ok') {
				
				$eventMonitorTable.datagrid('hideColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-empty'
				});
			} else {
				
				$eventMonitorTable.datagrid('showColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-ok'
				});
			}
		}
	});
	
	var fields = $eventMonitorTable.datagrid('getColumnFields');
	for ( var i = 0; i < fields.length; i++) {
		var field = fields[i];
		var col = $eventMonitorTable.datagrid('getColumnOption', field);
		_this.cmenu.menu('appendItem', {
			text : col.title,
			name : field,
			iconCls : 'icon-ok'
		});
	}
};

/**
 * 初始化 datagrid
 */
RefreshEvent.prototype.initDataGrid = function() {
	
	var _this = this;
	$('#event_monitor_table_id').datagrid({
		fit : true,
		title : '事件',
		iconCls : 'icon-grid',
		width : 'auto',
		height : 'auto',
		fitColumns : true,
		singleSelect : true,
		rownumbers : true,
		columns : _this.columns,
		onHeaderContextMenu : function(e, field) {
			e.preventDefault();
			if (!_this.cmenu) {
				_this.createColumnMenu();
			}
			_this.cmenu.menu('show', {
				left : e.pageX,
				top : e.pageY
			});
		},
		onDblClickRow : function(rowIndex, rowData) {}
	});
};

/**
 * 清空 datagrid 数据
 */
RefreshEvent.prototype.refresh = function() {
	
	var _this = this;
	var $datagrid = $('#event_monitor_table_id');
	var btnDomId = 'eventMonitorQuerySwitch';
	var _doRefresh = function(i) {
		
		var rowlen = $datagrid.datagrid("getRows").length;
		
		if(rowlen > 0) {// deleteRow
			$datagrid.datagrid("deleteRow", i);
			_doRefresh(i);
		}
	};
	
	_this.lockTheBtn(btnDomId);
	_this.status = true;
	_doRefresh(0);
	_this.status = false;
	_this.startupTheBtn(btnDomId);
};

/**
 * @deprecated
 * @param rowData
 */
RefreshEvent.prototype.showEventDetail = function(rowData) {
	
	var _rowdata = [];
	$.each(this.columns, function(i, cls) {
		
		$.each(cls, function(j, cs) {
			
			_rowdata.push({
				f1 : cs.title,
				f2 : "<input type='text' value='"
						+ (!rowData[cs.field] ? "" : rowData[cs.field])
						+ "' readonly/>"
			});
		});
	});
	
	var $event_detail = $("<div id='event_detail_id'></div>");
	$event_detail.append("<div style='width: 420px;margin: 0 auto;'><table id='_evt_detail_table_id' ></table></div>");
	$event_detail.dialog({
		
	    title: '事件详情',
	    width: 600,
	    closed: true,
	    cache: false,
	    resizable:true,
	    modal: true ,
		onClose : function() {
			
			$(this).dialog("destroy");
		}
	});
	
	$("#_evt_detail_table_id").datagrid({// 列表详情
		columns : [ [ {
			field : "f1",
			width : 100
		}, {
			field : "f2",
			width : 250
		} ] ],
		showHeader : false,
		width : 400,
		data : _rowdata
	});
	
	$("#event_detail_id").dialog("move",{top:$(document).scrollTop()+($(window).height()-600)*0.5});
	$event_detail.dialog("open");
};

/**
 * 创建一个 RefreshEvent 实例
 */
var refreshEvent = new RefreshEvent();

(function() {
	/**
	 * 默认启动刷新
	 */
	var doStart = function () {
		doEventMonitorStopMethod();
		refreshEvent.param["filter"] = "all" ;
		refreshEvent.start();
	};
	
	/**
	 * 初始化导航树
	 */
	var initMenu = function() {
		
		// 初始化树级菜单功能·停止-清除-启动
		var _restart = function(treeId) {
			// 定义tree们的id数组
			var treeArray = ["event_monitor_priority_menu_id", "event_monitor_rule_menu_id", "event_monitor_category_menu_id"];
			doEventMonitorStopMethod();
			doEventMonitorClean();
			doEventMonitorStart();
			$.each(treeArray, function(index, tempTreeId) {
				if(treeId != tempTreeId){
					var node = $("#" + tempTreeId).tree("getSelected");
					if(node != null){
						$(node.target).removeClass("tree-node-selected");
					}
				}
			});
		};
		
		// 优先级别
		$("#event_monitor_priority_menu_id").tree({
			url : '../../sim/eventMonitor/jsondata?json=evt_priority',
			onClick : function(node) {
				refreshEvent.param['filter'] = "PRIORITY," + node.id;
				_restart("event_monitor_priority_menu_id");
			}
		});
		
		// 规则名称菜单
		$("#event_monitor_rule_menu_id").tree({
			url : "../../sim/eventMonitor/eventRule",
			onClick : function(node) {
				refreshEvent.param['filter'] = "NAME," + node.text;
				_restart("event_monitor_rule_menu_id");
			}
		});
		
		// 分类
		$("#event_monitor_category_menu_id").tree({
			url : '/sim/event/eventCategory', 
			onClick : function(node) {
				if(!(node.attributes) || $.inArray(node.attributes.type, ["1", "2", "3"]) == -1) {
					return;
				}
				if(node.attributes.type == 1) {
					refreshEvent.param['filter'] = "CAT," + node.text;
				} else if(node.attributes.type == 2) {
					var pNode = $('#event_monitor_category_menu_id').tree('getParent', node.target)
					refreshEvent.param['filter'] = "CAT," + pNode.text + ":" + node.text;
				} else if(node.attributes.type == 3) {
					refreshEvent.param['filter'] = "NAME," + node.text;
				}
				_restart("event_monitor_category_menu_id");
			}
		});
	};
	
	/**
	 * 加载显示列集·列集的formatter
	 */
	var initColumnsCformatter = function(value, row, index) {
		var val = null;
		$.each(refreshEvent.priority, function(ind, p) {
			if(value == p['id']) {
				val = "<span class=\"priority" + value + "\">&nbsp;</span>";
				return;
			}
		});
		return val;
	}
	
	/**
	 * 加载显示列集
	 */
	var initColumns = function() {
		// 获得级别信息
		$.ajax({
			type : "post",
			url : "../../sim/eventMonitor/jsondata",
			dataType : "json",
			data : {"json" : "evt_priority"},
			async : false,
			success : function(data) {
				refreshEvent.priority = data;
			}
		});
		
		// 清空column、重新赋值
		refreshEvent.columns = [];
		$.ajax({
			type : "post",
			url : "../../sim/eventMonitor/jsondata",
			async : false,
			dataType : "json",
			data : {"json" : "evt_monitor_colums"},
			success : function(data) {
				$.each(data, function(i, c) {
					// 调整列集样式
					c['width'] = 35;
					var fieldTemp = c['field'];
					if(fieldTemp == 'PRIORITY') {
						
						c['formatter'] = initColumnsCformatter;
					} else if(fieldTemp == 'END_TIME') {
						
						c['width'] = 100;
					} else if(fieldTemp == 'NAME') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'SRC_ADDRESS') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'DEST_ADDRESS') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'DVC_ADDRESS') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'CAT1_ID') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'CAT2_ID') {
						
						c['width'] = 80;
					} else if(fieldTemp == 'DESCR') {
						c['width'] = 250;
						c['formatter'] = function(value,row,index){
							var spTemp = "<span title ='"+value+"'>"+ value +"</span>";
							return spTemp;
						} ;
					}
				});
				refreshEvent.columns.push(data);
			}
		});
	}
	
	initMenu();
	initColumns();
	refreshEvent.initDataGrid();
    doStart();
})();

/**
 * 清除数据
 */
function doEventMonitorClean() {
	refreshEvent.refresh();
}

/**
 * 开始刷新数据
 */
function doEventMonitorStart() {
	doEventMonitorStartBtn();
	refreshEvent.lockTheBtn("eventMonitorQuerySwitch");
	refreshEvent.start();
	refreshEvent.startupTheBtn("eventMonitorQuerySwitch");
}

/**
 * 开始刷新数据·切换按钮
 */
function doEventMonitorStartBtn() {
	var htmlFlag = $("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").html();
	if("停止刷新" == htmlFlag){
		return;
	}
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").html("停止刷新");
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").removeClass("icon-start");
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").addClass("icon-stop");
	$("#eventMonitorQuerySwitch").attr("onclick","doEventMonitorStop()");
}

/**
 * 停止刷新数据
 */
function doEventMonitorStop() {
	doEventMonitorStopBtn();
	refreshEvent.lockTheBtn("eventMonitorQuerySwitch");
	doEventMonitorStopMethod();
	refreshEvent.startupTheBtn("eventMonitorQuerySwitch");
}

/**
 * 停止刷新·停止方法
 */
function doEventMonitorStopMethod() {
	if (refreshEvent.stop) {
		refreshEvent.stop();
	}
}

/**
 * 停止刷新·切换按钮
 */
function doEventMonitorStopBtn() {
	var htmlFlag = $("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").html();
	if("启动刷新" == htmlFlag){
		return;
	}
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").html("启动刷新");
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").removeClass("icon-stop");
	$("#eventMonitorQuerySwitch>.l-btn-left>.l-btn-icon-left").addClass("icon-start");
	$("#eventMonitorQuerySwitch").attr("onclick", "doEventMonitorStart()");
}

/**
 * 设置刷新频率
 */
var _evtMonitorFreq = {"freq" : 5};
function setEventMonitorFreq() {
	
	var $freqbox = $("<div></div>"),
		freqSeleted = _evtMonitorFreq['freq'];
	
	$freqbox.dialog({
	    title : '设置自动刷新时间',
	    closed : false,
	    cache : false,
	    resizable : true,
	    width : 400,
	    height : 160,
	    iconCls : "icon-search",
	    href : '/page/event/evtRefFrqSetting.html',
	    modal : true ,
		onClose : function() {
			$(this).dialog("destroy");
		},
		onExpand : function() {
		},
		onOpen : function() {
		},
		buttons:[{
			text:'确定',
			handler:function(){
				freqSeleted = $("#event_monitor_freqtime_id").combobox("getValue");
				_evtMonitorFreq['freq'] = freqSeleted;
				
				doEventMonitorStopMethod();
				
				refreshEvent.freq = freqSeleted * 1000;
				
				doEventMonitorStart();
				
				$freqbox.dialog("destroy");
			}
		}]
	});
}
