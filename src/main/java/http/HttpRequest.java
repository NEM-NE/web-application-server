package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// 요청을 읽은 후 각 데이터를 사용하기 좋은 형태로 분리하는 역할을 가진다.
public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> params = new HashMap<String, String>();
    private RequestLine requestLine;

    public HttpRequest(InputStream in){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            log.debug("request : {}", line);
            if(line == null){
                return;
            }

            requestLine = new RequestLine(line);

            line = br.readLine();
            while(!line.equals("")){
                log.debug("HEADER: {}", line);
                String[] tokens = line.split(":");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if(requestLine.getMethod().isPost()){
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params = requestLine.getParams();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMethod() {
        return requestLine.getMethod().name();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key){
        return headers.get(key);
    }

    public String getParameter(String key){
        return params.get(key);
    }
}
