package com.topsec.tsm.sim.report.bean.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.topsec.tsm.sim.auth.util.SID;



/**
 * <dd>类名：画面实体基类
 * <dd>类说明：画面实体基类
 * <dd>备注
 */
public class BaseBean implements  Serializable {
	/**
	 * 版本信息标示ID
	 */
	private static final long serialVersionUID = 2727057219966142494L;

	/**
	 * HTTP请求
	 * 
	 */
	private HttpServletRequest request;

	/**
	 * 登陆者编号W
	 */
	private String userCode = "";

	/**
	 * 登陆者名称W
	 */
	private String userName = "";

	/**
	 * 登陆者角色
	 */
	private String userRole = "";

	/**
	 * 当前页号(分页用)W
	 */
	private int nowPage = 1;

	/**
	 * 一页的最大记录数(分页用)yh
	 */
	private int pageRecord;

	/**
	 * 所有页的总记录数(分页用)Rh
	 */
	private int recordCount;

	/**
	 * obNAbvf备份容器
	 */
	private BaseBean backBean;

	/**
	 * bZ消息W
	 */
	private String message = "";

	/**
	 * 消息字体颜色
	 */
	private String messageColor = "#000000";

	/**
	 * 记录上一次处理被设置为不可用控件名
	 */
	private String beforeDisableItems = "";

	/**
	 * G错误控件
	 */
	private String errorItem = "";

	/**
	 * 不可用控件
	 */
	private String disableItem = "";

	/**
	 * tHbJXݒ焦点
	 */
	private String focusItem = "";

	/**
	 * 前一次处理被设置焦点的控件名称JX
	 */
	private String beforeFocusItem;

	/**
	 * 客户端IP地址
	 */
	private String clnm;

	/**
	 * 对话框信息(JSP页面)ʓ
	 */
	private String confirmScript;

	/**
	 * 登出(JSP页面)ʓ
	 */
	private String logoutScript;

	/**
	 * 画面状态ʓݒ
	 */
	private String state = "false";

	/**
	 * 二次提交的key
	 */
	private String doubleVerb;

	/**
	 * 错误代号容器h
	 */
	private List errorCode = new ArrayList();

	/**
	 * G错误信息设置标签ݒtO
	 */
	private boolean errSetFlg = false;

	/**
	 * 错误消息设置标志位
	 * 
	 */
	boolean isSetFlg = false;

	/**
	 * <dd>方法名：画面实体基类实例化
	 * <dd>功能概要：画面实体基类实例化
	 * <dd>备注
	 */
	public BaseBean() {
		super();
	}

	/**
	 * <dd>方法名：画面实体基类实例化
	 * <dd>功能概要：画面实体基类实例化
	 * <dd>备注
	 */
	public BaseBean(HttpServletRequest request) {
		super();
		this.request = request;
		setSessionData(request);
	}

	/**
	 * <dd>方法名：指定焦点ݒ
	 * <dd>功能概要：指定焦点
	 * <dd>备注
	 * 
	 * @param item
	 *            String
	 */
	public void setFocus(String item) {
		setFocus(item, "0");
	}

	/**
	 * <dd>方法名：指定焦点ݒ
	 * <dd>功能概要：指定焦点
	 * <dd>备注
	 * 
	 * @param item
	 *            String
	 */
	public void setSelectFocus(String item) {

		if (!errSetFlg) {
			setFocus(item, "");
		}
	}

	/**
	 * <dd>方法名：指定焦点ݒ
	 * <dd>功能概要：指定焦点
	 * <dd>备注
	 * 
	 * @param item
	 *            String
	 * @param index
	 *            String
	 */
	public void setFocus(String item, String index) {
		setFocusItem(item, index);
	}

	/**
	 * <dd>方法名：指定不可用控件ݒ
	 * <dd>功能概要：指定不可用控件
	 * <dd>备注
	 * 
	 * @param item
	 *            String
	 */
	public void setDisabledItems(String item[]) {

		// zLf
		if ((item != null) && (item.length != 0)) {

			// zTCYŌJԂēIJavaScriptR[h𐶐
			for (int i = 0; i < item.length; i++) {

				if ((item[i] != null) && !item[i].trim().equals("")) {
					this.setDisableItem(item[i], "");
				}
			}
		}
	}

