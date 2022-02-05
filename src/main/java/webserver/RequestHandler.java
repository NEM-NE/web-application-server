package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String[] methods = { "GET", "POST", "DELETE", "PATCH" };
    private LinkedList<String> requests = new LinkedList<String>();
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // 각 요청 별로 쪼개서 관리
            String line = br.readLine();
            while(!line.equals("")){
                splitRequest(line);
                line = br.readLine();
            }

            //각 요청들 파싱 & 전송하기
            for(String request : requests){
                byte[] body = parseRequest(request);
                sendResponse(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void sendResponse(DataOutputStream dos, byte[] body) {
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void splitRequest(String line) {
        for(int i = 0; i < methods.length; i++){
            if(line.contains(methods[i])){
                requests.add(line);
            }
        }
    }

    private byte[] parseRequest(String request) {
        log.info(request);
        String[] tokens = request.split(" ");
        String method = tokens[0];
        byte[] body = new byte[1];

        switch (method){
            case "GET":
                String fileName = tokens[1];
                body = getFile(fileName);
                break;

            default:
                log.error("NOT FOUND METHOD!");
        }

        return body;
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
