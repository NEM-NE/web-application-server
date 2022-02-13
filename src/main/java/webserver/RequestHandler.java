package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String url = request.getPath();
            if("/user/create".equals(url)){
                User user =
                        new User(
                                request.getParameter("userId"), request.getParameter("password"),
                                request.getParameter("name"), request.getParameter("email")
                        );
                log.debug("User: {}", user);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            }else if("/user/login".equals(url)){
                User user = DataBase.findUserById(request.getParameter("userId"));
                if(user == null){
                    response.forward("/user/login_failed.html");
                    return;
                }

                if(user.getPassword().equals(request.getParameter("password"))) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect("/index.html");
                }else {
                    response.forward("/user/login_failed.html");
                }
            }else if("/user/list".equals(url)) {
                if(!isLogin(request.getHeader("Cookie"))){
                    response.forward("/user/login.html");
                    return;
                }

                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for(User user : users){
                    sb.append("<tr>");
                    sb.append("<td>").append(user.getUserId()).append("</td>");
                    sb.append("<td>").append(user.getEmail()).append("</td>");
                    sb.append("<td>").append(user.getName()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                response.forwardBody(body);
            }else {
                response.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line){
        Map<String, String> cookies = HttpRequestUtils.parseCookies(line);
        String value = cookies.get("logined");
        if(value == null){
            return false;
        }
        return Boolean.parseBoolean(value);
    }

}
