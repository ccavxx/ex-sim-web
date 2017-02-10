function EventLogTrace(){
	this.counter = 0 ;
}
EventLogTrace.prototype.doTrace=function(id){
	var self = this ;
	$.getJSON("/sim/eventQuery/correlatorData?evtId="+id,
		function(data){
			self.showLogs(data);
		}
	);
};
EventLogTrace.prototype.doTraceByUUID=function(uuid){
	var self = this ;
	$.getJSON("/sim/eventQuery/correlatorDataByUUID?uuid="+uuid,
		function(data){
			self.showLogs(data);
		}
	);
};
EventLogTrace.prototype.showLogs=function(data){
	var self = this ;
	if(!data.length){
		return ;
	}
	var tabs = $("#elt_tabs") ;
	$.each(data,function(i,_log){
		self.createTabs(tabs,_log) ;
	});
};
EventLogTrace.prototype.createTabs=function(tabs,_log){
	var self = this ;
	var _$tab_panel=$("<div></div>"),dgid="_t_"+this.counter++;
	tabs.tabs('add',{   
	    title:_log["dvcTypeName"],   
	    content:_$tab_panel   
	});  
	
	var headcolums=_log["headcolums"],logs=_log["columsdata"];
	var columns=[];//
	$.each(headcolums,function(i,_field){
		 //设置表头样式
		_field["width"]=100;
		if(_field['field']=='MESSAGE'){
			_field.formatter=function(value,row,index){//设置消息样式
				return '<a class="_m_value_class_'+index+'">'+value+'</a>';
			};
		}
	});
	columns.push(headcolums);
	
	var _field_o=[],logRecord=null;
	//提取原始日志
	  for(var k in logs){
		logRecord=logs[k];
	    _field_o.push({"ORIGINAL_DATA":(logRecord['ORIGINAL_DATA'] ? logRecord['ORIGINAL_DATA'] : "无原始日志")});
	}
	self.createTable(dgid,_$tab_panel,columns,logs,_field_o) ;
};
EventLogTrace.prototype.createTable=function(dgid,_$tab_panel,columns,logs,_field_o){
	var self = this ;
	$("<table id='"+dgid+"'/>").appendTo(_$tab_panel).datagrid({
		   view:detailview,
		   fitColumns:true,
		   scrollbarSize : 0,
		   singleSelect:true,
		   onExpandRow:function(index,row){
				var w= _$tab_panel.width()-55;
				$('#ddv-'+dgid+"-"+index).datagrid({//需要新创建   原始日志
					data:[_field_o[index]],
					nowrap:false,
					width:w,
					height:'auto',
					columns:[[{field:"ORIGINAL_DATA",title:"原始日志",width:w-4}]] ,
					onLoad:function(){
						$('#'+dgid).datagrid('fixDetailRowHeight',index);
			        }
				});
				$('#'+dgid).datagrid('fixDetailRowHeight',index);
		   },
		   detailFormatter:function(index,row){//原始日志
				return '<div style="padding:2px"><table id="ddv-'+dgid+"-" + index + '"></table></div>';
		   },
		   data:logs,//格式化日志数据
		   columns: columns ,
		   onLoadSuccess:function(data){
			  //格式格式化日志消息显示
			  if(data.rows){
				   $.each(data.rows,function(i,row){
					   $('._m_value_class_'+i).tooltip({   
						   position: 'left',   
						   content: '<div style="color:#fff;width:400px;">'+row['MESSAGE']+'</div>',   
						   onShow: function(){   
							   $(this).tooltip('tip').css({   
								   backgroundColor: '#666',   
								   borderColor: '#666'  
							   });   
						   }   
					   }); 
				   });
			  }
		   }
	 }); 
}