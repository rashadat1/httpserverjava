import org.json.JSONObject;

public class HttpResponseJson extends HttpResponse {
    JSONObject responseJson;
    String headers;

    public HttpResponseJson(JSONObject responseJson, String headers) {
        this.responseJson = responseJson;
        this.headers = headers;
    }
}
