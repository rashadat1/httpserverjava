
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class HttpParser {
    InputStream inputStream;
    String directory;
    String requestMethod;
    String urlPath; // if null then this is just a connection
    boolean isConnectionInitializer;
    String version;
    String userAgent;
    String host;
    String requestBody;
    int contentLength;
    char[] parsedBody;
    Set<String> validEncodings;
    String contentType;
    int responseContentLength;
    String clientEncoding;
    String endPointResponseBody;
    boolean closeConnection;
    String connectionHeaderValue;

    public HttpParser(InputStream inputStream, String directory) {
        this.inputStream = inputStream;
        this.directory = directory;
        this.urlPath = null;
        this.isConnectionInitializer = false;
        this.version = null;
        this.userAgent = null;
        this.host = null;
        this.requestBody = null;
        this.contentLength = 0;
        this.parsedBody = null;
        this.validEncodings = null;
        this.contentType = null;
        this.responseContentLength = 0;
        this.clientEncoding = null;
        this.endPointResponseBody = "";
        this.closeConnection = false;
        this.connectionHeaderValue = null;
    }

    private boolean isValidMethod(String statusLineMethod) {
        Set<String> httpMethods = new HashSet<>(Set.of("GET", "POST", "PATCH", "UPDATE", "DELETE", "PUT"));
        return httpMethods.contains(statusLineMethod);
    }

    private boolean isValidUrlPath(String statusLineUrl) throws ResourceNotFoundException {
        // right now we can only support / and /echo/{str} and /user-agent paths
        // otehr endpoints should throw a ResourceNotFoundException
        if (statusLineUrl.equals("") || statusLineUrl.equals("/") || statusLineUrl.startsWith("/echo/") || statusLineUrl.startsWith("/user-agent") || statusLineUrl.startsWith("/files/")) {
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
                this.responseContentLength = this.userAgent.length();
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
              if (connectionValue.equalsIgnoreCase("close")) {
                this.closeConnection = true;
              }
              this.connectionHeaderValue = connectionValue;
            }
        }
    }

    private void parseBody(BufferedReader in) throws IOException {
        char[] cbuf = new char[this.contentLength];
        in.read(cbuf, 0, this.contentLength);
        this.parsedBody = cbuf;
    }

    private String handleEchoEndpoint() {
        String regex = "/echo/([a-zA-Z0-9]*)";
        Pattern pattern = Pattern.compile(regex);
        System.out.println("urlPathString for echo command: " + this.urlPath);
        Matcher matcher = pattern.matcher(this.urlPath.trim());
        matcher.find();

        this.contentType = "text/plain";
        String toEcho = matcher.group(1);
        this.responseContentLength = toEcho.length();
        return toEcho;
    }

    private String handleFileEndpointGET() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.urlPath.trim());
        matcher.find();
        this.contentType = "application/octet-stream";
        String fileToOpen = matcher.group(1);
        String fullFilePathToOpen = this.directory + fileToOpen;
        System.out.println("File path to open: " + fullFilePathToOpen);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fullFilePathToOpen)))) {
            String fileContents = "";
            String line;
            while ((line = in.readLine()) != null) {
                fileContents += line;
            }
            this.responseContentLength = fileContents.length();
            return fileContents;
        }
    }

    private String handleFileEndpointPOST() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.urlPath.trim());
        matcher.find();

        String fileToCreate = matcher.group(1);
        String pathToCreate = this.directory + fileToCreate;
        if (!Files.exists(Paths.get(pathToCreate))) {
            // create the file if it does not exist
            System.out.println("Creating new file at path: " + pathToCreate);
            Files.createDirectories(Paths.get(this.directory));
            Files.createFile(Paths.get(pathToCreate));
        }
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathToCreate))) {
            System.out.println("Writing requestBody to file:\r\n" + this.requestBody);
            bw.write(this.parsedBody);
        }
        return "HTTP/1.1 201 Created\r\n\r\n";
    }

    private HttpResponseText httpResponse() throws MalformedRequestException, IOException, FileNotFoundException {
        String response = "";
        byte[] responseBodyBytes = new byte[0];
        if (this.urlPath.startsWith("/echo/")) {
            this.endPointResponseBody = this.handleEchoEndpoint();
        } else if (this.urlPath.startsWith("/files/")) {
            if (this.requestMethod.equals("GET")) {
                this.endPointResponseBody = this.handleFileEndpointGET();

            } else if (this.requestMethod.equals("POST")) {
                System.out.println("POST request received to /files/ endpoint");
                response = this.handleFileEndpointPOST();
                return new HttpResponseText(response);
            }
        } else if (this.urlPath.startsWith("/user-agent")) {
            this.contentType = "text/plain";
            this.endPointResponseBody = this.userAgent;
        }
        System.out.println("Building full response");
        StringBuilder fullResponse = new StringBuilder(this.version);
        fullResponse.append(" 200 OK\r\n");
        
        if (this.clientEncoding != null && this.clientEncoding.equals("gzip") && (this.endPointResponseBody != "")) {
          System.out.println("Getting gzip compression of response body");
          // if we have a client Encoding specified and we have a non-empty response body we should encode it
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
            gzipOut.write(this.endPointResponseBody.getBytes());
          }
          responseBodyBytes = byteOut.toByteArray();
          this.responseContentLength = responseBodyBytes.length; 
        }
        if (this.contentType != null) {
            fullResponse.append("Content-Type: ").append(this.contentType).append("\r\n");
        }
        if (this.responseContentLength != 0) {
            fullResponse.append("Content-Length: ").append(this.responseContentLength).append("\r\n");
        }
        if (this.clientEncoding != null) {
            fullResponse.append("Content-Encoding: ").append(this.clientEncoding).append("\r\n");
        }
        if (this.connectionHeaderValue != null) {
            fullResponse.append("Connection: ").append(this.connectionHeaderValue).append("\r\n");
        }
        fullResponse.append("\r\n");
        if (this.clientEncoding != null && this.clientEncoding.equals("gzip") && this.endPointResponseBody != "") {
          System.out.println("Encoding response body to client with " + this.clientEncoding);
          // if there is a client encoding and a non-empty response body we need to write the compressed bytes so we return the response for compression object
          return new HttpResponseText(fullResponse.toString(), responseBodyBytes);
        }
        // if there is no client encoding just append the body as a string and return the whole response wrapped in the compression object
        fullResponse.append(this.endPointResponseBody);
 
        return new HttpResponseText(fullResponse.toString());
    }
    private void requestParse() throws IOException, MalformedRequestException, ResourceNotFoundException {
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
    }

    public HttpResponseText parseAndReturnHttpResponseString() throws IOException, MalformedRequestException {
        try {
            this.requestParse();
            return httpResponse();
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return new HttpResponseText(e.returnResponse("HTTP/1.1"));
        } catch (FileNotFoundException e) {
            System.err.println("File Not Found Exception occurred while processing files/{file} endpoint request: " + e.getMessage());
            return new HttpResponseText("HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }
}
