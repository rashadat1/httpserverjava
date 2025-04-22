package api.v1;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.routers.EndpointHandler;
import customExceptions.ResourceNotFoundException;

public class Root implements EndpointHandler {
    HttpParseSuccess HttpParseSuccess;

    public Root(HttpParseSuccess HttpParseSuccess) {
        this.HttpParseSuccess = HttpParseSuccess;
    }
    @Override
    public HttpResponder handle() throws ResourceNotFoundException {
        if (!this.HttpParseSuccess.requestMethod.equals("GET")) {
            String response = this.HttpParseSuccess.version + " 200 OK\r\n";
            return new HttpResponderText(response);
        } else {
            throw new ResourceNotFoundException("Invalid request method for this resource: " + this.HttpParseSuccess.requestUrl);
        }
    }
}