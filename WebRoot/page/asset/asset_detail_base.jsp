<%@ page language="java" pageEncoding="utf-8"%>
<style>
div.hidContextNobr{
	display:inline-block;
	width:120px;
	overflow:hidden;
	text-overflow:ellipsis;
}
</style>
<div id="adt_detail_base_tb${tabSeq}" >
	<a iconCls="icon-save" title="保存" onclick="adb.editSaveAssetProperty();" href="javascript:void(0);"></a>
	<a iconCls="icon-cancel" title="取消" onclick="adb.editCancelAssetProperty();" href="javascript:void(0);"></a>
</div>
<div id="adt_detail_base_flag${tabSeq}" style="display:none" ></div>
<div class="easyui-panel" style="padding:5px;"
 			data-options="title:'${asset.masterIp}',
			collapsible:false,
			border:false,
			fit:true,
			headerCls:'sim-panel-header'">
<table style="width:100%;padding:0;margin:0;">
	<tr>
		<td style="vertical-align:top;">
			<div id="showStyle${tabSeq}" style="height:258px;overflow:auto;">
				<form id="adt_form${tabSeq}" class="asset-baseinfo hgap3" style="margin-bottom: 0px;">
					<table id="adt_base_info${tabSeq}" loadComplete="false" state="collapsed" class="table table-bordered" style="margin-bottom:0px;">
						<tbody>
							<tr>
								<td style="width:53px;">IP地址</td>
								<td style="width:150px;">
							      <div class="editable" forValue="adt_form_name${tabSeq}" style="display:inline-block;line-height:20px">
							      	<div class="hidContextNobr">${asset.ip}</div>
							      </div>
								</td>
							</tr>
							<tr>
								<td style="width:53px;">名称</td>
								<td style="width:150px;">
							      <div class="editable" forValue="adt_form_name${tabSeq}" style="display:inline-block;line-height:20px">
							      	<div class="hidContextNobr easyui-tooltip" title="${asset.name}" ><nobr>${asset.name}</nobr></div>
							      </div>
								</td>
							</tr>
							<tr>
								<td>类型</td>
								<td>
							      <div style="display:inline-block;">${deviceTypeName}</div>
								</td>
							</tr>
							<tr>
								<td>管理节点</td>
								<td>
							      <div class="editable" forValue="aa_scanNodes${tabSeq}" style="display:inline-block;">${scanNode.ip}</div>
								</td>
							</tr>
							<tr>
								<td>操作系统</td>
								<td>
							      <div class="editable" forValue="adt_form_osName${tabSeq}" style="display:inline-block;">${asset.os.osName}</div>
								</td>
							</tr>
							<tr>
								<td>安全等级</td>
								<td>
							      <div class="editable" forValue="adt_form_safeRank${tabSeq}" style="display:inline-block;">${asset.safeRank}</div>
								</td>
							</tr>
							<tr>
								<td>联系人</td>
								<td>
							      <div class="editable" forValue="adt_form_linkman${tabSeq}" style="display:inline-block;">${asset.linkman}</div>
								</td>
							</tr>							
						</tbody>
					</table>
				</form>
			</div>
		</td>
	</tr>
	<input id="asset_ip${tabSeq}" type="hidden" name="ip" value="${asset.masterIp}">
</table>
</div>
<script src="/js/sim/asset/asset_detail_base.js"></script>