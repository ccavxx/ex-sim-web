package com.topsec.tsm.sim.report.component;

/**
 * 图像填充模式
 * 此类定义在图像大小与指定的大小不一致时使用填充方式<br/>
 * CLIP图像在区域的边缘处结束。<br/>
 * REPEAT位图将重复以填充区域。<br/>
 * SCALE图像将拉伸以填充区域。<br/>
 * @author hp
 *
 */
public enum ImageFillMode {
	CLIP,REPEAT,SCALE
}
