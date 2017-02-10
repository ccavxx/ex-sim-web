package com.topsec.tsm.sim.node.util;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.lang.Validate;

/**
 * 功能描述: 节点队列
 */
public class NodeStatusQueue {
	private int capacityNum;
	private Vector<Object> vector;

	public NodeStatusQueue() {
		vector = new Vector<Object>();
		this.capacityNum = 10;
	}

	public NodeStatusQueue(int capacity) {
		vector = new Vector<Object>(capacity);
		capacityNum = capacity;
	}

	public synchronized void push(Object x) throws Exception {
		if (this.vector.size() == this.capacityNum) {
			this.pop();
		}
		vector.addElement(x);
	}

	public synchronized Object pop() throws Exception {
		if (this.isEmpty())
			throw new Exception();
		Object x = vector.elementAt(0);
		vector.removeElementAt(0);
		return x;
	}

	public Object firstElement() throws Exception {
		if (this.isEmpty())
			throw new Exception();
		return vector.firstElement();
	}

	public boolean isEmpty() {
		return vector.isEmpty();
	}

	public Enumeration<Object> getElements() {
		return vector.elements();
	}
	
	public int size(){
		return vector.size();
	}
	
	public Object lastElement(){
		return vector.lastElement();
	}
	
	public Object get(int i){
		//true不抛异常
		Validate.isTrue(i>=0&&i<size());
		return vector.get(i);
	}
}
