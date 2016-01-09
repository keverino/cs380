//Kevin Lee
//CS380

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(22222);
        System.out.println("Server is now running.");

        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected: " + socket.getInetAddress());
                PrintStream out = new PrintStream(socket.getOutputStream());
                //System.out.println("Hi client, thanks for connecting!");

                BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter w = new PrintWriter(socket.getOutputStream(), true);
                w.println("You are now connected to EchoServer.  Type 'exit' to close.");
                String line;

                do 
                {
                    line = r.readLine();
                    if ( line != null ) w.println("< Server >: " + line);
                }
                while ( !line.trim().equals("exit") );
                socket.close();
            }// end of try
        }//end of while
    }//end of main
}//end of EchoServer
