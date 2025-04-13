import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {
  private static final int PORT = 4221;

  private static String createHttpResponse(String urlPathString) {
    // default response
    String statusLine;
    if (urlPathString.equals("/")) {
      statusLine = "HTTP/1.1 200 OK\r\n\r\n";
    } else {
      statusLine = "HTTP/1.1 404 Not Found\r\n\r\n";
    }

    return statusLine; 
  }
  private static String extractUrlPath(InputStream inputStream) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream)); // input stream reader is a bridge from byte streams (input streams) to character streams Buffered Readers
    // readLine() reads a line of text demarkated by \r, \n, or \r\n
    String line = in.readLine();
    if (line != null) {
      String[] lineParts = line.split(" ");
      // HTTP Request is structured like GET /grape HTTP/1.1 so we just split by space and grab the second part
      return lineParts[1];
    }
    return "";
  }
  public static void main(String[] args) {

    System.out.println("Opening server socket to listen on port 4221 for connections...");
    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      serverSocket.setReuseAddress(true);

      while (true) { 
        Socket clientSocket = serverSocket.accept();
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        String urlPath = extractUrlPath(inputStream);
        if (!urlPath.equals("")) {
          String httpResponse = createHttpResponse(urlPath);
          outputStream.write(httpResponse.getBytes());
        }
      }
    } catch (IOException e) {
      System.err.println("Error initializing socket / creating output stream / writing to output stream: " + e.getMessage());
    }
  }
}
