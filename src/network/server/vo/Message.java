package network.server.vo;

import com.google.gson.Gson;

import network.server.dao.Device;

public class Message {
	private String deviceID;
	private String voType;
	private String jsonData;
	private static Gson gson = new Gson();
	
	// constructor
	private Message() {
        this.deviceID = Device.getDeviceID();
    }
	
	public Message(SensorData data) {
        this();
        this.voType = "SensorData";
        this.jsonData = Message.gson.toJson(data);
    }
	
	public Message(Alert data) {
        this();
        this.voType = "Alert";
        this.jsonData = Message.gson.toJson(data);
    }
	
	public Message(String states, String stateDetail) {
		this();
		this.voType = "Request";
		this.jsonData = Message.gson.toJson(new SensorData(states, stateDetail));
	}
	
	// get, set method
	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getVoType() {
		return voType;
	}

	public void setVoType(String voType) {
		this.voType = voType;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	@Override
	public String toString() {
		return "Message [deviceID=" + deviceID + ", voType=" + voType + ", jsonData=" + jsonData + "]";
	}
	
}
