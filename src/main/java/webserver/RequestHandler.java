package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import static util.HttpRequestUtils.*;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            log.debug("request : {}", line);

            if(line == null){
                return;
            }

            String[] tokens = line.split(" ");
            int contentLength = 0;
            while(!line.equals("")){
                line = br.readLine();
                log.debug("HEADER: {}", line);
                if(line.contains("Content-Length")){
                    contentLength = getContentLength(line);
                }
            }

            String url = tokens[1];
            if("/user/create".equals(url)){
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> queryMap = parseQueryString(body);
                User user =
                        new User(
                                queryMap.get("userId"),
                                queryMap.get("password"),
                                queryMap.get("name"),
                                queryMap.get("email")
                        );
                log.debug("User: {}", user);
                DataBase.addUser(user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }else if("/user/login".equals(url)){
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(params.get("userId"));
                if(user == null){
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if(user.getPassword().equals(params.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                }else {
                    responseResource(out, "/user/login_failed.html");
                }
            }else {
                responseResource(out, url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) {
        try {
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }
}
