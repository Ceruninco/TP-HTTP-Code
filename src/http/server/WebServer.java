///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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

        // read the data sent. We basically ignore it,
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        while (str != null && !str.equals("")){
          str = in.readLine();
          if(str != null && !str.equals("")){
            String[] req = str.split(" ");
            switch (req[0]){
              case "GET" :
                System.out.println("GET requested");
                handleGET(req,out);

                break;
              case "POST" :
                System.out.println("POST requested");
                handlePOST(req,out,in);

                break;
              case "HEAD" :
                System.out.println("HEAD requested");
                break;
              case "DELETE" :
                System.out.println("DELETE requested");
                break;
              case "PUT" :
                System.out.println("PUT requested");
                handlePUT(req,out,in);
                break;
            }
            System.out.println(str);
          }else{
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
  public String handleGET(String[] req, PrintWriter out){

    String page = "";
  if(req[1].equals("/")){

    page = "/page1.html";
  }else{
    page = req[1];
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
      }
      out.println("");
      myReader.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return "";
  }
  public String handlePOST(String[] req, PrintWriter out,BufferedReader in){

    String page = "";


    if(req[1].equals("/")){
      return "";
    }else{
      page = req[1];
    }
    Vector<String> content = new Vector<>();
    String current = "";

    try {
      File reader = new File( System.getProperty("user.dir") + "/web/html"+page);
      if (reader.createNewFile()) {
        System.out.println("File created: " + reader.getName());
      } else {
        System.out.println("File already exists.");
      }
      while (!(current=in.readLine()).equals("")){
        content.add(current);
        System.out.println(current);
      }
      System.out.println("number of lines of header : " +content.size());
      FileWriter historyFileWriter = new FileWriter(reader, true);
      while (!(current=in.readLine()).equals("")&&current!=null){
        historyFileWriter.write(current+"\n");
      }
      historyFileWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }


      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers

      out.println("");
    return "";
  }

  public String handlePUT(String[] req, PrintWriter out,BufferedReader in){

    String page = "";


    if(req[1].equals("/")){
      return "";
    }else{
      page = req[1];
    }
    Vector<String> content = new Vector<>();
    String current = "";

    try {
      File reader = new File( System.getProperty("user.dir") + "/web/html"+page);
      if (reader.createNewFile()) {
        System.out.println("File created: " + reader.getName());
      } else {
        System.out.println("File already exists.");
      }
      while (!(current=in.readLine()).equals("")){
        content.add(current);
        System.out.println(current);
      }
      System.out.println("number of lines of header : " +content.size());
      FileWriter historyFileWriter = new FileWriter(reader, false);
      while (!(current=in.readLine()).equals("")&&current!=null){
        historyFileWriter.write(current+"\n");
      }
      historyFileWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }


    out.println("HTTP/1.0 200 OK");
    out.println("Content-Type: text/html");
    out.println("Server: Bot");
    // this blank line signals the end of the headers

    out.println("");
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
