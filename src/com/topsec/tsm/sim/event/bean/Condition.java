package com.topsec.tsm.sim.event.bean;

import java.io.Serializable;

import com.topsec.tsm.ui.html.LabelValueBean;

//和form 的字段完全一致
public class Condition implements Serializable{
    
	private String confirm;
	private String confirm_person;
    
	//private String trueSate="1=1";
    private String totalLimit;
    
    private String dvc_addresses;//db
    private String dvc_address;//text
    private String[] dvc_ips;//list
    private LabelValueBean dvc_ips_beans[]; //list

    private String dvc_types;//db
    private String[] device_types;//list
    private String deviceTypeLabels;
    private LabelValueBean dvc_type_beans[]; //list
    
    private String src_addresses;
    private String src_address;//text
    private String[] src_ips;//list
    private LabelValueBean src_ips_beans[]; //list

    private String src_ports;
    private String src_port;//text
    private String[] src_pts;//list
    private LabelValueBean src_pts_beans[]; //list

    private String dest_addresses;
    private String dest_address;//text
    private String[] dest_ips;//list
    private LabelValueBean dest_ips_beans[]; //list

    private String dest_ports;
    private String dest_port;//text
    private String[] dest_pts;//list
    private LabelValueBean dest_pts_beans[]; //list
    
    private String start_time;

    private String end_time;

    private String eve_types;//db
    private String[] event_types;//list
    private String eventTypeLabels;
    private LabelValueBean event_type_beans[]; //list

    private String priori;
    
    private String[] priority;
    
    //用于高级查询的条件字符串
    private String adWhereSql;
    
    //要查询的表字段 逗号分隔
    private String columnsSet;
    
    /* modify by yangxuanjia at 2011-01-27 start */
    private String uuid;
    /* modify by yangxuanjia at 2011-01-27 end */
    
    private int sizeStart;
    private int pageSize;
    
