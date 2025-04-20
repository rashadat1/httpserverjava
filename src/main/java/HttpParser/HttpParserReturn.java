package HttpParser;
import java.util.HashMap;

public class HttpParserReturn {
    public String requestPath;
    public String requestMethod;
    public HashMap<String, String> queryParams;
    public HashMap<String, String> headers;
    public String requestBody;

    public HttpParserReturn(String requestPath, String requestMethod, HashMap<String, String> queryParams, HashMap<String, String> headers, String requestBody) {
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.queryParams =  queryParams;
        this.headers = headers;
        this.requestBody = requestBody;
    }
}
