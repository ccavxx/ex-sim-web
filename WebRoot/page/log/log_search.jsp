<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/page/common/taglib.jsp" %>
<div class="tabbable tabs-top log-query">
	<ul class="nav nav-tabs">
	
		<!-- 根据列集group生成tab标签 创建ID -->
		<c:forEach var="group" items="${groups}" varStatus="stat">
			<c:if test="${stat.index == 0}"><li class="active"></c:if>
			<c:if test="${stat.index != 0}"><li></c:if>
			
			<!-- 这个href要和下面内容ID一一对应 -->
			<a href="#tab${group.id }" data-toggle="tab">${group.name }</a></li>
		</c:forEach>
	</ul>
	<div class="tab-content" >
	
		<form class="form-horizontal" id="receipt_time_form" style="margin-bottom: 0px;">
			<!-- 以下这5个条件全部是日志备份导入条件 -->
			<input type="hidden" id="query_deviceType" value="${deviceType }">
			<input type="hidden" id="query_nodeId" value="${nodeId }">
			<input type="hidden" id="query_host" value="${host }">
			<input type="hidden" id="query_maxDate" value="${maxDate }">
			<input type="hidden" id="query_minDate" value="${minDate }">
			<div class="control-group" style="margin-bottom: 5px;">
				<label class="control-label">接收时间</label>
				<div class="controls">
					<div class="input-prepend input-append">
					  <input class="input-medium" value="${minDate }" id="begin_time" type="text" readonly="readonly">
					  <span class="add-on">-</span>
					  <input class="input-medium"  value="${maxDate }" id="end_time" type="text"  readonly="readonly">
					</div>
				</div>
			</div>	
		</form>	
		
		<!-- 遍历所有列集 创建每个tab标签内容 -->
		<c:forEach var="group" items="${groups}" varStatus="stat">
		
			<!-- 亲 这里的ID要和前边标签页ID相同，否则不能切换标签 -->
			<c:if test="${stat.index == 0}"><div class="tab-pane active" id="tab${group.id }"></c:if>
			<c:if test="${stat.index != 0}"><div class="tab-pane" id="tab${group.id }"></c:if>
			
				<!-- 为每个标签页创建一个表单，分配不同的ID -->
				<form class="form-horizontal" id="tab${group.id }_form">
					<!-- 组ID -->
					<input type="hidden" name="groupId" value="${group.id }"/>
				
					<!-- 这里创建默认的过滤条件 即那些讨厌的filter -->
					<c:forEach var="filter" items="${group.filterList}">
						<input type="hidden" name="operator_${filter.name }" value="${filter.operatorName}"/>
						<input type="hidden" name="${filter.name }" value="${filter.value }"/>
					</c:forEach>
					
					<!-- 开始遍历字段集 -->
					<c:forEach var="field" items="${fieldset}">
					
						<!-- 下面6行代码的意思是判断当前字段是否属于这个列集group -->
						<c:set var="groupFlag" value="false" scope="page"></c:set>
						<c:forEach var="g" items="${fn:split(field.group,',')}">
							<c:if test="${g eq group.id}">
								<c:set var="groupFlag" value="true"></c:set>
							</c:if>
						</c:forEach>
						
						<!-- 如果为真 即表示当前字段属于这个列集 -->
						<c:if test="${groupFlag}">
							
							<!-- 创建每个字段类型 -->
							<input type="hidden" name="${field.name }_type" value="${field.type }"/>
							
					  		<div class="control-group" style="margin-bottom: 5px;">
					    		<label class="control-label">${field.alias}</label>
					   			<div class="controls">
					   			
					   				<!-- 生成运算符下拉框 -->
									<select class="input-small" name="operator_${field.name}" >
										<c:if test="${field.operators ne null}">
											<c:forEach var="oper" items="${field.operators}">
													<option value="${oper }">${oper }</option>
											</c:forEach>
										</c:if>
									</select>
									
									<!-- 如果数组不为空 即表示该字段是复选框组,否则是文本框 -->
									<c:choose>
										<c:when test="${field.values ne null}">
											<c:forEach var="opts" items="${field.values}">
												<c:set var="op" value="${fn:split(opts,'#')}" scope="page"></c:set>
												<label class="checkbox inline">
													<input type="checkbox" name="${field.name}" value="${op[0]}">${op[1]}
													<c:if test="${op[0] eq 'customized'}">
														<input type="text" name="${field.name}" disabled="disabled" class="input-small" style="height: 10px;line-height: 10px;">
													</c:if>
												</label>
												
											</c:forEach>	
										</c:when>
										<c:otherwise>
										
											<!-- 这里根据类型type不同增加不同的验证规则 -->
											<c:choose>
												<c:when test="${fn:toLowerCase(field.type) eq 'ip'}">
													<input type="text" class="input-medium" name="${field.name}" >
												</c:when>
												<c:when test="${fn:toLowerCase(field.type) eq 'long'}">
													<input type="text" class="input-medium" data-rule="integer[+]" name="${field.name}" >
												</c:when>
												<c:when test="${fn:toLowerCase(field.type) eq 'int'}">
													<input type="text" class="input-medium" data-rule="integer[+];range[1~65536]" name="${field.name}" >
												    <span style="display:none;" id="tab${group.id }_tomsg">--</span> <input type="text" id="tab${group.id }_msg"  class="input-medium" data-rule="integer[+];range[1~65536]" style="display:none;" name="${field.name}" >
												</c:when>
												<c:when test="${fn:toLowerCase(field.type) eq 'mac'}">
													<input type="text" class="input-medium" data-rule="mac" name="${field.name}" >
												</c:when>
												<c:when test="${fn:toLowerCase(field.type) eq 'date'}">
													<input type="text" class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'});" name="${field.name}" readonly="readonly">
													<span style="display:none;" id="tab${group.id }_totime">--</span> <input type="text" class="Wdate" readonly="readonly" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'});" id="tab${group.id }_endtime" style="display:none;" name="${field.name}" >
												</c:when>
												<c:otherwise>
													<input type="text" class="input-medium" name="${field.name}" >
												</c:otherwise>
											</c:choose>
										</c:otherwise>
									</c:choose>
					   			</div>
					  		</div>
				  		</c:if>
				  	</c:forEach>
				</form>		
			</div>
		</c:forEach>
	</div>

	<p class="text-center">
		<button type="submit" class="btn btn-primary btn-large" id="log_search_submit">查询</button>
		<button class="btn btn-large" id="log_search_cancel">取消</button>
	</p>  	
</div>
<script src="/js/sim/log/log_search.js"></script>