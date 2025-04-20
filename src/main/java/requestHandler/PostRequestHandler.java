package requestHandler;

import HttpParser.HttpParserReturn;
import HttpResponseObject.HttpResponse;

public class PostRequestHandler implements RequestHandler {
    HttpParserReturn handlerParams;

    public PostRequestHandler(HttpParserReturn handlerParams) {
        this.handlerParams = handlerParams;
    }

    @Override
    public HttpResponse handleRequest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
    }

    
}
