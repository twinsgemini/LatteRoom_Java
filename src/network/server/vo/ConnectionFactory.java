package network.server.vo;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ConnectionFactory {
	
	INSTANCE;
	
	private Map<Integer, Connection> list = new ConcurrentHashMap<Integer, Connection>();
	
	
	public static ConnectionFactory getInstance() {
		return INSTANCE;
	}
	
	//
	public boolean isExist(String id) {
		return list.containsKey(id);
	}
	
	public Connection createConnection(String id, String type, Socket socket) {
		Connection conn = null;
		
		if(!isExist(id)) {
			switch (type) {
			case "DEVICE":
				conn = new Device();
				break;
				
			case "USER":
				conn = new User();
				break;

			default:
				break;
			}
		} else {
			conn = list.get(id);
			conn.setSocket(socket);
		}
		
		return conn;
	}
	
}
