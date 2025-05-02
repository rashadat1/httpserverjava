package services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import sslUtility.EnvLoader;
import sslUtility.SSLUtil;

public class RedisClient {
    public SocketChannel redisSocket;
    public SSLContext sslContext;
    public SSLEngine sslEngine;

    Boolean handshaking;
    Boolean isAuthenticated = false;

    ByteBuffer peerNetData; // encrypted bytes received from channel
    ByteBuffer peerAppData; // decrypted bytes after unwrap

    ByteBuffer netData; // encrypted bytes to send to channel
    ByteBuffer appData; // plain bytes to encrypt and send

    public RedisClient() throws Exception {
        System.out.println("Creating Redis Client...");
        redisSocket = SocketChannel.open();
        EnvLoader.loadEnv(".env");
        System.out.println("Loading environment");
        String keystorePassword = System.getProperty("KEYSTORE_PASSWORD");
        String truststorePassword = System.getProperty("TRUSTSTORE_PASSWORD");
        sslContext = SSLUtil.createSSLContext(System.getProperty("user.dir") + "/certs/client/client-keystore.p12", keystorePassword, System.getProperty("user.dir") + "/certs/ca/ca-truststore.p12", truststorePassword);

        sslEngine = sslContext.createSSLEngine("TarikRashada", 6379); // for SSL purposes
        sslEngine.setEnabledProtocols(new String[] {"TLSv1.3"});
        sslEngine.setEnabledCipherSuites(new String[] {
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256"
        });
        redisSocket.connect(new InetSocketAddress("127.0.0.1", 6379)); // for network connection
        System.out.println("Establishing SSL connection on " + redisSocket.getRemoteAddress());
        sslEngine.setUseClientMode(true);
        sslEngine.beginHandshake();
        System.out.println("Beginning Handshaking");
        handshaking = true;

        SSLSession session = sslEngine.getSession();
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        netData = ByteBuffer.allocate(session.getPacketBufferSize());
        appData = ByteBuffer.allocate(session.getApplicationBufferSize());

        SSLEngineResult result;
        SSLEngineResult.HandshakeStatus handshakeStatus;
        handshakeStatus = sslEngine.getHandshakeStatus();
        while(true) {
            System.out.println("Starting next iteration");

            switch (handshakeStatus) {
                case NEED_UNWRAP:
                System.out.println("NEED UNWRAP STEP");
                peerNetData.flip();
                result = sslEngine.unwrap(peerNetData, peerAppData);
                peerNetData.compact();
            
                handshakeStatus = result.getHandshakeStatus();
                System.out.println("State after NEED_UNWRAP: " + handshakeStatus);
            
                switch (result.getStatus()) {
                    case OK:
                        // Successfully unwrapped data
                        break;
            
                    case BUFFER_UNDERFLOW:
                        System.out.println("BUFFER UNDERFLOW: Need to read more data from socket");
                        int bytesRead = redisSocket.read(peerNetData);
                        if (bytesRead == -1) {
                            throw new IOException("Connection closed during handshake");
                        }
                        if (bytesRead == 0) {
                            System.out.println("No data available, wait for next OP_READ");
                            return;
                        }
                        break;
            
                    case CLOSED:
                        throw new IOException("SSLEngine closed during handshake");
            
                    default:
                        throw new IOException("Unexpected unwrap status: " + result.getStatus());
                }
                break;
            

                case NEED_WRAP:
                    System.out.println("NEED WRAP STEP");
                    netData.clear();
                    result = sslEngine.wrap(appData, netData);
                    netData.flip();
                    while (netData.hasRemaining()) {
                        System.out.println("Trying to write to socket");
                        int bytesWritten = redisSocket.write(netData);
                        if (bytesWritten == 0) {
                            break;
                        }
                    }
                    int bytesReadWrap = redisSocket.read(peerNetData);
                    if (bytesReadWrap == -1) {
                        throw new IOException("Channel closed after wrap");
                    }
                    peerNetData.flip();
                    result = sslEngine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    handshakeStatus = result.getHandshakeStatus();
                    System.out.println("State after NEED_WRAP: " + handshakeStatus);
                    break;

                case NEED_UNWRAP_AGAIN:
                    System.out.println("NEED UNWRAP AGAIN STEP");
                    peerNetData.flip();
                    result = sslEngine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    handshakeStatus = result.getHandshakeStatus();
                    System.out.println("State after NEED_UNWRAP_AGAIN: " + handshakeStatus);
                    break;
                
                case NEED_TASK:
                    System.out.println("NEED TASK STEP");
                    Runnable task;
                    while ((task = sslEngine.getDelegatedTask()) != null) {
                        task.run();
                    }
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    System.out.println("State after NEED_TASK: " + handshakeStatus);
                    break;

                case FINISHED:
                case NOT_HANDSHAKING:
                    handshaking = false;
                    System.out.println("Handshake complete for connection: " + redisSocket.getRemoteAddress());
                    return;
                default:
                    throw new IllegalStateException("Unexpected handshake status: " + handshakeStatus);
            }
            if (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && peerNetData.position() == 0) {
				break;
			}
        }
    }

    public void authenticateConnection() {
        System.out.println("Checking if authenticated");
        try {
            if (!isAuthenticated) {
                EnvLoader.loadEnv(".env");
                String redis_username = System.getProperty("REDIS_USERNAME");
                String redis_password = System.getProperty("REDIS_PASSWORD");
                
                System.out.println("Sending AUTH command to authenticate with redis");
                String respAuthString = "*3\r\n$4\r\nAUTH\r\n$" 
                    + redis_username.length() + "\r\n" 
                    + redis_username + "\r\n$"
                    + redis_password.length() + "\r\n"
                    + redis_password + "\r\n";
                
                encryptAndSendMessage(respAuthString);
                String message = readEncrypted();

                System.out.println("Message received after authentication: " + message);
                if (message.equals("+OK\r\n")) {
                    isAuthenticated = true;
                    System.out.println("Connection with Redis is authenticated");
                }
            }
        } catch (Exception e) {
            System.err.println("Error authenticating with Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void encryptAndSendMessage(String message) throws Exception {
        appData.clear();
        netData.clear();

        appData.put(message.getBytes());
        appData.flip();

        while (appData.hasRemaining()) {
            netData.clear();
            sslEngine.wrap(appData, netData);
            
            netData.flip();
            while (netData.hasRemaining()) {
                redisSocket.write(netData);
            }
        }
        netData.clear();
        appData.clear();
    }
    private String readEncrypted() throws Exception {
        peerAppData.clear();
        Thread.sleep(6000);
        int bytesRead = redisSocket.read(peerNetData);
        if (bytesRead == -1) {
            throw new IOException("Redis client socket closed unexpectedly");
        }
        peerNetData.flip();
        while (peerNetData.hasRemaining()) {
            SSLEngineResult result = sslEngine.unwrap(peerNetData, peerAppData);
            switch (result.getStatus()) {
                case OK:
                    break;
                case BUFFER_OVERFLOW:
                    throw new SSLException("App buffer overflow during reading from server");
                case BUFFER_UNDERFLOW:
                    peerNetData.compact();
                    bytesRead = redisSocket.read(peerNetData);
                    peerNetData.flip();
                    continue;
                case CLOSED:
                    throw new SSLException("SSL connection closed unexpectedly");
                    
            }
        }
        peerNetData.compact();
        peerAppData.flip();

        byte[] receivedBytes = new byte[peerAppData.remaining()];
        peerAppData.get(receivedBytes);
        String message = new String(receivedBytes);

        peerNetData.clear();
        peerAppData.clear();
        return message;
    }
    public String execute(String query) {

    }
}