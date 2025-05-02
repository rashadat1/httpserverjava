import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import Middleware.CompletedMiddlewareTask;
import Middleware.ConnectionContext;
import services.RedisClient;
import sslUtility.EnvLoader;
import sslUtility.SSLUtil;

public class HttpsServerEvent {
    private static final int port = 8443;
    private static String directory = null;
    private static int poolSize = 10;
    private static RedisClient redisClient;
    private static ExecutorService pool;
    private static ConcurrentLinkedQueue<CompletedMiddlewareTask> responseQueue = new ConcurrentLinkedQueue<>();

    private static void doHandshakeStep(ConnectionContext ctx) throws IOException {
        SSLEngineResult result;
        SSLEngineResult.HandshakeStatus handshakeStatus = ctx.sslEngine.getHandshakeStatus();

        while (true) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                System.out.println("SSLEngine in state NEED_UNWRAP");
            
                // Try reading bytes
                int bytesRead = ctx.channel.read(ctx.peerNetData);
                System.out.println("Bytes read from socket: " + bytesRead);
            
                if (bytesRead == -1) {
                    throw new IOException("Socket closed before handshake completed");
                } else if (bytesRead == 0) {
                    return; // Wait for next OP_READ
                }
            
                ctx.peerNetData.flip();
            
                while (true) {
                    result = ctx.sslEngine.unwrap(ctx.peerNetData, ctx.peerAppData);
                    SSLEngineResult.Status status = result.getStatus();
                    handshakeStatus = result.getHandshakeStatus();
            
                    System.out.println("Unwrap status: " + status + " / " + handshakeStatus);
            
                    switch (status) {
                        case OK:
                            if (handshakeStatus == SSLEngineResult.HandshakeStatus.FINISHED ||
                                handshakeStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                                ctx.handshaking = false;
                                System.out.println("TLS handshake complete!");
                                ctx.peerNetData.compact();
                                return;
                            }
                            // Continue if still NEED_UNWRAP
                            break;
            
                        case BUFFER_UNDERFLOW:
                            System.out.println("BUFFER_UNDERFLOW — need more TLS data");
                            ctx.peerNetData.compact();
                            return;
            
                        case CLOSED:
                            throw new IOException("SSLEngine was closed during unwrap");
            
                        default:
                            throw new IOException("Unexpected unwrap status: " + status);
                    }
            
                    if (handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                        break;
                    }
                }
            
                ctx.peerNetData.compact();
                break;

                case NEED_WRAP:
                    System.out.println("SSLEngine in state NEED_WRAP");
                    ctx.netData.clear();
                    result = ctx.sslEngine.wrap(ctx.appData, ctx.netData); // package bytes to send to remote
                    ctx.netData.flip();
                    while (ctx.netData.hasRemaining()) {
                        ctx.channel.write(ctx.netData);
                    }
                    handshakeStatus = result.getHandshakeStatus();
                    break;
                    
                case NEED_UNWRAP_AGAIN:
                    System.out.println("SSLEngine in state NEED_UNWRAP AGAIN");
                    ctx.peerNetData.flip();
                    result = ctx.sslEngine.unwrap(ctx.peerNetData, ctx.peerAppData);
                    ctx.peerNetData.compact();
                    handshakeStatus = result.getHandshakeStatus();
                    break;

                case NEED_TASK:
                    System.out.println("SSLEngine in state NEED_TASK");
                    Runnable task;
                    while ((task = ctx.sslEngine.getDelegatedTask()) != null) {
                        task.run();
                    }
                    handshakeStatus = ctx.sslEngine.getHandshakeStatus();
                    break;
                case FINISHED:
                case NOT_HANDSHAKING:
                    System.out.println("Finished or not handshaking");
                    ctx.handshaking = false;
                    System.out.println("Handshake complete for connection: " + ctx.channel.getRemoteAddress());
                    return;
    
                default:
                    throw new IllegalStateException("Unexpected Handshake Status: " + handshakeStatus);
            }
            if (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && ctx.peerNetData.position() == 0) {
                System.out.println("Server: No new data to unwrap, wait for next read...");
                return;
            }

        }
    }
    public static void main(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length / 2; i += 2) {
                if (args[i].equals("--directory")) {
                    directory = args[i + 1];
                }
            }
        }
        pool = Executors.newFixedThreadPool(poolSize);
        try {
            System.out.println("Opening SSL server socket to listen on port 8443");
            EnvLoader.loadEnv(".env");
            SSLContext sslContext = SSLUtil.createSSLContext(System.getProperty("user.dir") + "/certsForAppExchange/keystore.p12", System.getProperty("APPCLIENTSTORE_PASSWORD"));  

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            Selector selector = Selector.open();

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("SSL Server is running on port: " + port);

            redisClient = new RedisClient();
            redisClient.authenticateConnection();
            
            while (true) {
                // Event Loop
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey currKey = keyIterator.next();
                    if (currKey.isAcceptable()) {
                        // server keeps a socket open that listens perpetually for new connections on the port
                        // when a client makes a connection we create a new socket to establish a channel betweem
                        // the new client and the server for all future communications - the server then continues to listen on the port
                        ServerSocketChannel server = (ServerSocketChannel) currKey.channel();
                        SocketChannel clientChannel = server.accept();
                        if (clientChannel != null) {
                            // create SSLEngine instance using SSLUtil
                            // perform TLS handshake with the client that is trying to connect
                            clientChannel.configureBlocking(false);
                            SSLEngine sslEngine = sslContext.createSSLEngine();
                            sslEngine.setEnabledProtocols(new String[] {"TLSv1.3"});
                            sslEngine.setEnabledCipherSuites(new String[] {
                                "TLS_AES_128_GCM_SHA256",
								"TLS_AES_256_GCM_SHA384",
								"TLS_CHACHA20_POLY1305_SHA256"
                            });

                            sslEngine.setUseClientMode(false);
                            sslEngine.setNeedClientAuth(false);
                            sslEngine.beginHandshake();

                            ConnectionContext ctx = new ConnectionContext(clientChannel, sslEngine, "client");
                            doHandshakeStep(ctx);
                            clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, ctx);
                            System.out.println("New Client connected to Gateway: " + clientChannel.getRemoteAddress());

                        } else {
                            continue;
                        }
                    } else if (currKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) currKey.channel();
                        ConnectionContext ctx = (ConnectionContext) currKey.attachment();
                        String sourceType = ctx.entity;
                        System.out.println("Reading from " + sourceType + " channel" + channel.getRemoteAddress());
                        if (ctx.handshaking) {
                            // TLS Handshake step
                            doHandshakeStep(ctx);
                            break;
                        }                        
                        // ByteBuffer responseBuf = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Length: 5\r\n\r\nHello".getBytes());
                        // ctx.netData.clear();
                        // SSLEngineResult result = ctx.sslEngine.wrap(responseBuf, ctx.netData);
                        // ctx.netData.flip();
                        // while (ctx.netData.hasRemaining()) {
                        //     ctx.channel.write(ctx.netData);
                        // }

                    }
                }
            }


        } catch (Exception e) {
            System.err.println("Error creating SSLContext for Encrypting socket: " + e.getMessage());
        }

       


    }
}
