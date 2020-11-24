package http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Request {
    public String method;
    public String uri;
    public String version;
    public Map<String,String> headers;
    public Map<String, String > params;
    public String body;
    public boolean wellFormed;

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
            URL url = new URL(uri);
            uri = uri.substring(0,indexParam);
            params = splitQuery(url);
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
        System.err.println(contentLengthStr);
        if (contentLengthStr!=null && !contentLengthStr.isEmpty()){
            scanner.useDelimiter("\\z");
            body = scanner.next();
        }
        System.err.println("Request created");
        return new Request(method, uri, version, params, headers, body, wellFormed);

    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
