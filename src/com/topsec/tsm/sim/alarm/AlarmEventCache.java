/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-06-24
* @version 1.0
* 
*/
package com.topsec.tsm.sim.alarm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
* 功能描述: 告警事件缓存队列
*/
public class AlarmEventCache {
	
	private static AlarmEventCache instance;
	
	private final int maxLength=2000; 
	
	private List<Map<String,Object>> list;
	
	private AlarmEventCache(){
		list=new CopyOnWriteArrayList<Map<String,Object>>();
	}
	
	public synchronized static AlarmEventCache getInstence(){
		if(instance==null){
			instance=new AlarmEventCache();
		}
		return instance;
	}
	
	/***
	 *  得到第一个元素, 并删除队列中的元素
	 */
	public  Map<String,Object> pollFirst(){
		Map<String,Object> m=null;
		synchronized(list) {
			int size = list.size();
			if(size>0){
				m=list.get(size-1);
				list.remove(size-1);
			}
		}
		return m;
	}
	
	/**
	 * 在队首加入一个元素
	 */
	public void add(Map<String,Object> map){
		synchronized(list) {
			if(list.size()>=maxLength){
				list.remove(maxLength-1);
			}
			list.add(0,map);
		}
	}
	
	/**
	 * 队列大小
	 * @return
	 */
	public int size(){
		return list.size();
	}
	
	/**
	 * 得到元素
	 */
	public Map<String,Object> get(int index){
		int size=size();
		if(size==0){
			return null;
		}
		if(index>=size||index<0){
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * 得到第一个元素
	 */
	public Map<String,Object> getFirst() {
		if(list.size()==0){
			return null;
		}
		
		return list.get(list.size()-1);
	}
	
	/**
	 * 清空
	 */
	public void clear() {
		 list.clear();
	}
	
	/**
	 * 返回所有元素
	 */
	public List<Map<String,Object>> list() {
		try {
			return deepCopy(list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Map<String, Object>>();
	}

	private List deepCopy(List src) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(src);

		ByteArrayInputStream byteIn = new ByteArrayInputStream(
				byteOut.toByteArray());
		ObjectInputStream in = new ObjectInputStream(byteIn);
		List dest = (List) in.readObject();
		return dest;
	}

}
