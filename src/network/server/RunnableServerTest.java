package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class RunnableServerTest extends Application{
	
	private TextArea textarea;
	private Button startBtn, stopBtn;
	
	private ServerSocket server;
	private ExecutorService executor;
	
	
	public void displayText(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}
	
	public void serverStart() {
		displayText("start");
		Platform.runLater(() -> {
			startBtn.setDisable(true);
		});
		
		try {
			executor = Executors.newCachedThreadPool();
			
			server = new ServerSocket();
			server.bind(new InetSocketAddress(35566));
			server.setSoTimeout(3000);				
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Runnable runnable = () -> {
			Socket socket = null;
			while(true) {
				try {
					socket = server.accept();
					DeviceRunnable device = new DeviceRunnable(socket);
					executor.submit(device);
				} catch (SocketTimeoutException e1) {
					if(Thread.interrupted()) {
						break;
					} else continue;
				} catch (IOException e1) {
					e1.printStackTrace();
					break;
				}
			} // while
			// when loop out
			Platform.runLater(() -> {
				startBtn.setDisable(false);
				stopBtn.setDisable(true);
			});
		};
		executor.submit(runnable);
		Platform.runLater(() -> {
			stopBtn.setDisable(false);
		});
	}
	
	public void serverStop() {
		try {
			// server close
			if(server != null && !server.isClosed()) {
				server.close();
			}
			
			// connections shutdown (interrupt)
			if(executor != null && !executor.isShutdown()) {
				executor.shutdown();
				do { 
					if (executor.isTerminated()) {
						executor.shutdownNow();
					}
				} while (!executor.awaitTermination(10, TimeUnit.SECONDS));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		 
		BorderPane root = new BorderPane();
		root.setPrefSize(700, 500);
		
		// Center
		textarea = new TextArea();
		textarea.setEditable(false);
		root.setCenter(textarea);
		
		// Bottom
		startBtn = new Button("Start");
		startBtn.setPrefSize(200, 40);
		startBtn.setOnAction((e) -> {
			serverStart();
		}); // startBtn
		
		stopBtn = new Button("Stop");
		stopBtn.setPrefSize(200, 40);
		stopBtn.setOnAction((e) -> {
//			displayText("stop");
			serverStop();
		});
		
		FlowPane bottom = new FlowPane();
		bottom.setPrefSize(700, 40);
		bottom.setPadding(new Insets(5,5,5,5));
		bottom.setHgap(5);
		bottom.getChildren().addAll(startBtn, stopBtn);
		bottom.setAlignment(Pos.CENTER_RIGHT);
		root.setBottom(bottom);		
		
		// Scene
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Multi Room Chat Server");
		primaryStage.setOnCloseRequest((e) -> {
			
		});
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}

class DeviceRunnable implements Runnable {
	
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	
	private String deviceID;
	private String deviceType;
	
	public DeviceRunnable(Socket socket) {
		this.socket = socket;
		try {
			this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.output = new PrintWriter(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PrintWriter getOutput() {
		return this.output;
	}
	
	public void stop() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
				if(input != null)	input.close();
				if(output != null)	output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String msg) {
		output.println(msg);
		output.flush();
	}
	
	@Override
	public void run() {
		String line = "";
		while(true) {
			try {
				line = input.readLine();
				
				if(line == null) {
					throw new IOException();
				} else {
					// switch
					
					// echo
					send(line);
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		stop();
	} // run()
}