    private String cat1_id;//事件分类1
    private String[] category1;
    private String[] category2;
	private String cat2_id;//事件分类2
    private String cat3_id;//事件分类3
    private String trans_protocol;//传输协议
    private String name;//时间规则名称
    private String eventName;//事件名称
    private String[] query_event_Name;//用户保存下钻事件名称参数
    public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}
	
	public String getConfirm_person() {
		return confirm_person;
	}

	public void setConfirm_person(String confirm_person) {
		this.confirm_person = confirm_person;
	}
    public String[] getCategory1() {
		return category1;
	}

	public void setCategory1(String[] category1) {
		this.category1 = category1;
	}

	public String[] getCategory2() {
		return category2;
	}

	public void setCategory2(String[] category2) {
		this.category2 = category2;
	}

    public String getEventName() {
		return eventName;
	}

	public String[] getQuery_event_Name() {
		return query_event_Name;
	}

	public void setQuery_event_Name(String[] query_event_Name) {
		this.query_event_Name = query_event_Name;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	private Integer alarmState;
    private String ip ;
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCat1_id() {
		return cat1_id;
	}

	public void setCat1_id(String cat1Id) {
		cat1_id = cat1Id;
	}

	public String getCat2_id() {
		return cat2_id;
	}

	public void setCat2_id(String cat2Id) {
		cat2_id = cat2Id;
	}

	public String getCat3_id() {
		return cat3_id;
	}

	public void setCat3_id(String cat3Id) {
		cat3_id = cat3Id;
	}

	public String getTrans_protocol() {
		return trans_protocol;
	}

	public void setTrans_protocol(String transProtocol) {
		trans_protocol = transProtocol;
	}

	public int getSizeStart() {
		return sizeStart;
	}

	public void setSizeStart(int sizeStart) {
		this.sizeStart = sizeStart;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getColumnsSet() {
        return columnsSet;
    }

    public void setColumnsSet(String columnsSet) {
        this.columnsSet = columnsSet;
        if(this.columnsSet==null||"".equals(this.columnsSet))
            this.columnsSet=" CAT1_ID,CAT2_ID,CAT3_ID,GENERATOR,EVENT_TYPE,EVENT_ID,UUID,END_TIME";
//        else {
//            this.columnsSet+=this.columnsSet.contains("CAT1_ID")?"":",CAT1_ID";
//            this.columnsSet+=this.columnsSet.contains("CAT2_ID")?"":",CAT2_ID";
//            this.columnsSet+=this.columnsSet.contains("CAT3_ID")?"":",CAT3_ID";
//            this.columnsSet+=this.columnsSet.contains("GENERATOR")?"":",GENERATOR";
//            this.columnsSet+=this.columnsSet.contains("EVENT_TYPE")?"":",EVENT_TYPE";
//            this.columnsSet+=this.columnsSet.contains("EVENT_ID")?"":",EVENT_ID";
//            this.columnsSet+=this.columnsSet.contains(DataConstants.UUID)?"":",UUID";
//            this.columnsSet+=this.columnsSet.contains("END_TIME")?"":",END_TIME";
//        }
    }

    public String[] getPriority() {
        return priority;
    }

    public void setPriority(String[] priority) {
        this.priority = priority;
    }

    public String getPriori() {
        return priori;
    }

    public void setPriori(String priori) {
        this.priori = priori;
    }

    public Condition()
    {
        
    }

    public String getDvc_addresses() {
        return dvc_addresses;
    }

    public void setDvc_addresses(String dvc_addresses) {
        this.dvc_addresses = dvc_addresses;
    }

    public String getDvc_types() {
        return dvc_types;
    }

    public void setDvc_types(String dvc_types) {
        this.dvc_types = dvc_types;
    }

    public String getSrc_addresses() {
        return src_addresses;
    }

    public void setSrc_addresses(String src_addresses) {
        this.src_addresses = src_addresses;
    }

    public String getSrc_ports() {
        return src_ports;
    }

    public void setSrc_ports(String src_ports) {
        this.src_ports = src_ports;
    }

    public String getDest_addresses() {
        return dest_addresses;
    }

    public void setDest_addresses(String dest_addresses) {
        this.dest_addresses = dest_addresses;
    }

    public String getDest_ports() {
        return dest_ports;
    }

    public void setDest_ports(String dest_ports) {
        this.dest_ports = dest_ports;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDvc_address() {
        return dvc_address;
    }

    public void setDvc_address(String dvc_address) {
        this.dvc_address = dvc_address;
    }

    public LabelValueBean[] getDvc_ips_beans() {
        return dvc_ips_beans;
    }

    public void setDvc_ips_beans(LabelValueBean[] dvc_ips_beans) {
        this.dvc_ips_beans = dvc_ips_beans;
    }

    public String[] getDvc_ips() {
        return dvc_ips;
    }

    public void setDvc_ips(String[] dvc_ips) {
        this.dvc_ips = dvc_ips;
        dvc_ips_beans=new LabelValueBean[dvc_ips.length];
        createOption(dvc_ips_beans,dvc_ips,null);
    }
    
    
    
    private void createOption(LabelValueBean[] lvBeans,String[] optionValues,String optionLabels)
    {
        String[] optLabels=null;
        if(optionLabels!=null&&!"".equals(optionLabels))
        {
            optLabels=optionLabels.split(";");
        }
        for(int i=0;i<optionValues.length;i++)
        {
            LabelValueBean lvBean=new LabelValueBean();
            if(optLabels==null)
                lvBean.setText(optionValues[i]);
            else
                lvBean.setText(optLabels[i]);
            lvBean.setVal(optionValues[i]);
            lvBeans[i]=lvBean;
        }
    }

    public String[] getDevice_types() {
        return device_types;
    }

    public void setDevice_types(String[] device_types) {
        this.device_types = device_types;
        dvc_type_beans=new LabelValueBean[device_types.length];
        createOption(dvc_type_beans,device_types,deviceTypeLabels);
    }

    public String getDeviceTypeLabels() {
        return deviceTypeLabels;
    }

    public void setDeviceTypeLabels(String deviceTypeLabels) {
        this.deviceTypeLabels = deviceTypeLabels;
    }

    public LabelValueBean[] getDvc_type_beans() {
        return dvc_type_beans;
    }

    public void setDvc_type_beans(LabelValueBean[] dvc_type_beans) {
        this.dvc_type_beans = dvc_type_beans;
    }

    public String getSrc_address() {
        return src_address;
    }

    public void setSrc_address(String src_address) {
        this.src_address = src_address;
    }

    public String[] getSrc_ips() {
        return src_ips;
    }

    public void setSrc_ips(String[] src_ips) {
        this.src_ips = src_ips;
        src_ips_beans=new LabelValueBean[src_ips.length];
        createOption(src_ips_beans,src_ips,null);
    }

    public LabelValueBean[] getSrc_ips_beans() {
        return src_ips_beans;
    }

    public void setSrc_ips_beans(LabelValueBean[] src_ips_beans) {
        this.src_ips_beans = src_ips_beans;
    }

    public String getSrc_port() {
        return src_port;
    }

    public void setSrc_port(String src_port) {
        this.src_port = src_port;
    }

    public String[] getSrc_pts() {
        return src_pts;
    }

    public void setSrc_pts(String[] src_pts) {
        this.src_pts = src_pts;
        src_pts_beans=new LabelValueBean[src_pts.length];
        createOption(src_pts_beans,src_pts,null);
    }

    public LabelValueBean[] getSrc_pts_beans() {
        return src_pts_beans;
    }

    public void setSrc_pts_beans(LabelValueBean[] src_pts_beans) {
        this.src_pts_beans = src_pts_beans;
    }

    public String getDest_address() {
        return dest_address;
    }

    public void setDest_address(String dest_address) {
        this.dest_address = dest_address;
    }

    public String[] getDest_ips() {
        return dest_ips;
    }

    public void setDest_ips(String[] dest_ips) {
        this.dest_ips = dest_ips;
        dest_ips_beans=new LabelValueBean[dest_ips.length];
        createOption(dest_ips_beans,dest_ips,null);
    }

    public LabelValueBean[] getDest_ips_beans() {
        return dest_ips_beans;
    }

    public void setDest_ips_beans(LabelValueBean[] dest_ips_beans) {
        this.dest_ips_beans = dest_ips_beans;
    }

    public String getDest_port() {
        return dest_port;
    }

    public void setDest_port(String dest_port) {
        this.dest_port = dest_port;
    }

    public String[] getDest_pts() {
        return dest_pts;
    }

    public void setDest_pts(String[] dest_pts) {
        this.dest_pts = dest_pts;
        dest_pts_beans=new LabelValueBean[dest_pts.length];
        createOption(dest_pts_beans,dest_pts,null);
    }

    public LabelValueBean[] getDest_pts_beans() {
        return dest_pts_beans;
    }

    public void setDest_pts_beans(LabelValueBean[] dest_pts_beans) {
        this.dest_pts_beans = dest_pts_beans;
    }

    public String getEve_types() {
        return eve_types;
    }

    public void setEve_types(String eve_types) {
        this.eve_types = eve_types;
    }

    public String getEventTypeLabels() {
        return eventTypeLabels;
    }

    public void setEventTypeLabels(String eventTypeLabels) {
        this.eventTypeLabels = eventTypeLabels;
    }

    public LabelValueBean[] getEvent_type_beans() {
        return event_type_beans;
    }

    public void setEvent_type_beans(LabelValueBean[] event_type_beans) {
        this.event_type_beans = event_type_beans;
    }

    public void setEvent_types(String[] event_types) {
        this.event_types = event_types;
        event_type_beans=new LabelValueBean[event_types.length];
        createOption(event_type_beans,event_types,eventTypeLabels);
    }

    public String[] getEvent_types() {
        return event_types;
    }

//  public String getTrueSate() {
//      return trueSate;
//  }
//
//  public void setTrueSate(String trueSate) {
//      this.trueSate = trueSate;
//  }

    public String getAdWhereSql() {
        return adWhereSql;
    }

    public void setAdWhereSql(String adWhereSql) {
        this.adWhereSql = adWhereSql;
    }

    public String getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(String totalLimit) {
        this.totalLimit = totalLimit;
    }
    /* modify by yangxuanjia at 2011-01-27 end */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	public Integer getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(Integer alarmState) {
		this.alarmState = alarmState;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
    
    /* modify by yangxuanjia at 2011-01-27 end */
 
}
