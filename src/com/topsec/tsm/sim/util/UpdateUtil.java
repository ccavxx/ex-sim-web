package com.topsec.tsm.sim.util;

import com.topsec.tsm.update.manager.ApplyPatch;
import com.topsec.tsm.update.manager.Common;
import com.topsec.tsm.update.manager.Updator;

/*
 * 校验上传的升级包名称
 */
public class UpdateUtil extends Updator {

	public UpdateUtil(String patch, String patchVersion) {
		super(patch, patchVersion);
	}

	private void startInstalling(ApplyPatch apply) {
		try {
			Thread installThread = new Thread(apply);
			installThread.start();
			installThread.join();
		} catch (Exception exp) {
		}
	}

	/**
	 * 比较上传的升级包里面配置文件patch-info.xml的patchVersion的值是否与上传包的文件名一样
	 * 
	 * @param patch
	 *           升级包的绝对路径
	 * @param version
	 *           升级包的名称
	 * @return true 则相同，false不相同
	 */
	public static boolean checkNodeFile(String patch, String version) {
		Common common = new Common(patch);
		if (common.extractPatchInfo()) {
			String nVersion = common.getPatchVersion();
			if (version.equalsIgnoreCase(nVersion + ".sp")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 比较上传的升级包里面配置文件patch-info.xml的patchVersion的值是否与上传包的文件名一样
	 * 
	 * @param patch
	 *           升级包的绝对路径
	 * @param version
	 *           升级包的名称
	 * @return true 则相同，false不相同
	 */
	public static boolean checkFile(String patch, String version) {
		Common common = new Common(patch);

		if (common.extractPatchInfo()) {
			String nVersion = common.getPatchVersion();

			if (version.equalsIgnoreCase(nVersion + ".sp")) {// 自诉文件中的版本号与文件名是否一样
				String verStrs = nVersion.split("-")[0];
				String _version = verStrs.substring(3, verStrs.length()).replace("_", "");
				// String _oVersion = "3.1.001.036";// 现有的版本 如 格式3.1.001.022
				String _oVersion = System.getProperty("tal.version");// 现有的版本 如 格式3.1.001.022
				String oVersion = _oVersion.replace(".", "");
				if (oVersion.equals(_version)) {// 而且 如WEB_AAA-BBB，AAA与现在的版本一样
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检查上传升级包的版本不能低于现有的版本
	 * 
	 * @param patch
	 * @return
	 */
	public static boolean checkVersion(String patch) {
		Common common = new Common(patch);
		String _oVersion = System.getProperty("tal.version");// 现有的版本 如 格式3.1.001.022
		// String _oVersion = "3.1.001.036";
		if (common.extractPatchInfo() && _oVersion != null) {
			String _nVersion = common.getPatchVersion();// 上传升级包的版本 如格式WEB_3_1_001_002-3_1_001_003
			int nVersion = Integer.parseInt(_nVersion.split("-")[1].replace("_", ""));
			int oVersion = Integer.parseInt(_oVersion.replace(".", ""));
			if (nVersion >= oVersion) {// 不能低于现在的版本
				return true;
			}

		}
		return false;

	}

	/**
	 * 不能低于现有的版本是否需要重新启动
	 * 
	 * @param patch
	 * @return
	 */
	public static boolean checkRestart(String patch) {
		Common common = new Common(patch);
		if (common.extractPatchInfo()) {
			String restart = common.getRestart();
			if (restart != null) {
				return Boolean.valueOf(restart);
			}
		}
		return true;
	}

}
