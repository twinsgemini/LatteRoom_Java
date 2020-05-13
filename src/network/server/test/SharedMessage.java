package network.server.test;

import java.util.LinkedList;

import network.server.vo.Message;

public class SharedMessage {
	
	private LinkedList<Message> list = new LinkedList<Message>();
	private static final Object MONITOR = new Object();
	
	private SharedMessage() {}
	
	private static class SingleHandler {
		public static final SharedMessage INSTANCE = new SharedMessage();
	}
	
	public static SharedMessage getInstance() {
		return SingleHandler.INSTANCE;
	}
	
	
	/* method */
	public void put(Message data) {
//		System.out.println("out sync block");
		synchronized (MONITOR) {
//			System.out.println("in sync block");
			list.add(data);
//			System.out.println("in sync block2");
			MONITOR.notify();
//			System.out.println("in sync block3");
		}
//		System.out.println("finish sync block");
	} // put()
	
	public Message pop() {
		Message data = null;
		synchronized (MONITOR) {
			try {
				while(list.isEmpty()) {
					MONITOR.wait();
				}
				data = list.removeFirst();
			} catch (Exception e) {
				// do nothing
			}
		}
		return data;
	} // pop()

}
