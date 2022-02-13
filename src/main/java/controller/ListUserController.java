package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ListUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if(!isLogin(request.getHeader("Cookie"))){
            response.sendRedirect("/user/login.html");
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

        response.forwardBody(sb.toString());
    }
}
