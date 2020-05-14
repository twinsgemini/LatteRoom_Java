package network.server.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import network.server.dao.Device;
import network.server.test.AbstractClient;
import network.server.test.SharedMessage;
import network.server.test.User;
import network.server.vo.Message;
import network.server.vo.SensorData;


public class ServerService {
	
	private static final Object MONITOR = new Object();
	
	private Map<String, Device> list = new ConcurrentHashMap<String, Device>();
	private Gson gson=  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
	
	private ServerService() {}
	
	private static class InstanceHandler {
		public static final ServerService INSTANCE = new ServerService();
	}
	
	public static ServerService getInstance() {
		return InstanceHandler.INSTANCE;
	}
	
	//
	public void add(Device c) {
		this.list.put(c.getDeviceID(), c);
	}
	
	public void remove(Device c) {
		this.list.remove(c.getDeviceID());
	}
	
	public Device get(String id) {
		return this.list.get(id);
	}
	
	public Map<String, Device> getList() {
		return this.list;
	}
	
	public void dataHandler(String jsonData) {
		Message data = gson.fromJson(jsonData, Message.class);
		System.out.println(data.getDeviceID() + " : " + data.getJsonData());
		
		if (data.getDataType().equals("SensorData")) {
//			SensorData sensorData = gson.fromJson(data.getJsonData(), SensorData.class);
			SensorData sensorData = data.getSensorData();
		}
		
	}
	
}
