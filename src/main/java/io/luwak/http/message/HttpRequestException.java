package io.luwak.http.message;

/**
 * Thrown when HTTP request processing encounters an error
 *
 * @author Fredy Yanardi
 *
 */
public class HttpRequestException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1421607860482945744L;

    private HttpStatus status;

    /**
     * Constructs an HTTP request exception with the given HTTP status and message
     * 
     * @param status HTTP status code
     * @param message error message
     */
    public HttpRequestException(HttpStatus status, String message) {
        this(status, message, null);
    }

    /**
     * Constructs an HTTP request exception with the given HTTP status, message and cause
     * 
     * @param status HTTP status code
     * @param message error message
     * @param cause the cause of this exception
     */
    public HttpRequestException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Get the HTTP status code representation of this exception to be returned to the HTTP client
     *  
     * @return HTTP status code representation of this exception
     */
    public HttpStatus getStatus() {
        return this.status;
    }
}
