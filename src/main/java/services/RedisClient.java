package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import sslUtility.EnvLoader;
import sslUtility.SSLUtil;

public class RedisClient {
    public SocketChannel redisSocket;
    public SSLContext sslContext;

    public RedisClient() throws Exception {
        redisSocket = SocketChannel.open();
        EnvLoader.loadEnv(".env");
        String keystorePassword = System.getProperty("KEYSTORE_PASSWORD");
        String truststorePassword = System.getProperty("TRUSTSTORE_PASSWORD");
        sslContext = SSLUtil.createSSLContext(keystorePassword, truststorePassword);

        SSLEngine sslEngine = sslContext.createSSLEngine("localhost", 6379);
        redisSocket.connect(new InetSocketAddress("localhost", 6379));

        sslEngine.setUseClientMode(true);
        sslEngine.beginHandshake();


    }
    
}