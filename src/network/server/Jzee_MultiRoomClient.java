package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Jzee_MultiRoomClient extends Application{
	
	// UI
	private BorderPane root;
	private FlowPane namePane, menuPane, inputPane;
	
	private TextField nameField, inputField;
	private Button connBtn, disconnBtn, createBtn, menuBtn;
	private Label nameLabel;
	
	private ListView<String> roomListView;
	private ListView<String> participantsListView;		// 채팅방 참여목록
	
	// User
	private int userID;
	private String nickname;
	private TextArea textarea;
	
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private ExecutorService receiverPool;
	private ExecutorService senderPool;
	
	private Map<Integer, Client> users;
	private Map<Integer, Room> rooms;
	
	private Room currentRoom;
	
	private Gson gson = new Gson();
	
	
	// =================================================================
	// displayText(String msg)
	// displayText(TextArea ta, String msg)
	public void displayText(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}
	
	public void displayText(TextArea ta, String msg) {
		Platform.runLater(() -> {
			ta.appendText(msg + "\n");
		});
	}
	
	// setBottomPane(FlowPane pane)
	private void setBottomPane (FlowPane pane) {
		pane.setPrefSize(700, 40);
		pane.setPadding(new Insets(5,5,5,5));
		pane.setHgap(10);
	}
	
	
	// =================================================================
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		root = new BorderPane();
		root.setPrefSize(700, 500);
		
		// Center ----------------------------------------------
		textarea = new TextArea();
		textarea.setEditable(false);
		root.setCenter(textarea);
		
		
		// Right -----------------------------------------------
		// roomList
		roomListView = new ListView<String>();
		roomListView.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<?> arg0, Object arg1, Object arg2) -> {
					int index = roomListView.getSelectionModel().getSelectedIndex();
					Platform.runLater(() -> {
//						root.setCenter(taList.get(index));
						inputField.setEditable(true);
					});
				});
		
		// participantsList
		participantsListView = new ListView<String>();
		participantsListView.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<?> arg0, Object arg1, Object arg2) -> {
					int index = participantsListView.getSelectionModel().getSelectedIndex();
				});
		
		// Right GridPane
		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5,5,5,5));
		gridpane.setVgap(10);
		gridpane.add(roomListView, 0, 0);
		gridpane.add(participantsListView, 0, 1);	// (column, row)
		
		root.setRight(gridpane);
		
		
		// Bottom ----------------------------------------------
		namePane = new FlowPane();
		menuPane = new FlowPane();
		inputPane = new FlowPane();
		
		
		// namePane
		nameLabel = new Label("Nickname");
		nameLabel.setStyle("-fx-font-size: 15");
		nameLabel.setPrefSize(150, 40);
		nameLabel.setAlignment(Pos.CENTER);
		
		nameField = new TextField();
		nameField.setPromptText("Please Enter Your Nickname");
		nameField.setPrefSize(350, 40);
		
		connBtn = new Button("Conn");
		connBtn.setPrefSize(150, 40);
		connBtn.setOnAction((e) -> {
			startClient();
			Platform.runLater(() -> {
				root.setBottom(menuPane);
			});
		});
		
		setBottomPane(namePane);
		namePane.getChildren().addAll(nameLabel, nameField, connBtn);
		namePane.setAlignment(Pos.CENTER);
		root.setBottom(namePane);
		
		// menuPane
		disconnBtn = new Button("Disconn");
		disconnBtn.setPrefSize(150, 40);
		disconnBtn.setOnAction((e) -> {
//			System.out.println("disconnBtn call stopClient()");
//			stopClient("DisconnBtn");
			stopClient();
			Platform.runLater(() -> {
				root.setBottom(namePane);
			});
		});
		
		createBtn = new Button("Create Room");
		createBtn.setPrefSize(150, 40);
		createBtn.setOnAction((e) -> {
			Platform.runLater(() -> {
				root.setBottom(inputPane);
				inputField.setEditable(true);
			});
		});
		
		setBottomPane(menuPane);
		menuPane.getChildren().addAll(disconnBtn, createBtn);
		
		// inputPane
		menuBtn = new Button("Menu");
		menuBtn.setPrefSize(150, 40);
		menuBtn.setOnAction((e) -> {
			Platform.runLater(() -> {
				root.setBottom(menuPane);
			});
		});
		
		inputField = new TextField();
		inputField.setEditable(false);
		inputField.setPrefSize(500, 40);
		inputField.setOnAction((e) -> {
			send(inputField.getText());
//			displayText(inputField.getText());
			Platform.runLater(() -> {
				inputField.clear();
			});
		});
		
		setBottomPane(inputPane);
		inputPane.getChildren().addAll(menuBtn, inputField);
		
		
		
		// Scene ------------------------------------------------
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Multi Room Chat Client");
		primaryStage.setOnCloseRequest((e) -> {
			System.out.println("closeBtn call stopClient()");
//			stopClient("CloseBtn");
			stopClient();
		});
		primaryStage.show();
		
	} // start(Stage primaryStage)

	
	// =================================================================
	// main
	public static void main(String[] args) {
		launch();
	} // main
	
	
	// =================================================================
	public void startClient() {
		
		connBtn.setDisable(true);
		disconnBtn.setDisable(false);
		receiverPool = Executors.newFixedThreadPool(1);
		senderPool = Executors.newFixedThreadPool(1);
		users = new HashMap<Integer, Client>();
		rooms = new HashMap<Integer, Room>();
		
		Runnable runnable = () -> {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress("localhost", 55566));
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream());
				displayText("[Connected : " + socket.getRemoteSocketAddress() + "]");
				
				// send nickname
