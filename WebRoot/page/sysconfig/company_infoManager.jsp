<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<div class="easyui-panel" data-options="fit:true,title:'公司信息管理',headerCls:'sim-panel-header',bodyCls:'sim-panel-body'">
	<div class="alert alert-info">
		<strong>描述 </strong>
		产品名称、系统版权所有公司以及产品logo
	</div>
	<div class="row-fluid">
		<div class="span7">
			<form id="company_info" class="form-horizontal" style="font-size: 15px" enctype="multipart/form-data" method="post">
				<fieldset>
					<div class="control-group">
							<label class="control-label">产品名称：</label>
							<div class="controls">
								<input type="text" name="productName" id="productName"/>
							</div>
					</div>
					<div class="control-group">
							<label class="control-label">版权所有：</label>
							<div class="controls">
								<input type="text" name="companyName" id="companyName"/>
							</div>
					</div>
					<div class="control-group">
						<label class="control-label">产品logo：</label>
						<div class="controls">
							<input class="input-file horizon-fileupload" type="file" id="companyLogoFile" name="companyLogoFile"/>
						</div>
					</div>
					<div class="control-group">
						<div class="controls">
							<a class="easyui-linkbutton" iconCls="icon-apply" id="company_info_submit_button" href="javascript:importSubmit();">应用</a>&emsp;
							<a class="easyui-linkbutton" iconCls="icon-apply" id="company_info_submit_button" href="javascript:restoreDefault();">恢复默认</a>
						</div>
					</div>
				</fieldset>
			</form>
		</div>
		<div class="span5 well">
				<p class="text-info"><strong>产品名称：</strong>友情提示：<span style="color:red;">产品名称控制在20个字符以内！</span>显示效果在网页标题等处</p>
				<p class="text-info"><strong>版权公司：</strong>友情提示：<span style="color:red;">公司名称控制在20个字符以内！</span>显示效果在页脚处</p>
				<p class="text-info"><strong>产品logo：</strong>公司产品logo图片要求：<span style="color:red;">背景透明，宽在300px~500px之间、高在40px~51px之间，425px*51px为最佳，大小控制在1M以内!</span></p>	
				<p class="text-info"><strong>应用：</strong>点击应用后，即可看到修改后的效果</p>	
				<p class="text-info"><strong>恢复默认：</strong>将网页相应信息恢复为默认状态，即北京天融信相关信息</p>
		</div>
	</div>
</div>
<script src="/js/sim/sysconfig/company_info.js" type="text/javascript"></script>