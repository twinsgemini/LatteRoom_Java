package network.server.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import network.server.dao.Client;
import network.server.test.AbstractClient;
import network.server.test.User;


public class ServerService {
	
	private ServerSocket server;
	private ExecutorService executor;
	private Map<String, Client> list = new ConcurrentHashMap<String, Client>();
	private Gson gson = new Gson();
	
	
	//
	//
	public void startServer() {
		executor = Executors.newCachedThreadPool();
		
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(55566));
			server.setSoTimeout(3000);
		} catch (IOException e) {
			e.printStackTrace();
			if(!server.isClosed()) {
				stopServer();
			}
		}
		
		System.out.println("Ready to accept...");
		Runnable runnable = () -> {
			Socket socket = null;
			while(true) {
				try {
					socket = server.accept();
					
					System.out.println("[" + socket.getInetAddress() + "] connect");
					
					Client user = new Client(socket);
//					user.setSocket(socket);
					list.put(user.getDeviceID(), user);
					executor.submit(user);
					
//					Tester tester = new Tester(socket);
//					
//					list.put(tester.hashCode(), tester);
//					executor.submit(tester);
					
				} catch (SocketTimeoutException e) {
					if(Thread.interrupted()) {
						break;
					} else continue;
				} catch (IOException e) {
//					e.printStackTrace();
					break;
				}
			}
			stopServer();
		};
		executor.submit(runnable);
	} // startServer();
	
	public void stopServer() {
		try {
			for(String key : list.keySet()) {
				Client t = list.get(key);
				t.close();
				list.remove(key);
			}
			if(server != null && !server.isClosed()) {
				server.close();
			}
			if(executor != null && !executor.isShutdown()) {
				executor.shutdown();
				do {
					if (executor.isTerminated()) {
						List<Runnable> list = executor.shutdownNow();
						System.out.println(list.size() + " job is alive...");
					}
				} while (!executor.awaitTermination(10, TimeUnit.SECONDS));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // stopServer()
	
}
