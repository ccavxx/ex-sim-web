package com.topsec.tsm.sim.report.common;

import com.topsec.tsm.common.CallableCallback;
import com.topsec.tsm.sim.report.component.ChartSubject;

/**
 * 主题图片生成回调接口
 * 在主题的图片生成完成以后，将图片路径赋值给对应的ChartSubject
 * @author hp
 *
 */
public class SubjectImageCallback implements CallableCallback<String> {

	private ChartSubject subject ;
	
	public SubjectImageCallback(ChartSubject subject) {
		this.subject = subject;
	}

	@Override
	public void callback(String callResult) {
		if(callResult!=null){
			subject.getChartImage().setImageFile(callResult.replace('\\', '/')) ;
		}
	}
}
