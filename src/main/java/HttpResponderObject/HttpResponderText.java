package HttpResponderObject;

import java.util.HashMap;

public class HttpResponderText extends HttpResponder {
    public HashMap<String, String> returnHeaders;
    public byte[] responseBodyBytes;
  
    public HttpResponderText(HashMap<String, String> returnHeaders, byte[] responseBodyBytes) {
      this.returnHeaders = returnHeaders;
      this.responseBodyBytes = responseBodyBytes;
    }
    public HttpResponderText(HashMap<String, String>  returnHeaders) {
      this.returnHeaders = returnHeaders;
      this.responseBodyBytes = null;
    }
    public HttpResponderText(byte[] responseBodyBytes) {
      // GET request to root
      this.responseBodyBytes = responseBodyBytes;
      this.returnHeaders = null;
    }
    @Override
    public byte[] formatResponse() {
      // need to create status line and header and then concatenate
      // that byte array with the responseBodyByte array
      return new byte[0];
    }
  }
  