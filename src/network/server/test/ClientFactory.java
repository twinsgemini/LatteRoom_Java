package network.server.test;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ClientFactory {
	
	INSTANCE;
	
	private Map<Integer, AbstractClient> list = new ConcurrentHashMap<Integer, AbstractClient>();
	
	
	public static ClientFactory getInstance() {
		return INSTANCE;
	}
	
	//
	public boolean isExist(String id) {
		return list.containsKey(id);
	}
	
	public AbstractClient createClient(String id, String type, Socket socket) {
		AbstractClient client = null;
		
		if(!isExist(id)) {
			switch (type) {
			case "DEVICE":
				client = new Device();
				break;
				
			case "USER":
				client = new User();
				break;

			default:
				break;
			}
		} else {
			client = list.get(id);
			client.setSocket(socket);
		}
		
		return client;
	}
	
}
