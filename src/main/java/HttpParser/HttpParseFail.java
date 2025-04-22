package HttpParser;

public class HttpParseFail extends HttpParserReturn{
    public String errorMessage;

    public HttpParseFail(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
