package network.server.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import network.server.service.ServerService;
import network.server.vo.Message;

public class Device implements Runnable {
	
	public static String deviceID = "A0001";
//	public static String deviceID = "" + Client.hashCode();
//	private static String deviceID;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private ServerService service = ServerService.getInstance();
 	private static Gson gson = new Gson();
	
 	
 	// constructor
	public Device(Socket socket) {
		this.socket = socket;
		this.deviceID = "" + this.hashCode();
	}
	
	
	// get, set
	public static String getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void close() {
		String addr = socket.getInetAddress().toString();
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
				input.close();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} // try
		System.out.println("[" + addr + "] closed");
	}
	
	public void send(String msg) {
		if(this.socket != null && !socket.isClosed()) {
			output.println(msg);
			output.flush();
		}
	}
	
	public void send(Message message) {
		if(this.socket != null && !socket.isClosed()) {
			output.println(gson.toJson(message));
			output.flush();
		}
    }
	
	@Override
	public void run() {
		
		try {
			this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.output = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			this.close();
		} // try
		System.out.println("[" + socket.getInetAddress().toString() + "] connected");
		
		String line = "";
		while(true) {
			try {
				line = input.readLine();
				if(line == null) {
					throw new IOException();
				} else {
					
					System.out.println(line);
//					service.add(gson.fromJson(line, Message.class));
					service.dataHandler(line);
					System.out.println("send");
					send(line);
					
				}
			} catch (IOException e) {
				this.close();
				break;
			}
		} // while()
	} // run()
}