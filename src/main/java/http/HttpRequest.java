package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import webserver.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private BufferedReader br;
    private HashMap<String, String> header;
    private HashMap<String, String> body;

    public HttpRequest(InputStream in){
        this.br = new BufferedReader(new InputStreamReader(in));
        this.header = new HashMap<String, String>();
        this.body = new HashMap<String, String>();
        setHeader();
    }

    private void setHeader() {
        try {


            String line = br.readLine();
            log.debug("request : {}", line);
            header.put("header", line);

            if(line == null){
                return;
            }

            while(!"".equals(line)){
                line = br.readLine();
                log.debug("HEADER: {}", line);
                if(line.length() == 0) continue;
                int idx = line.indexOf(":");
                String attrName = line.substring(0, idx);
                String attrValue = line.substring(idx+2, line.length());

                header.put(attrName, attrValue);
            }

            if(getMethod().equals("POST")) setBody();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void setBody() {
        try {
            String line = br.readLine();
            this.body = (HashMap<String, String>) HttpRequestUtils.parseQueryString(line);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public String getMethod(){
        String requestLine = header.get("header");
        return requestLine.split(" ")[0];
    }

    public String getPath(){
        String requestLine = header.get("header");
        return requestLine.split(" ")[1].split("\\?")[0];
    }

    public String getHeader(String attrName){
        return header.get(attrName);
    }

    public String getParameter(String param) {
        String requestLine = header.get("header");
        String method = getMethod();

        Map<String, String> queryMap;
        if("POST".equals(method)){
            queryMap = this.body;
        }else {
            String queryString = requestLine.split(" ")[1].split("\\?")[1];
            queryMap = HttpRequestUtils.parseQueryString(queryString);
        }

        return queryMap.get(param);
    }
}
