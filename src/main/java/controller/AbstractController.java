package controller;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller{

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getMethod();

        if("POST".equals(method)){
            doPost(request, response);
        }else if("GET".equals(method)) doGet(request, response);
    }

    protected abstract void doPost(HttpRequest request, HttpResponse response) throws IOException;

    protected abstract void doGet(HttpRequest request, HttpResponse response) throws IOException;
}
