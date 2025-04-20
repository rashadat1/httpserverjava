package customExceptions;
public class MalformedRequestException extends Exception {
    public MalformedRequestException(String message) {
        super("400 Bad Request: Malformed Request - " + message);
    }
}