package api.v1;

import HttpParser.HttpParserReturn;
import HttpResponderObject.HttpResponder;

public class UserAgent implements EndpointHandler {
    HttpParserReturn httpParserReturn;

    public UserAgent(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    @Override
    public HttpResponder handle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }
    
}