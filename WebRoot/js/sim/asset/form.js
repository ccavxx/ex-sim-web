var form = {
}
/**
 * 
 *创建表单项,生成的每个表单项(div)都包含一个自定义的属性dynamic='y',表示此组件是动态生成的<br>
 *如果参数elementType是string类型会根据参数创建对应的input,select,password等<br>
 *如果参数是jquery对象，会直接将对象append到生成的item内
 *客户端可能通过dynamic属性来删除生成的表单项目
 *@param elementType string类型表示(input,select,password,checkbox)
 *@param itemId 项目id
 *@param name   项目name
 *@param value  数据信息
 *
 *@returns 创建的表单组件select,input,password,checkbox
 *<div class="control-group"> 
 *  <label class="control-label" for="dsa_dataSourceType">日志源类型：</label>
 *  <div class="controls">
 *    <input type="hidden" id="dsa_dataSourceTypeName" name="dataSourceTypeName">
 *    <select id="dsa_dataSourceType" name="dataSourceType" 
 *     		  class="easyui-combotree">
 *  </div> 
 *</div>  
 */
form.createFormItem = function(elementType,parent,label,itemId,name,options,value,itemName,dataRule){
	var outerDiv = $('<div/>').attr("dynamic","y").attr("itemName",itemName) ;
	outerDiv.addClass('control-group') ;
	var label = $('<label/>').addClass('control-label').attr('for',itemId).html(label) ;
	var required = "<span style='color:red;'>*</span>";
	var div = $('<div/>').addClass('controls') ;
	var input ;
	if($.type(elementType)=="string"){
		if(elementType=="select"){
			input = form.createSelect(itemId,name, options,value,itemName,dataRule) ;
		}else if(elementType=="text"){
			input = form.createInput('text',itemId, name, value,itemName,dataRule) ;
			input.attr("autocomplete","off") ;
			$(label).prepend(required);
		}else if(elementType=="password"){
			input = form.createInput('password',itemId, name, value,itemName,dataRule) ;
			input.attr("autocomplete","off") ;
			$(label).prepend(required);
		}else if(elementType=="hidden"){
			input = form.createInput('hidden',itemId, name, value,itemName,dataRule);
			outerDiv.css('display','none');
		}
	}else{
		input = elementType ;
	}
	div.append(input) ;
	$(label).appendTo(outerDiv) ;
	$(div).appendTo(outerDiv) ;
	if($.type(parent)=="string"){
		$("#"+parent).append(outerDiv) ;
	}else{
		parent.append(outerDiv) ;
	}
	return input ;
}

/**
 * 创建select组件
 * @param id   select id属性
 * @param name select name属性
 * @param datas select option数据
 */
form.createSelect = function createSelect(id,name,datas,selectValue,itemName,dataRule){
	var select = $('<select/>').attr('id',id).attr("name",name).attr("itemName",itemName ? itemName : "") ;
	if(dataRule){
		select.attr("data-rule",dataRule) ;
	}
	if($.isArray(datas)){
		form.fillSelect(select, datas, true,selectValue) ;
	}
	return select ;
}
/**
 * 为select组件加入数据
 * @param selector jquery selector或者jquery select对象 
 * @param datas 要加入的数据
 * @param deleteOldData 是否删除旧的数据<br> 
 *        true删除旧的数据<br>
 *        false保留旧数据
 */
form.fillSelect = function(selector,datas,deleteOldData,selectValue){
	var select ;
	if($.type(selector)=="string"){
		select = $(selector) ;
	}else{
		select = selector ;
	}
	if(deleteOldData){
		select.children().remove() ;
	}
	var selected = $.type(selectValue) == "number" ? "index" : "value";
	for(var i=0;i<datas.length;i++){
		var optionData = datas[i] ;
		if(optionData.name){
			var option = $("<option/>").attr("value",optionData.value).html(optionData.name);
			if((selected == "index" && i == selectValue) || 
			   (selected == "value" && selectValue == optionData.value)){
				option.attr("selected","selected") ;
			}
			$.each(optionData,function(fieldName,fieldValue){//继续循环数据中的每一个属性，将数据中其它属性也加入到当前option中
				if(fieldName != "name"){
					option.attr(fieldName,fieldValue) ;
				}
			}) ;
			select.append(option) ;
		}
	}
	return select ;
}

/**
 * 创建input组件
 * @param type input类型(text、password、hidden)
 * @param id   input id属性
 * @param name input name属性
 * @param value input value属性
 */
form.createInput = function(type,id,name,value,itemName,dataRule){
	if(!value){
		value = "";
	}
	var input = $('<input/>').attr('type',type).attr('id',id).attr('name',name).attr('value',value).attr("itemName",itemName ? itemName: "");
	if(dataRule){
		input.attr("data-rule",dataRule) ;
	}
	return input ;
}