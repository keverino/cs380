//Kevin Lee
//CS380

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
import javax.net.SocketFactory;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = SocketFactory.getDefault().createSocket("localhost", 22222)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter w = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader con = new BufferedReader(new InputStreamReader(System.in));
            String line;

            do
            {
            	line = br.readLine();
            	if(line != null) System.out.println(line);
            	System.out.print("< Client >: ");
            	line = con.readLine();
            	w.println(line);
            }
            while ( !line.trim().equals("exit"));
        }//end of try
    }//end of main
}//end of EchoCLient
