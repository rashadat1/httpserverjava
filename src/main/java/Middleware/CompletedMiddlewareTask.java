package Middleware;

public class CompletedMiddlewareTask {
    ConnectionContext ctx;
    String serviceName;
    String taskName;
    Object response;

    public CompletedMiddlewareTask(ConnectionContext ctx, String taskName, Object response) {
        this.ctx = ctx;
        this.taskName = taskName;
        this.response = response;
    }

}