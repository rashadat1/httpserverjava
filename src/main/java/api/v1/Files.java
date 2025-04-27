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

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.router.EndpointHandler;
import customExceptions.ResourceNotFoundException;

public class files implements EndpointHandler {
    HttpParseSuccess HttpParseSuccess;

    public files(HttpParseSuccess HttpParseSuccess) {
        this.HttpParseSuccess = HttpParseSuccess;
    }

    @Override
    public HttpResponder handle() throws IOException {
        try {
            if (HttpParseSuccess.requestMethod.equals("GET")) {
                return handleGet();
            } else if (HttpParseSuccess.requestMethod.equals("POST")) {
                return handlePost();
            }
            throw new ResourceNotFoundException("Invalid request method " + this.HttpParseSuccess.requestMethod + " for this resource: " + this.HttpParseSuccess.requestUrl);
        } catch (ResourceNotFoundException e) {
            return new HttpResponderText(e);
        }

    }

    public HttpResponder handleGet() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.HttpParseSuccess.requestUrl.trim());
        matcher.find();

        String fileToOpen = matcher.group(1);
        String fullFilePathToOpen = this.HttpParseSuccess.directory + fileToOpen;
        String fileContents = "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fullFilePathToOpen)))) {
            String line;
            while ((line = in.readLine()) != null) {
                fileContents += line;
            }
            this.HttpParseSuccess.returnHeaders.put("Content-Length: ", String.valueOf(fileContents.length()));
        }
        this.HttpParseSuccess.returnHeaders.put("Content-Type: ", "application/octet-stream");
        return new HttpResponderText(this.HttpParseSuccess.returnHeaders, fileContents);
    }

    public HttpResponder handlePost() throws IOException {
        String regex = "/files/([a-zA-Z0-9_-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.HttpParseSuccess.requestUrl.trim());
        matcher.find();

        String fileToCreate = matcher.group(1);
        String pathToCreate = this.HttpParseSuccess.directory + fileToCreate;
        if (!Files.exists(Paths.get(pathToCreate))) {
            // create the file if it does not exist
            Files.createDirectories(Paths.get(this.HttpParseSuccess.directory));
            Files.createFile(Paths.get(pathToCreate));
        }
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathToCreate))) {
            bw.write(this.HttpParseSuccess.requestBody);
        }
        return new HttpResponderText(this.HttpParseSuccess.version + " 201 Created\r\n\r\n");
    }
}
