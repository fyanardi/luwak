package io.luwak.httpd;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for the Luwak HTTP server
 *
 * @author Fredy Yanardi
 *
 */
public class LuwakHttpd {

    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_MAX_THREADS = 200;

    private String hostname = DEFAULT_HOSTNAME;
    private int port = DEFAULT_PORT;
    private int maxThreads = DEFAULT_MAX_THREADS;
    private final HttpdContext httpdContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(LuwakHttpd.class);

    private ServerRunnable serverRunnable;
    private Thread httpdThread;

    /**
     * Construct the HTTP server with the specified hostname, port, maximum number of threads and
     * the HttpdContext
     *
     * @param hostname the host name of the HTTP server
     * @param port the port to bind to
     * @param maxThreads maximum number of threads allowed to serve incoming connections
     * @param httpdContext the httpd context
     */
    public LuwakHttpd(String hostname, int port, int maxThreads, HttpdContext httpdContext) {
        this.hostname = hostname;
        this.port = port;
        this.maxThreads = maxThreads;
        this.httpdContext = httpdContext;
    }

    /**
     * Start the webserver by binding to the specified port and listening for incoming connection
     * request
     */
    public void start() {
        LOGGER.info("Starting Luwak HTTP server");
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);

            serverRunnable = new ServerRunnable(serverSocket, hostname, port, 0, maxThreads,
                    httpdContext);
            httpdThread = new Thread(serverRunnable);
            httpdThread.start();
            httpdThread.join();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
