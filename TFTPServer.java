import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.application.Platform;

import java.net.*;
import java.io.*;
import java.util.*;
/**
 * TFTPServer - A TFTP server to handle client requests and recieve files for transfer
 * together with TFTPClient.
 * @authors  Caleb Ghatt and Matthew Luke
 * @version 2205
**/
 


public class TFTPServer extends Application implements EventHandler<ActionEvent> {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root;
   
   // GUI Elements
   private Button btnChooseFolder = new Button("Choose Folder");
   private TextField tfDirectory = new TextField();
   private Label lblServerState = new Label("Start the server:");
   private Button btnStart = new Button("Start");
   private TextArea taLog = new TextArea();
   
   // Current Directory
   private String currentDirectory = System.getProperty("user.dir");
   
   // Server Socket
   private ServerSocket sSocket = null;
   private DatagramSocket mainSocket = null;
   private long startTime = 0;
   private BufferedReader infromclient =null;
   private DataOutputStream outtoclient =null;
   
   //Server Thread
   private TFTPServerThread serverThread = null;

   public static final int SERVER_PORT = 69;
   
   public static final int MAX_PACKET = 516;
   


   /**
    * main program
   */
   public static void main(String[] args) {
      launch(args);
   }
   
   public void start(Stage _stage) {
      // Window setup
      stage = _stage;
      stage.setTitle("TFTPServer");
      stage.setOnCloseRequest(
         new EventHandler<WindowEvent>() {
            public void handle(WindowEvent evt) { 
               // Popup alert asking user to confirm if they want to close out program
               // If possible, check to see if the connection is on, and ask them if they want to exit without disconnecting server
                  //If user clicks yes, create try catch method to sever IOs and close ports
               System.exit(0);
               //serverThread.doStopServer();
               // Output Orders to orders.obj
            }
         });
      stage.setResizable(false);
      root = new VBox(8);
      
      // Top FlowPane with Choose Folder Button
      FlowPane fpTop = new FlowPane(8,8);
      fpTop.getChildren().add(btnChooseFolder);
      root.getChildren().add(fpTop);
      
      // Mid FlowPane with directory Location
      FlowPane fpMid = new FlowPane(8,8);
      fpMid.getChildren().add(tfDirectory);
      root.getChildren().add(fpMid);
      
      // Bottom FlowPane with Server State labels
      FlowPane fpBot = new FlowPane(8,8);
      fpBot.getChildren().addAll(lblServerState, btnStart);
      root.getChildren().add(fpBot);
      
      // TextArea Log
      FlowPane fpLog = new FlowPane(8,8);
      fpLog.getChildren().add(taLog);
      root.getChildren().add(fpLog);
      
      //Size taLog
      taLog.setPrefRowCount(10);
      taLog.setPrefColumnCount(35);
      taLog.setPrefHeight(500);
      taLog.setPrefWidth(400);
      
      // Show Current FileLocation (startup)
      tfDirectory.setPrefColumnCount(35);
      File f = new File(currentDirectory);
      tfDirectory.setText(currentDirectory);
      tfDirectory.setEditable(false);
      
      // Add scrollbar to filelocation 
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfDirectory);
   
      // Enable Buttons
      btnChooseFolder.setOnAction(this);
      btnStart.setOnAction(this);
      
