// Kevin Lee
// CS380
// Project 3.5

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

//----------------------------------------------------------------------------------------------------
class Ipv6Client
{
   // toggle showBytes to display the bytes being sent
   static boolean showBytes = false;
   static String input = "";
   static int payloadSize;

   public static void main(String args[])
   {
      try
      {
         while(true)
         {
            // connect to the server
            Socket socket = new Socket("76.91.123.97",22222);
            readFromServer rfs = new readFromServer(socket);
            new Thread(rfs).start();
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("Connected: " + socket);
            System.out.println("-----------------------------------------------------------------");

            // ask user for size of data & wait for exit command
            System.out.println("Enter a payload/data size or type exit to stop sending packets.");
            Scanner sc = new Scanner(System.in);
            input = sc.nextLine();
            try { payloadSize = Integer.parseInt(input); }
            catch(NumberFormatException e)
            {
               if(!input.equals("exit"))
                  System.out.println("ERROR: You did not enter a valid input. Please reload the program.");

               System.exit(0);
            }

            // creates and fill payload data
            byte[] payloadData = new byte[payloadSize];
            Random rand = new Random();
            for(int i = 0; i < payloadSize; i++) { payloadData[i] = (byte) rand.nextInt(); }

            // creates values
            byte placeHolder        = 0;
            byte ipVersion          = 6;  // 4 bits
            byte trafficClass       = 0;  // 8 bits
            byte flowLabel          = 0b00000000000000000000;  // 20 bits
            short payloadLength     = (short) (payloadData.length);  // 16 bits
            byte nextHeader         = 17; // UDP protocol value, 8 bits
            byte hopLimit           = 20; // 8 bits
            byte[] sourceAddress    = toIpv6Address(InetAddress.getLocalHost().getHostAddress());
            byte[] destAddress      = toIpv6Address("76.91.123.97");

            // put values into byte buffer
            byte[] b = new byte[payloadLength + 40];
            ByteBuffer buf = ByteBuffer.wrap(b);
            buf.put((byte) ((ipVersion & 0xf) << 4));
            buf.put((byte) trafficClass);
            buf.put((byte) flowLabel);
            buf.put((byte) placeHolder);
            buf.putShort(payloadLength);
            buf.put((byte) (nextHeader));
            buf.put((byte) hopLimit);
            buf.put(sourceAddress);
            buf.put(destAddress);
            if(payloadData != null) buf.put(payloadData);

            if(showBytes == true) printByteData(b);

            // send bytes through the stream
            OutputStream os = socket.getOutputStream();
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.write(b);

            // wait a second before restarting loop. this gives time for server's response
            try { Thread.sleep(1000); }
            catch (InterruptedException iex) {}

         }// end of while
      }//end of try
      catch (IOException e) {}
   }
//----------------------------------------------------------------------------------------------------
   public static byte[] toIpv6Address(String ipAddress)
   {
      if (ipAddress == null) throw new IllegalArgumentException("format incorrect.");
      String[] octets = ipAddress.split("\\.");
      if (octets.length != 4) throw new IllegalArgumentException("format incorrect.");

      byte[] octectBytes = new byte[4];
      for(int i = 0; i < 4; i++) octectBytes[i] = (byte) Integer.parseInt(octets[i]);

      byte ipv6[] = new byte[16];
      ipv6[10] = (byte)0xff;
      ipv6[11] = (byte)0xff;
      ipv6[12] = octectBytes[0];
      ipv6[13] = octectBytes[1];
      ipv6[14] = octectBytes[2];
      ipv6[15] = octectBytes[3];
      return ipv6;
   }
//----------------------------------------------------------------------------------------------------
   public static void printByteData(byte[] b)
   {
      System.out.print("\nbytes sent: ");
      for ( byte testing : b ) System.out.print(testing + " ");
      System.out.println("\n");
   }
//----------------------------------------------------------------------------------------------------
}// end of Ipv6Client class
// Reads text from the server, displays to console.
class readFromServer implements Runnable
{
   Socket socket;
   readFromServer(Socket s) { socket = s; }

   public void run()
   {
      try
      {
         BufferedReader streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String line;
         while((line = streamIn.readLine()) != null) System.out.println(line);
      }
      catch (IOException e) {}
   }
}// end of readFromServer class
//----------------------------------------------------------------------------------------------------
