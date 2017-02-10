
function EventQuery() {
	this._BASE_EVENT_QUERY_URL = "/sim/eventRestQuery/basicEventQuery";
	this.columns = [];
	this.cmenu = null;
	this.rmenu =null;
	this.logTabsDiv=null;
	this.data = [];
	this.tabs=[];
	this.queryParams = null;
	this.eventRowMenu=null;
	this.mainDataTableId=null;
};

/**
 * 格式化列
 * @param cfds
 */
EventQuery.prototype.columFormat=function(cfds){};

EventQuery.prototype.initColumns=function (_col_param) {//加载列集
	var _this=this;this.columns = [];
	$.ajax({
		type : "post",
	 	url : "/sim/eventRestQuery/jsondata",
	 	data:_col_param,
		async : false,
		dataType : "json",
		success : function(data) {
			if(data&&data.length==1){
				_this.columFormat(data[0]);
				_this.columns.push(data[0]);
			}
		}
	});
};

EventQuery.prototype.createColumnMenu = function(id) {
	var _this = this;
	_this.cmenu = $('<div/>').appendTo('body');
	_this.cmenu.menu({
		onClick : function(item) {
			if (item.iconCls == 'icon-ok') {
				$(id).datagrid('hideColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-empty'
				});
			} else {
				$(id).datagrid('showColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-ok'
				});
			}
		}
	});
	var fields = $(id).datagrid('getColumnFields');
	for ( var i = 0; i < fields.length; i++) {
		var field = fields[i];
		var col = $(id).datagrid('getColumnOption', field);
		_this.cmenu.menu('appendItem', {
			text : col.title,
			name : field,
			iconCls : 'icon-ok'
		});
	}
};

EventQuery.prototype.createTabsLayout=function(){//使用到其它地方需要复写
	return $("<div data-options='border:true' fit='true' class='easyui-panel sim'></div>").appendTo($("#event_correl_id"));
};
 
/**
 * 双击事件处理
 */
