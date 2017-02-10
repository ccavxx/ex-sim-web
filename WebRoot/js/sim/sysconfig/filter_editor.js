	var nodeIdCounter = 1 ;
	var treerootdata = [{
			id : 0,
			text : '当前过滤器',
			iconCls : 'conditions-tab',
			attributes:{increase:0,reduce:99,orders:"0"}
		}
	];
	function filterTreeLoadSuccess(node,data){
		var filterSql = $("#filter_editor_seletor_text").val() ;
		if(filterSql == "" || data[0].id != 0){
			return ;
		}
		var tree = $("#filter_etitor_tree") ;
		var param = {
				filterSql : filterSql,
				_time:new Date().getTime()
		}
		$.post("/sim/LogFilterRule/parseFilterSql",param,function(result){
			if(result.children && result.children.length > 0){
				buildFilterTree(result.children,tree.tree("getRoot"),tree) ;
			}
		},"json") ;
	}
	function buildFilterTree(nodes,parent,tree){
		if(nodes.length > 0){
			for(var i=0;i<nodes.length;i++){
				var node = nodes[i] ;
				if(isCondition(node)){
					appendCondition(node.text, parent, tree) ;
				}else if(isOperator(node)){
					appendOperator(node.text.toLowerCase(), parent, tree) ;
				}else if(isBracket(node) || isNotOperator(node)){
					var createNodeId = appendOperator(node.text.toLowerCase(), parent, tree) ;
					buildFilterTree(node.children, tree.tree("find",createNodeId), tree);
				}
			}
		}
	}
	/**
	 * 初始化过滤器树
	 */
	$('#filter_etitor_tree').tree({
		data : treerootdata,
		lines : true,
		animate : true,
		onLoadSuccess:filterTreeLoadSuccess,
		onContextMenu : function (e, node) {
			e.preventDefault();
			//如果是根节点则不显示删除按钮
			if (node.id == 0)
				$('#filter_etitor_tree_menu_remove').hide();
			else 
				$('#filter_etitor_tree_menu_remove').show();
			// select the node
			$(this).tree('select', node.target);
			// display context menu
			$('#filter_etitor_tree_menu').menu('show', {
				left : e.pageX,
				top : e.pageY
			});
		}
	});
	
	/**
	 * 初始化日期时间控件
	 */
	$('#filter_editor_form .form-datetime').datetimepicker({
		language : 'zh-CN',
		format : 'yyyy-mm-dd hh:ii:ss',
		autoclose : true,
		todayBtn : true
	});

	/**
	 * 向过滤器树插入运算符,运算符节点只能插入到根节点或运算符节点下
	 */
	function operatorClickHandler(event) {
		var tree = $('#filter_etitor_tree') ;
		var seltreenode = tree.tree('getSelected');
		if (!seltreenode){
			seltreenode = tree.tree('getRoot') ;
		}
		if(seltreenode.id == 0){
			appendOperator(event.data, seltreenode, tree) ;
		}else{
			insertOperator(event.data, seltreenode,tree) ;
		}
		selectCreateNode(seltreenode, tree) ;
		$("#filter_editor_seletor_text").val(getSelectorText(tree)) ;
	}
	
	function appendOperator(text,parent,tree){
		if(!tree){
			tree = $('#filter_etitor_tree') ;
		}
		var attr = parent.attributes ; 
		attr.increase = attr.increase+1 ;
		var iconClsSuffix = text == "()" ? "bracket" : text ;
		tree.tree('append', {
			parent : parent.target,
			data : [{
					id:++nodeIdCounter,
					iconCls : 'conditions-' + iconClsSuffix,
					text : new String(text).toUpperCase(),
					attributes:{increase:0,reduce:99,orders:attr.orders+"_"+attr.increase,parentId:parent.id,text:text}
				}
			]
		});
		return nodeIdCounter ;
	}
	
	function insertOperator(text,afterNode,tree){
		var attr = afterNode.attributes ;
		attr.reduce = attr.reduce -1 ;
		if(!tree){
			tree = $('#filter_etitor_tree') ; 
		}
		var iconClsSuffix = text == "()" ? "bracket" : text ;
		tree.tree('insert', {
			after : afterNode.target,
			data : [{
					id:++nodeIdCounter,
					iconCls : 'conditions-' + iconClsSuffix,
					text : new String(text).toUpperCase(),
					attributes:{increase:0,reduce:99,orders:attr.orders+"_"+attr.reduce,parentId:attr.parentId,text:text}
				}
			]
		});
		return nodeIdCounter ;
	}
	function appendCondition(text,parent,tree){
		if(!tree){
			tree = $('#filter_etitor_tree') ;
		}
		var attr = parent.attributes ; 
		attr.increase = attr.increase+1 ;
		tree.tree('append', {
			parent : parent.target,
			data : [{
					id:++nodeIdCounter,
					iconCls : 'conditions-condition',
					text : text.replace(/</g,"&lt;").replace(/>/g,"&gt;"),
					attributes:{increase:0,reduce:99,orders:attr.orders+"_"+attr.increase,parentId:parent.id,text:text}
				}
			]
		});
		return nodeIdCounter ;
	}
	function insertCondition(text,afterNode,tree){
		if(!tree){
			tree = $('#filter_etitor_tree') ;
		}
		var attr = afterNode.attributes ;
		attr.reduce = attr.reduce -1 ;
		tree.tree('insert', {
			after : afterNode.target,
			data : [{
					id:++nodeIdCounter,
					iconCls : 'conditions-condition',
					text : text.replace(/</g,"&lt;").replace(/>/g,"&gt;"),
					attributes:{increase:0,reduce:99,orders:attr.orders+"_"+attr.reduce,parentId:attr.parentId,text:text}
				}
			]
		});
		return nodeIdCounter ;
	}
	/**
	 * 向过滤器树插入()
	 */
	function bracketOrNotClickHandler(event) {
		var tree = $('#filter_etitor_tree') ;
		var seltreenode = tree.tree('getSelected');
		//如果是根节点或者是运算符节点则将新运算符插入到此节点内
		if (!seltreenode){
			seltreenode = tree.tree('getRoot') ;
		}
		if(seltreenode.id == 0){
			appendOperator(event.data, seltreenode, tree) ;
			selectCreateNode(seltreenode, tree) ;
		}else if(isOperator(seltreenode)){
			insertOperator(event.data, seltreenode,tree) ; 
			selectCreateNode(seltreenode, tree) ;
		}else{//selectCreateNode不能移到外面，而且必须在insertOperator('and')以前，否则无法选中创建的结点
			insertOperator(event.data, seltreenode,tree) ;
			selectCreateNode(seltreenode, tree) ;
			insertOperator("and", seltreenode,tree) ;
		}
	}
	function selectCreateNode(currentSelectNode,tree){
		var nextNode ;
		if(currentSelectNode.id == 0){
			nodes = getSonAndOrder(tree.tree('getChildren',currentSelectNode.target), currentSelectNode.id) ;
			nextNode = nodes[nodes.length-1] ;
		}else{
			nextNode = getNextNode(currentSelectNode, tree) ;
		}
		tree.tree('select',nextNode.target) ;
	}
	function isOperator(selectNode){
		var text = selectNode.text ;
		if(text == "AND" || text == "OR"){
			return true ;
		}
		return false ;
	}
	function isNotOperator(selectNode){
		if(selectNode.text == "NOT"){
			return true ;
		}
		return false ;
	}
	function isBracket(selectNode){
		var text = selectNode.text ;
		if(text == "()"){
			return true ;
		}
		return false ;
	} 
	function isCondition(selectNode){
		if(isOperator(selectNode) || isNotOperator(selectNode) || isBracket(selectNode)){
			return false ;
		}
		return true ;
	}
	function getPreNode(node,tree){
		var nodes = getSiblingNode(node,tree) ;
		var nodeIndex = $.inArray(node,nodes) ;
		if(nodeIndex == 0){
			return null ;
		}
		return nodes[nodeIndex-1] ;
	}
	
	function getNextNode(node,tree){
		var nodes = getSiblingNode(node,tree) ;
		var nodeIndex = $.inArray(node,nodes) ;
		if(nodeIndex == nodes.length -1){
			return null ;
		}
		return nodes[nodeIndex+1] ;
	}
	
	function getSiblingNode(node,tree){
		if(!tree){
			tree = $('#filter_etitor_tree') ;
		}
		var children = tree.tree('getChildren',tree.tree('getParent',node.target).target) ;
		return getSonAndOrder(children,node.attributes.parentId) ;
	}
	
	function getSonAndOrder(children,parentId){
		if(!children){
			return null ;
		}
		var nodes = $.map(children,function(item){
			if(item.attributes.parentId == parentId){
				return item ;
			}
			return null ;
		}) ;
		nodes.sort(function(node1,node2){
			var order1 = node1.attributes.orders.split("_") ;
			var order2 = node2.attributes.orders.split("_") ;
			var loopTimes = order1.length > order2.length ? order1.length : order2.length ;
			for(var i=0;i<loopTimes;i++){
				var o1 = parseInt(order1[i]) ;
				var o2 = parseInt(order2[i]) ; 
				if( o1 > o2 ){
					return 1 ;
				}else if(o1 < o2){
					return -1
				}
			}
			return order1.length > order2.length ? 1 : -1;
		}) ;
		return nodes ;
	}
	/**
	 * 创建表单验证实例
	 */
	var filter_editor_form_validation = $('#filter_editor_form').validator({
			theme : 'simple_right',
			stopOnError : true,
			timely : 2,
			showOk : "",
			rules : simHandler.rules
		}).data("validator");

	/**
	 * 构造表单数据，成为可添加到过滤器树中的数据
	 */
	function buildFormData() {
		//得到表单数据集合
		var formdataarray = $('#filter_editor_form').serializeArray();
		//所有表达式数组
		var _tmpexp = jQuery.grep(formdataarray, function (o, i) {
				return o.name.indexOf("_exp") != -1;
			});
		//属性值数组，过滤掉没有值的属性
		var _tmpval = jQuery.grep(formdataarray, function (o, i) {
				return o.name.indexOf("_exp") == -1 && o.value != "";
			});
		//表单无值则返回
		if (_tmpval.length == 0)
			return null;
		//将有值的表达式添加到属性值对象中[{name:'',value:'',operator:''}]
		for (var i in _tmpval) {
			var expname = _tmpval[i].name + "_exp";
			for (var j in _tmpexp) {
				if (expname == _tmpexp[j].name) {
					_tmpval[i].operator = _tmpexp[j].value; //运算符
					break;
				}
			}
			//根据dataType属性判断是否是整形，如果是则进行转换
			var datatype = $('#' + _tmpval[i].name).attr('dataType');
			_tmpval[i].datatype = datatype;
			if (datatype == "int" || datatype == "long") {
				_tmpval[i].value = Number(_tmpval[i].value);
			}
		}
		//得到需要添加到过滤器树的节点数组
		return $.map(_tmpval, function (n) {
			var t = n.name + " " + n.operator + " '" + n.value + "'";
			if (n.datatype == "int" || n.datatype == "long")
				t = n.name + " " + n.operator + " " + n.value;
			return {
				attributes : n,
				iconCls : 'conditions-condition',
				id : new Date().getTime(),
				text : t
			};
		});
	}
	
	function getSelectorText(tree){
		if(!tree){
			tree = $('#filter_etitor_tree') ;
		}
		var root = tree.tree('getRoot') ;
		var text = "SELECTOR(" ;
		var childText = getChildText(tree.tree('getChildren',root.target),root.id, tree) ;
		if(!childText){
			text = "SELECTOR(TRUE)" ;
		}else{
			text = "SELECTOR(" + childText + ")" ;
		}
		return text ;
	}
	function getChildText(children,parentId,tree){
		var text;
		var sonNodes = getSonAndOrder(children, parentId) ;
		if(!sonNodes){
			text = null ;
		}else{
			text = "" ;
			var nodeCount = sonNodes.length ;
			var nodeStack = new Array(2) ;
			for(var i=0;i<nodeCount;i++){
				var node = sonNodes[i] ;
				if(isCondition(node)){
					var preNode = nodeStack.pop() ;
					if(i==0){//第一个节点不需要操作符
						text = text + node.attributes.text;
					}else if(preNode){//条件前面有操作符
						text = text + " " + preNode.attributes.text + " " + node.attributes.text ;
					}else{//条件前面没有操作符，也不是第一个节点，跳过此节点继续往下
						continue ;
					}
				}else if(isNotOperator(node) || isBracket(node)){
					var prefix = isNotOperator(node) ? "NOT" : "" ;
					var subText = getChildText(tree.tree('getChildren',node.target),node.id,tree) ;
					if(subText){
						var operatorNode = nodeStack.pop() ;
						if(i == 0){
							text = text + prefix + "(" + subText + ")" ;
						}else if(operatorNode){
							text = text + " " + operatorNode.text + " " + prefix + "(" + subText + ")" ;
						}else{
							continue ;
						}
					}
				}else{
					nodeStack.push(node) ;
				}
			}
		}
		return text ;
	}
	/**
	 * 提交表单，添加过滤器树节点
	 */
	$('#filter_editor_form').submit(function () {
		//判断表单验证是否成功
		var isFormValid = filter_editor_form_validation.isFormValid();
		if (!isFormValid)
			return;
		var _newtreedata = buildFormData();
		//得到需要添加的节点数据
		if(!_newtreedata)
			return;
		var tree = $('#filter_etitor_tree') ;
		//获取选择的节点，即将新的节点插入到此节点下
		var seltreenode = tree.tree('getSelected');
		if (!seltreenode){
			var root = tree.tree('getRoot') ;
			for(var i=0;i<_newtreedata.length;i++){
				if(i != 0){
					appendOperator('and', root, tree) ;
				}
				appendCondition(_newtreedata[i].text, root, tree)
			}
		}else if(seltreenode.id == 0 || isBracket(seltreenode) || isNotOperator(seltreenode)){//选中的是根结点或者是括号节点
			var children = getSonAndOrder(tree.tree('getChildren',seltreenode),seltreenode.id) ;
			if(children.length != 0 && !isOperator(children[children.length-1])){
				appendOperator('and', seltreenode, tree) ;
			}
			for(var i=0;i < _newtreedata.length;i++){
				if(i != 0){
					appendOperator('and', seltreenode, tree) ;
				}
				appendCondition(_newtreedata[i].text, seltreenode, tree) ;
			}
		}else{
			//使用倒序插入数据，并且要先插入条件，再插入操作符
			for(var i=_newtreedata.length-1;i >= 0;i--){
				insertCondition(_newtreedata[i].text, seltreenode, tree) ;
				if(i == 0){
					if(!isOperator(seltreenode)){
						insertOperator('and', seltreenode, tree) ;
					}
				}else{
					insertOperator('and', seltreenode, tree) ;					
				}
			}
		}
		//将新的节点加入到操作符节点下
		//清除所有条件
		$('#filter_editor_form :input').not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
		//清除所有验证消息
		filter_editor_form_validation.cleanUp();
		$("#filter_editor_seletor_text").val(getSelectorText(tree)) ;
	});
	function inputEnterHandler(){
		
	}
	//点击AND按钮和右键AND按钮
	$('#filter_editor_and_btn,#filter_etitor_tree_menu_and').click('and', operatorClickHandler);
	//点击OR按钮和右键OR按钮
	$('#filter_editor_or_btn,#filter_etitor_tree_menu_or').click('or', operatorClickHandler);
	//点击NOT按钮和右键NOT按钮
	$('#filter_editor_not_btn,#filter_etitor_tree_menu_not').click('not', bracketOrNotClickHandler);
	$('#filter_editor_bracket_btn,#filter_etitor_tree_menu_bracket').click('()', bracketOrNotClickHandler);
	// 点击右键删除按钮
	$('#filter_etitor_tree_menu_remove,#filter_editor_remove_btn').click(function () {
		var tree = $('#filter_etitor_tree')
		var seltreenode = tree.tree('getSelected');
		if(!seltreenode) return ;
		var selectNode = getNextNode(seltreenode, tree) ;
		if(!selectNode){
			selectNode = getPreNode(seltreenode, tree) ;
			if(!selectNode){
				selectNode = tree.tree("getParent",seltreenode.target) ;
			}
		}
		tree.tree("select",selectNode.target) ;
		tree.tree('remove', seltreenode.target);
		$("#filter_editor_seletor_text").val(getSelectorText(tree)) ;
	});
	//点击插入按钮
	$('#filter_editor_insert_btn').click(function () {
		$('#filter_editor_form').submit();
	});
	//点击完成按钮
	$('#filter_editor_finish_btn').click(function () {
		var sql = $("#filter_editor_seletor_text").val() ;
		$.post("/sim/LogFilterRule/checkSelector?",{selector:sql},function(result){
			if(result.status){
				$("textarea[name='filterSql']").val(sql);
				$('#sysconfig_filter_dialog').panel('close');
			}else{
				showErrorMessage("无效的过滤条件") ;
			}
		},"json") ;
	});	
	$('#filter_editor_check_btn').click(function(){
		var selector = $("#filter_editor_seletor_text").val() ;
		$.post("/sim/LogFilterRule/checkSelector",{selector:selector},function(result){
			if(result.status){
				showAlertMessage("验证通过！") ;
			}else{
				showErrorMessage("无效的过滤条件") ;
			}
		},"json") ;
	}) ;
	