////////////////////////////////////////////////////////////////////////////
// 系统名称：TA-L 日志审计系统
// 包: com.topsec.tsm.tal.ui.search.action
// 类：LogSearchTimerTask
// 文件名：LogSearchTimerTask
// 开发：天融信TSM Ta-l项目组
//
//Copyright (c) 2012 Topsec.
//
// Rev.   日期                                  部门                      担当               备注  
// 1.0.0  2012-03-03   	   TA-L       周小虎      新增开发
////////////////////////////////////////////////////////////////////////////
package  com.topsec.tsm.sim.log.util;

import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import com.topsec.tsm.sim.log.web.LogSearchController;

public class LogSearchTimerTask extends TimerTask {

	@Override
	public void run() {
//		System.out.println("????");
		while(true){
			try{
				Set<String> ks = LogSearchController.queryMap.keySet();
//				System.out.println(ks);
				for (Iterator<String> itKs = ks.iterator(); itKs.hasNext();) {
					String key = itKs.next();
//				for (String key : LogSearcherAction.queryMap.keySet()) {
					int count = LogSearchController.queryMap.get(key).getCounts();
					if(count >= 12){
						LogUtil lu = LogSearchController.queryMap.get(key);
						lu.getSearchObject().setCancel(true);
						LogSearchController.stopSearch(lu);
						LogSearchController.queryMap.remove(key);
//						System.out.println("后台取消");
					}else{
						LogSearchController.queryMap.get(key).setCounts(count+1);
//						System.out.println("++"+key+" "+LogSearcherAction.queryMap.get(key).getCounts());
					}
				}
				Thread.sleep(5000);
			}catch(Exception e){
				
			}
		}
	}

}
