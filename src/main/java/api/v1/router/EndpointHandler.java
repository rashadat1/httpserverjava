package api.v1.router;
import java.io.IOException;

import HttpResponderObject.HttpResponder;

public interface EndpointHandler {
    HttpResponder handle() throws IOException;
}
