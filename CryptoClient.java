// Kevin Lee
// CS380
// Project 6

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import javax.crypto.*;

//----------------------------------------------------------------------------------------------------
class CryptoClient
{
   static OutputStream os;
   static InputStream is;
   static int port, sourceAddress, destinationAddress;
   static double totalTime;
   static Key currentSessionKey;
   static Cipher cipher;

   public static void main(String args[]) throws Exception
   {
      try
      {
         // read RSA key file
         File publicKeyFile = new File("public.bin");
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(publicKeyFile));
         RSAPublicKey publicKey = (RSAPublicKey) ois.readObject();
         ois.close();

         while(true)
         {
            // connect to the server
            Socket socket = new Socket("76.91.123.97", 22222);
            //oos = new ObjectOutputStream(socket.getOutputStream());
            os = socket.getOutputStream();
            is = socket.getInputStream();

            // wait for command
            System.out.println("Hit 'Enter' to begin. Type 'exit' to disconnect.");
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            if(input.equals("exit")) System.exit(0);

            // generate a new AES session key
            System.out.print("Generating new AES session key...");
            AESKey sessionKey = new AESKey();
            currentSessionKey = sessionKey.getKey();
            System.out.print("DONE\n");

            // cipher the AES key with the RSA key
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherText = cipher.doFinal(sessionKey.getSerializedKeyBytes());

            // set source and destination addresses
            sourceAddress = toIpv4Address(InetAddress.getLocalHost().getHostAddress());
            destinationAddress = toIpv4Address("76.91.123.97");

            // send the Ipv4 packet & UDP packet
            totalTime = 0;
            SendIpv4Packet(sourceAddress, destinationAddress, cipherText);
            SendUDPPacket(sourceAddress, destinationAddress);

            // close the connection
            socket.close();
            System.out.println("Average RTT: " + totalTime/10 + "ms\n");
         }// end of while loop
      }// end of try
      catch (IOException e) { System.out.println("ERROR: Could not connect to server."); }
   }
//----------------------------------------------------------------------------------------------------
   public static void SendIpv4Packet(int sourceAddress, int destinationAddress, byte[] cipherText)
   {
      try
      {
         // Ipv4 values
         byte ipVersion = 4;
         byte hLength = 5;
         byte tos = 0;
         short totalLength = (short) (20 + cipherText.length);
         short id = 0;
         byte flags = 0;
         short fragmentOffset = 1024;
         byte ttl = 50;
         byte protocol = 17;
         short checksum = 0;

         // construct Ipv4 packet
         byte[] b = new byte[totalLength];
         ByteBuffer buf = ByteBuffer.wrap(b);
         buf.put((byte) (((ipVersion & 0xf) << 4) | (hLength & 0xf)));
         buf.put((byte) tos);
         buf.putShort(totalLength);
         buf.putShort(id);
         buf.putShort((short) (((flags & 0x7) << 13) | (fragmentOffset & 0x1fff) << 4));
         buf.put((byte) ttl);
         buf.put((byte) protocol);
         buf.putShort(checksum);
         buf.putInt(sourceAddress);
         buf.putInt(destinationAddress);
         calculateChecksum(buf, hLength, checksum);
         buf.put(cipherText);

         // send Ipv4 packet
         System.out.println("Sending Ipv4 packet data...");
         os.write(b);

         // read 4 bytes from server
         Thread.sleep(1000);
         byte[] responseByte = new byte[is.available()];
         is.read(responseByte);

         //convert 4 bytes into hex
         String response = "";
         for(byte temp : responseByte)
         {
            int intValue = Integer.parseInt(String.valueOf(temp & 0x00ff));
            response += Integer.toString(intValue, 16);
         }

         // print server response
         System.out.println("Response: 0x" + response.toUpperCase() + "\n");
         //printByteData(b);
      }
      catch (IOException io) {}
      catch (InterruptedException iex) {}
   }
