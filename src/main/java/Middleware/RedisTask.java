package Middleware;

import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;

import services.RedisClient;

public class RedisTask implements Runnable {
    String query;
    Selector selector;
    RedisClient redisClient; // persistent connection to Redis server
    ConnectionContext ctx; // socket channel with client who submitted this task / requires the result of this task
    ConcurrentLinkedQueue<CompletedMiddlewareTask> responseQueue; // thread-safe queue of completed tasks 

    public RedisTask(String query, Selector selector, RedisClient redisClient, ConnectionContext ctx, ConcurrentLinkedQueue<CompletedMiddlewareTask> responseQueue) {
        this.query = query;
        this.selector = selector;
        this.redisClient = redisClient;
        this.ctx = ctx;
        this.responseQueue = responseQueue;
    }
    @Override
    public void run() {
        //String response = this.redisClient.execute(query);
    }
}
