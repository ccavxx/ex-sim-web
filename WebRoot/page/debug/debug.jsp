<%@ page language="java" pageEncoding="utf-8"%>
<style>
<!--
	.lineSelect{
		background-color: #FF0000;
	}
-->
</style>
<div id="sourceTree" class="easyui-layout" fit="true">
	<div data-options="region:'north',height:35,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" class="sim">
		<form id="loginForm" class="form-inline" style="padding: 3px;margin: 0px;" onsubmit="return false" onkeydown="att.enterHandler(event, att.remoteLogin)">
			<span>主机：</span><input id="debug_host" type="text" class="input-meduium" value="127.0.0.1" autocomplete="off">
			<span>端口：</span><input id="debug_port" type="text" class="input-mini" value="8787" autocomplete="off">
			<button type="button" class="btn" onclick="debug.connect()">连接</button>
			<button type="button" class="btn" onclick="debug.disconnect()">断开</button>
		</form>
	</div>
	<div data-options="region:'west',
					split:true,
					headerCls:'layoutWHeader',
					bodyCls:'layoutWBody'" style="width:250px;" title="源代码">
					
		<div class="easyui-accordion" data-options="fit:true">
			<div title="最近打开文件">
				<ul id="debug_recent_files_tree" class="easyui-tree sim-high-tree"
				    data-options="url:'/sim/debug/recentOpenFiles',animate:true,
				    			  onDblClick:debug.openRecentFile"/>
			</div>
			<div title="源程序目录">
				<ul id="debug_source_tree" class="easyui-tree sim-high-tree"
				    data-options="url:'/sim/debug/listSource',animate:true,onBeforeLoad:debug.rebuildRequestParam,
				    			  onContextMenu:debug.createSourceMenu,
				    			  onDblClick:debug.openFile"></ul>
			</div>
		</div>			
	</div>
	<div data-options="region:'center',border:false,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" class="sim" >
		<div id="debug_source_file_tab" class="easyui-tabs" data-options="fit:true,onLoad:debug.sourceFileOpenHandler">
			<div title="说明">
				
			</div>
		</div>
	</div>
	<div data-options="region:'east',collapsed:'true',border:false,width:200,split:true,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" 
		 class="sim" title="Class文件">
		<ul id="debug_class_tree" class="easyui-tree sim-high-tree"
		    data-options="url:'/sim/debug/listClass',onBeforeLoad:debug.rebuildRequestParam,animate:true,
		    			  onContextMenu:debug.createClassMenu"></ul>
	</div>
	<div data-options="region:'south',height:200,split:true" class="sim">
		<div class="easyui-layout" fit="true">
			<div id="debug_thread_tools">
				<a href="javascript:void(0)" class="icon-export" title="进入" onclick="debug.step('stepInto')"/>
				<a href="javascript:void(0)" class="icon-stop" title="单步跳过" onclick="debug.step('stepOver')"/>
				<a href="javascript:void(0)" class="icon-import" title="返回" onclick="debug.step('stepOut')"/>
				<a href="javascript:void(0)" class="icon-start" title="继续" onclick="debug.step('stepContinue')"/>
			</div>
			<div data-options="region:'center',headerCls:'layoutWHeader',bodyCls:'layoutWBody',tools:'#debug_thread_tools'" title="线程信息" class="sim">
				<ul id="debug_thread_tree" class="easyui-tree sim-high-tree" data-options="onSelect:debug.threadSelectHandler"/>
			</div>
			<div data-options="region:'east',split:true,width:500,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" title="变量信息" class="sim">
				<table id="debug_variable_tree" class="easyui-treegrid sim-high-tree" 
					   data-options="idField:'name',treeField:'value',fitColumns:true">
					<thead>  
				        <tr>  
				            <th data-options="field:'name',width:300">名称</th>  
				            <th data-options="field:'value',width:700,align:'left'">值</th>  
				        </tr>  
				    </thead>
				</table>
			</div>
		</div>
	</div>
	
	<div id="debug_class_menu" class="easyui-menu" style="width:120px;">
        <div onclick="debug.decompile()">反编译</div>
    </div>
	 <div id="debug_source_menu" class="easyui-menu" style="width:120px;">
        <div onclick="debug.refreshSource()">刷新</div>
    </div>
</div>
<script type="text/javascript" src="/js/sim/debug/debug.js"></script>