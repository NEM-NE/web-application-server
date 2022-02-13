package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private DataOutputStream dos;
    private HashMap<String, String> headerMap = new HashMap<String, String>();

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void forward(String url) throws IOException {
        String contentType = url.endsWith(".html") ? "text/html;charset=utf-8" : "text/css";
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

        addHeader("header", "HTTP/1.1 200 OK");
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", body.length + "");

        response200Header();
        responseBody(body);
    }

    public void forwardBody(byte[] body) throws IOException {
        addHeader("header", "HTTP/1.1 200 OK");
        addHeader("Content-Length", body.length + "");

        response200Header();
        responseBody(body);
    }

    public void sendRedirect(String url) throws IOException{
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        addHeader("Location", url);
        processHeader();
        dos.writeBytes("\r\n");
    }

    public String addHeader(String attrName, String attrValue){
        return headerMap.put(attrName, attrValue);
    }

    private void processHeader() throws IOException{
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"header".equals(key)) {
                dos.writeBytes(key + ": " + value + "\r\n");
            }
        }
    }

    public void response200Header() {
        try {
            dos.writeBytes(headerMap.get("header") + " \r\n");
            dos.writeBytes("Content-Type: " + headerMap.get("Content-Type") + "\r\n");
            dos.writeBytes("Content-Length: " + headerMap.get("Content-Length") + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
