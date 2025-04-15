import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer implements Runnable {
  private final int port;
  private final ExecutorService pool;
  private final String directory;
  
  public HttpServer(int port, int poolSize, String directory) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(poolSize); // create thread pool once server starts
    this.directory = directory;
  }

  private void handleClient(Socket clientSocket) {
    try {

      InputStream inputStream = clientSocket.getInputStream();
      OutputStream outputStream = clientSocket.getOutputStream();
      HttpParser httpParser = new HttpParser(inputStream, this.directory, null, false, null, null, null, null, 0, null);
      String response = httpParser.parseAndReturnHttpResponseString();
      outputStream.write(response.getBytes());
    } catch (IOException e) {
      System.err.println("IO Exception occurred while initializing input/output stream or reading/writing: " + e.getMessage());
    } catch (MalformedRequestException e) {
      System.err.println("Malformed Request Exception occurred while parsing http header/body: " + e.getMessage());
    }
  }

  @Override
  public void run() {
    System.out.println("Opening server socket to listen on port 4221 for connections...");
    try (ServerSocket serverSocket = new ServerSocket(this.port)) {
      serverSocket.setReuseAddress(true);
      // no shutdown configured to occur programmatically - always on
      while (true) {
        Socket clientSocket = serverSocket.accept();
        pool.submit(() -> handleClient(clientSocket));
      }
    } catch (IOException e) {
      System.err.println("IO Exception occurred while establishing client connection: " + e.getMessage());
    }
  }
  public static void main(String[] args) {
    String directory = null;
    if (args.length > 0) {
      for (int i = 0; i < args.length / 2; i+=2) {
        if (args[i].equals("--directory")) {
          directory = args[1];
        }
      }
    }
    HttpServer server = new HttpServer(4221, 10, directory);
    server.run();
  }

}