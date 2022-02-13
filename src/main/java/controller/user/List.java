package controller.user;

import controller.AbstractController;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class List extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(List.class);
    private HttpRequest request;
    private HttpResponse response;

    public List(HttpRequest request, HttpResponse response){
        this.request = request;
        this.response = response;
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {

    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
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
