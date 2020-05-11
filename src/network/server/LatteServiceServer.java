package network.server;

import network.server.service.ServerService;

public class LatteServiceServer {

	public static void main(String[] args) {
		
		ServerService service = new ServerService();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("close");
//				server.stopServer();
			}
		});
		
		
		System.out.println("엔터를 치면 서버가 종료됩니다.");
        try {
        	service.startServer();
            System.in.read();
            service.stopServer();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		
	}

}
