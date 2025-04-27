package api.v1;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.router.EndpointHandler;
import customExceptions.ResourceNotFoundException;

public class Root implements EndpointHandler {
    HttpParseSuccess HttpParseSuccess;

    public Root(HttpParseSuccess HttpParseSuccess) {
        this.HttpParseSuccess = HttpParseSuccess;
    }
    @Override
    public HttpResponder handle() {
        try {
            if (!this.HttpParseSuccess.requestMethod.equals("GET")) {
                String response = this.HttpParseSuccess.version + " 200 OK\r\n";
                return new HttpResponderText(response);
            }
            throw new ResourceNotFoundException("Invalid request method for this resource: " + this.HttpParseSuccess.requestUrl);
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return new HttpResponderText(e);
        }
    }
}