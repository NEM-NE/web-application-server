package util;

import org.junit.Before;
import org.junit.Test;
import webserver.RequestHandler;

import java.net.Socket;

public class RequestHandlerTest {
    private RequestHandler requestHandler;

    @Before
    public void setup(){
        requestHandler = new RequestHandler(new Socket());
    }

    @Test
    public void parseRequest() {
//        String request = "GET /index.html HTTP1.1";
        String request = "GET /user/create?userId=camper&password=1234&name=%EC%9E%84%EC%84%B1%EB%B9%88&email=dlatqdlatq%40naver.com HTTP1.1";
        requestHandler.parseRequest(request);



        request = "GET /fonts/glyphicons-halflings-regular.woff HTTP1.1";

        request = "GET /user/create?userId=camper&password=1234&name=%EC%9E%84%EC%84%B1%EB%B9%88&email=dlatqdlatq%40naver.com HTTP1.1";
    }

}
