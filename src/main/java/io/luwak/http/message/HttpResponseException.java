package io.luwak.http.message;

/**
 * Thrown when HTTP response processing encounters an error
 *
 * @author Fredy Yanardi
 *
 */
public class HttpResponseException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1421607860482945744L;

    /**
     * Constructs an HTTP response exception with the given message
     *
     * @param message error message
     */
    public HttpResponseException(String message) {
        this(message, null);
    }

    /**
     * Constructs an HTTP response exception with the given message and cause
     *
     * @param message error message
     * @param cause the cause of this exception
     */
    public HttpResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
