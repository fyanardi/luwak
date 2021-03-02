package io.luwak.httpd;

import java.net.Socket;

/**
 * Interface for HTTP Session Factory
 *
 * @author Fredy Yanardi
 *
 */
public interface HttpSessionFactory {

    /**
     * Creates a new instance of HttpSession based on the specified accept socket that will handle
     * incoming HTTP requests received on the accept socket.
     *
     * @param acceptSocket the server accept socket
     * @return an instance of HttpSession
     */
    public HttpSession newInstance(Socket acceptSocket);

}
