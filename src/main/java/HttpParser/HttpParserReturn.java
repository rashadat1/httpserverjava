package HttpParser;
import java.util.HashMap;

public class HttpParserReturn {
    public String requestUrl;
    public String requestMethod;
    public HashMap<String, String> queryParams;
    public HashMap<String, String> headers;
    public String requestBody;
    public String version;
    public HashMap<String, String> returnHeaders;

    public HttpParserReturn(String requestUrl, String requestMethod, HashMap<String, String> queryParams, HashMap<String, String> headers, String requestBody, String version, HashMap<String, String> returnHeaders) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.queryParams =  queryParams;
        this.headers = headers;
        this.requestBody = requestBody;
        this.version = version;
        this.returnHeaders = returnHeaders;

    }
}
