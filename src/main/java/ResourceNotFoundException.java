public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String message) {
        super("404 Not Found: " + message);
    }
    public String returnResponse(String version) {
        return version + " 404 Not Found\r\n\r\n";
    }
}