
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpParser {
    InputStream inputStream;
    String requestMethod;
    String urlPath; // if null then this is just a connection
    boolean isConnectionInitializer;
    String version;
    String userAgent;
    String host;

    public HttpParser(InputStream inputStream, String urlPath, boolean isConnectionInitializer, String version, String userAgent, String host) {
        this.inputStream = inputStream;
        this.urlPath = urlPath;
        this.isConnectionInitializer = isConnectionInitializer;
        this.version = version;
        this.userAgent = userAgent;
        this.host = host;
    }

    private boolean isValidMethod(String statusLineMethod) {
        Set<String> httpMethods = new HashSet<>(Set.of("GET", "POST", "PATCH", "UPDATE", "DELETE", "PUT"));
        return httpMethods.contains(statusLineMethod);
    }

    private boolean isValidUrlPath(String statusLineUrl) throws ResourceNotFoundException {
        // right now we can only support / and /echo/{str} and /user-agent paths
        // otehr endpoints should throw a ResourceNotFoundException
        if (statusLineUrl.equals("") || statusLineUrl.equals("/") || statusLineUrl.startsWith("/echo/") || statusLineUrl.startsWith("/user-agent")) {
            return true;
        } else if (!statusLineUrl.startsWith("/")) {
            return false;
        }
        else {
            throw new ResourceNotFoundException(statusLineUrl + " is not a valid endpoint");
        }
    }

    private boolean isValidHttpVersion(String statusLineVersion) {
        Set<String> supportedVersions = new HashSet<>(Set.of("HTTP/1.1"));
        return supportedVersions.contains(statusLineVersion);
    }

    private void statusLineParser(String statusLine) throws MalformedRequestException, ResourceNotFoundException {
        if (statusLine != null) {
            String[] lineParts = statusLine.split(" ");
            if (lineParts.length == 3) {
                boolean validMethod = this.isValidMethod(lineParts[0]);
                boolean validVersion = this.isValidHttpVersion(lineParts[2]);
                boolean validUrl = this.isValidUrlPath(lineParts[1]);

                boolean isValid = validMethod && validUrl && validVersion;
                if (!isValid) {
                    throw new MalformedRequestException("Status line method, url, or http version is invalid");
                }
                this.requestMethod = lineParts[0];
                this.urlPath = lineParts[1];
                this.version = lineParts[2];
            } else {
                throw new MalformedRequestException("Expected status line to have request method, urlPath, and request version: one or more is missing");
            }
        } else {
            this.isConnectionInitializer = true;
        }
    }

    public void parseHeader(BufferedReader in) throws IOException, MalformedRequestException {
        // extensible parseHeader method to extract header values from the header section
        String line;
        while ((line = in.readLine()) != null && !line.equals("")) {
            // header section ends with a \r\n line
            System.out.println("Parsing line: " + line);
            if (line.startsWith("User-Agent: ")) {
                if (line.split("User-Agent: ").length != 2) {
                    throw new MalformedRequestException("Malformed user-agent header: " + line);
                }
                this.userAgent = line.split("User-Agent: ")[1].trim();
            } else if (line.startsWith("Host: ")) {
                if (line.split("Host: ").length != 2) {
                    throw new MalformedRequestException("Malformed host header: " + line);
                }
                this.host = line.split("Host: ")[1].trim();
             }
        }
    }

    private String handleEchoEndpoint() {
        String regex = "/echo/([a-zA-Z0-9]*)";
        Pattern pattern = Pattern.compile(regex);
        System.out.println("urlPathString for echo command: " + this.urlPath);
        Matcher matcher = pattern.matcher(this.urlPath.trim());
        matcher.find();

        String toEcho = matcher.group(1);
        String echoResponse = this.version + " 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + toEcho.length() + "\r\n\r\n" + toEcho;
        return echoResponse;  
    }

    private String handleUserAgentEndpoint() throws MalformedRequestException {
        if (this.userAgent != null) {
            return this.version + " 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + this.userAgent.length() + "\r\n\r\n" + this.userAgent;
        } else {
            throw new MalformedRequestException("Request to /user-agent endpoint must have User-Agent header");
        }
    }

    private String handleConnectionEndpoint() {
        return "";
    }

    private String handleRootEndpoint() {
        return this.version + " 200 OK\r\n\r\n";
    }

    private String HttpResponse() throws MalformedRequestException {
        String response = null;
        if (this.urlPath.startsWith("/echo/")) {
            response = this.handleEchoEndpoint();
        } else if (this.urlPath.startsWith("/user-agent")) {
            response = this.handleUserAgentEndpoint();
        } else if (this.urlPath.equals("")) {
            response = this.handleConnectionEndpoint();
        } else if (this.urlPath.equals("/")) {
            response = this.handleRootEndpoint();
        } else {
            throw new MalformedRequestException("Uncaught malformed request");
        }
        return response;
    }

    private void requestParse() throws IOException, MalformedRequestException, ResourceNotFoundException {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
        // first line contains the http method and the urlpath - parse the status line
        System.out.println("Parsing HTTP status line");
        String statusLine = in.readLine();
        System.out.println("First line: " + statusLine);
        this.statusLineParser(statusLine); // parse status line
        this.parseHeader(in); // parse header
        // create parse body method
    }

    public String parseAndReturnHttpResponseString() throws IOException, MalformedRequestException {
        try {
            this.requestParse();
            return this.HttpResponse();
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return e.returnResponse("HTTP/1.1");
        }
    }
}