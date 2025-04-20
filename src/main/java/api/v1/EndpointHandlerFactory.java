package api.v1;
import HttpParser.HttpParserReturn;
import customExceptions.ResourceNotFoundException;

public class EndpointHandlerFactory {
    public HttpParserReturn httpParserReturn;

    public EndpointHandlerFactory(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    public EndpointHandler createRequestHandler() throws ResourceNotFoundException {
        if (this.httpParserReturn.requestUrl.equals("/")) {
            return new Root(httpParserReturn);
        } else if (this.httpParserReturn.requestUrl.startsWith("/echo/")) {
            return new Echo(httpParserReturn);
        } else if (this.httpParserReturn.requestUrl.startsWith("/files/")) {
            return new files(httpParserReturn);
        } else if (this.httpParserReturn.requestUrl.startsWith("user-agent")) {
            return new UserAgent(httpParserReturn);
        }
        throw new ResourceNotFoundException("The requested url path does not exist: " + this.httpParserReturn.requestUrl);
    }
}
