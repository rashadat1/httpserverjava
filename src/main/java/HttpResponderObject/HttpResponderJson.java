package HttpResponderObject;
import org.json.JSONObject;

public class HttpResponderJson extends HttpResponder {
    JSONObject responseJson;
    String headers;

    public HttpResponderJson(JSONObject responseJson, String headers) {
        this.responseJson = responseJson;
        this.headers = headers;
    }
}