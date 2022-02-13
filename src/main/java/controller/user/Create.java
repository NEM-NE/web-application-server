package controller.user;

import controller.AbstractController;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

public class Create extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(Create.class);
    private HttpRequest request;
    private HttpResponse response;

    public Create(HttpRequest request, HttpResponse response){
        this.request = request;
        this.response = response;
    }


    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
        User user =
                new User(
                        request.getParameter("userId"), request.getParameter("password"),
                        request.getParameter("name"), request.getParameter("email")
                );
        log.debug("User: {}", user);
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {

    }

}
