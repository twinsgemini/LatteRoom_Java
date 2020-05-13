package network.server.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User extends AbstractClient {

	private String deviceID;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	
	public Socket getSocket() {
		return socket;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	@Override
	public void setSocket(Socket socket) {
		// TODO Auto-generated method stub
		this.socket = socket;
	}

	@Override
	public String getDeviceID() {
		// TODO Auto-generated method stub
		return this.deviceID;
	}

	@Override
	public void receive() {
		// TODO Auto-generated method stub
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
				
				System.out.println("[" + socket.getInetAddress() + "] " + line);
				
				if(line == null) {
					throw new IOException();
				} else {
					
					send(line);
					
				}
			} catch (IOException e) {
				this.close();
				break;
			}
		} // while()
	}

	@Override
	public void send(String msg) {
		// TODO Auto-generated method stub
		output.println(msg);
		output.flush();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.receive();
	}

}
