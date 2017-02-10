package com.topsec.tsm.sim.util;

import java.util.Comparator;

import com.topsec.tsm.ui.util.tree.TreeNode;

/**
* 功能描述: 拼音比较类
*/
class PinYinCompartor implements Comparator<TreeNode>{
	
	private static final String TOPSEC="TOPSEC";
	private static final String Unknown="Unknown";
	
	

	@Override
	public int compare(TreeNode o1, TreeNode o2) {
		String nodeTextO1 = o1.getNodeText(); 
		String nodeTextO2 = o2.getNodeText();
		
		if(Unknown.equalsIgnoreCase(nodeTextO1)){
			return 1;
		}else if(Unknown.equalsIgnoreCase(nodeTextO2)){
			return -1;
		}
		
		if (!TOPSEC.equalsIgnoreCase(nodeTextO1) && !TOPSEC.equalsIgnoreCase(nodeTextO2)) { 
			 String o1ZH = o1.getNodeTextZhCN();
			 String o2ZH = o2.getNodeTextZhCN();
			 String[] o1PinyinArray = PinyingUtil.stringToPinyin(o1ZH);
			 String[] o2PinyinArray = PinyingUtil.stringToPinyin(o2ZH);
			if(o1PinyinArray!=null&&o2PinyinArray!=null){
				String o1Pinyin = PinyingUtil.stringArrayToString(o1PinyinArray);
				String o2Pinyin = PinyingUtil.stringArrayToString(o2PinyinArray);
				int i=o1Pinyin.compareToIgnoreCase(o2Pinyin);
				return i;
			}else{
				return 0;
			}
		} else {
			if (nodeTextO1.equalsIgnoreCase(nodeTextO2)) {
				return 0;
			} else if (TOPSEC.equalsIgnoreCase(nodeTextO1)) {
				return -1;
			} else if (TOPSEC.equalsIgnoreCase(nodeTextO2)) {
				return 1;
			}
		}
		return 0;
	}
}
