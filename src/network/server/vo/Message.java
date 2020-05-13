package network.server.vo;

import com.google.gson.Gson;

import network.server.dao.Device;

public class Message {
	private String deviceID;
	private String dataType;
	private String jsonData;
	private static Gson gson = new Gson();
	
	
	// constructor
	private Message() {
        this.deviceID = Device.getDeviceID();
    }
	
	public Message(SensorData data) {
        this();
        this.dataType = "SensorData";
        this.jsonData = Message.gson.toJson(data);
    }
	
	public Message(Alert data) {
        this();
        this.dataType = "Alert";
        this.jsonData = Message.gson.toJson(data);
    }
	
	public Message(String id, String type, String data) {
		this.deviceID = id;
		this.dataType = type;
		this.jsonData = data;
	}
	
	
	// custom method
	public SensorData getSensorData() {
		return Message.gson.fromJson(this.jsonData, SensorData.class);
	}
	
	public Alert getAlertData() {
		return Message.gson.fromJson(this.jsonData, Alert.class);
	}
	
	public String getRequestData() {
		return this.jsonData;
	}
	
	
	// get, set method
	public String getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public String getJsonData() {
		return jsonData;
	}
	
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
	
	@Override
	public String toString() {
		return "Message [deviceID=" + deviceID + ", voType=" + dataType + ", jsonData=" + jsonData + "]";
	}
	
}
