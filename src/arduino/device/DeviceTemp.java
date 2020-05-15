package arduino.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import arduino.device.DeviceTemp.SerialListener;
import arduino.device.DeviceTemp.ServerListener;
import arduino.device.vo.*;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DeviceTemp extends Application {

	private static final String DEVICE_ID = "LATTE01";
	private static final String DEVICE_TYPE = "DEVICE";		// App : "USER"
	
	private static final String COMPORT_NAMES = "COM11";
	private static final String SERVER_ADDR = "70.12.60.105";
	private static final int SERVER_PORT = 55566;
	
	private BorderPane root;
	private TextArea textarea;
	
	private ServerListener toServer = new ServerListener();
	private SerialListener toArduino = new SerialListener();
	private TempSharedObject sharedObject;
	
	private static Sensor sensorTemp = new Sensor("sensorTemp", "TEMP");
	private static Sensor heat = new Sensor("HEAT", "HEAT");
	private static Sensor cool = new Sensor("COOL", "COOL");
	
	private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
	
	
	// ======================================================
	public void displayText(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}
	
	public static Gson getGson() {
		return gson;
	}
	
	public static String getDeviceId() {
		return DEVICE_ID;
	}
	
	public static String getDeviceType() {
		return DEVICE_TYPE;
	}
	
	public static Map<String, Sensor> getSensorList() {
		Map<String, Sensor> sensorList = new HashMap<String, Sensor>();
		sensorList.put(sensorTemp.getSensorID(), sensorTemp);
		sensorList.put(heat.getSensorID(), heat);
		sensorList.put(cool.getSensorID(), cool);
		System.out.println(gson.toJson(sensorList));
		return sensorList;
	}
	
	
	// ======================================================
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Logic
		toServer.connect();
		toArduino.initialize();

		// SharedObject
		sharedObject = new TempSharedObject(toServer, toArduino);
		
		
		// UI ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		root = new BorderPane();
		root.setPrefSize(700, 500);
		
		// Center ----------------------------------------------
		textarea = new TextArea();
		textarea.setEditable(false);
		root.setCenter(textarea);
		
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("DeviceTemp");
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
				
				// 
				send(DeviceTemp.getDeviceId());
				send(DeviceTemp.getDeviceType());
				send(new Message(DeviceTemp.getDeviceId()
						, "SENSOR_LIST"
						, DeviceTemp.gson.toJson(DeviceTemp.getSensorList())));
				
				
				String line = "";
				while(true) {
					try {
						line = serverIn.readLine();
						
						if(line == null) {
							displayText("server error. disconnected");
							throw new IOException();
						} else {
							displayText("Server ] " + line);
							
							// ì„œë²„ì—�ì„œ ë‚ ì•„ì˜¨ í�¬ë§�ì˜¨ë�„ë¥¼ ê³µìœ ê°�ì²´ì—� ì €ìž¥
							Message messate = gson.fromJson(line, Message.class);
							int hopeTemp = Integer.parseInt(gson.fromJson(messate.getJsonData(), SensorData.class).getStates());
							sharedObject.setHopeStates(hopeTemp);
							
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
		
		public void send(Message msg) {
			serverOut.println(gson.toJson(msg));
//			serverOut.println("서버야 좀 받아라~!");
			serverOut.flush();
			displayText("서버로 보냈다!!! "+msg);
		}
		
		public void send(String sensorID, String states) {
			Message message = new Message(new SensorData(sensorID, states));
			send(message);
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
//					displayText("Serial ] " + inputLine);
					float eventTemp = Float.parseFloat(inputLine);
					displayText("Serial ] " + eventTemp);
					
					if(sensorTemp.getRecentData() == null) {
						int currentTemp = (int)eventTemp;
						// í˜„ìž¬ recentData, sharedObject.states ê°’ ì—†ì�„ ë•Œ
						sensorTemp.setRecentData(
								new SensorData(sensorTemp.getSensorID(), String.valueOf(currentTemp)));
						sharedObject.setStates(currentTemp);
						
						Message message = new Message(sensorTemp.getRecentData());
						
						displayText("" + currentTemp + "\n--Message]"+message.toString()+"\n");
						System.out.println(message);
						toServer.send(message);
						return;
					}
					
					int currentTemp = sharedObject.getStates();
					
					if(currentTemp + 0.6 < eventTemp) {
						currentTemp++;
						// sensorì�˜ recentData ê°±ì‹ 
						sensorTemp.setRecentData(String.valueOf(currentTemp));
						sharedObject.setStates(currentTemp);
						
						Message message = new Message(sensorTemp.getRecentData());
						displayText(message.toString());
						toServer.send(gson.toJson(message));
					} else if(currentTemp - 0.6 > eventTemp) {
						currentTemp--;
						// sensorì�˜ recentData ê°±ì‹ 
						sensorTemp.setRecentData(String.valueOf(currentTemp));
						sharedObject.setStates(currentTemp);
						
						Message message = new Message(sensorTemp.getRecentData());
						toServer.send(gson.toJson(message));
					}
				} catch (Exception e) {
//					System.err.println(e.toString() + "  : prb de lecture");
				}
			}
			// Ignore all the other eventTypes, but you should consider the other ones.
		}
		
		
		
	}

}

class TempSharedObject {
	// Temperature & Heat & Cool
	private int hopeStates = 28;
	private int states;
	private String heat = "OFF";
	private String cool = "OFF";
	private Object MONITOR = new Object();
	
	private ServerListener toServer;
	private SerialListener toArduino;
	
	TempSharedObject(ServerListener toServer, SerialListener toArduino) {
		this.toServer = toServer;
		this.toArduino = toArduino;
	}
	
	public int getHopeStates() {
		synchronized (MONITOR) {
			return this.hopeStates;
		}
	}
	
	public void setHopeStates(int hopeStates) {
		synchronized (MONITOR) {
			this.hopeStates = hopeStates;
			control();
		}
	}
	
	public int getStates() {
		synchronized (MONITOR) {
			return states;
		}
	}
	
	public void setStates(int states) {
		synchronized (MONITOR) {
			this.states = states;
			control();
		}
	}
	
	private synchronized void control() {
		if(hopeStates > states) {
			if(cool.equals("ON")) {
				toArduino.send("COOLOFF");
				toServer.send("COOL","OFF");
				cool = "OFF";
			}
			
			if(heat.equals("OFF")) {
				toArduino.send("HEATON");
				toServer.send("HEAT","ON");
				heat = "ON";
			}
		} else if(hopeStates < states) {
			if(heat.equals("ON")) {
				toArduino.send("HEATOFF");
				toServer.send("HEAT", "OFF");
				heat = "ON";
			}
			
			if(cool.equals("OFF")) {
				toArduino.send("COOLON");
				toServer.send("COOL", "ON");
				cool = "ON";
			}
		} else {
			if(heat.equals("ON")) {
				toArduino.send("HEATOFF");
				toServer.send("HEAT", "OFF");
				heat = "OFF";
			}
			
			if(cool.equals("ON")) {
				toArduino.send("COOLOFF");
				toServer.send("COOL","OFF");
				cool = "OFF";
			}
		}
	}
	
}