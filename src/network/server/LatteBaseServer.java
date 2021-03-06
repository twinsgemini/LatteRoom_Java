package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class LatteBaseServer {
	
	private ServerSocket server;
	private ExecutorService executor;
	private Map<Integer, Tester> list = new ConcurrentHashMap<Integer, Tester>();	
	private Gson gson=  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
	
	//
	public static void main(String[] args) {
		
		LatteBaseServer server = new LatteBaseServer();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("close");
//				server.stopServer();
			}
		});
		
		
		System.out.println("엔터를 치면 프로그램이 종료됩니다.");
        try {
        	server.startServer();
            System.in.read();
            server.stopServer();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
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
					Tester tester = new Tester(socket);
					
					list.put(tester.hashCode(), tester);
					executor.submit(tester);
					
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
			for(Integer key : list.keySet()) {
				Tester t = list.get(key);
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

	class TesterSharedObject {
		private List<Tester> list = new ArrayList<Tester>();
		
		public synchronized void put(Tester t) {
			this.list.add(t);
		}
		
		public synchronized Tester pop() {
			return null;
		}
		
		public void broadcast(String line) {
			for(Tester t : list) {
				t.send(line);
			}
		}
	}
	
	
	class Tester implements Runnable {
		
		private Socket socket;
		private BufferedReader input;
		private PrintWriter output;
		
		Tester(Socket socket) {
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
				list.remove(this);
			} catch (IOException e) {
				e.printStackTrace();
			} // try
			System.out.println("[" + addr + "] closed");
		}
		
		public void send(String msg) {
			output.println(msg);
			output.flush();
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
					System.out.println(line);
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
		} // run()
	} // Tester.class
	
}

