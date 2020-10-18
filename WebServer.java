/* Alex D'Amico , Jason Chen, Abhi Vempati
 CSC 360 Project 1 
*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.channels.*;

public final class WebServer {

  static String HOST_NAME = "localhost";

  public static void main(String[] args) throws Exception {
 
    ServerSocketChannel server1 = ServerSocketChannel.open();
    ServerSocketChannel server2 = ServerSocketChannel.open();
    server1.socket().bind(new InetSocketAddress(8888));
    server2.socket().bind(new InetSocketAddress(5555));
    server1.configureBlocking(false);
    server2.configureBlocking(false);
    Selector selector = Selector.open();
    server1.register(selector, SelectionKey.OP_ACCEPT);
    server2.register(selector, SelectionKey.OP_ACCEPT);
    
    while(true) {
     
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
       
        for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
          SelectionKey key = i.next();
          i.remove();

          Channel c = key.channel();

          if (key.isAcceptable() && c == server1) {
              HttpRequest httpRequest = new HttpRequest(server1.accept().socket());
              new Thread(httpRequest).start();
          } else  {
              MovedRequest mRequest = new MovedRequest(server2.accept().socket());
              new Thread(mRequest).start();
          }
        }
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
        System.out.println("Exception " + e);
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

    // System.out.println(requestLine);
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
   
    // Open the requested file
    FileInputStream fis = null;
    boolean fileExists = true;
    try {
      fis = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      fileExists = false;
    }
   
    // Construct the response message
    String statusLine = null;
    String contentTypeLine = null;
    String entityBody = null;
    if(fileExists) {
      statusLine = "HTTP/1.1 200 OK" + CRLF;
      contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
    }
    else {
      statusLine = "HTTP/1.1 404 NOT FOUND" + CRLF;
      contentTypeLine = "Content-type: text/html" + CRLF;
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
    //os.close();
  
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
    if (fileName.endsWith(".mp4"))
      return  "video/mp4";
    if (fileName.endsWith(".mp3"))
      return "audio/mp3";
    if (fileName.endsWith(".webm"))
      return "video/webm";
    return "application/octet-stream";
  }
} 


final class MovedRequest implements Runnable {

  final static String CRLF = "\r\n";
  Socket socket;

  public MovedRequest(Socket socket) throws Exception{
    this.socket = socket;
  }

  public void run(){
    try{
      processRequest();
    } catch (Exception e){
      System.out.println("Exception: " + e);
    }
  }

  private void processRequest() throws Exception{
    //Setting up In-stream and Out-stream
    System.out.println("Processing MovedRequest");
    InputStream is = socket.getInputStream();
    DataOutputStream os = new DataOutputStream(socket.getOutputStream());

    //Setting up reader & buffer
    InputStreamReader reader = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(reader);

    //Recieving the request line
    String requestLine = br.readLine();

    //Getting the header line
    String headerLine = null;
    while ((headerLine = br.readLine()).length() != 0) {
      System.out.println(headerLine);
    }

    String statusLine = "HTTP/1.1 301 Moved Permanently" + CRLF;
    String locationLine = "Location: http://www.google.com" + CRLF;

    os.writeBytes(statusLine);
    System.out.println("Status Line: " + statusLine);
    os.writeBytes(locationLine);
    System.out.println("Location Line: " + locationLine);
    os.writeBytes(CRLF);

    os.close();
  }
}
