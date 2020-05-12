package network.server.vo;

import network.server.dao.Device;

public class Sensor {
	private String deviceID;
	private String sensorID = "" + this.hashCode();
	private String sensorType;
	private SensorData recentData;
	
	
	//
	public Sensor(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public Sensor(Device device, String sensorType) {
		this.deviceID = device.getDeviceID();
		this.sensorType = sensorType;
	}
	
	
	//
	public String getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getSensorID() {
		return sensorID;
	}
	
	public void setSensorID(String sensorID) {
		this.sensorID = sensorID;
	}
	
	public String getSensorType() {
		return sensorType;
	}
	
	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public SensorData getRecentData() {
		return recentData;
	}
	
	public void setRecentData(SensorData recentData) {
		this.recentData = recentData;
	}
	
}
