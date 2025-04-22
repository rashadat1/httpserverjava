package HttpParser;

import java.util.HashMap;

public class HttpParseSuccess extends HttpParserReturn{
    public String requestUrl;
    public String requestMethod;
    public HashMap<String, String> queryParams;
    public HashMap<String, String> headers;
    public char[] requestBody;
    public String version;
    public HashMap<String, String> returnHeaders;
    public String directory;
    public String error;

    public HttpParseSuccess(String requestUrl, String requestMethod, HashMap<String, String> queryParams, HashMap<String, String> headers, char[] requestBody, String version, HashMap<String, String> returnHeaders, String directory) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.queryParams =  queryParams;
        this.headers = headers;
        this.requestBody = requestBody;
        this.version = version;
        this.returnHeaders = returnHeaders;
        this.directory = directory;
        this.error = null;
    }
}
