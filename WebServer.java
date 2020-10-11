/* Alex D'Amico , Jason Chen, Abhi Vempati
 CSC 360 Project 1 
*/
import java.io.*;
import java.net.*;
import java.util.*;


public final class WebServer {

  String HOST_NAME = "127.0.0.1";

  public static void main(String[] args) throws Exception {
        // Da port
        int port = 8888;
        
        // Server Socket 
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("******************** YEET ********************");
        while (true){
            //Listen for a TCP connection request
            Socket clientSocket = serverSocket.accept();
     
            // Construct an object to process HTTP request message
            HttpRequest request = new HttpRequest(clientSocket);

            // Create a new thread to process the request
            Thread thread = new Thread(request);
            
            // Start the thread
            thread.start();
        }
    
  }
}

final class HttpRequest implements Runnable {
      
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequest(Socket socket) throws Exception {
      this.socket = socket;
    }

    // Run() method of the Runnable interface
    public void run() {
      try {
          processRequest();
      } catch (Exception e) {
          System.out.println(e);
      }
    }

    private void processRequest() throws Exception {
      // Get a reference to the socket's input and output streams
      InputStream is = socket.getInputStream();
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());

      // Set up input stream filters
    //   FilterInputStream is = new FilterInputStream(is);
    //   BufferedReader br = new BufferedReader(is);

      System.out.println(is);
    }
    

}