//				String nickname = nameField.getText();
//				output.println(nickname);
//				output.println(new Message("NICKNAME", nickname));
//				output.flush();
				
				// request Room list
//				output.println(new Message("ROOMLIST", userID));
//				output.flush();
				
			} catch (Exception e) {	
//				System.out.println("Connection Exception");
				if(!socket.isClosed()) { 
//					System.out.println("Connecter call stopClient()");
//					stopClient("Connecter");
					stopClient();
				}
//				System.out.println("Connecter return");
				return;
			}
//			System.out.println("Connecter start receiver");
			receive();
		};
//		System.out.println("out of runnable");
		receiverPool.submit(runnable);
//		System.out.println("pool.submit(runnable)");
	} // startClient()
	
	public void stopClient() {
//	public void stopClient(String who) {
//		System.out.println(who + "] stopClient() start");
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
//				System.out.println(who + "] stopClient() socket close");
				if(input != null) input.close();
				if(output != null) output.close();
//				System.out.println(who + "] stopClient() stream close");
				displayText("[ Disconnected ]");
			}
//			System.out.println(who + "] stopClient() socket clean");
			if(receiverPool != null && !receiverPool.isShutdown()) {
				List<Runnable> list = receiverPool.shutdownNow();
//				System.out.println("reveiver ] " + list.size() + " is until running...");
//				receiverPool.shutdown();
//				do {
//					if(receiverPool.isTerminated()) {
//						receiverPool.shutdownNow();
//					}
//				} while (!receiverPool.awaitTermination(10, TimeUnit.SECONDS));
//				System.out.println(who + "] stopClient() receiver pool close");
			}
//			System.out.println(who + "] stopClient() receiver pool clean");
			if(senderPool != null && !senderPool.isShutdown()) {
				List<Runnable> list = senderPool.shutdownNow();
//				System.out.println("sender ] " + list.size() + " is until running...");
//				senderPool.shutdown();
//				do {
//					if(senderPool.isTerminated()) {
//						senderPool.shutdownNow();
//					}
//				} while (!senderPool.awaitTermination(10, TimeUnit.SECONDS));
//				System.out.println(who + "] stopClient() sender pool close");
			}
//			System.out.println(who + "] stopClient() sender pool clean");
		} catch (Exception e) {
//			System.out.println(who + "] stopClient() Exception");
			displayText("[ Disconnection Error ]");
			e.printStackTrace();
		} finally {
			Platform.runLater(() -> {
				connBtn.setDisable(false);
				disconnBtn.setDisable(true);
				root.setBottom(namePane);
			});
		} // try
