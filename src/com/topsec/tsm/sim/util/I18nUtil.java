package com.topsec.tsm.sim.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18nUtil {
	public static String getI18nValue(Locale locale, String key) {
		if (key == null)
			throw new NullPointerException();		
		ResourceBundle rb = ResourceBundle.getBundle("resources.application", locale);
		String value = null;
		try {
			value = rb.getString(key);
		} catch (MissingResourceException e) {

		}
		return value != null ? value : key;
	}
}
