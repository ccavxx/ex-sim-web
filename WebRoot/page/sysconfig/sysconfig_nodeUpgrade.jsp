<%@ page language="java" pageEncoding="utf-8"%>
<div class="easyui-layout sim" data-options="fit:true" >
	<div data-options="region:'center',title:'代理升级',headerCls:'sim-panel-header',bodyCls:'sim-panel-body'" >
		<div id="sysconf_nodeUpgrade_table_panel" class="easyui-panel" data-options="fit:true,border:false">
			<!-- 升级包列表 -->
			<table id="sysconf_nodeUpgrade_table" >
			    <thead>
			        <tr>
			            <th data-options="field:'name',width:10,sortable:true">升级包名称</th>
			            <th data-options="field:'creater',width:10,sortable:true">上传人</th>
			            <th data-options="field:'createDate',width:10,sortable:true">上传时间</th>
			            <th data-options="field:'description',width:15,sortable:true">描述</th>
			            <th data-options="field:'operation',width:10,align:'center',formatter:function(value,row,index){
												var result = '';
												if(row['delete']){
													var btn = $('<a></a>').addClass('node_upgrade_table_row_btn').attr('plain','true');
														btn.attr('iconCls','icon-remove').attr('title','删除').attr('onclick','simSysConfNodeUpgrade.removeNodeUpgradeFile(\''+row.id+'\',\''+row.name+'\')');
													result = btn[0].outerHTML;
												}
												return result;
			            	}">操作</th>
			        </tr>
			    </thead>
			</table>
			
		</div>
		<div id="sysconf_nodeUpgrade_table_toolbar" style="padding:2px 0 2px 5px">
			<a href="javascript:void(0)" class="easyui-linkbutton" data-options="iconCls:'icon-import'" onclick="javascript:simSysConfNodeUpgrade.openUploadFileDialog();">上传升级包</a>
			<a href="javascript:void(0)" class="easyui-linkbutton" data-options="iconCls:'icon-edit'" onclick="javascript:simSysConfNodeUpgrade.configNodeUpgradePlan();">设置升级计划</a>
		</div>
	</div>
	<div id="sysconf_nodeUpgrade_editor_dialog"></div>
</div>
<script src="/js/sim/sysconfig/sysconfig_nodeUpgrade.js" type="text/javascript"></script>
<script type="text/javascript">
	/**
	 * 删除节点升级包
	 */
	simSysConfNodeUpgrade.removeNodeUpgradeFile = function(rowid,fileName){
		if(rowid){
			$.messager.confirm("警告","您确定要删除这条记录？",function(r){
			    if (r){
			        $.getJSON("/sim/sysconfig/upgrade/deleteNodeUpgradeFile?_time=" + new Date().getTime(),{id:rowid,fileName:fileName},function(data){
			        	if(data.status){
			        		simSysConfNodeUpgrade.reloadNodeUpgradeTable();
			        	}
			        });
			    }   
			});			
		}
	}
</script>
