package api.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HttpParser.HttpParserReturn;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import customExceptions.ResourceNotFoundException;

public class files implements EndpointHandler {
    HttpParserReturn httpParserReturn;

    public files(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    @Override
    public HttpResponder handle() throws ResourceNotFoundException, IOException {
        if (httpParserReturn.requestMethod.equals("GET")) {
            return handleGet();
        } else if (httpParserReturn.requestMethod.equals("POST")) {
            return handlePost();
        }
        throw new ResourceNotFoundException("Invalid request method " + this.httpParserReturn.requestMethod + " for this resource: " + this.httpParserReturn.requestUrl);
    }
    public HttpResponder handleGet() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.httpParserReturn.requestUrl.trim());
        matcher.find();

        String fileToOpen = matcher.group(1);
        String fullFilePathToOpen = this.httpParserReturn.directory + fileToOpen;
        String fileContents = "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fullFilePathToOpen)))) {
            String line;
            while ((line = in.readLine()) != null) {
                fileContents += line;
            }
            this.httpParserReturn.returnHeaders.put("Content-Length: ", String.valueOf(fileContents.length()));
        }
        this.httpParserReturn.returnHeaders.put("Content-Type: ", "application/octet-stream");
        return new HttpResponderText(this.httpParserReturn.returnHeaders, fileContents.getBytes());
    }
    public HttpResponder handlePost() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.httpParserReturn.requestUrl.trim());
        matcher.find();

        String fileToCreate = matcher.group(1);
        String pathToCreate = this.httpParserReturn.directory + fileToCreate;
        if (!Files.exists(Paths.get(pathToCreate))) {
            // create the file if it does not exist
            System.out.println("Creating new file at path: " + pathToCreate);
            Files.createDirectories(Paths.get(this.httpParserReturn.directory));
            Files.createFile(Paths.get(pathToCreate));
        }
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathToCreate))) {
            bw.write(this.httpParserReturn.requestBody);
        }
        return new HttpResponderText((this.httpParserReturn.version + " 201 Created\r\n\r\n").getBytes());
    }
}