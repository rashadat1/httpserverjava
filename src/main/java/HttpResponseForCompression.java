public class HttpResponseForCompression {
    String httpResponse;
    byte[] encodedBody;
  
    public HttpResponseForCompression(String httpResponse, byte[] encodedBody) {
      this.httpResponse = httpResponse;
      this.encodedBody = encodedBody;
    }
    public HttpResponseForCompression(String httpResponse) {
      this.httpResponse = httpResponse;
      this.encodedBody = null;
    }
  }
  