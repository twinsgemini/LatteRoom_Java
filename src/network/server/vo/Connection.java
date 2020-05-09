package network.server.vo;

import java.net.Socket;

public interface Connection {
	
	public String getDeviceID();
	public void setSocket(Socket socket);
	public void receive();
	public void send(String msg);
	public void close();
	
}
