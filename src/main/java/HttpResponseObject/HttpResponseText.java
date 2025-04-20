package HttpResponseObject;
public class HttpResponseText extends HttpResponse {
    public String httpResponse;
    public byte[] encodedBody;
  
    public HttpResponseText(String httpResponse, byte[] encodedBody) {
      this.httpResponse = httpResponse;
      this.encodedBody = encodedBody;
    }
    public HttpResponseText(String httpResponse) {
      this.httpResponse = httpResponse;
      this.encodedBody = null;
    }
  }
  