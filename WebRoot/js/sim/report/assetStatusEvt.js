/**
 * 
 */
var assReport={};

function divshow(divid){
	var divids=new Array("ass_evt_ba_div","ass_evt_leaf_div","ass_monit_ba_div","ass_monit_leaf_div");
	for(var i=0;i<divids.length;i++){
		try{
			if(divid!=divids[i]){
				$("#"+divids[i]).css("display","none");
			}
		}catch(er){
			continue;
		}
	}
	$("#"+divid).css("display","block");
}

assReport.cancelfunc= function(JObjId){
	var choosediv=$("#"+JObjId);
	choosediv.dialog('close');
	if('nodeTypeShowsdiv'===JObjId){
		$('#report-right').panel('refresh');
	}
};
assReport.refreshPanel= function(JObjId){
	var choose=$("#"+JObjId);
	choose.panel('refresh');
};
assReport.refreshRepRi= function(){
	assReport.refreshPanel('report-right');
};
assReport.onClickHandler=function(node){
	var topoId="";
	var nodeLevel="";
	try{
		topoId=node.attributes.topoId;
		nodeLevel=node.attributes.nodeLevel;
	}catch(e){topoId="";nodeLevel="";}
	if(nodeLevel=="0"){
		return;
	}
	var ass_rpt_tree=$("#assetevt_tree_id") ;
	var parent = ass_rpt_tree.tree("getParent",node.target) ;
	while(null!=parent){
		var parentpar = ass_rpt_tree.tree("getParent",parent.target) ;
		if(null==parentpar){
			break;
		}
		parent=parentpar;
	}
	if(null==parent){
		parent=node;
	}
	var params=null;
	if(node.type=="TRUNK"){
		params = "dvctype=Profession/Group"+"&nodeId="+node.attributes.scanNodeId+"&mstrptid=10000&screenWidth="+screen.availWidth;
		params+="&rootId="+parent.id+"&assGroupNodeId="+node.id+"&topoId="+topoId+"&nodeLevel="+nodeLevel;
		/*if(node.id=="0"||parent.id=="0"){
			divshow("ass_evt_ba_div");
		}else if(node.id=="1"||parent.id=="1"){
			divshow("ass_monit_ba_div");$("#chartContainer").css("display","none");
			return;
		}*/
		
	}else if(node.type=="LEAF"){
		params = "dvctype=Profession/Group/Asset"+"&nodeId="+node.attributes.scanNodeId+"&mstrptid=10001&screenWidth="+screen.availWidth;
		var ip="";$("#chartContainer").css("display","none");
		var nodeType=null;
		try{
			ip=node.attributes.ip;
			nodeType=node.attributes.deviceType;
		}catch(e){ip="";nodeType="";}
		params+="&rootId="+parent.id+"&dvcaddress="+ip+"&nodeType="+nodeType;
		/*if(node.id=="0"||parent.id=="0"){
			divshow("ass_evt_leaf_div");
		}else if(node.id=="1"||parent.id=="1"){
			divshow("ass_monit_leaf_div");
			return;
		}*/
	}
	var talStartTimeobj=$("#talStartTime");
	var talEndTimeobj=$("#talEndTime");
	
	if(undefined!=talStartTimeobj.val()
			&& null!=talStartTimeobj.val()
			&& undefined!=talEndTimeobj.val()
			&& null!=talEndTimeobj.val()){
		params+="&talStartTime="+talStartTimeobj.val()+"&talEndTime="+talEndTimeobj.val();
	}
	if(undefined!=$("#talTop").val()
			&& null!=$("#talTop").val()){
		params+="&talTop="+$("#talTop").val();
	}
	$('#report-right').panel('refresh',"/sim/topoReport/reportQuery?"+params);
};