	/**
	 * <dd>方法名：指定不可用控件ݒ
	 * <dd>功能概要：指定不可用控件
	 * <dd>备注
	 * 
	 * @param item
	 *            String
	 */
	public void setDisableItem(String disableItem) {
		this.disableItem = disableItem;
	}

	/**
	 * <dd>方法名：添加设定一个不控件ݒ
	 * <dd>功能概要：添加设定一个不控件
	 * <dd>备注
	 */
	protected void addDisableJs() {
		this.disableItem = this.beforeDisableItems;
	}

	/**
	 * <dd>方法名：清除控件不可用状态ݒ
	 * <dd>功能概要：清除控件不可用状态
	 * <dd>备注
	 */
	public void clearDiableJs() {
		this.beforeDisableItems = this.disableItem;
		this.disableItem = "";
	}

	/**
	 * <dd>方法名：判断是有不可用代码ݒ
	 * <dd>功能概要：判断是有不可用代码
	 * <dd>备注
	 * 
	 * @return boolean LtO
	 */
	protected boolean isHasDisableCode() {

		return ((this.beforeDisableItems != null) && (this.beforeDisableItems
				.trim().length() > 0));
	}

	/**
	 * <dd>方法名：得到焦点控件名ݒ
	 * <dd>功能概要：得到焦点控件名
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getFocusItem() {

		return focusItem;
	}

	/**
	 * <dd>方法名：设定焦点ݒ
	 * <dd>功能概要：设定焦点
	 * <dd>备注
	 * 
	 * @param itemNm
	 * @param index
	 */
	private void setFocusItem(String itemNm, String index) {
		this.focusItem = itemNm + '|' + index;
	}

	/**
	 * <dd>方法名：得到不可用控件ݒ
	 * <dd>功能概要：得到不可用控件
	 * <dd>备注
	 * 
	 * @return 񊈐ڐݒljava.String
	 */
	public String getDisableItem() {

		return disableItem;
	}

	/**
	 * <dd>方法名：指定不可用控件ݒ
	 * <dd>功能概要：指定不可用控件
	 * <dd>备注
	 * 
	 * @param itemNm
	 * @param index
	 */
	private void setDisableItem(String itemNm, String index) {
		this.disableItem += (itemNm + '|' + index + '|');
	}

	/**
	 * <dd>方法名：得到分页的总页数ݒ
	 * <dd>功能概要：得到分页的总页数
	 * <dd>备注
	 * 
	 * @return int
	 */
	public int getPageCount() {

		return (int) Math.ceil((double) recordCount / getPageRecord());
	}

	/**
	 * <dd>方法名：得到当前页数ݒ
	 * <dd>功能概要：得到当前页数
	 * <dd>备注
	 * 
	 * @return int Jgy[Wԍ
	 */
	public int getNowPage() {

		return nowPage;
	}

