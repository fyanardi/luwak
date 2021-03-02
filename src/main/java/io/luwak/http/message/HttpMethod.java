package io.luwak.http.message;

/**
 * HTTP Request methods, with the ability to decode a <code>String</code> back to its enum value.
 */
public enum HttpMethod {
    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,
    PATCH,
    PROPFIND,
    PROPPATCH,
    MKCOL,
    MOVE,
    COPY,
    LOCK,
    UNLOCK;

    /**
     * Returns the corresponding HttpMethod from the string representation of the HTTP method
     *
     * @param method the string representation of the HTTP method
     * @return the HttpMethod, or null if the string is not a known HTTP method
     */
    public static HttpMethod fromString(String method) {
        if (method == null) {
            return null;
        }

        try {
            return valueOf(method);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
