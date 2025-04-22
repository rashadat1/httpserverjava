package HttpParser;

public class HttpParseFail extends HttpParserReturn{
    String errorMessage;

    public HttpParseFail(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