assReport.dynmicReportQuery=function(){
	var params=null;
	var talStartTimeobj=$("#talStartTime");
	var talEndTimeobj=$("#talEndTime");
	
	params="dvctype=DynamicComprehensiveReport";
	if(undefined!=talStartTimeobj.val()
			&& null!=talStartTimeobj.val()
			&& undefined!=talEndTimeobj.val()
			&& null!=talEndTimeobj.val()){
		params+="&talStartTime="+talStartTimeobj.val()+"&talEndTime="+talEndTimeobj.val();
	}
	if(undefined!=$("#talTop").val()
			&& null!=$("#talTop").val()){
		params+="&talTop="+$("#talTop").val();
	}
	$('#report-right').panel('refresh',"/sim/topoReport/dynamicComprehensiveReport?"+params);
};

assReport.query = function(talCategory,mstId){
	var sTime = $('#talStartTime').val();
	var eTime = $('#talEndTime').val();
	var top = $('#talTop').val();
	
	if(null!=mstId && mstId.length>1){
		var ass_rpt_tree=$("#assetevt_tree_id") ;
		var node = ass_rpt_tree.tree("getSelected");
		var parent = ass_rpt_tree.tree("getParent",node.target) ;
		var topoId="";
		var nodeLevel="";
		try{
			topoId=node.attributes.topoId;
			nodeLevel=node.attributes.nodeLevel;
		}catch(e){topoId="";nodeLevel="";}
		if(nodeLevel=="0"){
			return;
		}
		while(null!=parent){
			var parentpar = ass_rpt_tree.tree("getParent",parent.target) ;
			if(null==parentpar){
				break;
			}
			parent=parentpar;
		}
		if(null==parent){
			parent=node;
		}
		var params=null;
		params="/sim/topoReport/reportQuery?";
		if(node.type=="TRUNK"){
			params+= "dvctype=Profession/Group"+"&nodeId="+node.attributes.scanNodeId+"&mstrptid=10000&screenWidth="+screen.availWidth;
			params+="&rootId="+parent.id+"&assGroupNodeId="+node.id+"&topoId="+topoId+"&nodeLevel="+nodeLevel;
			/*if(node.id=="0"||parent.id=="0"){
				divshow("ass_evt_ba_div");
			}else if(node.id=="1"||parent.id=="1"){
				divshow("ass_monit_ba_div");$("#chartContainer").css("display","none");
				return;
			}*/
		}else if(node.type=="LEAF"){
			params += "dvctype=Profession/Group/Asset"+"&nodeId="+node.attributes.scanNodeId+"&mstrptid=10001&screenWidth="+screen.availWidth;
			var ip="";$("#chartContainer").css("display","none");
			var nodeType=null;
			try{
				ip=node.attributes.ip;
				nodeType=node.attributes.deviceType;
			}catch(e){ip="";nodeType="";}
			
			params+="&rootId="+parent.id+"&dvcaddress="+ip+"&nodeType="+nodeType;
			/*if(node.id=="0"||parent.id=="0"){
				divshow("ass_evt_leaf_div");
			}else if(node.id=="1"||parent.id=="1"){
				divshow("ass_monit_leaf_div");
				return;
			}*/
		}
		params+="&talStartTime="+sTime+"&talEndTime="+eTime+"&talTop="+top;
		if(undefined!=talCategory && talCategory!=null){
			var talCategorys = talCategory.split(",");
			for(var i=0,len=talCategorys.length;i<len;i++){
				params+="&talCategory="+talCategorys[i];
			}
		}
		$('#report-right').panel('refresh',encodeURI(params));
	}else{//dynamicComprehensiveReport
		var params='/sim/topoReport/comprehensiveInformReport?dvctype=DynamicComprehensiveReport&reportType=comprehensiveReportIndex&screenWidth='+screen.availWidth;
		params+="&talStartTime="+sTime+"&talEndTime="+eTime+"&talTop="+top;
		if(undefined!=talCategory && talCategory!=null){
			var talCategorys = talCategory.split(",");
			for(var i=0,len=talCategorys.length;i<len;i++){
				params+="&talCategory="+talCategorys[i];
			}
		}
		$('#report-right').panel('refresh',encodeURI(params));
	}
	
} ;

