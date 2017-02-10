package com.topsec.tsm.sim.newreport.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @ClassName: Messages
 * @Declaration: TODO
 * 
 * @author: WangZhiai create on2016年5月27日下午3:48:02
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class Messages {
	private static final String BUNDLE_NAME = "com.topsec.tsm.sim.newreport.model.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
