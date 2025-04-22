package customExceptions;

public abstract class CustomException extends Exception {

    public CustomException(String error, String message) {
        super(error + message);
    }

    abstract public String returnResponse(String version);
}

