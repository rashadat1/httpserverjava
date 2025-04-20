package requestHandler;
import HttpParser.HttpParserReturn;

public class RequestHandlerFactory {
    public HttpParserReturn httpParserReturn;

    public RequestHandlerFactory(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    public RequestHandler createRequestHandler() {
        if (this.httpParserReturn.requestMethod.equals("GET")) {
            return (RequestHandler) new GetRequestHandler(this.httpParserReturn);
        } else if (this.httpParserReturn.requestMethod.equals("POST")) {
            return (RequestHandler) new PostRequestHandler(this.httpParserReturn);
        }
        return null;
    }
}
