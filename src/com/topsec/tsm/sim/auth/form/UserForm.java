/*
 * 创建日期 2005-8-18
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.topsec.tsm.sim.auth.form;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.auth.manage.AuthUserDevice;

/**
 * @author lei_peng
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class UserForm {
	public UserForm(){}
	private String password;// 密码
	private String userName;// 用户名
	private String status;// 状态：0禁用 1启用
	private String passwordAgain;// 确认密码
	private String expireTime;// 有效期
	private String minIp;// 最小ip
	private String maxIp;// 最大ip
	private String description;// 描述
	private String userid;//用户id
    private String createUser;//创建者
    private String lastModifyUser;//最后修改者
    private Date lastModifyTime;//最后修改时间
    private String roleName;//角色名称
    private String roleid;//角色id
    private String userDevices;//用户对应的设备权限
    private String deviceGroup;//列集
    public String getDeviceGroup() {
		return deviceGroup;
	}

	public void setDeviceGroup(String deviceGroup) {
		this.deviceGroup = deviceGroup;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(String expireTime) {
		this.expireTime = expireTime;
	}
    public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleid() {
		return roleid;
	}

	public void setRoleid(String roleid) {
		this.roleid = roleid;
	}

	public HashSet<AuthUserDevice> getUserDevices() {
		//设置用户查看权限  
		HashSet<AuthUserDevice> userDevices = new HashSet<AuthUserDevice>();
		if(this.userDevices != null && !"undefined".equals(this.userDevices) && !"".equals(this.userDevices)){
			String[] dataSources = this.userDevices.split(";");
			//switch,Switch/Cisco/Cisco Switch,10096,192.168.75.30,3dfca666-69f4-4b12-8c96-c7a8a3fc1e4e
			for(String dataS : dataSources){
				AuthUserDevice  userDevice = new AuthUserDevice();
				String[] dataSource =dataS.split(",");
				userDevice.setDataSourceName(dataSource[0]);
				userDevice.setDeviceType(dataSource[1]);
				userDevice.setDeviceId(dataSource[2]);
				userDevice.setIp(dataSource[3]);
				userDevice.setNodeId(StringUtil.nvl(""));
				userDevices.add(userDevice);
			}
		}
		return userDevices;
	}

	public void setUserDevices(String userDevices) {
		this.userDevices = userDevices;
	}
    public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getLastModifyUser() {
		return lastModifyUser;
	}

	public void setLastModifyUser(String lastModifyUser) {
		this.lastModifyUser = lastModifyUser;
	}

	public Date getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Date lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPasswordAgain() {
		return passwordAgain;
	}

	public void setPasswordAgain(String passwordAgain) {
		this.passwordAgain = passwordAgain;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getMinIp() {
		try {
			InetAddress.getByName(minIp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return minIp;
	}

	public final void setMinIp(String minIp) {
		this.minIp = minIp;
	}

	public String getMaxIp() {
		try {
			InetAddress.getByName(maxIp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return maxIp;
	}

	public final void setMaxIp(String maxIp) {
		this.maxIp = maxIp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
