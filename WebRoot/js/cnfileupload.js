/**
 * 自定义文件表单样式
 * by wang_qiao
 */
(function( $ ){
	$.fn.cnfileupload = function(options) {
		return this.each(function(){
			var $fileElement = $(this);
			if($fileElement.attr("cnf") === "_cnfileupload"){
				return true;
			}
			if (options) {
				$.extend(defaults, options);
			}

			var fileName = $fileElement.attr("name");
			if(!fileName) {
				fileName = "cn";
			}
			var fileId = fileName + "_" + new Date().getTime();
			$fileElement.attr("cnf", "_cnfileupload").css({"opacity":"0", "width":"73px", "height":"26px", "padding":"0", "margin":"0"});
			
			var $toltalDivElement = $("<div></div>");
			var divs = "<span style=\"display:'inline-block';\"><input type='text' id='" + fileId + "' readonly='readonly' value='请选择数据文件' style='height:22px;width:200px;padding:0;margin:0 10px 0 0;' /></span>";
			$toltalDivElement.css({"height":"30px"})
					.append(divs);
			
			$toltalDivElement.insertAfter($fileElement);
			$toltalDivElement.append("<input type='hidden' name='fileid' value='"+fileId+"'/>");
			
			var $fileSpanElement = $("<span></span>");
			$toltalDivElement.append($fileSpanElement);
			$fileSpanElement.css({"background-image":"url(/img/cnfileupload.png)", "background-repeat":"no-repeat", "width":"75px", "display":"inline-block", "padding":"0", "margin":"0"})
					.append($fileElement);
			$fileElement.change(function(){
				var fileVal = $(this).val();
				var fileNameArray;
				if(fileVal.valueOf("\\")){
					fileNameArray = fileVal.split("\\");
				} else if(fileVal.valueOf("/")) {
					fileNameArray = fileVal.split("/");
				}
				var leg = fileNameArray.length;
				if(leg > 0){
					$("#" + fileId).val(fileNameArray[leg-1]);
				}
			});
		});
	};
})(jQuery);