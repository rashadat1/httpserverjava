import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import services.RedisClient;
import sslUtility.EnvLoader;
import sslUtility.SSLUtil;

public class HttpsServer extends HttpServer {
    private final int port;
    private final ExecutorService pool;
    @SuppressWarnings("unused")
    private final String directory;
    private int poolSize;

    public HttpsServer(int port, int poolSize, String directory) {
        super(port, poolSize, directory);
        this.port = port;
        this.poolSize = poolSize;
        this.directory = directory;

        this.pool = Executors.newFixedThreadPool(this.poolSize);
    }

    @Override
    public void run() {
        System.out.println("Opening SSL server socket to listen on port 443");

        try {
            EnvLoader.loadEnv(".env");
            SSLContext sslContext = SSLUtil.createSSLContext(System.getProperty("user.dir") + "/certsForAppExchange/keystore.p12", System.getProperty("APPCLIENTSTORE_PASSWORD"));
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);

            serverSocket.setEnabledProtocols(new String[] { "TLSv1.2", "TLSv1.3" });

            String[] strongSuites = {
                    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256"
            };
            serverSocket.setEnabledCipherSuites((strongSuites));

            serverSocket.setReuseAddress(true);
            try {
                RedisClient redisClientTest = new RedisClient();
            } catch (Exception e) {
                System.err.println("Error when starting Redis Client / handling handshake: " + e.getMessage());
                
            }
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                clientSocket.setKeepAlive(true);
                this.pool.submit(() -> super.handleClient(clientSocket));
            }

        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such algorithm in environment error triggered: " + e.getMessage());
        } catch (IOException e) {
            System.out.println(
                    "IOException caught while opening ssl server socket on port- " + this.port + ": " + e.getMessage());
        } catch (KeyManagementException e) {
            System.out.println("Key Management Exception thrown: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            System.out.println("Key cannot be recovered error: " + e.getMessage());
        } catch (KeyStoreException e) {
            System.out.println("Key Store Exception thrown: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String directory = null;
        if (args.length > 0) {
            for (int i = 0; i < args.length / 2; i += 2) {
                if (args[i].equals("--directory")) {
                    directory = args[1];
                }
            }
        }
        HttpsServer encryptedServer = new HttpsServer(443, 10, directory);
        encryptedServer.run();
    }

}
