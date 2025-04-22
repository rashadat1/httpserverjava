package api.v1;

import HttpParser.HttpParseSuccess;
import HttpResponderObject.HttpResponder;
import HttpResponderObject.HttpResponderText;
import api.v1.routers.EndpointHandler;
import customExceptions.ResourceNotFoundException;

public class UserAgent implements EndpointHandler {
  HttpParseSuccess httpParse;

  public UserAgent(HttpParseSuccess httpParserReturn) {
    this.httpParse = httpParserReturn;
  }

  @Override
  public HttpResponder handle() {
    try {
      if (this.httpParse.requestMethod.equals("GET")) {
        String userAgent = this.httpParse.headers.get("User-Agent: ");
        this.httpParse.returnHeaders.put("Content-Type: ", "text/plain");
        this.httpParse.returnHeaders.put("Content-Length: ", String.valueOf(userAgent.length()));

        String responseBody = userAgent;
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