//----------------------------------------------------------------------------------------------------
   public static void SendUDPPacket(int sourceAddress, int destinationAddress) throws Exception
   {
      int payloadSize = 1;
      int doubleTheSize = 2;
      double time = 0;

      System.out.println("Sending Ipv4 + UDP packet data...");

      // send Ipv4 + UDP data 10 times, each time doubling the data size
      for(int j = 0; j < 10; j++)
      {
         try
         {
            // creates and randomly fill payload data
            byte[] payloadData = new byte[payloadSize * doubleTheSize];
            payloadSize = payloadData.length;
            Random rand = new Random();
            for(int i = 0; i < payloadSize; i++) { payloadData[i] = (byte) rand.nextInt(); }

            // Ipv4 values
            byte ipVersion = 4;
            byte hLength = 5;
            byte tos = 0;
            short totalLength = (short) (20 + 8 + payloadSize);
            short id = 0;
            byte flags = 0;
            short fragmentOffset = 1024;
            byte ttl = 50;
            byte protocol = 17;
            short checksum = 0;

            // udp header + data, 8 bytes + payload data
            byte[] udp = new byte[8 + payloadData.length];
            ByteBuffer header = ByteBuffer.wrap(udp);
            header.putShort((short) 22222);
            header.putShort((short) 22222);
            header.putShort((short) udp.length);
            header.putShort(getUDPChecksum(udp.length, checksum, payloadData));
            header.put(payloadData);

            // Ipv4 packet + udp header
            byte[] b = new byte[totalLength];
            ByteBuffer buf = ByteBuffer.wrap(b);
            buf.put((byte) (((ipVersion & 0xf) << 4) | (hLength & 0xf)));
            buf.put((byte) tos);
            buf.putShort(totalLength);
            buf.putShort(id);
            buf.putShort((short) (((flags & 0x7) << 13) | (fragmentOffset & 0x1fff) << 4));
            buf.put((byte) ttl);
            buf.put((byte) protocol);
            buf.putShort(checksum);
            buf.putInt(sourceAddress);
            buf.putInt(destinationAddress);
            calculateChecksum(buf, hLength, checksum);
            buf.put(udp);

            // cipher
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, currentSessionKey);
            byte[] cipherText = cipher.doFinal(b);

            //start timer, send Ipv4 + UDP packet, wait for response
            StopWatch sw = new StopWatch();
            os.write(cipherText);
            Thread.sleep(1000);

            // read 4 bytes from server
            byte[] responseByte = new byte[is.available()];
            is.read(responseByte);
            time = sw.elapsedTime(); //stop timer

            //convert 4 bytes into hex
            String response = "";
            for(byte temp : responseByte)
            {
               int intValue = Integer.parseInt(String.valueOf(temp & 0x00ff));
               response += Integer.toString(intValue, 16);
            }

            // print server response & reponse time
            System.out.println("Response: 0x" + response.toUpperCase());
            totalTime += time - 1000;
            System.out.println(time - 1000 + "ms");

         }// end of try
         catch (IOException io) {}
         catch (InterruptedException iex) {}
      }// end of 10 loops
   }
//----------------------------------------------------------------------------------------------------
   public static int toIpv4Address(String ipAddress)
   {
      if (ipAddress == null) throw new IllegalArgumentException("format incorrect.");
      String[] octets = ipAddress.split("\\.");
      if (octets.length != 4) throw new IllegalArgumentException("format incorrect.");

      int result = 0;
      for (int i = 0; i < 4; ++i) { result |= Integer.valueOf(octets[i]) << ((3-i)*8); }
      return result;
   }
//----------------------------------------------------------------------------------------------------
   public static void calculateChecksum(ByteBuffer buf, byte hLength, short checksum)
   {
      if (checksum == 0)
      {
         buf.rewind();
         int x = 0;
         for (int i = 0; i < hLength * 2; ++i) { x += 0xffff & buf.getShort(); }
         x = ((x >> 16) & 0xffff) + (x & 0xffff);
         checksum = (short) (~x & 0xffff);
         buf.putShort(10, checksum);
      }
   }
//----------------------------------------------------------------------------------------------------
   public static short getUDPChecksum(int length, short checksum, byte[] payloadData)
   {
      // udp header, 8 bytes
      ByteBuffer header = ByteBuffer.allocate(length);
      header.putShort((short) 22222);
      header.putShort((short) 22222);
      header.putShort((short) length);
      header.putShort((short) 0);
      header.put(payloadData);
      header.rewind();
      int accumulation = 0;

      // compute pseudo header, 12 bytes
      accumulation += ((sourceAddress >> 16) & 0xffff) + (sourceAddress & 0xffff);
      accumulation += ((destinationAddress >> 16) & 0xffff) + (destinationAddress & 0xffff);
      accumulation += (byte) 17 & 0xffff;
      accumulation += length & 0xffff;

      // compute header
      for (int i = 0; i < length / 2; ++i) accumulation += 0xffff & header.getShort();

      // if length of header is odd
      if(length % 2 > 0) accumulation += (header.get() & 0xff) << 8;

      accumulation = ((accumulation >> 16) & 0xffff) + (accumulation & 0xffff);
      short result = (short) (~accumulation & 0xffff);
      return result;
   }
//----------------------------------------------------------------------------------------------------
   public static void printByteData(byte[] b)
   {
      System.out.print("\nbytes sent: ");
      for ( byte testing : b ) System.out.print(testing + " ");
      System.out.println("\n");
   }
//----------------------------------------------------------------------------------------------------
}// end of CryptoClient class
//----------------------------------------------------------------------------------------------------
class StopWatch
{
   private double start;
//---------------------------------------------------------------------------------------------------
   public StopWatch() { start = System.currentTimeMillis(); }
//----------------------------------------------------------------------------------------------------
   public double elapsedTime() { return ( (System.currentTimeMillis() - start) ); }
//----------------------------------------------------------------------------------------------------
}//end of StopWatch class
//----------------------------------------------------------------------------------------------------
class AESKey implements Serializable
{
   Key sessionKey;
   byte[] serializedKeyBytes;
//----------------------------------------------------------------------------------------------------
   public AESKey() throws Exception
   {
      // create AES session key
      sessionKey = KeyGenerator.getInstance("AES").generateKey();

      try
      {
         // serialize the AES session key
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos);
         out.writeObject(sessionKey);
         serializedKeyBytes = bos.toByteArray();
         out.close();
         bos.close();
      }
      catch (IOException e) { System.out.println("Error trying to serialize the AES key"); }
   }
//----------------------------------------------------------------------------------------------------
   public Key getKey() { return sessionKey; }
//----------------------------------------------------------------------------------------------------
   public byte[] getSerializedKeyBytes() { return serializedKeyBytes; }
//----------------------------------------------------------------------------------------------------
}// end of AESKey class
//----------------------------------------------------------------------------------------------------
