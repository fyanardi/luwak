package io.luwak.httpd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The runnable that will be used by the main listening thread. This runnable will accept new
 * connections and then run a new runnable to serve the connection
 *
 * @author Fredy Yanardi
 */
public class ServerRunnable implements Runnable {

    private ServerSocket serverSocket;
    private String hostname;
    private int port;
    private final int timeout;
    private ExecutorService executorService;
    private HttpdContext httpdContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRunnable.class);

    /**
     * Create a new server runnable
     * 
     * @param serverSocket the server socket
     * @param hostname the host name for this server socket
     * @param port the port to bind to
     * @param timeout socket timeout (SO_TIMEOUT to be applied to the server socket)
     * @param maxThreads maximum number of threads, this sets the maximum number of concurrent
     *      incoming connections that can be handled at the same time
     * @param httpdContext the HTTPD Context object
     */
    public ServerRunnable(ServerSocket serverSocket, String hostname, int port, int timeout,
            int maxThreads, HttpdContext httpdContext) {
        this.serverSocket = serverSocket;
        this.hostname = hostname;
        this.port = port;
        this.timeout = timeout;
        this.httpdContext = httpdContext;
        this.executorService = Executors.newFixedThreadPool(maxThreads);

        LOGGER.info("ServerRunnable created hostname={} port={} timeout={} httpdContext={} maxThread={}",
                hostname, port, timeout, httpdContext, maxThreads);
    }

    @Override
    public void run() {
        try {
            SocketAddress sockAddress = hostname != null ?
                    new InetSocketAddress(hostname, port) : new InetSocketAddress(port);
            serverSocket.bind(sockAddress);
            LOGGER.info("Bound to: {}", sockAddress);
        }
        catch (IOException e) {
            LOGGER.error("Exception caught while binding server socket", e);
            return;
        }

        do {
            try {
                final Socket acceptSocket = serverSocket.accept();
                LOGGER.debug("Accepted connection from {}", acceptSocket.getRemoteSocketAddress());
                if (timeout > 0) {
                    acceptSocket.setSoTimeout(this.timeout);
                }
                executorService.execute(new ClientHandler(acceptSocket, httpdContext));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } while (!serverSocket.isClosed());
    }
}