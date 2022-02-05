package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private DataOutputStream dos;
    private Socket connection;
    private String main;
    private String requestBody;
    private String method;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            dos = new DataOutputStream(out);

            // 각 요청 별로 쪼개서 관리
            main = br.readLine();
            method = main.split(" ")[0];
            String line = main;
            int contentLength = 0;
            while(!line.equals("")){
                if(line.contains("Content-Length")) {
                    contentLength = Integer.parseInt(line.substring(16, line.length()));
                }
                line = br.readLine();
            }
            // 요청 바디 확인
            if(method.equals("POST")){
                requestBody = IOUtils.readData(br, contentLength);
            }

            byte[] responseBody = parseRequest(main);
            sendResponse(responseBody);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void sendResponse(byte[] body) {
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {

            if(method.equals("POST")){
                dos.writeBytes("HTTP/1.1 302 OK \r\n");
                dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
            }else dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public byte[] parseRequest(String request) {
        log.info(request);

        String[] tokens = request.split(" ");
        String path = tokens[1];
        String[] paths = path.split("/");
        String route = parseURL(paths);

        byte[] body = {10, 10, 10};

        //controller
        switch (route){
            case "/":
                String fileName = tokens[1];
                body = getFile(fileName);
                break;
            case "/user/create":
                Map<String, String> queryMap = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(queryMap.get("userId"), queryMap.get("password"), queryMap.get("name"), queryMap.get("email"));
                System.out.println(user.toString());
                break;
            default:
                System.out.println("DEFAULT!!!");
                fileName = tokens[1];
                body = getFile(fileName);
                break;
        }

        return body;
    }

    private String parseURL(String[] urls){
        String route = "";

        if(urls.length == 2){
            route = "/";
        }else {
            for(int i = 1; i < urls.length; i++){
                String path = urls[i];
                int idx = path.indexOf("?");
                if(idx != -1){
                    path = path.substring(0, idx);
                }

                route += "/" + path;
            }
        }

        return route;
    }

    private byte[] getFile(String fileName) {
        try {
            File file = new File("./webapp" + fileName);
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return null;
    }
}
