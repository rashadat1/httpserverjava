package requestHandler;

import HttpParser.HttpParserReturn;
import HttpResponseObject.HttpResponse;

public class GetRequestHandler implements RequestHandler {
    HttpParserReturn handlerParams;

    public GetRequestHandler(HttpParserReturn handlerParams) {
        this.handlerParams = handlerParams;
    }

    @Override
    public HttpResponse handleRequest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
    }

    
}
