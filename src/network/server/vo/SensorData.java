package network.server.vo;

import java.sql.Date;

public class SensorData {
	private int dataID;
	private String sensorID;
	private Date time;
	private String states;
	private String stateDetail;
	
	
	//
	private SensorData() {
		this.dataID = this.hashCode();
		this.time = new Date(System.currentTimeMillis());
	}
	
	public SensorData(String states) {
		this();
		this.states = states;
	}
	
	public SensorData(String states, String stateDetail) {
		this(states);
		this.stateDetail = stateDetail;
	}
	
	
}
