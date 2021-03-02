package io.luwak.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.luwak.http.HttpServerStreams;
import io.luwak.http.message.HttpRequest;
import io.luwak.http.message.HttpRequestException;
import io.luwak.http.message.HttpResponse;

/**
 * Client handler that handles one incoming connection from a client. Every instance of this class
 * should be running in a separate thread, but this class should not be concerned with the thread
 * management itself
 *
 * @author Fredy Yanardi
 *
 */
public class ClientHandler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private Socket acceptSocket;
    private Socket clientSocket;
    private HttpdContext httpdContext;

    /**
     * Create a client handler from an accept socket and an HTTPD Context
     *
     * @param acceptSocket accept socket
     * @param httpdContext the HTTPD Context object
     */
    public ClientHandler(Socket acceptSocket, HttpdContext httpdContext) {
        this.acceptSocket = acceptSocket;
        this.httpdContext = httpdContext;
    }

    /**
     * Create a client handler from an accept socket and an already established client socket.
     *
     * @param acceptSocket accept socket
     * @param clientSocket already established client socket
     */
    public ClientHandler(Socket acceptSocket, Socket clientSocket) {
        this.acceptSocket = acceptSocket;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (InputStream inputStream = acceptSocket.getInputStream();
                OutputStream outputStream = acceptSocket.getOutputStream()) {
            HttpServerStreams httpServerStreams = new HttpServerStreams(inputStream, outputStream);
            // TODO: honour keep alive
            while (!acceptSocket.isClosed()) {
                HttpRequest httpRequest = null;
                try {
                    httpRequest = httpServerStreams.read();

                    HttpSessionFactory httpSessionFactory = httpdContext.getHttpSessionFactory(
                            httpRequest.getUriPath(), httpRequest.getMethod());
                    HttpSession httpSession = httpSessionFactory.newInstance(acceptSocket);
                    HttpResponse httpResponse = httpSession.serve(httpRequest);
                    httpServerStreams.send(httpResponse);
                    httpSession.onResponseSent(httpResponse);
                }
                catch (IOException | HttpRequestException e) {
                    LOGGER.error("Exception caught while reading streams", e);
                }
            }
            LOGGER.debug("Accept socket {} closed", acceptSocket.getRemoteSocketAddress());
        } catch (IOException e1) {
            LOGGER.error("Exception caught while reading server streams", e1);
            return;
        }
    }

    // TODO
    // To be used if we need to set up a transparent HTTPS tunnel, i.e. we are not intercepting the
    // HTTPS traffic
    /*private void setupTunnel(HttpRequest httpRequest, InputStream clientIs, OutputStream clientOs) {
        String hostHeader  = httpRequest.getHeaders().get("Host".toLowerCase());
        System.out.println("----- Host header: " + hostHeader);
        String[] fragments = hostHeader.split(":");
        String host = fragments[0];
        int port = Integer.parseInt(fragments[1]);
        
        PipedStreams streamToHttp = null;
        PipedStreams streamToEndpoint = null;
        System.out.println(String.format("[%s] Connecting to remote host %s:%d", Thread.currentThread().getName(), host, port));

        try (Socket endpoint = SocketFactory.getDefault().createSocket(host, port)) {
            System.out.println("Connection established");

            CountDownLatch latch = new CountDownLatch(1);

            streamToHttp = new PipedStreams(endpoint.getInputStream(), clientOs, latch);
            streamToEndpoint = new PipedStreams(clientIs, endpoint.getOutputStream(), latch);

            new Thread(streamToHttp, "endpoint-read -> http-write    ").start();
            new Thread(streamToEndpoint, "http-read     -> endpoint-write").start();

            latch.await();

            System.out.println(String.format("[%s] Safely shut down servlet-to-%s:%d proxy.",
                    Thread.currentThread().getName(), host, port));
        } catch (IOException e) {
            System.out.println(String.format("[%s] IO Exception, shutting down servlet-to-%s:%d proxy.",
                    Thread.currentThread().getName(), host, port));
        } catch (InterruptedException e) {
            System.out.println(String.format("[%s] Interrupted, shutting down servlet-to-%s:%d proxy.",
                    host, port));
        } finally {
            if (streamToHttp != null) {
                streamToHttp.close();
            }
            if (streamToEndpoint != null) {
                streamToEndpoint.close();
            }
        }
    }*/

}
