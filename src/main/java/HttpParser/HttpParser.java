package HttpParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import customExceptions.MalformedRequestException;
import customExceptions.ResourceNotFoundException;

public class HttpParser {
    InputStream inputStream;
    String directory;
    String requestMethod;
    String urlPath; // if null then this is just a connection
    String version;
    String userAgent;
    String host;
    String requestBody;
    int contentLength;
    char[] parsedBody;
    Set<String> validEncodings;
    String contentType;
    String clientEncoding;
    public String connectionHeaderValue;

    public HttpParser(InputStream inputStream, String directory) {
        this.inputStream = inputStream;
        this.directory = directory;
        this.urlPath = null;
        this.version = null;
        this.userAgent = null;
        this.host = null;
        this.requestBody = null;
        this.contentLength = 0;
        this.parsedBody = null;
        this.validEncodings = null;
        this.contentType = null;
        this.clientEncoding = null;
        this.connectionHeaderValue = null;
    }

    private boolean isValidMethod(String statusLineMethod) {
        Set<String> httpMethods = new HashSet<>(Set.of("GET", "POST", "PATCH", "UPDATE", "DELETE", "PUT"));
        return httpMethods.contains(statusLineMethod);
    }

    private boolean isValidUrlPath(String statusLineUrl) throws ResourceNotFoundException {
        // right now we can only support / and /echo/{str} and /user-agent paths
        // otehr endpoints should throw a ResourceNotFoundException
        if (statusLineUrl.equals("") || statusLineUrl.equals("/") || statusLineUrl.startsWith("/echo/")
                || statusLineUrl.startsWith("/user-agent") || statusLineUrl.startsWith("/files/")) {
            return true;
        } else if (!statusLineUrl.startsWith("/")) {
            return false;
        } else {
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
                throw new MalformedRequestException(
                        "Expected status line to have request method, urlPath, and request version: one or more is missing");
            }
        }
    }

    public void parseHeader(BufferedReader in) throws IOException, MalformedRequestException {
        // extensible parseHeader method to extract header values from the header
        // section
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
            } else if (line.startsWith("Content-Length: ")) {
                if (line.split("Content-Length: ").length != 2) {
                    throw new MalformedRequestException("Malformed Content-Length header: " + line);
                }
                this.contentLength = Integer.parseInt(line.split("Content-Length: ")[1].trim());
            } else if (line.startsWith("Content-Type: ")) {
                if (line.split("Content-Type: ").length != 2) {
                    throw new MalformedRequestException("Malformed Content-Type header: " + line);
                }
                this.contentType = line.split("Content-Type: ")[1].trim();
            } else if (line.startsWith("Accept-Encoding: ")) {
                if (line.split("Accept-Encoding: ").length != 2) {
                    throw new MalformedRequestException("Malformed Accept-Encoding header: " + line);
                }
                String commaSeparatedEncodings = line.split("Accept-Encoding: ")[1].trim();
                this.validEncodings = new HashSet<>(Set.of(commaSeparatedEncodings.split(", ")));
                if (this.validEncodings.contains("gzip")) {
                    this.clientEncoding = "gzip";
                }
            } else if (line.startsWith("Connection: ")) {
                if (line.split("Connection: ").length != 2) {
                    throw new MalformedRequestException("Malformed Connection header: " + line);
                }
                String connectionValue = line.split("Connection: ")[1].trim();
                this.connectionHeaderValue = connectionValue;
            }
        }
    }

    private void parseBody(BufferedReader in) throws IOException {
        char[] cbuf = new char[this.contentLength];
        in.read(cbuf, 0, this.contentLength);
        this.parsedBody = cbuf;
    }

    private HttpParserReturn requestParse() throws IOException, MalformedRequestException, ResourceNotFoundException {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
        // first line contains the http method and the urlpath - parse the status line
        System.out.println("Parsing HTTP status line");
        String statusLine = in.readLine();
        System.out.println("First line: " + statusLine);
        this.statusLineParser(statusLine); // parse status line
        this.parseHeader(in); // parse header
        // parse body if there is a non-zero context length number of bytes to parse
        if (this.contentLength > 0) {
            this.parseBody(in);
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent: ", this.userAgent);
        headers.put("Host: ", this.host);
        
        HashMap<String, String> returnHeaders = new HashMap<>();
        returnHeaders.put("Content-Encoding: ", this.clientEncoding);
        returnHeaders.put("Connection: ", this.connectionHeaderValue);
        returnHeaders.put("version", this.version);

        return new HttpParseSuccess(this.urlPath, this.requestMethod, null, headers, this.parsedBody, this.version, returnHeaders, this.directory);
        
    }

    public HttpParserReturn parseAndReturnHttpResponseString() throws IOException, MalformedRequestException {
        try {
            HttpParserReturn parseSuccessObject = this.requestParse();
            return parseSuccessObject;
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return new HttpParseFail(e.returnResponse("HTTP/1.1"));
        } catch (FileNotFoundException e) {
            System.err.println("File Not Found Exception occurred while processing files/{file} endpoint request: "
                    + e.getMessage());
            return new HttpParseFail("HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }
}
