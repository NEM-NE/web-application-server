package webserver;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import controller.Controller;
import controller.user.Create;
import controller.user.List;
import controller.user.Login;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private HashMap<String, Controller> controllerMap;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.controllerMap = new HashMap<String, Controller>();

    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            this.controllerMap.put("/user/create", new Create(request, response));
            this.controllerMap.put("/user/login", new Login(request, response));
            this.controllerMap.put("/user/list", new List(request, response));

            String url = request.getPath();

            Controller controller = this.controllerMap.get(url);

            if(controller != null){
                controller.service(request, response);
            }else response.forward(url);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


}
