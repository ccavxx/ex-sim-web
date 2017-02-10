$.extend($.fn.validatebox.defaults.rules, {   
    ipFormat: {   
        validator: function(value){  
        	var regx=/^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/;
            return regx.test(value);   
        },   
        message: '请输入正确的IP地址,如：192.168.0.1!'
    }   
});  