//		System.out.println(who + "] stopClient() finish");
	} // stopClient()
	
	// ---------------------------------------------------
	public void receive() {
//		System.out.println("receive");
		String message = "";
		while(true) {
			try {
//				System.out.println("receive running...");
				message = input.readLine();
				if(message == null) {
					// Server's socket closed
					throw new IOException();
				} else {
					displayText(message);
//					Message data = gson.fromJson(message, Message.class);
//					
//					switch (data.getCode()) {
//					case "NICKNAME":
//						userID = data.userID;
//						break;
//						
//					case "ROOMLIST":
//						//
//						break;
//					
//					case "MESSAGE":
//						if(data.userID == userID) {
//							displayText("[ 나 ] : " + data.getJsonData());
//						} else {
//							displayText("[ " + "남" + " ] : " + data.getJsonData());
//						}
//						break;
//						
//					case "NEW_ROOM":
//						break;
//						
//					case "ENTER_ROOM":
//						break;
//						
//					case "EXIT_ROOM":
//						break;
//
//					default:
//						break;
//					}
				}
//				System.out.println("receive loop end");
			} catch (IOException e) {
//				System.out.println("receiver call stopClient()");
//				stopClient("receive()");
				stopClient();
				break;
			}
		}
	} // receive()
	
	public void send(String message) {
		
		if(message != null && !message.equals(""))
			send(new Message("MESSAGE", userID, message));

	} // send(String message)
	
	public void send(Message message) {
		Runnable runnable = () -> {
			try {
				String jsonMsg = gson.toJson(message);
				output.println(jsonMsg);
				output.flush();
			} catch (Exception e) {
				displayText("send Error");
//				System.out.println("send() call stopClient()");
//				stopClient("send()");
				stopClient();
			}
		};
		senderPool.submit(runnable);
	} // send(Message message)

	

	// =================================================================
	class Client {
		private int userID;
		private String nickname;
		
		public Client(int userID, String nickname) {
			this.userID = userID;
			this.nickname = nickname;
		}

		public int getUserID() {
			return userID;
		}

		public String getNickname() {
			return nickname;
		}
		
		public void setUserID(int userID) {
			this.userID = userID;
		}
		
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		
	} // Client
	
	
	// =================================================================
	class Room {
		private int roomID;
		private String roomName;
		private TextArea textArea;
		private List<String> list;		// participants(id) list
		
		// constructor
		public Room(TextArea textArea) {
			this.textArea = textArea;
		}
		
		public Room(int roomID, String roomName) {
			this.roomID = roomID;
			this.roomName = roomName;
			this.textArea = new TextArea();
			this.list = new ArrayList<String>();
		}
		
		public Room(RoomForm form) {
			this.roomID = form.roomID;
			this.roomName = form.roomName;
			this.textArea = new TextArea();
		}
		
		// getter - setter
		public int getRoomID() {
			return roomID;
		}
		
		public void setRoomID(int roomID) {
			this.roomID = roomID;
		}
		
		public String getRoomName() {
			return roomName;
		}
		
		public void setRoomName(String roomName) {
			this.roomName = roomName;
		}
		
		public TextArea getTextArea() {
			return textArea;
		}
		
//		public List<String> getList() {
//			return list;
//		}
//		
//		public void addPart(String id) {
//			this.list.add(id);
//		}
//		
//		public void addParts(List<String> list) {
//			for(String id : list) {
//				this.list.add(id);
//			}
//		}
//		
//		public void removePart(Client client) {
//			for(Client c : list) {
//				if(c.getNickname().equals(client.getNickname())) {
//					this.list.remove(c);
//				}
//			}
//		}
		 
	} // Room
	
	
	
	// =================================================================
	class RoomForm {
		int roomID;
		String roomName;
		
		RoomForm(Room room) {
			this.roomID = room.getRoomID();
			this.roomName = room.getRoomName();
		}
		
		public int getRoomID() {
			return roomID;
		}
		public void setRoomID(int roomID) {
			this.roomID = roomID;
		}
		public String getRoomName() {
			return roomName;
		}
		public void setRoomName(String roomName) {
			this.roomName = roomName;
		}
		
	} // RoomForm
	
	
	
	// =================================================================
	class Message {
		private String code;
		private int userID;
		private int destID;		// destination room id
		private String jsonData;
		
		// constructor
		public Message(String code) {
			this.code = code;
		}
		
		public Message(String code, int userID) {
			this.code = code;
			this.userID = userID;
		}
		
		public Message(String code, String jsonData) {
			this.code = code;
			this.jsonData = jsonData;
		}
		
		public Message(String code, int userID, String jsonData) {
			this.code = code;
			this.userID = userID;
			this.jsonData = jsonData;
		}
		
		public Message(String code, int userID, int destID, String jsonData) {
			this(code, userID, jsonData);
			this.destID = destID;
		}

		// getter - setter
		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public int getUserID() {
			return userID;
		}

		public void setUserID(int userID) {
			this.userID = userID;
		}

		public int getDestID() {
			return destID;
		}

		public void setDestID(int destID) {
			this.destID = destID;
		}

		public String getJsonData() {
			return jsonData;
		}

		public void setJsonData(String jsonData) {
			this.jsonData = jsonData;
		}

		@Override
		public String toString() {
			return "Message [code=" + code + ", userID=" + userID + ", destID=" + destID + ", jsonData=" + jsonData
					+ "]";
		}
		
	} // class Message
	
}