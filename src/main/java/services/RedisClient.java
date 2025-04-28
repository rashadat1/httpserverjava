package services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import sslUtility.EnvLoader;
import sslUtility.SSLUtil;

public class RedisClient {
    public SocketChannel redisSocket;
    public SSLContext sslContext;

    Boolean handshaking;

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
        sslContext = SSLUtil.createSSLContext(keystorePassword, truststorePassword);

        SSLEngine sslEngine = sslContext.createSSLEngine("TarikRashada", 6379); // for SSL purposes
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
        this.handshaking = true;

        SSLSession session = sslEngine.getSession();
        this.peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        this.peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        this.netData = ByteBuffer.allocate(session.getPacketBufferSize());
        this.appData = ByteBuffer.allocate(session.getApplicationBufferSize());

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
                    this.netData.clear();
                    result = sslEngine.wrap(this.appData, this.netData);
                    this.netData.flip();
                    while (this.netData.hasRemaining()) {
                        System.out.println("Trying to write to socket");
                        int bytesWritten = redisSocket.write(this.netData);
                        if (bytesWritten == 0) {
                            Thread.sleep(2);
                        }
                    }
                    int bytesReadWrap = redisSocket.read(this.peerNetData);
                    if (bytesReadWrap == -1) {
                        throw new IOException("Channel closed after wrap");
                    }
                    this.peerNetData.flip();
                    result = sslEngine.unwrap(this.peerNetData, this.peerAppData);
                    this.peerNetData.compact();
                    handshakeStatus = result.getHandshakeStatus();
                    System.out.println("State after NEED_WRAP: " + handshakeStatus);
                    break;

                case NEED_UNWRAP_AGAIN:
                    System.out.println("NEED UNWRAP AGAIN STEP");
                    this.peerNetData.flip();
                    result = sslEngine.unwrap(this.peerNetData, this.peerAppData);
                    this.peerNetData.compact();
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
                    this.handshaking = false;
                    System.out.println("Handshake complete for connection: " + redisSocket.getRemoteAddress());
                    return;
                default:
                    throw new IllegalStateException("Unexpected handshake status: " + handshakeStatus);
            }
            if (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && this.peerNetData.position() == 0) {
				break;
			}
        }

    }
}