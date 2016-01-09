//Kevin Lee
//CS380
//Project 2

import java.net.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////////////////////////////
class ChatClient
{
   public static void main(String args[]) throws Exception 
   {
      Socket socket = new Socket("76.91.123.97",22222);
      
      new readFromServer(socket);
      new sendToServer(socket);

      System.out.println("Connected: " + socket);
      //System.out.println("type 'exit' to leave");
      System.out.println("Please enter a username before you start chatting.");
   }
}
//////////////////////////////////////////////////////////////////////////////////////////////////
//Reads text from the server, displays to console.
class readFromServer extends Thread
{
   Socket socket; 
   readFromServer(Socket s)
   {
      socket = s;
      start();
   }
   
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
}
//////////////////////////////////////////////////////////////////////////////////////////////////
//Sends text from console to server.
class sendToServer extends Thread
{
   Socket clientSocket;
   sendToServer(Socket socket)
   {
      clientSocket = socket;
      start();
   }

   public void run()
   {
      try
      {
         PrintWriter streamOut = new PrintWriter(clientSocket.getOutputStream(),true);
         BufferedReader streamIn = new BufferedReader(new InputStreamReader(System.in));  
         String line;

         while((line = streamIn.readLine()) != null) streamOut.println(line);
      }
      catch (IOException e) {}    
   }
}
//////////////////////////////////////////////////////////////////////////////////////////////////