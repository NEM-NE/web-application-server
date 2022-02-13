package controller.user;

import controller.AbstractController;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Login extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(Login.class);
    private HttpRequest request;
    private HttpResponse response;

    public Login(HttpRequest request, HttpResponse response){
        this.request = request;
        this.response = response;
    }


    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
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
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {

    }
}
