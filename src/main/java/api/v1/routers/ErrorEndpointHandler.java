package api.v1.routers;

import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import customExceptions.CustomException;

public class ErrorEndpointHandler implements EndpointHandler{
    CustomException raisedException;

    public ErrorEndpointHandler(CustomException raisedException) {
        this.raisedException = raisedException;
    }

    public HttpResponder handle() {
        return new HttpResponderText(this.raisedException);
    }
}
