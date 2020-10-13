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
      System.out.println("connected!");
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
    InputStreamReader reader = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(reader);

    // Get the request line of the HTTP request image
    String requestLine = br.readLine();

    // Display the request line
    System.out.println("******************");
    System.out.println(requestLine);

    // Get and display the header lines
    String headerLine = null;
    while ((headerLine = br.readLine()).length() != 0) {
      System.out.println(headerLine);
    }

    // shop closed ;)
    // os.close();
    // br.close();
    // socket.close();
    
    // Extract the filename from the request line
    StringTokenizer tokens = new StringTokenizer(requestLine);
    tokens.nextToken(); // skip over the method, which should be "GET"
    String fileName = tokens.nextToken();

    // Prepend a "." so that file req is within current directory
    fileName = "." + fileName;

    //Testing to see the filename
    System.out.println("*********************");
    System.out.println(requestLine);
    System.out.println("*********************");

    System.out.println(fileName);

    // Open the requested file
    FileInputStream fis = null;
    boolean fileExists = true;
    try {
      fis = new FileInputStream(fileName);
      System.out.println("THE FILE NAME IS THIS " + fileName);
    } catch (FileNotFoundException e) {
      fileExists = false;
      System.out.println(fileExists);
    }

    // Construct the response message
    String statusLine = null;
    String contentTypeLine = null;
    String entityBody = null;
    if(fileExists) {
      statusLine = "200 OK" + CRLF;
      contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
    }
    else {
      System.out.println("in 404");
      statusLine = "404 NOT FOUND" + CRLF;
      contentTypeLine = "Content-type: " + CRLF;
      entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
    }

    // Send the status line
    os.writeBytes(statusLine);

    // Send the content type line
    os.writeBytes(contentTypeLine);

    // Send a blank line to indicate the end of the header line
    os.writeBytes(CRLF);
  
    // send the entity body
    if (fileExists) {
      sendBytes(fis, os);
      fis.close();
    } else {
      os.writeBytes(entityBody);
    }
    os.close();
  
  }

  private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
    // Construct a 1K buffer to hold bytes on their way to the socket
    byte[] buffer = new byte[1024];
    int bytes = 0;
    
    // Copy requested file into the socket's output stream
    while((bytes = fis.read(buffer)) != -1 ) {
      os.write(buffer, 0, bytes);
    }
  }
  
  // Examines the file name and their type to represent it's MIME type. 
  private static String contentType(String fileName){
    if(fileName.endsWith(".htm") || fileName.endsWith(".html"))
      return "text/html";
    if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
      return "image/jpeg";
    if(fileName.endsWith(".gif"))
      return "image/gif";
    return "application/octet-stream";
  }
}
