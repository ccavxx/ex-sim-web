$(function(){
	/**
	 * 设置关联分析规则基本属性
	 */
	 var _set_base_properties = function() {
		 if(eventRuleGroup.currentGroupDispatch) {
			 // 设置ID
			 if(!eventRuleGroup.cloneEventGroup) {
				 rule_group_prop_form.id.value = eventRuleGroup.currentGroupDispatch.group.groupId;
			 }
			 // 设置组名
			 rule_group_prop_form.rule_group.value = eventRuleGroup.currentGroupDispatch.group.groupName;
			 // 启用
			 if(eventRuleGroup.currentGroupDispatch.group.status) {
				 rule_group_prop_form.status.checked = true;
			 }
			 // 超时
			 if(eventRuleGroup.currentGroupDispatch.group.timeout) {
				 rule_group_prop_form.rule_timeout.value = eventRuleGroup.currentGroupDispatch.group.timeout;
			 }
			 // 分类
			 if(eventRuleGroup.currentGroupDispatch.group.cat1id) {
				var cat1id = eventRuleGroup.currentGroupDispatch.group.cat1id,
				 	cat2id = eventRuleGroup.currentGroupDispatch.group.cat2id;
				// 分类1
				$.each(rule_group_prop_form._r_cat1.options, function(i, option) {
				 	if(option.text == cat1id) {
						rule_group_prop_form._r_cat1.value = option.value;
				 		$("#_r_cat1_id").change();
				 	}
				});
				// 分类2
				$.each(rule_group_prop_form._r_cat2.options, function(i, option) {
				 	if(option.text == cat2id) {
				 		rule_group_prop_form._r_cat2.value = option.value;
				 	}
				});
			 }
			 // 优先级
			 rule_group_prop_form.level.value = eventRuleGroup.currentGroupDispatch.group.priority;
			 // 描述
			 if(eventRuleGroup.currentGroupDispatch.group.desc === null) {
				 rule_group_prop_form.descContent.value = "";
			 } else {
				 rule_group_prop_form.descContent.value = eventRuleGroup.currentGroupDispatch.group.desc;
			 }
		 }
	}
	/**
	 * 初始化知识库列表
	 */
	var _kn_onOpen = function() {
		knowledge._plugin_set["associateList"] = new Array();// IE
		if(knowledge.colums == null) {
			knowledge.initColum("kn_query_colums");
		}
		knowledge.refreshKnowledgeTable();
	}
	/**
	 * 知识库
	 */
	var _set_kn = function() {
		if(eventRuleGroup.currentGroupDispatch) {
			var kns = eventRuleGroup.currentGroupDispatch.knowledge;
			if(kns) {
				$.each(kns, function(i, kn) {
					 knowledge._plugin_set['associateList'].push(kn.id);
					 var $kn_title = $('<div class="alert alert-warning fade in" style="width:80px;float:left;margin:5px 0 0 5px;"></div>'),
					 $cl_btn = $('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
					 $kn_title.append($cl_btn).append($("<span></span>").html(kn.name).attr("id", "kn_" + kn.id));
					 var _knid = kn.id;
					 $kn_title.bind('closed.bs.alert', function() {
						 var _associateList = knowledge._plugin_set['associateList'];
						 for(var _k = 0; _k < knowledge._plugin_set['associateList'].length; _k++) {
							 if( parseInt(_associateList[_k] + "") == parseInt(_knid + "")) {
								 knowledge._plugin_set['associateList'].splice(_k, 1);
								 break;
							 }
						 }
					});
					$("#selected_knowledge_id").append($kn_title);
				});
			}
		}
	}
	/**
	 * 初始化关联告警方式
	 */
	var _initrResp = function() {
		$.getJSON("../../sim/sysconfig/event/allResponse?_time=" + new Date().getTime(),
			function(json){
				var responseIds = $("#_responseIds");
				var allresponseIds = $("#_allResponseId");
				// 清空被选择列
				responseIds.find("option").remove();
				allresponseIds.find("option").remove();
				// 更新选择框
				$.each(json, function(i,resp){
					var option = "<option value='" + resp.id + "'>" + resp.name + "</option>";
					allresponseIds.append(option);
				});
				// 更新整个规则时 ·如果当前已有选择的响应方式
				if(eventRuleGroup.currentGroupDispatch) {
					var resps = eventRuleGroup.currentGroupDispatch.resp;
					if(resps) {
						$.each(resps, function(index, tempResp) {
							var option = "<option value='" + tempResp.rid + "'>" + tempResp.rname + "</option>";
							responseIds.append(option);
							allresponseIds.find("option[value='" + tempResp.rid + "']").remove();
						});
					}
				}
			}
		);
		$("#_allResponseId").dblclick(function(){
			var selectedIndex = event_resp_choice_form.allResponse.selectedIndex,
				_option = event_resp_choice_form.allResponse.options[selectedIndex];
			event_resp_choice_form.responseIds.options.add(new Option(_option.text, _option.value));
			event_resp_choice_form.allResponse.options.remove(selectedIndex);
		});
		$("#_responseIds").dblclick(function() {
			var selectedIndex = event_resp_choice_form.responseIds.selectedIndex,
				_option = event_resp_choice_form.responseIds.options[selectedIndex];
			event_resp_choice_form.allResponse.options.add(new Option(_option.text, _option.value));
			event_resp_choice_form.responseIds.options.remove(selectedIndex);
		});
	}
	/**
	 * 验证初始化方法组
	 */
	var _validatorMethods = function() {
		$('#rule_group_rule_form_id').validator({
			theme: 'simple_right',
			showOk: "",
			rules: simHandler.rules
		});
		$('#rule_group_assc_form_id').validator({
			theme: 'simple_right',
			showOk: ""
		});
		
		$('#rule_group_prop_form_id').validator({// 基本信息验证初始化
			theme: 'simple_right',
			showOk: "",
			rules: $.extend({},simHandler.rules,{nospace:[/^([0-9a-zA-Z_\u4e00-\u9fa5-]+)$/,"请输入非空字符"]}),
			fields:{
				rule_group:'required;nospace;length[1~30];remote[/sim/sysconfig/event/validateEventRuleGroup, id];',
				rule_timeout:'required;range[1~1800];integer[+]',
				level:'required;',
				_r_cat1:'required(not, -1);',
				_r_cat2:'required(not, -1);',
				desContent:'length[~100]'
			}
		});
	}
	/**
	 * 锚点滚动监听方法组
	 */
	var _aScrollMethods = function() {
		// 描点切换
		$(".horizon-breadcrumb").find(".horizon-breadcrumb-li-a").die().live("click", function(){
			var it = this;
			$(it).parent().parent().children(".horizon-breadcrumb-li").addClass("active");
			$(it).parent(".horizon-breadcrumb-li").removeClass("active");
		});
		// 监听父级div滚动控制导航css样式
		var mainTop = $("#horizon-eventRuleGroupMainFormPanel-mainCenter").offset().top;
		$("#horizon-eventRuleGroupMainFormPanel-mainCenter").scroll(function(){
			var top_base = $("a[name='horizon-eventRule-base']").offset().top;
			var top_knowledge = $("a[name='horizon-eventRule-knowledge']").offset().top;

			var $it = null;
			var topBaseTop = top_base - mainTop;
			var topKnowledgeTop = top_knowledge - mainTop;
			if(topBaseTop >= -100 && topBaseTop <= 100) {
				$it = $("a[href='#horizon-eventRule-base']");
			}
			if(topKnowledgeTop >= -100 && topKnowledgeTop <= 100) {
				$it = $("a[href='#horizon-eventRule-knowledge']");
			} else if(topKnowledgeTop < -100) {
				$it = $("a[href='#horizon-eventRule-informWay']");
			}
			if($it !== null){
				$it.parent().parent().children(".horizon-breadcrumb-li").addClass("active");
				$it.parent(".horizon-breadcrumb-li").removeClass("active");
			}
		});
	}
	/**
	 * ===========
	 * start run =
	 * ===========
	 */
	function runMethods() {
		_set_base_properties();// 基础信息赋值
		ruleFlow.set_base_rule();// 设计组件赋值·基本规则
		ruleFlow.set_default_rule(0);// 设计组件赋值·设置默认选择第一个节点规则
		ruleFlow.set_base_assc();// 设计组件赋值·规则关联
		_initrResp();// 初始化告警方式
		_validatorMethods();
		_kn_onOpen();// 知识库列表初始化
		_set_kn();
		_aScrollMethods();
	}
	runMethods();
});
