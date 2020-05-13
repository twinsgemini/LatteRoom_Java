package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import network.server.vo.Message;

public class TestClient extends Application{

	private BorderPane root;
	private FlowPane bottom, right;
	
//	private Button ledBtn, heatBtn, coolBtn;
//	private Button blindBtn, bedBtn;
	
	private Button connBtn, disconnBtn;
	private TextField inputField;
	private TextArea textarea;
	
	//
	private String deviceID = "" + this.hashCode();
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private ExecutorService executor;
	
	private Gson gson = new Gson();
	
	
	// ======================================================
	public void displayText(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}
	
	
	
	// ======================================================
	@Override
	public void start(Stage primaryStage) throws Exception {
		root = new BorderPane();
		root.setPrefSize(700, 500);
		
		// Center ----------------------------------------------
		textarea = new TextArea();
		textarea.setEditable(false);
		root.setCenter(textarea);
		
		// Bottom ----------------------------------------------
		connBtn = new Button("conn");
		connBtn.setPrefSize(150, 40);
		connBtn.setOnAction((e) -> {
			startClient();
		});
		
		disconnBtn = new Button("disconn");
		disconnBtn.setPrefSize(150, 40);
		disconnBtn.setOnAction((e) -> {
			stopClient();
		});
		
		inputField = new TextField();
		inputField.setPrefSize(400, 40);
		inputField.setOnAction((e) -> {
			send(inputField.getText());
			inputField.clear();
		});
		
		bottom = new FlowPane();
		bottom.getChildren().addAll(connBtn, disconnBtn, inputField);
		root.setBottom(bottom);
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Test");
		primaryStage.setOnCloseRequest((e) -> {
			stopClient();
		});
		primaryStage.show();
	}// start()
	
	
	
	// ======================================================
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	// ======================================================
	public void startClient() {
		
		executor = Executors.newFixedThreadPool(1);
		
		Runnable runnable = () -> {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress("localhost", 55566));
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
//				e.printStackTrace();
				stopClient();
				return;
			}
			
			String line = "";
			while(true) {
				try {
					line = input.readLine();
					System.out.println(line);
					if(line == null) {
						displayText("server error. disconnected");
						throw new IOException();
					} else {
						displayText(line);
						//
					}
				} catch (IOException e) {
//					e.printStackTrace();
					stopClient();
					break;
				}
			} // while()
		};
		executor.submit(runnable);
	} // startClient()
	
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
				if(input != null) input.close();
				if(output != null) output.close();
			}
			if(executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // stopClient()
	
	public void send(String msg) {
//		output.println(msg);
		output.println(gson.toJson(new Message(this.deviceID, "MESSAGE", msg)));
		output.flush();
	}

}