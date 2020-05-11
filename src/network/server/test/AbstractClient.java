package network.server.test;

import java.net.Socket;

public abstract class AbstractClient implements Runnable{
	
	public abstract String getDeviceID();
	public abstract void setSocket(Socket socket);
	public abstract void receive();
	public abstract void send(String msg);
	public abstract void close();
	
}
