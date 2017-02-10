/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//            判断select选项中 是否存在Value="paraValue"的Item 
function jsSelectIsExitItem(objSelect, objItemValue) {
	
    var isExit = false;
    for (var i = 0; i < objSelect.options.length; i++) {
        if (objSelect.options[i].value === objItemValue) {
            isExit = true;
            break;
        }
    }
    return isExit;
}
// 向select选项中 加入一个Item
function jsAddItemToSelect(objSelect, objItemText, objItemValue,clearInputId, it) {
    
    if (jsSelectIsExitItem(objSelect, objItemValue)) {// 判断是否存在
    	showAlertMessage("此项已加入,请填写新内容！");
    } else {
        var varItem = new Option(objItemText, objItemValue);
        if (objItemValue === null || objItemValue === '') {
        	showAlertMessage('填入信息不能为空！');
        } else if($(objSelect)[0].length > 29){
        	showAlertMessage("至多存在30个选项");
	 	} else if (it && !$(it).parent().find("input").isValid("")) {
			showAlertMessage("输入验证不通过！");
		}else {
            objSelect.options.add(varItem);
        }
        if(clearInputId) {
        	$('#'+clearInputId).val("");
        }
    }
}
//            删除select中选中的项    
function jsRemoveSelectedItemFromSelect(objSelect) {
    var length = objSelect.options.length - 1;
    for (var i = length; i >= 0; i--) {
        if (objSelect[i].selected === true) {
            objSelect.options[i] = null;
        }
//                    else {
//                        alert("请选择要删除的列！");
//                    }
    }
}
function jsRemoveAllOption(objSelect) {
    var length=0;
    try{
    	length= objSelect.options.length;
    }catch(e){length=0;}
    if(length<1){return;}
    for (var i = length-1; i >= 0; i--) {
        objSelect.options[i] = null;
    }
}
//根据radioName获取选中radio对象value
function   getRadioBoxValue(radioName)
{
    var obj = $("radioName");             //这个是以标签的name来取控件  
    for (i = 0; i < obj.length; i++) {
        if (obj[i].checked) {
            return   obj[i].value;
        }
    }
    return "undefined";
}