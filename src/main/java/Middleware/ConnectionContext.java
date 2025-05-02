package Middleware;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

public class ConnectionContext {
    public SocketChannel channel;
    public String entity;
    public SSLEngine sslEngine;

    public ByteBuffer peerNetData; // encrypted bytes received from channel
    public ByteBuffer peerAppData; // decrypted bytes received through peerNetData
    public ByteBuffer netData; // encrypted bytes to write to channel 
    public ByteBuffer appData; // decrupted bytes to wrap to netData 
    
    public Boolean handshaking;

    public ConnectionContext(SocketChannel channel, SSLEngine sslEngine, String entity) {
        this.channel = channel;
        this.sslEngine = sslEngine;
        this.entity = entity;
        this.handshaking = true;

        SSLSession sslSession = sslEngine.getSession();

        this.peerNetData = ByteBuffer.allocate(sslSession.getPacketBufferSize());
        this.peerAppData = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
        this.netData = ByteBuffer.allocate(sslSession.getPacketBufferSize());
        this.appData = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
    }
}
