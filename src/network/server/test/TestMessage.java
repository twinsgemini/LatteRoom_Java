package network.server.test;

import com.google.gson.Gson;

import network.server.vo.Message;
import network.server.vo.SensorData;

public class TestMessage {

	public static void main(String[] args) {
		
		Gson gson = new Gson();
		
		SensorData currData = new SensorData("30");
		
		Message data = new Message(currData);
		
		String jsonData = gson.toJson(data);
		
		System.out.println(jsonData);
		
		Message fromJson = gson.fromJson(jsonData, Message.class);
		System.out.println(fromJson.toString());
		
		SensorData msgJsonData = gson.fromJson(fromJson.getJsonData(), SensorData.class);
		System.out.println(msgJsonData.toString());
	}

}
