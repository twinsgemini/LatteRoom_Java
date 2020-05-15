package network.server.vo;

import network.server.dao.Device;

public class Sensor {
	private String deviceID;
	private String sensorID;
	private String sensorType;
	private SensorData recentData;
	
	
	// constructor
	public Sensor(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public Sensor(Device device, String sensorType) {
		this.deviceID = device.getDeviceID();
		this.sensorType = sensorType;
	}
	
	
	// custom method
	public String getStates() {
        return this.recentData.getStates();
    }

    public String getStateDetail() {
        return this.recentData.getStateDetail();
    }
    
    // 지정된 센서에 최신 데이터 업데이트 (states)
    public SensorData setRecentData(String states) {
//        this.recentData = new SensorData(this.sensorID, states);
    	this.recentData.update(states);
        return this.recentData;
    }

    // 지정된 센서에 최신 데이터 업데이트 (states, stateDetail)
    public SensorData setRecentData(String states, String stateDetail) {
//        this.recentData = new SensorData(this.sensorID, states, stateDetail);
        this.recentData.update(states, stateDetail);
        return this.recentData;
    }
	
	
	// get, set
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
	
//	public void setRecentData(SensorData recentData) {
//		this.recentData = recentData;
//	}
	
	public SensorData setRecentData(SensorData data) {
        this.recentData = data;
        return this.recentData;
    }
	
}
