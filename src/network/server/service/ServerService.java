package network.server.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import network.server.dao.Device;
import network.server.vo.Message;
import network.server.vo.Sensor;
import network.server.vo.SensorData;


public class ServerService {
	
	private static final String DEVICE_ID = "SERVER";
	private static final String DEVICE_TYPE = "SERVER";
	
	private Map<String, Device> deviceList = new ConcurrentHashMap<String, Device>();
	private Map<String, Device> userList = new ConcurrentHashMap<String, Device>();
	private Map<String, Sensor> sensorList = new ConcurrentHashMap<String, Sensor>();
	private List<String> typeHEAT = new ArrayList<String>();		// List<DeviceID>
	private List<String> typeCOOL = new ArrayList<String>();
	private List<String> typeTEMP = new ArrayList<String>();
	private List<String> typeBED = new ArrayList<String>();
	private List<String> typeLIGHT = new ArrayList<String>();
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
	
	// Singleton
	private ServerService() {}
	
	private static class InstanceHandler {
		public static final ServerService INSTANCE = new ServerService();
	}
	
	public static ServerService getInstance() {
		return InstanceHandler.INSTANCE;
	}
	
	//
	public static String getDeviceId() {
		return DEVICE_ID;
	}
	
	public static String getDeviceType() {
		return DEVICE_TYPE;
	}
	
	
	// method
	public Device add(String deviceID, String deviceType, Socket socket) {
//		this.list.put(c.getDeviceID(), c);
		Device device = null;
		if(deviceType.equals("USER")) {
			if((device = userList.get(deviceID)) == null) {
				/* First connection check : true
				 * Create new Device.class			*/
				device = new Device(deviceID, socket);
				userList.put(deviceID, device);
			} else {
				/* First connection check : false
				 * Update the socket				*/
				device.setSocket(socket);
			}
		} else {
			if((device = deviceList.get(deviceID)) == null) {
				/* First connection check : true
				 * Create new Device.class			*/
				device = new Device(deviceID, socket);
				deviceList.put(deviceID, device);
			} else {
				/* First connection check : false
				 * Update the socket				*/
				device.setSocket(socket);
			}
		}
		
		return device;
	}

	public void remove(Device c) {
		this.deviceList.remove(c.getDeviceID());
	}
	
	public Device get(String id) {
		return this.deviceList.get(id);
	}
	
	public Map<String, Device> getList() {
		return this.deviceList;
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
