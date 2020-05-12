package network.server.vo;

public class Message {
	private String deviceID;
	private String voType;
	private String jsonData;
	
	public Message(String id, String type, String data) {
		this.deviceID = id;
		this.voType = type;
		this.jsonData = data;
	}
	
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
	
}