assReport.exportReport = function (url,type,mstId,sTime,eTime,top){
	var _sTime = $("#talStartTime").val();
	var _eTime = $("#talEndTime").val();
	var _top = $("#talTop").val();
	if(_sTime==null||_sTime==""){
		_sTime = sTime;
	}
	if(_eTime==null||_eTime==""){
		_eTime = eTime;
	}
	if(_top==null||_top==""){
		_top = top;
	}
	var ass_rpt_tree=$("#assetevt_tree_id") ;
	var node = ass_rpt_tree.tree("getSelected");
	var parent = ass_rpt_tree.tree("getParent",node.target) ;
	var topoId="";
	var nodeLevel="";
	try{
		topoId=node.attributes.topoId;
		nodeLevel=node.attributes.nodeLevel;
	}catch(e){topoId="";nodeLevel="";}
	if(nodeLevel=="0"){
		return;
	}
	while(null!=parent){
		var parentpar = ass_rpt_tree.tree("getParent",parent.target) ;
		if(null==parentpar){
			break;
		}
		parent=parentpar;
	}
	if(null==parent){
		parent=node;
	}
	var params="";
	if(node.type=="TRUNK"){
		params+="&rootId="+parent.id+"&assGroupNodeId="+node.id+"&topoId="+topoId+"&nodeLevel="+nodeLevel;
		
	}else if(node.type=="LEAF"){
		var ip="";
		var nodeType=null;
		try{
			ip=node.attributes.ip;
			nodeType=node.attributes.deviceType;
		}catch(e){ip="";nodeType="";}
		params+="&rootId="+parent.id+"&dvcaddress="+ip+"&nodeType="+nodeType;
	}
	url+=params+"&exprpt="+type+"&mstrptid="+mstId+"&talTop="+_top+"&talStartTime="+_sTime+"&talEndTime="+_eTime+"&viewItem=1,2,3";
	window.open(encodeURI(url)) ;
};

assReport.configurationShowReport= function(){
	var panelchoose=$('#asset_report-main').layout('panel', 'center');//$('#report-right').panel('panel');
	var lefttree=$('#asset_report-main').layout('panel', 'west');
	if(!panelchoose)return;
	$(panelchoose).parent().css('position','relative');
	var width = $(panelchoose).width();
	var height = $(panelchoose).height()-30;
	var left = $(lefttree).width()+8;
	var nodeTypeShowsdiv=$("#nodeTypeShowsdiv");
	nodeTypeShowsdiv.css("visibility","visible");
	/*nodeTypeShowsdiv.dialog({
		title:false,
//    	top:30,
//    	left:left,
    	fit:true,
//		width:width,
//		height:height,
		inline:true,
		noheader:true,
		modal:true,
		shadow:false,
		border:false,
		style:{'padding':0,'border':0}
    });
	nodeTypeShowsdiv.dialog('expand',true);*/
	nodeTypeShowsdiv.dialog({modal: true,closable:true,width:700,height:400,title:'配置显示',onClose:assReport.refreshRepRi}).dialog('open');
	$("#reportshow_table").datagrid({
		url:'/sim/topoReport/showAllRoleNodeTypeShow',
    	selectOnCheck: true,
    	checkOnSelect: true
    });
};

assReport.assConfShowReport= function(){
	var panelchoose=$('#asset_report-main').layout('panel', 'center');//$('#report-right').panel('panel');
	var lefttree=$('#asset_report-main').layout('panel', 'west');
	if(!panelchoose)return;
	$(panelchoose).parent().css('position','relative');
	var width = $(panelchoose).width();
	var height = $(panelchoose).height()-30;
	var left = $(lefttree).width()+8;
	var assConfshowdiv=$("#assConfshowdiv");
	assConfshowdiv.css("visibility","visible");
	assConfshowdiv.dialog({
		title:false,
		href:'/page/report/assetConf.html',
    	top:30,
    	left:left,
//    	fit:true,
		width:width,
		height:height,
		inline:true,
		noheader:true,
		modal:true,
		shadow:false,
		border:false,
		style:{'padding':0,'border':0},
		buttons:[{
			text:'保存',
			handler:function(){
				assReport.chooseAndSaveAssConf();
			}
		},{
			text:'取消',
			handler:function(){
				assReport.cancelfunc('assConfshowdiv');
			}
		}
		]
    });
	assConfshowdiv.dialog('expand',true);
//	assConfshowdiv.dialog({modal: true,href:'/page/report/assetConf.html',closable:true,width:900,height:600,title:'配置显示',onClose:assReport.refreshRepRi}).dialog('open');
	$("#reportshow_table").datagrid({
		url:'/sim/topoReport/showAllRoleNodeTypeShow',
    	selectOnCheck: true,
    	checkOnSelect: true
    });
};