      // Show window
      scene = new Scene(root, 400, 600);
      stage.setScene(scene);
      stage.show();
   }
   
   public void handle(ActionEvent ae) {
      String label = ((Button)ae.getSource()).getText();
      switch(label) {
         case "Start":
            startServer();
            break;
         case "Stop":
            stopServer();
            break;
         case "Choose Folder":
            // Call Choose Folder
            break;
      }
      
      Platform.runLater(
         new Runnable() {
            public void run() { }});
   }
   //
   
   public void startServer() {
   // Create Server Thread
      serverThread = new TFTPServerThread();
   
   // Call Server Start
      serverThread.start();
   
   // Change btn's text to stop
      btnStart.setText("Stop");
   
   // Append Log Server Start
      taLog.appendText("The server has started.\n");
   
   }
   
   
   
   public void stopServer() {
   // Stop Server Thread
      serverThread.stopThread();
   
   // Append Log Server Stop
      taLog.appendText("The server has been stopped.\n");
   
   // Change Start Button's text to Start
      btnStart.setText("Start");
   
   }
   
   class TFTPServerThread extends Thread {
   
      private volatile boolean exit = false;
   
      public void run() {
         try {
            mainSocket = new DatagramSocket(SERVER_PORT);
            
         }
         catch(IOException ioe) {
            taLog.appendText("IO Exception (1) : " + ioe + "\n");
         }
         
         while (!exit) {
            System.out.println("testing");
            byte[] holder = new byte[MAX_PACKET];
            // packet for 1st packet from a client
            DatagramPacket pkt = new DatagramPacket(holder, MAX_PACKET);
            
            try {
               //Wait for 1st packet
               mainSocket.receive(pkt);
            }
            catch(IOException ioe) {
               return;
            }
            
            TFTPClientThread ct = new TFTPClientThread(pkt);
            ct.start();
         }
         
      
      }
      
      public void stopThread() {
         exit = true;
         mainSocket.close();
      }
   }
   
   class TFTPClientThread extends Thread implements TFTPConstants {
      private DatagramSocket cSocket = null;
      private DatagramPacket firstPkt = null;
      
      public TFTPClientThread(DatagramPacket _pkt) {
         firstPkt = _pkt;
         
         try {
            cSocket = new DatagramSocket();
         }
         catch (IOException ioe) {
            return;
         }
      }
      
      public void rrq(RRQPacket packet) {
         
         File file = new File(packet.getFileName());
         FileInputStream fis = null;
         DataInputStream disX = null;
         
         byte[] buffer = new byte[MAX_PACKET];
         
         try{fis = new FileInputStream(file);
            disX = new DataInputStream(fis);} 
         catch (Exception e) {}
         try {
            disX.read(buffer, 0, 512); } 
         catch (Exception e) {}
         
         
         
         
         boolean eof = false;
         for (int blockNo = 2; eof == false; blockNo++) {
         
            byte[] holder = new byte[MAX_PACKET];
            DatagramPacket incoming = new DatagramPacket(holder, MAX_PACKET);
            try { cSocket.receive(incoming); } 
            catch (Exception e) {}
         
         
            int opcode = 0;
            ByteArrayInputStream bais = new ByteArrayInputStream(firstPkt.getData(), firstPkt.getOffset(), firstPkt.getLength());
            DataInputStream dis = new DataInputStream(bais);
            try { opcode = dis.readShort(); } 
            catch (Exception e) {}
            
            if (opcode == TFTPConstants.ERROR) {
               ERRORPacket recieved = new ERRORPacket();
               recieved.dissect(incoming);
               System.out.println(recieved.getErrorMsg());
               eof = true;
            }
         
            ACKPacket recieved = new ACKPacket();
            recieved.dissect(incoming);
            if (recieved.getBlockNo() == blockNo) {
               DATAPacket response = new DATAPacket(packet.getAddress(), packet.getPort(), blockNo, buffer, MAX_PACKET);
               try { cSocket.send(response.build()); } 
               catch (Exception e) {}
            
            }
            int x = 0;
            System.out.println("BUILDING DATAPACKET");
                        
            try {x = disX.read();} catch (Exception e) {}
            
            
            if (x == -1) {
               eof = true;          
            }
         }
      
      }
      
      public void wrq(WRQPacket packet) {
         String fileName = packet.getFileName();
         taLog.appendText("Downloading file: " + fileName);
         DataOutputStream dos = null;
         FileOutputStream fos = null;
         try {
            fos = new FileOutputStream(fileName);
            dos = new DataOutputStream(fos);
         } catch (Exception e) {}
         
         ACKPacket initialResponse = new ACKPacket(packet.getAddress(), packet.getPort(), 0);
         try { cSocket.send(initialResponse.build()); } 
         catch (Exception e) {}
         boolean eof = false;
         while (eof == false) {
         
            byte[] holder = new byte[MAX_PACKET];
            DatagramPacket incoming = new DatagramPacket(holder, MAX_PACKET);
            try { cSocket.receive(incoming); } 
            catch (Exception e) {}
         
         
            int opcode = 0;
            ByteArrayInputStream bais = new ByteArrayInputStream(firstPkt.getData(), firstPkt.getOffset(), firstPkt.getLength());
            DataInputStream dis = new DataInputStream(bais);
            try { opcode = dis.readShort(); } 
            catch (Exception e) {}
            
            if (opcode == TFTPConstants.ERROR) {
               ERRORPacket recieved = new ERRORPacket();
               recieved.dissect(incoming);
               System.out.println(recieved.getErrorMsg());
               eof = true;
            }
         
            DATAPacket recieved = new DATAPacket();
            recieved.dissect(incoming);
            try {
               dos.write(recieved.getData()); } 
            catch (Exception e) {}
            
            
            ACKPacket response = new ACKPacket(packet.getAddress(), packet.getPort(), recieved.getBlockNo());
            try { cSocket.send(response.build()); } 
            catch (Exception e) {}
            
            if (recieved.getDataLen() < MAX_PACKET-4) {
               
               eof = true;          
            }
         }
         
         try {
            cSocket.close();
            dos.close();
            fos.close(); }
         catch (Exception e) {}
         
      }
      
   
      
      public void run() {
         taLog.appendText("Client packet recieved!\n");
         int opcode = 0;
         ByteArrayInputStream bais = new ByteArrayInputStream(firstPkt.getData(), firstPkt.getOffset(), firstPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort(); } 
         catch (Exception e) {}
         switch(opcode) {
            case TFTPConstants.RRQ :
               RRQPacket rrqPacket = new RRQPacket();
               rrqPacket.dissect(firstPkt);
               rrq(rrqPacket);
               break; 
            case TFTPConstants.WRQ :
               WRQPacket wrqPacket = new WRQPacket();
               wrqPacket.dissect(firstPkt);
               wrq(wrqPacket);
               break; 
            default :
            // Statements
         }        
      }
   }
   // ListenThread innerClass
   
   
   
   class RRQPacket {
   
      InetAddress toAddress;
      InetAddress address;
      int port;
      String fileName;
      String mode;
      int opcode = TFTPConstants.RRQ;
   
      public RRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode) {
         toAddress = _toAddress;
         port = _port;
         fileName = _fileName;
         mode = _mode;
      }
      
      public RRQPacket() {
      
      }
      
      public int getOpcode() {
         return opcode;
      }
      
      public InetAddress getToAddress() {
         return toAddress;
      }
      
      public InetAddress getAddress() {
         return address;
      }
      
      public int getPort() {
         return port;
      }
      
      public String getFileName() {
         return fileName;
      }
      
      public String getMode() {
         return mode;
      }
      
      public DatagramPacket build() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + fileName.length() + 1 /*0*/ + "octet".length() + 1 /*0*/);
         DataOutputStream dos = new DataOutputStream(baos);
         try {
            dos.writeShort(opcode);
            dos.writeBytes(fileName);
            dos.writeByte(0);
            dos.writeBytes("octet");
            dos.writeByte(0);
         }
         catch (Exception e) {}
         
         //Close the DataOutputStream to flush the last of the packet
         //out to the ByteArrayOutputStream
         try {
            dos.close();}
         catch (Exception e) {}
         
         byte[] holder = baos.toByteArray(); //Get the underlying byte[]
         DatagramPacket rrqPkt = new DatagramPacket(holder, holder.length, toAddress, port);
         
         return rrqPkt;
      }
      
      public void dissect (DatagramPacket rrqPkt) {
      
         address = rrqPkt.getAddress();
         port = rrqPkt.getPort();
      
      //Create a ByteArrayInputStream from the payload
      //NOTE: gigve the packet data, offset, and length to ByteArrayInputStream
         ByteArrayInputStream bais = new ByteArrayInputStream(rrqPkt.getData(), rrqPkt.getOffset(), rrqPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort(); } 
         catch (Exception e) {}
         if (opcode != TFTPConstants.RRQ) {
            fileName = "";
            mode = "";
            try { dis.close(); } 
            catch (Exception e) {}
            return;
         }
      
         fileName = readToZ(dis);
         mode = readToZ(dis);
         try { dis.close(); } 
         catch(Exception e) {}
      }
   }//RRQPacket

   class DATAPacket {
   
      InetAddress toAddress;
      InetAddress address;
      int port;
      int blockNo;
      byte[] data;
      int dataLen;
      int opcode = TFTPConstants.DATA;
   
      public DATAPacket (InetAddress _toAddress, int _port, int _blockNo, byte[] _data, int _dataLen) {
         toAddress = _toAddress;
         port = _port;
         blockNo = _blockNo;
         data = _data;
         dataLen = _dataLen;
      }
      
      public DATAPacket() {
      
      }
      
      public int getOpcode() {
         return opcode;
      }
      
      public InetAddress getToAddress() {
         return toAddress;
      }
      
      public InetAddress getAddress() {
         return address;
      }
      
      public int getPort() {
         return port;
      }
      
      public int getBlockNo() {
         return blockNo;
      }
      
      public byte[] getData() {
         return data;
      }
      
      public int getDataLen() {
         return dataLen;
      }
      
      public DatagramPacket build() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + 2 /*block number*/ + dataLen);
         DataOutputStream dos = new DataOutputStream(baos);
         try {
            dos.writeShort(opcode);
            dos.writeShort(blockNo);
            dos.write(data);
         }
         catch (Exception e) {}
         
         //Close the DataOutputStream to flush the last of the packet
         //out to the ByteArrayOutputStream
         try {
            dos.close();}
         catch (Exception e) {}
         
         byte[] holder = baos.toByteArray(); //Get the underlying byte[]
         DatagramPacket dataPkt = new DatagramPacket(holder, holder.length, toAddress, port);
         
         return dataPkt;
      }
      
      public void dissect (DatagramPacket dataPkt) {
      
         address = dataPkt.getAddress();
         port = dataPkt.getPort();
         dataLen = dataPkt.getLength()-4;
         data = new byte[dataLen];
         
      
      //Create a ByteArrayInputStream from the payload
      //NOTE: gigve the packet data, offset, and length to ByteArrayInputStream
         ByteArrayInputStream bais = new ByteArrayInputStream(dataPkt.getData(), dataPkt.getOffset(), dataPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort(); 
            blockNo = dis.readShort();
         } 
         catch (Exception e) {}
         if (opcode != TFTPConstants.DATA) {
            
            try { dis.close(); } 
            catch (Exception e) {}
            return;
         }
         
         try { int nread = dis.read(data, 0, dataLen); 
         
            
            
         } 
         catch (Exception e) {}
         try { dis.close(); } 
         catch(Exception e) {}
      }
   }//DATAPacket
   
   class ACKPacket {
   
      InetAddress toAddress;
      InetAddress address;
      int port;
      int blockNo;
      int opcode = TFTPConstants.ACK;
      
      public ACKPacket (InetAddress _toAddress, int _port, int _blockNo) {
         toAddress = _toAddress;
         port = _port;
         blockNo = _blockNo;
      }
      
      public ACKPacket() {
      
      }
      
      public int getOpcode() {
         return opcode;
      }
      
      public InetAddress getToAddress() {
         return toAddress;
      }
      
      public InetAddress getAddress() {
         return address;
      }
      
      public int getPort() {
         return port;
      }
      
      public int getBlockNo() {
         return blockNo;
      }
   
   
      
      public DatagramPacket build() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + 2 /*block number*/);
         DataOutputStream dos = new DataOutputStream(baos);
         try {
            dos.writeShort(opcode);
            dos.writeShort(blockNo);
         }
         catch (Exception e) {}
         
         //Close the DataOutputStream to flush the last of the packet
         //out to the ByteArrayOutputStream
         try {
            dos.close();}
         catch (Exception e) {}
         
         byte[] holder = baos.toByteArray(); //Get the underlying byte[]
         DatagramPacket ackPkt = new DatagramPacket(holder, holder.length, toAddress, port);
         
         return ackPkt;
      }
      
      public void dissect (DatagramPacket ackPkt) {
      
         address = ackPkt.getAddress();
         port = ackPkt.getPort();
         
         
      
      //Create a ByteArrayInputStream from the payload
      //NOTE: gigve the packet data, offset, and length to ByteArrayInputStream
         ByteArrayInputStream bais = new ByteArrayInputStream(ackPkt.getData(), ackPkt.getOffset(), ackPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort();} 
         catch (Exception e) {}
         if (opcode != TFTPConstants.ACK) {
            
            try { dis.close(); } 
            catch (Exception e) {}
            return;
         }
         
         try { blockNo = dis.readShort(); }
         catch (Exception e) {}
         try { dis.close(); } 
         catch(Exception e) {}
      }
   }//ACKPacket
   
   class WRQPacket {
   
      InetAddress toAddress;
      InetAddress address;
      int port;
      String fileName;
      String mode;
      int opcode = TFTPConstants.WRQ;
   
      public WRQPacket (InetAddress _toAddress, int _port, String _fileName, String _mode) {
         toAddress = _toAddress;
         port = _port;
         fileName = _fileName;
         mode = _mode;
      }
      
      public WRQPacket() {
      
      }
      
      public int getOpcode() {
         return opcode;
      }
      
      public InetAddress getToAddress() {
         return toAddress;
      }
      
      public InetAddress getAddress() {
         return address;
      }
      
      public int getPort() {
         return port;
      }
      
      public String getFileName() {
         return fileName;
      }
      
      public String getMode() {
         return mode;
      }
      
      public DatagramPacket build() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + fileName.length() + 1 /*0*/ + "octet".length() + 1 /*0*/);
         DataOutputStream dos = new DataOutputStream(baos);
         try {
            dos.writeShort(opcode);
            dos.writeBytes(fileName);
            dos.writeByte(0);
            dos.writeBytes("octet");
            dos.writeByte(0);
         }
         catch (Exception e) {}
         
         //Close the DataOutputStream to flush the last of the packet
         //out to the ByteArrayOutputStream
         try {
            dos.close();}
         catch (Exception e) {}
         
         byte[] holder = baos.toByteArray(); //Get the underlying byte[]
         DatagramPacket wrqPkt = new DatagramPacket(holder, holder.length, toAddress, port);
         
         return wrqPkt;
      }
      
      public void dissect (DatagramPacket wrqPkt) {
      
         address = wrqPkt.getAddress();
         port = wrqPkt.getPort();
      
      //Create a ByteArrayInputStream from the payload
      //NOTE: gigve the packet data, offset, and length to ByteArrayInputStream
         ByteArrayInputStream bais = new ByteArrayInputStream(wrqPkt.getData(), wrqPkt.getOffset(), wrqPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort(); } 
         catch (Exception e) {}
         if (opcode != TFTPConstants.WRQ) {
            fileName = "";
            mode = "";
            try { dis.close(); } 
            catch (Exception e) {}
            return;
         }
      
         fileName = readToZ(dis);
         mode = readToZ(dis);
         try { dis.close(); } 
         catch(Exception e) {}
      }
   }//WRQPacket
   
   class ERRORPacket {
   
      InetAddress toAddress;
      InetAddress address;
      int port;
      int errorNo;
      String errorMsg;
      int opcode = TFTPConstants.ERROR;
   
      public ERRORPacket (InetAddress _toAddress, int _port, int _errorNo, String _errorMsg) {
         toAddress = _toAddress;
         port = _port;
         errorNo = _errorNo;
         errorMsg = _errorMsg;
      }
      
      public ERRORPacket() {
      
      }
      
      public int getOpcode() {
         return opcode;
      }
      
      public InetAddress getToAddress() {
         return toAddress;
      }
      
      public InetAddress getAddress() {
         return address;
      }
      
      public int getPort() {
         return port;
      }
      
      public int getErrorNo() {
         return errorNo;
      }
      
      public String getErrorMsg() {
         return errorMsg;
      }
      
      public DatagramPacket build() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(2 /*opcode*/ + 2 /*block number*/ + errorMsg.length());
         DataOutputStream dos = new DataOutputStream(baos);
         try {
            dos.writeShort(opcode);
            dos.writeShort(errorNo);
            dos.writeBytes(errorMsg);
         }
         catch (Exception e) {}
         
         //Close the DataOutputStream to flush the last of the packet
         //out to the ByteArrayOutputStream
         try {
            dos.close();}
         catch (Exception e) {}
         
         byte[] holder = baos.toByteArray(); //Get the underlying byte[]
         DatagramPacket errorPkt = new DatagramPacket(holder, holder.length, toAddress, port);
         
         return errorPkt;
      }
      
      public void dissect (DatagramPacket errorPkt) {
      
         address = errorPkt.getAddress();
         port = errorPkt.getPort();
         
         
      
      //Create a ByteArrayInputStream from the payload
      //NOTE: gigve the packet data, offset, and length to ByteArrayInputStream
         ByteArrayInputStream bais = new ByteArrayInputStream(errorPkt.getData(), errorPkt.getOffset(), errorPkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         try { opcode = dis.readShort(); } 
         catch (Exception e) {}
         if (opcode != TFTPConstants.ERROR) {
            
            try { dis.close(); } 
            catch (Exception e) {}
            return;
         }
         
         try { errorNo = dis.readShort(); 
            errorMsg = readToZ(dis);}
         catch (Exception e) {}
         try { dis.close(); } 
         catch(Exception e) {}
      }  
   }//ERRORPacket
   
   public static String readToZ(DataInputStream dis) {
      String value = "";
      while (true) {
         try { 
            byte b = dis.readByte(); 
            if (b == 0)
               return value;
            value += (char) b;
         } catch (Exception e) {}
      }
   }
}

