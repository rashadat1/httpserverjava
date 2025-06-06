package api.v1.router;
import java.io.IOException;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
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
    public EndpointHandler createRequestHandler() {
        try {
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
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return new ErrorEndpointHandler(e);
        }

    }
    public HttpResponder executeRequestHandler() throws IOException {
        EndpointHandler endpointHandler = this.createRequestHandler();

        HttpResponder endpointResponse = endpointHandler.handle();
        return endpointResponse;
    }
}
