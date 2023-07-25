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

public class ACKPacket {
   
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
