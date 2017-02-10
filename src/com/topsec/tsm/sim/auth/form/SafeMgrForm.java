package com.topsec.tsm.sim.auth.form;


public class SafeMgrForm {
	private String minCount;
	private String minUpperCount;
	private String minLowerCount;
	private String minNumCount;
	private String pwdModifyCycle;
	private String failCount;
	private String lostTime;
	private boolean securityCheck;
	private Integer logSearchLimit;
	private Integer logDisplayLimit;
  
public boolean isSecurityCheck() {
		return securityCheck;
	}
	public void setSecurityCheck(boolean securityCheck) {
		this.securityCheck = securityCheck;
	}
public SafeMgrForm(){}
	public String getMinCount() {
		return minCount;
	}

	public void setMinCount(String minCount) {
		this.minCount = minCount;
	}

	public String getMinUpperCount() {
		return minUpperCount;
	}

	public void setMinUpperCount(String minUpperCount) {
		this.minUpperCount = minUpperCount;
	}

	public String getMinLowerCount() {
		return minLowerCount;
	}

	public void setMinLowerCount(String minLowerCount) {
		this.minLowerCount = minLowerCount;
	}

	public String getMinNumCount() {
		return minNumCount;
	}

	public void setMinNumCount(String minNumCount) {
		this.minNumCount = minNumCount;
	}

	public String getPwdModifyCycle() {
		return pwdModifyCycle;
	}
	public void setPwdModifyCycle(String pwdModifyCycle) {
		this.pwdModifyCycle = pwdModifyCycle;
	}
	public String getFailCount() {
		return failCount;
	}

	public void setFailCount(String failCount) {
		this.failCount = failCount;
	}

	public String getLostTime() {
		return lostTime;
	}

	public void setLostTime(String lostTime) {
		this.lostTime = lostTime;
	}
	public Integer getLogSearchLimit() {
		return logSearchLimit;
	}
	public void setLogSearchLimit(Integer logSearchLimit) {
		this.logSearchLimit = logSearchLimit;
	}
	public Integer getLogDisplayLimit() {
		return logDisplayLimit;
	}
	public void setLogDisplayLimit(Integer logDisplayLimit) {
		this.logDisplayLimit = logDisplayLimit;
	}

}
