package io.luwak.http.message;

import java.util.Map;

/**
 * A representation of HTTP response
 *
 * @author Fredy Yanardi
 *
 */
public class HttpResponse {

    public static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";

    private String httpVersion;
    private HttpStatus status;
    private Map<String, String> headers;
    private HttpEntity body;

    /**
     * Constructs an HttpResponse with the specified HTTP status, headers and body
     *
     * @param status the HTTP status
     * @param headers HTTP response headers
     * @param body HTTP response body
     */
    public HttpResponse(HttpStatus status, Map<String, String> headers, HttpEntity body) {
        this(DEFAULT_HTTP_VERSION, status, headers, body);
    }

    /**
     * Constructs an HttpResponse with the specified HTTP version, status, headers and body
     *
     * @param httpVersion the HTTP version
     * @param status the HTTP status
     * @param headers HTTP response headers
     * @param body HTTP response body
     */
    public HttpResponse(String httpVersion, HttpStatus status, Map<String, String> headers,
            HttpEntity body) {
        this.httpVersion = httpVersion;
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Gets the HTTP version of this response
     *
     * @return the HTTP version
     */
    public String getHttpVersion() {
        return this.httpVersion;
    }

    /**
     * Gets the HTTP status
     *
     * @return the HTTP status
     */
    public HttpStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the headers of this HTTP response
     *
     * @return the HTTP response headers
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Gets the response body
     *
     * @return the response body
     */
    public HttpEntity getEntityBody() {
        return this.body;
    }
}
