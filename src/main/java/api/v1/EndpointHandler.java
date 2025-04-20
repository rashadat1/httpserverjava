package api.v1;
import java.io.IOException;

import HttpResponderObject.HttpResponder;
import customExceptions.ResourceNotFoundException;

public interface EndpointHandler {
    HttpResponder handle() throws ResourceNotFoundException, IOException;
}
