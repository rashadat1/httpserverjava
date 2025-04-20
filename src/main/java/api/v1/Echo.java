package api.v1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import HttpParser.HttpParserReturn;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;

public class Echo implements EndpointHandler {
    HttpParserReturn httpParserReturn;

    public Echo(HttpParserReturn httpParserReturn) {
        this.httpParserReturn = httpParserReturn;
    }
    @Override
    public HttpResponder handle() throws IOException {
        String regex = "/echo/([a-zA-Z0-9-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.httpParserReturn.requestUrl.trim());
        matcher.find();

        String toEcho = matcher.group(1);
        String responseBody = toEcho;
        byte[] responseBodyBytes = new byte[0];
        String contentLength = String.valueOf(responseBody.length());

        if (this.httpParserReturn.returnHeaders.get("Content-Encoding: ").equals("gzip")) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
                gzipOut.write(responseBody.getBytes());
            }
            responseBodyBytes = byteOut.toByteArray();
            contentLength = String.valueOf(responseBodyBytes.length);
        } else {
            responseBodyBytes = responseBody.getBytes();
        }
        this.httpParserReturn.returnHeaders.put("Content-Length: ", contentLength);
        this.httpParserReturn.returnHeaders.put("Content-Type: ", "text/plain");

        return new HttpResponderText(this.httpParserReturn.returnHeaders, responseBodyBytes);

    }
    
}