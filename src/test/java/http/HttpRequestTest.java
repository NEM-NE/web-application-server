package http;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class HttpRequestTest extends TestCase {
    private static String directory = "./src/test/resource/";

    @Test()
    public void request_GET() throws FileNotFoundException {
        InputStream in = new FileInputStream(new File(directory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals(request.getMethod(), "GET");
        assertEquals(request.getPath(), "/index.html");
        assertEquals(request.getHeader("Connection"), "keep-alive");
        assertEquals(request.getParameter("userId"), "javajigi");
    }
}