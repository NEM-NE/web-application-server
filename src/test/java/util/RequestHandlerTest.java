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
    }

}
