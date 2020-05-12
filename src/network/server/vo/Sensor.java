package network.server.vo;

import network.server.dao.Device;

public class Sensor {
	private String deviceID;
	private String sensorID;
	private String sensorType;
	private SensorData recentData;
	
	
	//
	public Sensor(String sensorID, String sensorType) {
		this.sensorID = sensorID;
		this.sensorType = sensorType;
	}
	
//	public Sensor()
	
	
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
