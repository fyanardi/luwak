package io.luwak.httpd;

import io.luwak.http.message.HttpMethod;

/**
 * Interface for HTTPD Context. Implementation of this interface is responsible for returning an
 * instance of HttpSessionFactory based on the request URI and HTTP method 
 *
 * @author Fredy Yanardi
 *
 */
public interface HttpdContext {

    /**
     * Return an instance of HttpSessionFactory based on the request URI and HTTP method
     *
     * @param uri the request URI
     * @param method the HTTP method
     * @return an instance of HttpSessionFactory
     */
    public HttpSessionFactory getHttpSessionFactory(String uri, HttpMethod method);

}
