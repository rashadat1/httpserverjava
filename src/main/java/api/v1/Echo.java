package api.v1;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.routers.EndpointHandler;
import customExceptions.ResourceNotFoundException;

public class Echo implements EndpointHandler {
    HttpParseSuccess httpParse;

    public Echo(HttpParseSuccess HttpParseSuccess) {
        this.httpParse = HttpParseSuccess;
    }

    @Override
    public HttpResponder handle() throws IOException {
        try {
            if (this.httpParse.requestMethod.equals("GET")) {
                String regex = "/echo/([a-zA-Z0-9-]*)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(this.httpParse.requestUrl.trim());
                matcher.find();
        
                String toEcho = matcher.group(1);
                String responseBody = toEcho;
        
                this.httpParse.returnHeaders.put("Content-Type: ", "text/plain");
                return new HttpResponderText(this.httpParse.returnHeaders, responseBody);
            }
            throw new ResourceNotFoundException("Invalid request method " + this.httpParse.requestMethod
              + " for this resource: " + this.httpParse.requestUrl);
        } catch (ResourceNotFoundException e) {
            System.err.println("Resource Not Found Exception occurred: " + e.getMessage());
            return new HttpResponderText(e);
        }

    }

}
