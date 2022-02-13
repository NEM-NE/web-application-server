package http;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class RequestLineTest {
    @Test()
    public void create_method() {
        RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");

        Assert.assertEquals("GET", requestLine.getMethod());
        Assert.assertEquals("/index.html", requestLine.getPath());

        requestLine = new RequestLine("POST /index.html HTTP/1.1");
        Assert.assertEquals("POST", requestLine.getMethod());
        Assert.assertEquals("/index.html", requestLine.getPath());
    }

    @Test()
    public void create_params_and_path() {
        RequestLine requestLine = new RequestLine("GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1");

        Assert.assertEquals("/user/create", requestLine.getPath());

        Map<String, String> params = requestLine.getParams();
        Assert.assertEquals("javajigi", params.get("userId"));
        Assert.assertEquals("password", params.get("password"));
    }
}