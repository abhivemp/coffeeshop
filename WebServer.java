/* Alex D'Amico , Jason Chen, Abhi Vempati
 CSC 360 Project 1 
*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.channels.*;

public final class WebServer {

  public static void main(String[] args) throws Exception {
    // Da ports
    int ports[] = {8888, 5555};

    Selector selector = Selector.open();
    
    // Server Socket Channel
    for (int port : ports){
      System.out.println("CONFIGURING PORT #: " + port);
      ServerSocketChannel ssc = ServerSocketChannel.open();
      ssc.configureBlocking(false);
      System.out.println("BINDING SOCKET WITH PORT #: " + port);
      ssc.socket().bind(new InetSocketAddress(port));
      ssc.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("AFTER THE BINDING!!!");
    }

    while (true){
      selector.select();
   
      Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
      System.out.println(selector);
      while(selectedKeys.hasNext()){
        System.out.println("In the second while loop");
        SelectionKey selectedKey = selectedKeys.next();

        if(selectedKey.isAcceptable())
        {
          SocketChannel socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
          socketChannel.configureBlocking(false);
          System.out.println("THE KEY IS ACCEPTABLE!!!");
          switch (socketChannel.socket().getPort()) 
          {
            case 8888:
              HttpRequest httpRequest = new HttpRequest(socketChannel.socket());
              Thread thread1 = new Thread(httpRequest);   //Handles HttpRequest
              thread1.start();
              break;
            case 5555:
              MovedRequest movedRequest = new MovedRequest(socketChannel.socket());
              Thread thread2 = new Thread(movedRequest);   //Handles MovedRequest
              thread2.start();
              break;
          }
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
    System.out.println("Did we get this far?");
    InputStream is = socket.getInputStream();
    DataOutputStream os = new DataOutputStream(socket.getOutputStream());

    // Set up input stream filters
    InputStreamReader reader = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(reader);

    // Get the request line of the HTTP request image
    String requestLine = br.readLine();

    // Display the request line
    System.out.println("******************");
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

    //Testing to see the filename
    System.out.println("*********************");
    System.out.println(requestLine);
    System.out.println("*********************");

    System.out.println("FILE: " + fileName);

    // Open the requested file
    FileInputStream fis = null;
    boolean fileExists = true;
    try {
      fis = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      fileExists = false;
      System.out.println("THIS FILE IS NOT FOUND!!!!!!%^&*(&^%^&*(&^%^*()");
      System.out.println(fileExists);
    }

    // Construct the response message
    String statusLine = null;
    String contentTypeLine = null;
    String entityBody = null;
    if(fileExists) {
      statusLine = "HTTP/1.1 200 OK" + CRLF;
      contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
      System.out.println("Content typeline is " + contentTypeLine);
    }
    else {
      System.out.println("in 404");
      statusLine = "HTTP/1.1 404 NOT FOUND" + CRLF;
      contentTypeLine = "Content-type: text/html" + CRLF;
      entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
    }

    // Send the status line
    os.writeBytes(statusLine);
    System.out.println("Status line is " + statusLine);
    // Send the content type line
    os.writeBytes(contentTypeLine);
    System.out.println("Content typeline is " + contentTypeLine);
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
    System.out.println("In sendbytes!");
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
      return "video/mp4";
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

    //Response Header for 301
    String statusLine = "HTTP/1.1 301 Moved Permanently" + CRLF;
    String locationLine = "Location: http://www.google.com" + CRLF;

    os.writeBytes(statusLine);
    //System.out.println("Status Line: " + statusLine); //Bug testing
    os.writeBytes(locationLine);
    //System.out.println("Location Line: " + locationLine); //Bug testing
    os.writeBytes(CRLF);

    os.close();
  }
}
