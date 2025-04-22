package api.v1.routers;
import HttpParser.HttpParseSuccess;
import api.v1.Echo;
import api.v1.Root;
import api.v1.UserAgent;
import api.v1.files;
import customExceptions.ResourceNotFoundException;

public class EndpointHandlerFactory {
    public HttpParseSuccess HttpParseSuccess;

    public EndpointHandlerFactory(HttpParseSuccess HttpParseSuccess) {
        this.HttpParseSuccess = HttpParseSuccess;
    }
    public EndpointHandler createRequestHandler() throws ResourceNotFoundException {
        if (this.HttpParseSuccess.requestUrl.equals("/")) {
            return new Root(HttpParseSuccess);
        } else if (this.HttpParseSuccess.requestUrl.startsWith("/echo/")) {
            return new Echo(HttpParseSuccess);
        } else if (this.HttpParseSuccess.requestUrl.startsWith("/files/")) {
            return new files(HttpParseSuccess);
        } else if (this.HttpParseSuccess.requestUrl.startsWith("user-agent")) {
            return new UserAgent(HttpParseSuccess);
        }
        throw new ResourceNotFoundException("The requested url path does not exist: " + this.HttpParseSuccess.requestUrl);
    }
}
