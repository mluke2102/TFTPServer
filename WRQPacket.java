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

public class WRQPacket {
   
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
      
         // fileName = readToZ(dis);
//          mode = readToZ(dis);
//          try { dis.close(); } 
//          catch(Exception e) {}
      }
   }//WRQPacket
