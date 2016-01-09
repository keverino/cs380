// Kevin Lee
// CS380
// Project 5

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;

//----------------------------------------------------------------------------------------------------
class TicTacToeClient implements Serializable
{
   private static ObjectOutputStream oos;
   static byte[][] board = new byte[3][3];

//----------------------------------------------------------------------------------------------------
   public static void main(String args[])
   {
      Scanner scanner = new Scanner(System.in);

      try
      {
         // connect to the server & start new thread to read from server
         Socket socket = new Socket("76.91.123.97", 22222);
         ReadFromServer rfs = new ReadFromServer(socket);
         new Thread(rfs).start();
         System.out.println("\nConnected: " + socket + "\n");
         oos = new ObjectOutputStream(socket.getOutputStream());

         System.out.println("Type 'start' to begin or 'help' for more information.");

         // user input
         while(true)
         {
            if(scanner.hasNextInt())
            {
               int i = scanner.nextInt();
               int j = scanner.nextInt();
               byte row = (byte) i;
               byte col = (byte) j;
               oos.writeObject(new MoveMessage(row, col));
            }
            else if(scanner.hasNext())
            {
               String input = scanner.next();
               if(input.equals("help")) help();
               if(input.equals("start"))
               {
                  //start a new game
                  oos.writeObject(new ConnectMessage("Player1"));
                  oos.writeObject(new StartGameMessage(null));
               }
               if(input.equals("exit"))
               {
                  oos.writeObject(new CommandMessage(CommandMessage.Command.EXIT));
                  try { Thread.sleep(1000); }
                  catch (InterruptedException iex) {}
                  System.exit(0);
               }
               if(input.equals("surrender"))
                  oos.writeObject(new CommandMessage(CommandMessage.Command.SURRENDER));
               if(input.equals("players"))
                  oos.writeObject(new CommandMessage(CommandMessage.Command.LIST_PLAYERS));
            }

         }//end while
      }///end try
      catch (IOException e) {}
   }//end of main()
//----------------------------------------------------------------------------------------------------
   public static void help()
   {
      System.out.println("HOW TO PLAY:");
      System.out.println("1. type 'start' to begin the game.");
      System.out.println("2. Enter in a move.");
      System.out.println("\ttype in '0 1' for first first row, second column.");
      System.out.println("3. type 'players' to get a list of players.");
      System.out.println("4. type 'surrender' to give up.");
      System.out.println("5. type 'exit' to quit the program.");
      System.out.println();
   }
//----------------------------------------------------------------------------------------------------
}// end of TicTacToeClient class
//----------------------------------------------------------------------------------------------------
// Reads text from the server, displays to console.
class ReadFromServer implements Runnable
{
   Socket socket;
   //BoardMessage.Status status;

   ReadFromServer(Socket s) { socket = s; }
//----------------------------------------------------------------------------------------------------
   public void run()
   {
      try
      {
         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

         while(true)
         {
            Object object = ois.readObject();

            if(object instanceof BoardMessage)
            {
               BoardMessage board = (BoardMessage) object;
               System.out.println(board.getStatus());
               byte[][] updatedBoard = board.getBoard();
               printBoard(updatedBoard);
            }

            else if(object instanceof ErrorMessage)
            {
               ErrorMessage errorMsg = (ErrorMessage) object;
               System.out.println(errorMsg.getError());
            }

            else if(object instanceof PlayerListMessage)
            {
               PlayerListMessage players = (PlayerListMessage) object;
               String[] playerList = players.getPlayers();
               System.out.print("Current players: ");
               for(int i = 0; i < playerList.length; i++) System.out.print(playerList[i] + " ");
               System.out.println();
            }
         }//end while
      }//end try
      catch (IOException e) {}
      catch (ClassNotFoundException cnfe) { System.out.println("Class not found."); }
   }//end of run()
//----------------------------------------------------------------------------------------------------
   public static void printBoard(byte[][] board)
   {
      for(int i = 0; i < 3; i++)
      {
         for(int j = 0; j < 3; j++) System.out.print("\t" + board[i][j]);
         System.out.println();
      }
   }//end printBoard()
}// end of ReadFromServer class
//----------------------------------------------------------------------------------------------------
