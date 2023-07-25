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

public class DATAPacket {

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
