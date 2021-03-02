package io.luwak.httpd;

import io.luwak.http.message.HttpRequest;
import io.luwak.http.message.HttpResponse;

/**
 * Interface for HTTP Session handler
 *
 * @author Fredy Yanardi
 *
 */
public interface HttpSession {

    /**
     * Serve an HTTP request
     *
     * @param httpRequest the HTTP request to be served by this method
     * @return the HTTP response to be sent back as response to the received request
     */
    public HttpResponse serve(HttpRequest httpRequest);

    /**
     * This method will be called once the full HTTP response has been sent to the requesting
     * client
     * 
     * @param httpResponse the HTTP response sent to the client
     */
    public void onResponseSent(HttpResponse httpResponse);

}
