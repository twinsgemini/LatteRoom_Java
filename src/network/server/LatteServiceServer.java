package network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import network.server.dao.Client;
import network.server.service.ServerService;

public class LatteServiceServer {
	
	private ServerSocket server;
	private ExecutorService executor;
	private ServerService service = ServerService.getInstance();

	//
	private void startServer() {
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
		
		Runnable getConnect = () -> {
			Socket socket = null;
			while(true) {
				try {
					socket = server.accept();
					
//					System.out.println("[" + socket.getInetAddress() + "] connect");
					
					Client user = new Client(socket);
					service.add(user);
					executor.submit(user);
					
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
		executor.submit(getConnect);
	}
	
	private void stopServer() {
		try {
			for(String key : service.getList().keySet()) {
				Client t = service.get(key);
				t.close();
				service.remove(t);
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
	}
	
	//
	public static void main(String[] args) {
		
		LatteServiceServer server = new LatteServiceServer();
//		ServerService service = new ServerService();
		
		System.out.println("엔터를 치면 서버가 종료됩니다.");
        try {
        	server.startServer();
            System.in.read();
            server.stopServer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		
	}

}
