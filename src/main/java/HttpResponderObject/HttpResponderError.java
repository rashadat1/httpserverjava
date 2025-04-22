package HttpResponderObject;

public class HttpResponderError extends HttpResponder {
    String error;

    public HttpResponderError(String error) {
        this.error = error;
    }

    @Override
    public byte[] formatResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'formatResponse'");
    }
    
}