	/**
	 * <dd>方法名：设定当前分页数ݒ
	 * <dd>功能概要：设定当前分页数
	 * <dd>备注
	 * 
	 * @param nowPageԍ
	 */
	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}

	/**
	 * <dd>方法名：得到分页中一页的最大记录数
	 * <dd>功能概要：得到分页中一页的最大记录数
	 * <dd>备注
	 * 
	 * @return int yh
	 */
	public int getPageRecord() {

		return pageRecord;
	}

	/**
	 * <dd>方法名：指定分页中一页的最大记录数ݒ
	 * <dd>功能概要：指定分页中一页的最大记录数
	 * <dd>备注
	 * 
	 * @param pageRecord
	 */
	public void setPageRecord(int pageRecord) {
		this.pageRecord = pageRecord;
	}

	/**
	 * <dd>方法名：得到消息ݒ
	 * <dd>功能概要：得到消息
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * <dd>方法名：得到客户端IP
	 * <dd>功能概要：得到客户端IP
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getClnm() {

		return clnm;
	}

	/**
	 * <dd>方法名：指定客户端IP
	 * <dd>功能概要指定客户端IP
	 * <dd>备注
	 * 
	 * @param clnm
	 */
	public void setClnm(String clnm) {
		this.clnm = clnm;
	}

	/**
	 * <dd>方法名：指定确认对话框信息ݒ
	 * <dd>功能概要：指定确认对话框信息
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getConfirmScript() {

		return confirmScript;
	}

	/**
	 * <dd>方法名：得到状态ݒ
	 * <dd>功能概要：得到状态
	 * <dd>备注
	 * 
	 * @return String ʓ
	 */
	public String getState() {

		return state;
	}

	/**
	 * <dd>方法名：指定确认对话框信息ݒ
	 * <dd>功能概要：指定确认对话框信息
	 * <dd>备注
	 * 
	 * @param confirmScript
	 */
	public void setConfirmScript(String confirmScript) {
		this.confirmScript = confirmScript;
	}

	/**
	 * <dd>方法名：指定状态ݒ
	 * <dd>功能概要：指定状态
	 * <dd>备注
	 * 
	 * @param state
	 *            ʓ String
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * <dd>方法名：得到二次提交状态标记ݒ
	 * <dd>功能概要：得到二次提交状态标记
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getDoubleVerb() {

		return doubleVerb;
	}

	/**
	 * <dd>方法名：指定二次提交状态标记ݒ
	 * <dd>功能概要：指二次提交状态标记
	 * <dd>备注
	 * 
	 * @param doubleVerb
	 */
	public void setDoubleVerb(String doubleVerb) {
		this.doubleVerb = doubleVerb;
	}

	/**
	 * <dd>方法名：得到错误控件ݒ
	 * <dd>功能概要：得到错误控件
	 * <dd>备注
	 * 
	 * @return String G[ڐݒl
	 */
	public String getErrorItem() {

		return errorItem;
	}

	/**
	 * <dd>方法名：指定错误控件ݒ
	 * <dd>功能概要：指定错误控件
	 * <dd>备注
	 * 
	 * @param itemNm
	 *            String
	 * @param index
	 */
	private void setErrorItem(String itemNm, String index) {
		this.errorItem += (itemNm + '|' + index + '|');
	}

	/**
	 * <dd>方法名：得到错误代号集ݒ
	 * <dd>功能概要：得到错误代号集
	 * <dd>备注
	 * 
	 * @return List ̃R
	 */
	public List getError() {

		return errorCode;
	}

	/**
	 * <dd>方法名：指定消息ݒ
	 * <dd>功能概要：指定消息
	 * <dd>备注
	 * 
	 * @param errCode
	 */
	public void setMessage(String errCode) {
		if (isSetFlg) {
			return;
		}
		setMessage(errCode, "");

	}

	/**
	 * <dd>方法名：指定消息ݒ
	 * <dd>功能概要：指定消息
	 * <dd>备注
	 * 
	 * @param errCode
	 * @param param1
	 */
	public void setMessage(String errCode, String param1) {
		setMessage(errCode, param1, "");

	}

	/**
	 * <dd>方法名：指定消息ݒ
	 * <dd>功能概要：指定消息
	 * <dd>备注
	 * 
	 * @param errCode
	 * @param param1
	 * @param param2
	 */
	public void setMessage(String errCode, String param1, String param2) {
		this.message = errCode;
	}

	/**
	 * <dd>方法名：最终制定消息内容（共通用）ݒ
	 * <dd>功能概要：最终制定消息内容（共通用）
	 * <dd>备注
	 * 
	 * @param msg
	 */
	public void setfinalMessage(String msg) {
		this.message = msg;
		isSetFlg = true;
	}
	public void setError(List errors){
		 this.errorCode = errors ;
	}
	/**
	 * <dd>方法名：指定错误ݒ
	 * <dd>功能概要：指定错误
	 * <dd>备注
	 * 
	 * @param errCd /
	 *            public void setError(String errCd) { setError(errCd, ""); }
	 *            /**
	 *            <dd>方法名：指定错误ݒ
	 *            <dd>功能概要：指定错误
	 *            <dd>备注
	 * @param errCd
	 * @param param
	 */
	public void setError(String item, String errCd) {
		setError(item, errCd, "");
	}

	/**
	 * <dd>方法名：指定错误ݒ
	 * <dd>功能概要：指定错误
	 * <dd>备注
	 * 
	 * @param item
	 * @param errCd
	 * @param param
	 */
	public void setError(String item, String errCd, String param) {
		setError(item, errCd, param, "");
	}

	/**
	 * <dd>方法名：指定错误ݒ
	 * <dd>功能概要：指定错误
	 * <dd>备注
	 * 
	 * @param item
	 * @param errCd
	 * @param param1
	 * @param index
	 */
	public void setError(String item, String errCd, String param1, String index) {
		setError(item, errCd, param1, "", index);
	}

	/**
	 * <dd>方法名：指定错误ݒ
	 * <dd>功能概要：指定错误
	 * <dd>备注
	 * 
	 * @param item
	 * @param errCd
	 * @param param1
	 * @param param2
	 * @param index
	 */
	public void setError(String item, String errCd, String param1,
			String param2, String index) {

		if (isSetFlg) {
			return;
		}

		String msg;

		if ((item != null) && !item.equals("")) {
			setErrorItem(item, index);
		}

		// 
		if (!errSetFlg) {
			setFocusItem(item, index);
			errSetFlg = true;
			setMessage(errCd, param1, param2);
			setMessageColor("red");
		}

	}

	/**
	 * <dd>方法名：得到消息字体颜色ݒ
	 * <dd>功能概要：得到消息字体颜色
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getMessageColor() {

		return messageColor;
	}

	/**
	 * <dd>方法名：指定消息字体颜色ݒ
	 * <dd>功能概要：指定消息字体颜色
	 * <dd>备注
	 * 
	 * @param messageColor
	 */
	public void setMessageColor(String messageColor) {
		this.messageColor = messageColor;
	}

	/**
	 * <dd>方法名：清楚错误信息ݒ
	 * <dd>功能概要：清楚错误信息
	 * <dd>备注܂B
	 */
	public void clearError() {
		errorCode = new ArrayList();
		message = "";
		setMessageColor("#000000");
		beforeFocusItem = this.focusItem;
		errSetFlg = false;
		focusItem = "";
		errorItem = "";
	}

	/**
	 * <dd>方法名：得到登陆者角色ݒ
	 * <dd>功能概要：得到登陆者角色
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getUserRole() {
		return userRole;
	}

	/**
	 * <dd>方法名：设定登陆者角色
	 * 
	 * <dd>功能概要：设定登陆者角色
	 * <dd>备注
	 * 
	 * @param userCent
	 */
	public void setUserRole(String userCent) {
		this.userRole = userCent;
	}

	/**
	 * <dd>方法名：得到登陆者编号ݒ
	 * <dd>功能概要：得到登陆者编号
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getUserCode() {
		return userCode;
	}

	/**
	 * <dd>方法名：设定登陆者编号
	 * <dd>功能概要：设定登陆者编号
	 * <dd>备注
	 * 
	 * @param userCode
	 */
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	/**
	 * <dd>方法名：得到登陆者名称ݒ
	 * <dd>功能概要：得到登陆者名称
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * <dd>方法名：设定登陆者名称
	 * <dd>功能概要：设定登陆者名称
	 * <dd>备注
	 * 
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * <dd>方法名：设定会话信息
	 * <dd>功能概要：设定会话信息
	 * <dd>备注
	 * 
	 * @param userName
	 */
	public void setSessionData(HttpServletRequest request) {

		BaseSession se = null;
		SID userInfo = null;

		se = BaseSession.getSession(request);

		if (se != null) {
			userInfo = se.getUser();
		}

		if (userInfo != null) {
			this.setUserCode(userInfo.getAccountID()+""); // 登陆用户编号
			this.setUserName(userInfo.getUserName()); // 登陆用户名
			this.setUserRole(userInfo.getUserType()); // 登陆用户角色

		}

		if (!("startUp").equals(request.getParameter("verb"))
				&& !("logout").equals(request.getParameter("verb"))) {
			setLogoutScript("<A href=\"#\" onclick=\"logout()\">\u9000\u51fa</A>");
		}
	}

	/**
	 * <dd>方法名：得到登出(JSP页面)ݒ
	 * <dd>功能概要：得到登出(JSP页面)
	 * <dd>备注
	 * 
	 * @return String
	 */
	public String getLogoutScript() {
		return logoutScript;
	}

	/**
	 * <dd>方法名：设定登出(JSP页面)
	 * <dd>功能概要：设定登出(JSP页面)
	 * <dd>备注
	 * 
	 * @param logoutScript
	 */
	public void setLogoutScript(String logoutScript) {
		this.logoutScript = logoutScript;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
