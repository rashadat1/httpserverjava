package customExceptions;
public class MalformedRequestException extends CustomException {
    public MalformedRequestException(String message) {
        super("400 Bad Request: Malformed Request - ", message);
    }
    public String returnResponse(String version) {
        return version + " 400 Bad Request\r\n\r\n";
    }
}