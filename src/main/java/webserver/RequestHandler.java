package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private DataOutputStream dos;
    private BufferedReader br;
    private Socket connection;
    private String main;
    private String requestBody;
    private String method;
    private int contentLength;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            br = new BufferedReader(new InputStreamReader(in));
            dos = new DataOutputStream(out);

            // 각 요청 별로 쪼개서 관리
            parseRequest();

            runRoute(main);

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
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, Map<String, String> attrMap) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");

            Set set = attrMap.entrySet();
            Iterator it = set.iterator();
            while(it.hasNext()){
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                dos.writeBytes(entry.getKey()+ ": " + entry.getValue() + "\r\n");
            }

            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
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

    public void parseRequest() throws IOException {
        main = br.readLine();
        method = main.split(" ")[0];
        contentLength = 0;

        String line = main;
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

    public void runRoute(String request) {
        log.info(request);

        String[] tokens = request.split(" ");
        String path = tokens[1];
        String[] paths = path.split("/");
        String route = parseURL(paths);

        byte[] body = { 10, 10 };

        //controller
        switch (route){
            case "/":
                String fileName = tokens[1];
                body = getFile(fileName);
                sendResponse(body);
                break;
            case "/user/create":
                Map<String, String> queryMap = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(queryMap.get("userId"), queryMap.get("password"), queryMap.get("name"), queryMap.get("email"));
                DataBase.addUser(user);
                Map<String, String> attrMap = Maps.newHashMap();
                attrMap.put("Location", "http://localhost:8080/index.html");
                response302Header(dos, 0, attrMap);
                break;
            case "/user/login":
                queryMap = HttpRequestUtils.parseQueryString(requestBody);
                user = DataBase.findUserById(queryMap.get("userId"));
                attrMap = Maps.newHashMap();
                if(user == null || !user.getPassword().equals(queryMap.get("password"))){
                    attrMap.put("Location", "http://localhost:8080/user/login_failed.html");
                    attrMap.put("Set-Cookie", "logined=false");
                    response302Header(dos, 0, attrMap);
                }else {
                    attrMap.put("Location", "http://localhost:8080/index.html");
                    attrMap.put("Set-Cookie", "logined=true");
                    response302Header(dos, 0, attrMap);
                }
                break;
            default:
                fileName = tokens[1];
                body = getFile(fileName);
                sendResponse(body);
                break;
        }

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
