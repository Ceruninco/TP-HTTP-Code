package http.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

public class ServerThread extends Thread{
    BufferedReader in;
    PrintWriter out;
    Request req;
    Socket remote;

    ServerThread(Socket remote) throws IOException {
        this.remote = remote;
        this.in = new BufferedReader(new InputStreamReader(
                remote.getInputStream()));;
        this.out = new PrintWriter(remote.getOutputStream());
        this.req = Request.parse(remote.getInputStream());
    }

    public void run() {
        if (req != null){
            if (!req.wellFormed){
                System.out.println("Bad Request");
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");
                out.println("<h1>Status code 400 : Bad Request</h1>");
                out.println("");

            } else {
                switch (req.method) {
                    case "GET":
                        System.out.println("GET requested");
                        try {
                            handleGET(req, remote.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "POST":
                        System.out.println("POST requested");
                        handlePOST(req, out);

                        break;
                    case "HEAD":
                        System.out.println("HEAD requested");
                        handleHEAD(req, out);
                        break;
                    case "DELETE":
                        System.out.println("DELETE requested");
                        handleDELETE(req, out);
                        break;
                    case "PUT":
                        System.out.println("PUT requested");
                        handlePUT(req, out);
                        break;
                    default :
                        handleUnrecognized(out);
                        break;
                }

            }
        }
        out.flush();
        try {
            remote.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String handleUnrecognized(PrintWriter out){
        out.println("HTTP/1.0 501 NOT IMPLEMENTED");
        out.println("Content-Type: text/html");
        out.println("Server: Bot");
        // this blank line signals the end of the headers
        out.println("");
        return "";
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

            Scanner myReader = null;

            if (fileType.equals(null)) {
                writer.println("HTTP/1.1 415 Unsupported Media Type");
                writer.println("Server: Bot");
                // this blank line signals the end of the headers
                writer.println("");
            }else{
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: "+fileType);
                System.out.println("type : "+fileType);
                writer.println("Server: Bot");
                // this blank line signals the end of the headers
                writer.println("");
            }


            if(fileType.equals("text/html") || fileType.equals("text/javascript")){
                myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    writer.println(data);
                }
                writer.println("");
            }else if(!fileType.equals(null)){
                writer.flush();
                Files.copy(file.toPath(),out);
                out.flush();
            }
            writer.println("");
            writer.flush();
            writer.close();
            if(myReader!=null){
                myReader.close();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            writer.println("HTTP/1.1 404 Not Found");
            writer.println("Content-Type: text/html");
            writer.println("Server: Bot");
            // this blank line signals the end of the headers
            writer.println("");
            writer.println("<h1>Status code 404 : Not Found</h1>");
            writer.flush();
            writer.close();
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
            out.println("");
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
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");
            out.println("<h1>Status code 404 : Not Found</h1>");
            out.println("");
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
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");
            out.println("<h1>Status code 404 : Not Found</h1>");
            out.println("");
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
            out.println("");
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
            System.err.println(req.body);
            historyFileWriter.write(req.body + "\r\n");

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



}
