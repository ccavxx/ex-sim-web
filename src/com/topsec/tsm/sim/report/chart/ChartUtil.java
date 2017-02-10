package com.topsec.tsm.sim.report.chart;

import java.awt.Color;

public class ChartUtil {

	public static Color getColor(Color color) {
		return color;
	}

	public static String getHexColor(Color color) {
		if (color != null) {
			String red = Integer.toHexString(color.getRed());
			if (red.length() == 1 && red.equals("0")) {
				red = "00";
			} else if (red.length() == 1) {
				red = "0" + red;
			}
			String green = Integer.toHexString(color.getGreen());
			if (green.length() == 1 && green.equals("0")) {
				green = "00";
			} else if (green.length() == 1) {
				green = "0" + green;
			}
			String blue = Integer.toHexString(color.getBlue());
			if (blue.length() == 1 && blue.equals("0")) {
				blue = "00";
			} else if (blue.length() == 1) {
				blue = "0" + blue;
			}
			return "#" + red + green + blue;
		}
		return "";
	}
}
