package http.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * A thread of the server that is created when a new connection
 * is established and that handles the associated request
 */
public class ServerThread extends Thread{
    /**
     * The input stream
     */
    BufferedReader in;
    /**
     * The output stream
     */
    PrintWriter out;
    /**
     * The request associated
     */
    Request req;
    /**
     * The socket to which this <code>ServerThread</code>
     * is connected
     */
    Socket remote;

    /**
     * Constructor of ServerThread that initiates the input/output
     * streams and creates the associated request
     * @param remote the socket to which the server is connected
     * @throws IOException
     */
    ServerThread(Socket remote) throws IOException {
        this.remote = remote;
        this.in = new BufferedReader(new InputStreamReader(
                remote.getInputStream()));;
        this.out = new PrintWriter(remote.getOutputStream());
        this.req = Request.parse(remote.getInputStream());
    }

    /**
     * Method responsible for handling the request in this thread
     */
    @Override
    public void run() {
        System.out.println(req.toString());
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

    /**
     * Method called when the <code>method</code> of the request
     * is not supported by our server
     * @param out Output stream to which to write
     */
    public void handleUnrecognized(PrintWriter out){
        out.println("HTTP/1.0 501 NOT IMPLEMENTED");
        out.println("Content-Type: text/html");
        out.println("Server: Bot");
        // this blank line signals the end of the headers
        out.println("");
    }

    /**
     * Method used to handle GET Requests:
     * Sends the requested file if there are no parameters in the URI,
     * processes the action to do otherwise.
     * If URI is equal to '/', sends the file page1.html by default
     * @param req the request to handle
     * @param out Output stream to which to write
     */
    public void handleGET(Request req, OutputStream out){
        String page = "";
        if(req.uri.equals("/")){
            page = "/page1.html";
        }else{
            page = req.uri;
        }

        PrintWriter writer = new PrintWriter(out);
        try {
            if (req.params.isEmpty()) {
                File file = new File(System.getProperty("user.dir") + "/web/html" + page);
                String fileType = null;
                fileType = Files.probeContentType(file.toPath());
                String extension = "";
                if (fileType == null) {
                    extension = page.substring(page.lastIndexOf(".") + 1);
                    if (extension.equals("js")) {
                        fileType = "text/javascript";
                    }
                }

                Scanner myReader = null;

                if (fileType.equals(null)) {
                    writer.println("HTTP/1.1 415 Unsupported Media Type");
                    writer.println("Server: Bot");
                    // this blank line signals the end of the headers
                    writer.println("");
                } else {
                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Content-Type: " + fileType);
                    System.out.println("type : " + fileType);
                    writer.println("Server: Bot");
                    // this blank line signals the end of the headers
                    writer.println("");
                }


                if (fileType.equals("text/html") || fileType.equals("text/javascript")) {
                    myReader = new Scanner(file);
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        writer.println(data);
                    }
                    writer.println("");
                } else if (!fileType.equals(null)) {
                    writer.flush();
                    Files.copy(file.toPath(), out);
                    out.flush();
                }
                writer.println("");
                writer.flush();
                writer.close();
                if (myReader != null) {
                    myReader.close();
                }
            } else {
                switch (req.uri){
                    case "/Adder.html":
                        switch (req.params.get("todo")){
                            case "add":
                                int a = Integer.valueOf(req.params.get("a"));
                                int b = Integer.valueOf(req.params.get("b"));
                                int res = a + b;
                                writer.println("HTTP/1.1 200 OK");
                                writer.println("Content-Type: text/html");
                                writer.println("Server: Bot");
                                // this blank line signals the end of the headers
                                writer.println("");
                                writer.println(res);
                                writer.println("");
                                writer.flush();
                                writer.close();
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
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
    }

    /**
     * Method used to handle POST Requests:
     * Writes the content of the body of the Request at the end of the file
     * specified in the URI : if URI is absent (is equal to '/'),
     * sends 404 Code
     * @param req the request to handle
     * @param out Output stream to which to write
     */
    public void handlePOST(Request req, PrintWriter out){
        String page = checkURIAbsent(req,out);

        if (!page.equals("")) {

            try {
                File reader = new File(System.getProperty("user.dir") + "/web/html" + page);
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
        }

    }

    /**
     * Method used to handle HEAD Requests:
     * Sends the head of the requested file in the URI.
     * If URI is equal to '/', sends the file page1.html by default
     * @param req the request to handle
     * @param out Output stream to which to write
     */
    public void handleHEAD(Request req, PrintWriter out){
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
    }

    /**
     * Method used to handle DELETE Requests:
     * Deletes the file specified in the URI
     * If URI is equal to '/' or if the file is not found,
     * sends 404 Code
     * @param req the request to handle
     * @param out Output stream to which to write
     */
    public void handleDELETE(Request req, PrintWriter out){
        String page = checkURIAbsent(req,out);

        if (!page.equals("")) {
            File file = new File(System.getProperty("user.dir") + "/web/html" + page);
            if (file.delete()) {
                out.println("HTTP/1.0 200 OK");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");
                System.out.println("deleted");
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println("Server: Bot");
                // this blank line signals the end of the headers
                out.println("");
                out.println("<h1>Status code 404 : Not Found</h1>");
                out.println("");
            }
        }
    }

    /**
     * Method used to handle POST Requests:
     * Writes the content of the body of the Request in the file
     * specified in the URI deleting the previous content:
     * if URI is absent (is equal to '/'), sends 404 Code
     * @param req the request to handle
     * @param out Output stream to which to write
     */
    public void handlePUT(Request req, PrintWriter out){
        String page = checkURIAbsent(req,out);

        if (!page.equals("")){
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
        }
    }

    /**
     * Method used to check if the URI is absent (is equal to '/'):
     * if absent, sends 404 code and return an empty string,
     * otherwise, returns the URI of the request
     * @param req the request to handle
     * @param out Output stream to which to write
     * @return name of page/file requested or an empty string
     */
    public String checkURIAbsent(Request req, PrintWriter out){
        String page = "";
        if(req.uri.equals("/")){
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");
            out.println("<h1>Status code 404 : Not Found</h1>");
            out.println("");
        }else{
            page = req.uri;
        }
        return page;
    }



}
