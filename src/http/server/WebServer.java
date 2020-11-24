///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.Vector;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    System.out.println("Webserver starting up on port 80");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(3000);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
            remote.getInputStream()));
        PrintWriter out = new PrintWriter(remote.getOutputStream());
        Request req = Request.parse(remote.getInputStream());
        if (req != null){
          switch (req.method){
            case "GET" :
              System.out.println("GET requested");
              handleGET(req,remote.getOutputStream());
              break;
            case "POST" :
              System.out.println("POST requested");
              handlePOST(req,out);

              break;
            case "HEAD" :
              System.out.println("HEAD requested");
              handleHEAD(req,out);
              break;
            case "DELETE" :
              System.out.println("DELETE requested");
              handleDELETE(req,out);
              break;
            case "PUT" :
              System.out.println("PUT requested");
              handlePUT(req,out);
              break;
          }
        }
        out.flush();
        remote.close();

      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }
  public String handleGET(Request req, OutputStream out){
    String page = "";
    if(req.uri.equals("/")){
      page = "/page1.html";
    }else{
      page = req.uri;
    }

    PrintWriter writer = new PrintWriter(out);
    try {
      File file = new File( System.getProperty("user.dir") + "/web/html"+page);
      String fileType = null;
      fileType =  Files.probeContentType(file.toPath());
      String extension = "";
      if(fileType==null){
        extension = page.substring(page.lastIndexOf(".") +1);
        if(extension.equals("js")){
          fileType = "text/javascript";
        }
      }

      System.out.println(fileType);
      String category = fileType.split("/")[0];
      Scanner myReader = null;


      writer.println("HTTP/1.1 200 OK");
      writer.println("Content-Type: "+fileType);
  //    writer.println("Content-Length: "+file.length());
   //   System.out.println(file.length());
      System.out.println("type : "+fileType);
      writer.println("Server: Bot");
      // this blank line signals the end of the headers
      writer.println("");

      if(fileType.equals("text/html") || fileType.equals("text/javascript")){
        System.out.println("sending html");
        myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
          String data = myReader.nextLine();
          System.out.println(data);
          writer.println(data);
        }
        writer.println("");
      }else{
        writer.flush();
        System.out.println("sending image to server");
        Files.copy(file.toPath(),out);
   //     out.flush();
        System.out.println("image sent");
    //    out.close();
      }

      writer.println("");
      writer.flush();
      writer.close();
      myReader.close();

    System.out.println("end of transmission");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      writer.println("HTTP/1.1 404 Not Found");
      writer.println("Content-Type: text/html");
      writer.println("Server: Bot");
      // this blank line signals the end of the headers
      writer.println("");
      writer.println("<h1>Status code 404 : Not Found</h1>");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  public String handlePOST(Request req, PrintWriter out){

    String page = "";
    if(req.uri.equals("/")){
      out.println("HTTP/1.1 404 Not Found");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");
      out.println("<h1>Status code 404 : Not Found</h1>");
      return "";
    }else{
      page = req.uri;
    }

    try {
      File reader = new File( System.getProperty("user.dir") + "/web/html"+page);
      if (reader.createNewFile()) {
        System.out.println("File created: " + reader.getName());
      } else {
        System.out.println("File already exists.");
      }

      FileWriter historyFileWriter = new FileWriter(reader, true);
      System.out.println("number of chars of body " + req.body.length());

      System.err.println(req.body);
      historyFileWriter.write(req.body + '\n');

      historyFileWriter.close();
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.print("\r\n");

    } catch (IOException e) {
      e.printStackTrace();
    }


    return "";
  }

  public String handleHEAD(Request req, PrintWriter out){

    String page = "";
    if(req.uri.equals("/")){

      page = "/page1.html";
    }else{
      page = req.uri;
    }
    File html = new File( System.getProperty("user.dir") + "/web/html"+page);
    Scanner myReader = null;
    try {
      myReader = new Scanner(html);
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");

      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        out.println(data);
        if(data.equals("</head>")){
          break;
        }
      }
      out.println("");
      myReader.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return "";
  }
  public String handleDELETE(Request req, PrintWriter out){

    String page = "";
    if(req.uri.equals("/")){

      page = "/page1.html";
    }else{
      page = req.uri;
    }
    File file = new File( System.getProperty("user.dir") + "/web/html"+page);
    if(file.delete()){
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");
      System.out.println("deleted");
    }else{
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");
      System.out.println("not deleted");
    }
    return "";
  }

  public String handlePUT(Request req, PrintWriter out){
    String page = "";
    if(req.uri.equals("/")){
      out.println("HTTP/1.1 404 Not Found");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");
      out.println("<h1>Status code 404 : Not Found</h1>");
      return "";
    }else{
      page = req.uri;
    }

    try {
      File reader = new File( System.getProperty("user.dir") + "/web/html"+page);
      if (reader.createNewFile()) {
        System.out.println("File created: " + reader.getName());
      } else {
        System.out.println("File already exists.");
      }

      FileWriter historyFileWriter = new FileWriter(reader, false);
      System.out.println("number of chars of body " + req.body.length());
      /*for (int i=0; i<content.size(); ++i){
        historyFileWriter.write(content.get(i) + "\n");
      }*/
      System.err.println(req.body);
      historyFileWriter.write(req.body + "\n");

      historyFileWriter.close();
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("\r\n");

    } catch (IOException e) {
      e.printStackTrace();
    }


    return "";
  }





  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
