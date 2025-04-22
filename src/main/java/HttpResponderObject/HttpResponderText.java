package HttpResponderObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import customExceptions.CustomException;

public class HttpResponderText extends HttpResponder {
  public HashMap<String, String> returnHeaders;
  public String responseBody;
  public CustomException raisedException;

  public HttpResponderText(CustomException raisedException) {
    this.raisedException = raisedException;
    this.returnHeaders =  null;
    this.responseBody = null;
  }

  public HttpResponderText(HashMap<String, String> returnHeaders) {
    this.returnHeaders = returnHeaders;
    this.responseBody = null;
    this.raisedException = null;
  }

  public HttpResponderText(String responseBody) {
    // GET request to root
    this.responseBody = responseBody;
    this.returnHeaders = null;
    this.raisedException = null;
  }

  public HttpResponderText(HashMap<String, String> returnHeaders, String responseBody) {
    this.returnHeaders = returnHeaders;
    this.responseBody = responseBody;
    this.raisedException = null;
  }

  @Override
  public byte[] formatResponse() throws IOException {

    byte[] responseBodyBytes = new byte[0];
    if (this.raisedException != null) {
      // failed request 
      String exceptionMessage = this.raisedException.returnResponse("HTTP/1.1");
      responseBodyBytes = exceptionMessage.getBytes();
      return responseBodyBytes;
    }
    if (this.returnHeaders == null) {
      // GET Request to Root to establish connection
      return this.responseBody.getBytes();
    }
    StringBuilder responseHeader = new StringBuilder(this.returnHeaders.get("version"));
    responseHeader.append(" 200 OK\r\n");
    
    String clientEncoding = this.returnHeaders.get("Content-Encoding: ");
    String contentType = this.returnHeaders.getOrDefault("Content-Type: ", "text/plain");
    String connectionHeaderValue = this.returnHeaders.get("Connection: ");

    int responseContentLength = Integer.parseInt(this.returnHeaders.getOrDefault("Content-Length: ", "0"));
    if (clientEncoding != null && clientEncoding.equals("gzip") && this.responseBody != null) {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
        gzipOut.write(this.responseBody.getBytes());
      }
      responseBodyBytes = byteOut.toByteArray();
      responseContentLength = responseBodyBytes.length;

    } else if (this.responseBody != null) {
      responseBodyBytes = this.responseBody.getBytes();
    }
    responseHeader.append("Content-Type: ")
      .append(contentType)
      .append("\r\n");

    if (responseContentLength != 0) {
      responseHeader.append("Content-Length: ")
        .append(responseContentLength)
        .append("\r\n");
    }
    if (clientEncoding != null) {
      responseHeader.append("Content-Encoding: ")
        .append(clientEncoding)
        .append("\r\n");
    }
    if (connectionHeaderValue != null) {
      responseHeader.append("Connection: ")
        .append(connectionHeaderValue)
        .append("\r\n");
    }
    responseHeader.append("\r\n");
    byte[] responseHeaderBytes = responseHeader.toString().getBytes();

    if (clientEncoding == null) {
      responseBodyBytes = this.responseBody.getBytes();
    }

    ByteArrayOutputStream fullResponseStream = new ByteArrayOutputStream();
    fullResponseStream.write(responseHeaderBytes);
    fullResponseStream.write(responseBodyBytes);

    byte[] fullResponseBytes = fullResponseStream.toByteArray();
    return fullResponseBytes;
  }
}
