package HttpResponderObject;

import org.json.JSONObject;

public class HttpResponderJson extends HttpResponder {
    JSONObject responseJson;
    String headers;
    String errors;

    public HttpResponderJson(JSONObject responseJson, String headers) {
        this.responseJson = responseJson;
        this.headers = headers;
    }

    @Override
    public byte[] formatResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'formatResponse'");
    }

}