package http.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
/**
 * Class representing a Request with all its attributes
 */
public class Request {
    /**
     * Type of Method of the Request
     */
    public String method;
    /**
     * The URI of the request, without the parameters (starting at ?)
     */
    public String uri;
    /**
     * HTTP Version of the request
     */
    public String version;
    /**
     * Map of the headers of the request with
     * the key being the name of the header
     */
    public Map<String,String> headers;
    /**
     * Map of parameters from the URI with the
     * key being the name of the parameter
     */
    public Map<String, String > params;
    /**
     * The body of the request
     */
    public String body;
    /**
     * Boolean which indicates if the request is well
     * formed, ie. it's not a Bad Request
     */
    public boolean wellFormed;

    /**
     * Constructor of the Request
     * @param method
     * @param uri
     * @param version
     * @param params
     * @param headers
     * @param body
     * @param wellFormed
     */
    public Request(String method, String uri, String version, Map<String,String> params,
                   Map<String,String> headers, String body, boolean wellFormed){
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.headers = headers;
        this.body = body;
        this.wellFormed = wellFormed;
        this.params = params;
    }

    /**
     * Method that parses the <code>InputStream</code> and
     * creates an instance of Request which is then returned
     * @param is the InputStream from the connected socket
     * @return the created Request
     * @throws IOException
     */
    public static Request parse(InputStream is) throws IOException{
        Scanner scanner = new Scanner(is);
        String requestLine = scanner.nextLine();
        String parts[] = requestLine.split(" ");
        boolean wellFormed = true;
        if(parts.length != 3)
            wellFormed = false;

        String method = parts[0];
        String uri = parts[1];
        String version = parts[2];
        int indexParam = uri.indexOf('?');
        Map<String,String> params = new HashMap<String, String>();;
        if (indexParam != -1){
            String paramsStr = uri.substring(indexParam+1);
            uri = uri.substring(0,indexParam);
            String[] pairs = paramsStr.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                params.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        Map<String, String> headers = new HashMap<String, String>();
        String headerLine;
        while (true){
            headerLine = scanner.nextLine();
            if (headerLine==null || headerLine.isEmpty()){
                break;
            }
            String headerParts[] = headerLine.split(": ");
            if(headerParts.length != 2)
                wellFormed = false;

            headers.put(headerParts[0], headerParts[1]);
        }

        String body=null;
        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr!=null && !contentLengthStr.isEmpty()){
            scanner.useDelimiter("\\z");
            body = scanner.next();
        }
        return new Request(method, uri, version, params, headers, body, wellFormed);

    }

    @Override
    public String toString(){
        String res = this.method + " " + this.uri + " " + this.version + "\n";
        res += "Parameters : " + this.params.toString()+ "\n";
        res += "Headers : " + this.headers.toString() + "\n";
        res += "Body : " + this.body ;
        return  res;
    }
}
