package api.v1;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.routers.EndpointHandler;

public class Echo implements EndpointHandler {
    HttpParseSuccess HttpParseSuccess;

    public Echo(HttpParseSuccess HttpParseSuccess) {
        this.HttpParseSuccess = HttpParseSuccess;
    }

    @Override
    public HttpResponder handle() throws IOException {
        String regex = "/echo/([a-zA-Z0-9-]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.HttpParseSuccess.requestUrl.trim());
        matcher.find();

        String toEcho = matcher.group(1);
        String responseBody = toEcho;
        /*
         * if
         * (this.HttpParseSuccess.returnHeaders.get("Content-Encoding: ").equals("gzip")
         * ) {
         * ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         * try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
         * gzipOut.write(responseBody.getBytes());
         * }
         * responseBodyBytes = byteOut.toByteArray();
         * contentLength = String.valueOf(responseBodyBytes.length);
         * } else {
         * responseBodyBytes = responseBody.getBytes();
         * }
         */
        this.HttpParseSuccess.returnHeaders.put("Content-Type: ", "text/plain");
        return new HttpResponderText(this.HttpParseSuccess.returnHeaders, responseBody);

    }

}
