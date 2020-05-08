package arduino.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class TestClient extends Application {

	private static final String COMPORT_NAMES = "COM13";
	private static final String SERVER_ADDR = "localhost";
	private static final int SERVER_PORT = 55566;
	
	private BorderPane root;
	private FlowPane bottom, right;
	
	private Button connBtn, disconnBtn;
	private TextField inputField;
	private TextArea textarea;
	
	private ServerListener toServer = new ServerListener();
	private SerialListener toArduino = new SerialListener();
	
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
		
		toServer.connect();
		toArduino.initialize();
		
//		// Bottom ----------------------------------------------
//		connBtn = new Button("conn");
//		connBtn.setPrefSize(150, 40);
//		connBtn.setOnAction((e) -> {
//			toServer.connect();
//		});
//		
//		disconnBtn = new Button("disconn");
//		disconnBtn.setPrefSize(150, 40);
//		disconnBtn.setOnAction((e) -> {
//			toServer.disconnect();
//		});
//		
//		inputField = new TextField();
//		inputField.setPrefSize(400, 40);
//		inputField.setOnAction((e) -> {
//			toServer.send(inputField.getText());
//			inputField.clear();
//		});
//		
//		bottom = new FlowPane();
//		bottom.getChildren().addAll(connBtn, disconnBtn, inputField);
//		root.setBottom(bottom);
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Test");
		primaryStage.setOnCloseRequest((e) -> {
			toServer.disconnect();
			toArduino.close();
		});
		primaryStage.show();
	}// start()
	
	
	
	// ======================================================
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	// ======================================================
	class ServerListener {
		private Socket socket;
		private BufferedReader serverIn;
		private PrintWriter serverOut;
		private ExecutorService executor;
		
		
		public void connect() {
			
			executor = Executors.newFixedThreadPool(1);
			
			Runnable runnable = () -> {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(SERVER_ADDR, SERVER_PORT));
					serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					serverOut = new PrintWriter(socket.getOutputStream());
				} catch (IOException e) {
//					e.printStackTrace();
					disconnect();
					return;
				}
				
				String line = "";
				while(true) {
					try {
						line = serverIn.readLine();
						
						if(line == null) {
							displayText("server error. disconnected");
							throw new IOException();
						} else {
							displayText("Server ] " + line);
							
							// Arduino로 전송하는 메서드
							 toArduino.send(line);
						}
					} catch (IOException e) {
//						e.printStackTrace();
						disconnect();
						break;
					}
				} // while()
			};
			executor.submit(runnable);
		} // startClient()
		
		public void disconnect() {
			try {
				if(socket != null && !socket.isClosed()) {
					socket.close();
					if(serverIn != null) serverIn.close();
					if(serverOut != null) serverOut.close();
				}
				if(executor != null && !executor.isShutdown()) {
					executor.shutdownNow();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} // stopClient()
		
		public void send(String msg) {
			serverOut.println(msg);
			serverOut.flush();
		}
		
	}
	
	
	// ======================================================
	class SerialListener implements SerialPortEventListener {
		
		SerialPort serialPort;
		
		private BufferedReader serialIn;
		private PrintWriter serialOut;
		private static final int TIME_OUT = 2000;
		private static final int DATA_RATE = 9600;
		
		public void initialize() {
			CommPortIdentifier portId = null;
			try {
				portId = CommPortIdentifier.getPortIdentifier(COMPORT_NAMES);
			} catch (NoSuchPortException e1) {
				e1.printStackTrace();
			};
			
			if (portId == null) {
				System.out.println("Could not find COM port.");
				return;
			}

			try {
				// open serial port, and use class name for the appName.
				serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

				// set port parameters
				serialPort.setSerialPortParams(
						DATA_RATE,					// 9600 
						SerialPort.DATABITS_8, 
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// open the streams
				serialIn = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				serialOut = new PrintWriter(serialPort.getOutputStream());

				// add event listeners
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}

		/**
		 * This should be called when you stop using the port. This will prevent port
		 * locking on platforms like Linux.
		 */
		public synchronized void close() {
			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}
		
		public synchronized void send(String msg) {
			serialOut.println(msg);
			serialOut.flush();
		}

		/**
		 * Handle an event on the serial port. Read the data and print it.
		 */
		public synchronized void serialEvent(SerialPortEvent oEvent) {
			if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					String inputLine = serialIn.readLine();
					
//					toServer.send(inputLine);
					send(inputLine);
					
					displayText("Serial ] " + inputLine);
				} catch (Exception e) {
//					System.err.println(e.toString() + "  : prb de lecture");
				}
			}
			// Ignore all the other eventTypes, but you should consider the other ones.
		}
		
	}

}
