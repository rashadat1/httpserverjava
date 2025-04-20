package api.v1;

import HttpParser.HttpParserReturn;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import customExceptions.ResourceNotFoundException;

public class Root implements EndpointHandler {
    HttpParserReturn httpParserReturn;

    public Root(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    @Override
    public HttpResponder handle() throws ResourceNotFoundException {
        if (!this.httpParserReturn.requestMethod.equals("GET")) {
            String response = this.httpParserReturn.version + " 200 OK\r\n";
            return new HttpResponderText(response.getBytes());
        } else {
            throw new ResourceNotFoundException("Invalid request method for this resource: " + this.httpParserReturn.requestUrl);
        }
    }
}