package com.topsec.tsm.ui.topo.vo;

public class EventStaticsVo { 
	
	private String severity;   
	
	private long total;
	
	private String devAddress;
	
	private String eventType;
	
	private String srcAddress;
	
	private String destAddress;


	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public String getDevAddress() {
		return devAddress;
	}

	public void setDevAddress(String devAddress) {
		this.devAddress = devAddress;
	}


	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(String srcAddress) {
		this.srcAddress = srcAddress;
	}

	public String getDestAddress() {
		return destAddress;
	}

	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
    
}