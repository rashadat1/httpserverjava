package HttpResponderObject;

import java.io.IOException;

public abstract class HttpResponder {

    abstract public byte[] formatResponse() throws IOException;
}
