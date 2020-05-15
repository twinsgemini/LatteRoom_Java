import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import arduino.device.vo.Sensor;

public class test {
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
		
		
		String s = "{\"HEAT\":{\"deviceID\":\"\",\"sensorID\":\"HEAT\",\"sensorType\":\"HEAT\"}}";
		
		
		Map<String,Sensor> map = new HashMap<String,Sensor>();
		map = new Gson().fromJson(s, map.getClass());
		System.out.println(map.toString());
		
		
	}

}
