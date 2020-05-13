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
//import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

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

class ClientSharedObject {
   // Temperature
   private float sensorTemp;
   private float hopeTemp;
   private int sensorMoter;
   private Object MONITOR = new Object();
   
   public float getSensorTemp() {
      synchronized (MONITOR) {
         return this.sensorTemp;
      }
   }
   
   public void setSensorTemp(float data) {
      synchronized (MONITOR) {
         this.sensorTemp = data;
      }
   }
   
   public float getHopeTemp() {
      synchronized (MONITOR) {
         return this.hopeTemp;
      }
   }
   
   public void setHopeTemp(float data) {
      synchronized (MONITOR) {
         this.hopeTemp = data;
      }
      
   
   }

   public int getSensorMoter() {
      synchronized (MONITOR) { 
      return this.sensorMoter;}
   }

   public void setSensorMoter(int data) {
      synchronized (MONITOR) {
      this.sensorMoter = data;
      }}
      
}

public class Client extends Application {

   private static final String COMPORT_NAMES = "COM13";
   private static final String SERVER_ADDR = "70.12.60.94";
   private static final int SERVER_PORT = 55566;
   
   private BorderPane root;
   private FlowPane bottom, right;
   
   private Button connBtn, disconnBtn;
   private TextField inputField;
   private TextArea textarea;
   
   private ServerListener toServer = new ServerListener();
   private SerialListener toArduino = new SerialListener();
   private ClientSharedObject temp;
   private ClientSharedObject sensor;
   private String operate;
   private Float past=999.9f;
   
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
      
      // Logic
      toServer.connect();
      toArduino.initialize();

      // SharedObject
      temp = new ClientSharedObject();
      sensor = new ClientSharedObject();
      
      
      // UI ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      root = new BorderPane();
      root.setPrefSize(700, 500);
      
      // Center ----------------------------------------------
      textarea = new TextArea();
      textarea.setEditable(false);
      root.setCenter(textarea);
      
      
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
//               e.printStackTrace();
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
                     
                     // Message jsonData = gson.fromJson(line, Message.class);
                     
                     int data = 20;   // 데이터는 line에서 받아요
                     // int data = jsonData.states;
                     
                     
                     if(line.split(",")[0].equals("temp"))
                     {
                  
                                           
                     // 서버에서 날아온 희망온도를 공유객체에 저장해요
                     temp.setHopeTemp(Float.parseFloat(line));
                     
                     
                     
                     // Arduino로 전송하는 메서드
                      
                      if((temp.getSensorTemp()-26)>=1)
                      {
                         
                         //(temp.getHopeTemp())-(temp.getSensorTemp())<1
                         operate = "ON";
                         
                         
                      }else if((temp.getSensorTemp()-26)<1)
                      
                      {
                         operate = "OFF";
                      }
                      
                   
                    System.out.println(operate);
                       toArduino.send(operate);
                       
                      

                         toArduino.send("15");
                         
                         
                         
                  
                  
                  
                  
                  // Arduino로 전송하는 메서드
                   
                   if((temp.getSensorTemp()-26)>=1)
                   {
                      
                      //(temp.getHopeTemp())-(temp.getSensorTemp())<1
                      operate = "상태,ON";
                      
                      
                   }else if((temp.getSensorTemp()-26)<1)
                   
                   {
                      operate = "상태,OFF";
                   }
                   
                       
                     
                      
                      System.out.println(line);
                      
                     }
                     //서브모터를 돌리기위한  
                     
                  
                     
                      
                     
//                     System.out.println( Math.round(temp.getSensorTemp()));
                     
                      //온도를 주고받기 
                     
                     
                     
                     
                     
                      
                  }
                  
               } catch (IOException e) {
//                  e.printStackTrace();
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
                  DATA_RATE,               // 9600 
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
               
               toServer.send(inputLine);
               
               //toServer.send(past.toString());
               
//               float current = Float.parseFloat(inputLine);
//               
//               if(Math.abs(past-current)>1.0f)
//               {  float temp;
//                   temp=current;
//                  past=temp;
//                  toServer.send(past.toString());
//               }
               
               
               
               //과거에 보냈던 데이터 
               
               
               // 아두이노에서 받은 데이터를 사용하기 쉽게 자르거나 객체화 해요
               // Message jsonData = gson.fromJson(line, Message.class);
               //temp.setSensorTemp(Float.parseFloat(inputLine));
               // 아두이노에서 받은 데이터를 공유객체의 희망온도랑 비교해요
            
               
               // 데이터를 필요한 곳으로 전달해요
//               toServer.send(inputLine);
               
               
               /*if((temp.getSensorTemp()-temp.getSensorTemp())>=1)
                  
                {
                   
                   recheck = temp.getSensorTemp();
                  
                   send(Float.toString(recheck));
                   System.out.println(operate);
                   
                }*/
//               else if((temp.getSensorTemp()-26)<1)
//                
//                {
//                   operate = 0;
//                   send(Integer.toString(operate));
//                   System.out.println(operate);
//                }
               /*
                * else { operate = "OF"; // send(Integer.toString(operate)); //
                * System.out.println(operate); }
                */
//               System.out.println(operate);
//               send(operate);
//               displayText("Serial : " + inputLine);
               
            
//               send(inputLine);
               
            } catch (Exception e) {
//               System.err.println(e.toString() + "  : prb de lecture");
            }
         }
         // Ignore all the other eventTypes, but you should consider the other ones.
      }

      private void ParseFloat(String inputLine) {
         // TODO Auto-generated method stub
         
      }
      
   }

}