EventQuery.prototype._tab_close=function(){
	if(!this.logTabsDiv){
		return;
	}
	var tabs=this.logTabsDiv.tabs('tabs');
	if(tabs&&tabs.length>=1){
		this.logTabsDiv.tabs('close',0);
		this._tab_close(); 
	}
};
EventQuery.prototype.showCorrelatelog=function(data){
	var _this=this;
	if(!this.logTabsDiv){
		this.logTabsDiv=this.createTabsLayout();
	}
	
	this.logTabsDiv.tabs();
	this._tab_close();
	 
	
	if(data.length){
  		$.each(data,function(i,_log){

  			var _$tab_panel=$("<div></div>"),dgid="_t_"+i;
  			_this.logTabsDiv.tabs('add',{   
  			    title:_log["dvcTypeName"],   
  			    content:_$tab_panel,   
  			    closable:true,
  			    selected:true
  			});  
  			
  			var headcolums=_log["headcolums"],_f_log=_log["columsdata"];
  			var _field_m=[];//
  			$.each(headcolums,function(i,_field){
  				 //设置表头样式
  				_field["width"]=100;
  				if(_field['field']=='MESSAGE'){
  					_field.formatter=function(value,row,index){//设置消息样式
  						return '<a class="_m_value_class_'+index+'">'+value+'</a>';
  					};
  				}
  			});
  			_field_m.push(headcolums);
  			
  			var _field_o=[],_row_log_o=null;
  			//提取原始日志
  			  for(var k in _f_log){
  				_row_log_o=_f_log[k];
  			    _field_o.push({"ORIGINAL_DATA":(_row_log_o['ORIGINAL_DATA']?_row_log_o['ORIGINAL_DATA']:"无原始日志")});
  			} 
  			
  		  $("<table id='"+dgid+"'/>").appendTo(_$tab_panel).datagrid({
 				view:detailview,
 				fitColumns:true,
 			//	nowrap:false,
 				closable:true,
 				onExpandRow:function(index,row){
 							var w= _$tab_panel.width()-55;
 							$('#ddv-'+dgid+"-"+index).datagrid({//需要新创建   原始日志
 								data:[_field_o[index]],
 								nowrap:false,
 								width:w,
 								height:'auto',
 								columns:[[{field:"ORIGINAL_DATA",title:"原始日志",width:w-4}]] ,
 								onResize:function(){},
 								onLoadSuccess:function(){},
 								onLoad:function(){
 						               $('#'+dgid).datagrid('fixDetailRowHeight',index);
 						        }
 							});
 							
 							$('#'+dgid).datagrid('fixDetailRowHeight',index);
 			   },
 			   detailFormatter:function(index,row){//原始日志
 					return '<div style="padding:2px"><table id="ddv-'+dgid+"-" + index + '"></table></div>';
 			   },
 			   data:_f_log,//格式化日志数据
 			   columns: _field_m ,
 			   onClose:function(title,index){},
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
		 });
	}
	
	
	
	//$panel.panel({title: '回溯详情'});
};
EventQuery.prototype.doCorrelateOnRow=function(rowData){
	var evtId=rowData['EVENT_ID'],_this=this;
	$.ajax({
		type : "post",
	 	url : "/sim/eventRestQuery/correlatorData",
	 	data:{evtId:evtId,requestIp:simEventQueryHandler.targetIp},
		async : false,
		dataType : "json",
		success : function(data) {
			_this.showCorrelatelog(data);
		}
	});
};

EventQuery.prototype.doCorrelateOnRowIndex = function(rowIndex){
	 this.doCorrelateOnRow($(this.mainDataTableId).datagrid("getRows")[rowIndex]) ;
};

EventQuery.prototype._dbClickRowHandler=function(rowIndex, rowData){
	//this.showEventDetail(rowData);
	this.doCorrelateOnRow(rowData);
};

EventQuery.prototype._loadSuccessHandler=function(){//其他地方使用可复写
	
	$('.event_corr_linkbtn').linkbutton({   
	    iconCls: 'icon-search',
	    plain:true
	});  
};
EventQuery.prototype.expandTimeline=function(_param){
	$.ajax({
		type : "post",
	 	url : "/sim/eventRestQuery/expandTimeline",
	 	data:_param,
		dataType : "json",
		success : function(data) {
			if(data!=null && data!=""){
				var seriesData=[],category=[];
				$.each(data,function(i,_data){
					seriesData.push(_data.y);
					category.push(_data.x);
				});
				createLogTimelineChartInstance(seriesData,category);
			}
		}
	});
};

EventQuery.prototype.queryEvent = function(param,id,_title) {
	var _this = this,
	_wrappDetailView=function(_data){
		var contents = "",
			content = null;
		$.each(_data, function(i, c) {
			content = '<div class="horizon_wrappDetailView">';
			content += '<p class="horizon_wrappDetailView_pHead">';
			content += '<span class="horizon_wrappDetailView_p_spHead">名称：</span>';
			content += '<span>' + c.name + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">等级：</span>';
			content += '<span>' + c.priority + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">描述：</span>';
			content += '<span>' + c.description + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">解决方案：</span>';
			content += '<span>' + c.solution + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">创建人：</span>';
			content += '<span>' + c.creater + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">时间：</span>';
			content += '<span>' + c.createTime + '</span>';
			content += '</p>';
			content += "</div>";
			contents += content;
		}); 
		return contents;
	};
	this.queryParams = param;
	this.mainDataTableId=id;
	
	
	$(_this.mainDataTableId).datagrid({
		url : _this._BASE_EVENT_QUERY_URL,
		method : 'post',
	//	title : ((!_title)?'事件':_title),
		width : 'auto',
		height : 'auto',
		iconCls:'icon-grid',
		fitColumns : true,
		
		singleSelect : true,
		pageSize:10,
		pageNumber:1,
		/*pageList:[10],*/
		pagination : true,
	//	rownumbers : true,
	//	striped : true,
		nowrap:true,
		view: detailview,
		detailFormatter:function(index,row){
		        return '<div class="ddv" style="padding:5px 0"></div>';
		},
	    onExpandRow: function(index,row){
	         var ddv= $(this).datagrid('getRowDetail',index).find('div.ddv');
	         ddv.panel({
	            border:false,
	            cache:false,
	            href:'/sim/eventRestQuery/getAssociatedKnowledgebyEvtId?evtid='+row['EVENT_ID']+"&requestIp="+simEventQueryHandler.targetIp,
	            extractor: function(data){
	            	var _data=eval(data);
	            	if(_data&&_data.length>0){
	            		return _wrappDetailView(_data);
	            	}
	            	return "暂无解决方案";
	            },
	            onLoad:function(){
	                $(_this.mainDataTableId).datagrid('fixDetailRowHeight',index);
	            }
	        });  
	        $(_this.mainDataTableId).datagrid('fixDetailRowHeight',index);
	    },
		    
		columns : _this.columns,
		queryParams : _this.queryParams,
//		onHeaderContextMenu : function(e, field) {
//			e.preventDefault();
//			if(!_this.cmenu){
//				_this.createColumnMenu(_this.mainDataTableId);
//			}
//			
//			_this.cmenu.menu('show', {
//				left : e.pageX,
//				top : e.pageY
//			});
//			
//		},
		onDblClickRow : function(rowIndex, rowData) {
			_this._dbClickRowHandler(rowIndex, rowData);
		},
		onRowContextMenu:function(e, rowIndex, rowData){
			e.preventDefault();
		},
		onSelect:function(rowIndex, rowData){
		},
		onLoadSuccess:function(data){	
			_this._loadSuccessHandler(data);
			var pager = $(_this.mainDataTableId).datagrid('getPager');
			if(pager.pagination('options').showPageList){
				pager.pagination({showPageList:false});
			} 
			if(data.rows.length>0){
				_this.doCorrelateOnRowIndex(0);
			}else{
				_this._tab_close();
			}
		}
	});
};
//格式化日期
function formatterDate(str){
	var date1=parseInt(str);
	var startdate=new Date(date1);
	startdate=startdate.getFullYear()
	          +"-"+((startdate.getMonth()+1)<10 ? ("0"+(startdate.getMonth()+1)):(startdate.getMonth()+1))
	          +"-"+(startdate.getDate()<10 ? ("0"+startdate.getDate()):startdate.getDate())
	          +" "+(startdate.getHours()<10 ? ("0"+startdate.getHours()):startdate.getHours())
	          +":"+(startdate.getMinutes()<10 ? ("0"+startdate.getMinutes()):startdate.getMinutes())
	          +":"+(startdate.getSeconds()<10 ? ("0"+startdate.getSeconds()):startdate.getSeconds()) ;
	return startdate;
}
/**
 * 创建日志时间线对象
 */
var savetime=[];
var eventModel = new EchartsModel();
function createLogTimelineChartInstance(seriesData,category){
	var w = $('#event_timeline_chart').parent().width()-2;
	var h = $('#event_timeline_chart').parent().height();
	
	var barWidth ;
	if(seriesData != null){
		if(seriesData.length<50){
			barWidth = 20;
		}
	}
	
	$("#event_timeline_chart").css("height",h);
	$("#event_timeline_chart").css("width",w);
	 var option = {
		  color:['#6CD7D9'],
		   tooltip: {
			   trigger: 'axis'
           },
           grid:{
        	 x:0,
        	 y:0,
        	 x2:0,
        	 y2:0
           },
           tooltip: {
        	   backgroundColor:'#FAFAFA',
               borderColor : '#6CD7D9',
               borderRadius : 8,
               borderWidth: 1,
               position : function(p) {
            	   return eventModel.setTooltipPosition('#event_timeline_chart', p, 320, -10);
               },
               textStyle : {
                   color: '#333333',
                   decoration: 'none',
                   fontFamily: 'Verdana, sans-serif',
                   fontSize: 12
                  // fontStyle: 'italic'
                  // fontWeight: 'bold'
               },
              formatter: function (params) {
        		var str = params.name;
        		var dateArr = str.split("&");
        		var date1 = parseInt(dateArr[0]);
        		var startdate = formatterDate(date1);
        		var date2 = parseInt(dateArr[1]);
        		var enddate = formatterDate(date2);
                return  startdate + "至" + enddate +  '<br/>匹配的事件：<span style="color:red">' + params.value + '</span>条';   
              }  
            },
           xAxis:[
                    {
                       type: 'category', 
                       data:category,
                       splitLine:{
                    	   show:false
                       }
                    }
                 ],
           yAxis:[
                {
                    type : 'value',
                    splitLine:{
                    	   show:false
                       }
                }
            ],
            series: [{
                name: 'logCount',
                type:'bar',
                barMaxWidth:barWidth,
                data:seriesData
            }]
	   };
	    require.config({
			paths : {
				echarts : '/js/echart/build/dist',
			}
		});
		
		require(
		[
		    'echarts',
			'echarts/chart/bar'
			], 
		function(ec) {
			var myChart = ec.init(document.getElementById("event_timeline_chart"));
			myChart.setOption(option);
			myChart.on('click', function(param){
				 if (param.type == 'click') { 
					 var str=param.name;
                		var dateArr=str.split("&");
                		var date1=parseInt(dateArr[0]);
                		var startdate=formatterDate(date1);
                		var date2=parseInt(dateArr[1]);
                		var enddate=formatterDate(date2);
                		if(startdate == $("#startTimeId").val() && enddate == $("#endTimeId").val()){
                			return false;
                		}
                		savetime[savetime.length]=$("#startTimeId").val()+"&"+$("#endTimeId").val();
                		$("#startTimeId").val(startdate);
                		$("#endTimeId").val(enddate);
                		$("#event_floatDiv").fadeIn("normal");
                		doEventQuery();
				 }
				
			});
		});
}