assReport.createShowReport= function(){
	var panelchoose=$('#asset_report-main').layout('panel', 'center');//$('#report-right');
	var lefttree=$('#asset_report-main').layout('panel', 'west');
	if(!panelchoose)return;
	$(panelchoose).parent().css('position','relative');
	var width = $(panelchoose).width();
	var height = $(panelchoose).height()-30;
	var left = $(lefttree).width()+8;
	var choosereportshowdiv=$("#choosereportshowdiv");
	choosereportshowdiv.dialog({
		title:false,
    	top:30,
		left:left,
		width:width,
		height:height,
		inline:true,
		noheader:false,
		modal:true,
		shadow:false,
		border:false,
		style:{'padding':0,'border':0}
    });
	
	$('#nodeType').combotree({   
	    url: '/sim/topoReport/showHasRoleAllowScanNodeType',
	    checkbox:true
	}); 
//    required: true,
	choosereportshowdiv.dialog('expand',true);
	
};
assReport.editreportShow=function(selRowIndex){
	var reportshow_table=$('#reportshow_table');
	var rows = reportshow_table.datagrid('getRows');
	var row = rows[selRowIndex];
	if (row) {
        $.ajax({
            url: '/sim/topoReport/updateNodeTypeShows',
            type: "POST",
            dataType: "json",
            data: {'idS': row.id,
            	'isShow':!row.isShow
            	},
            success: function(dats) {
            	reportshow_table.datagrid('reload');
            },
            error: function() {
            	showErrorMessage('无法加载');
            }
        });
    } 
};

assReport.changeShowreportState=function(statusubmit){
	var reportshow_table=$('#reportshow_table');
	var rows = reportshow_table.datagrid("getSelections");
	var ids=null;
	if(rows!=null && rows.length>0){
		ids='';
		for(var i=0;i<rows.length;i++){
			if(rows[i].isShow !== statusubmit){
				ids+=rows[i].id+',';
			}
		}
		if(ids.length>1){
			ids=ids.substring(0, ids.length-1);
		}
	}
	if (ids!=null && ids!='') {
		$.ajax({
            url: '/sim/topoReport/updateNodeTypeShows',
            type: "POST",
            dataType: "json",
            data: {'idS': ids,
            	'isShow':statusubmit
            	},
            success: function(dats) {
            	reportshow_table.datagrid('reload');
            	reportshow_table.datagrid("uncheckAll");
            },
            error: function() {
            	showErrorMessage('无法加载');
            }
        });
    } else {
    	showAlarmMessage('请选择可以操作的行操作');
    }
};

assReport.chooseAndSaveShowReport=function(){
	$('#choosereportform').form('submit', {
        url: '/sim/topoReport/saveOrUpdateNodeTypeShow',
        onSubmit: function() {
        	var scanNodeType=$('#nodeType');
        	var nodeTypeTree=scanNodeType.combotree('tree');
        	if(nodeTypeTree.tree('getRoots').length<1){
        		showErrorMessage("没有可供选择的其他类型!");
        		return false;
        	}
        	var selector=nodeTypeTree.tree('getSelected');
        	if(selector==null){
        		showErrorMessage("没有选择类型!");
        		return false;
        	}
            return true;//planReportObj.validatPlanReport("add");
        },
        success: function(result) {
            if(result==='true') {
            	showAlertMessage("新建成功!");
            	assReport.cancelfunc('choosereportshowdiv');
            	$('#reportshow_table').datagrid('reload');
            } else {
            	showErrorMessage("新建失败!");
            }
            
        }
    });
};

$(function(){
	
});