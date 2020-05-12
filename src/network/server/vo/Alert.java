package network.server.vo;

import network.server.dao.Device;

public class Alert {
	private String deviceID;
    private int hour;           // 시간
    private int min;            // 분
    private String weeks;       // 알람 수행 요일
    private boolean flag;       // 알람 on/off

    private Alert() {
    	this.deviceID = Device.getDeviceID();
    }

    public Alert(int hour, int min, String weeks, boolean flag) {
        this();
        this.hour = hour;
        this.min = min;
        this.weeks = weeks;
        this.flag = flag;
    }
	
}
