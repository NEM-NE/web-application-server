package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;
    private String path;
    private Map<String, String> params = new HashMap<String, String>();

    public RequestLine(String line){
        String[] tokens = line.split(" ");
        method = HttpMethod.valueOf(tokens[0]);

        String[] urlTokens = tokens[1].split("\\?");
        path = urlTokens[0];

        if(method.isPost()) return;
        if(urlTokens.length == 2){
            params = HttpRequestUtils.parseQueryString(urlTokens[1]);
        }
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getParams() {
        return params;
    }
}

