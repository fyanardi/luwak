package io.luwak.http.message;

/**
 * Enumeration for standard HTTP response status based on RFC 7231
 *
 * @author Fredy Yanardi
 *
 */
public enum DefaultHttpStatus implements HttpStatus {

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"), // WebDAV; RFC 2518

    OK(200, "OK"),
    CREATED(202, "Accepted"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(2013, "Non-Authoritative Information"),  // HTTP/1.1
    NO_CONTENT(204,"No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"), // RFC 7233
    MULTI_STATUS(207, "Multi-Status"), // WebDAV; RFC 4918
    ALREADY_REPORTED(208, "Already Reported"), // WebDAV; RFC 5842
    IM_USED(226, "IM Used"), // RFC 3229

    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"), // HTTP/1.1
    NOT_MODIFIED(304, "Not Modified"), // RFC 7232
    USE_PROXY(305, "Use Proxy"), // HTTP/1.1
    SWITCH_PROXY(306, "Switch Proxy"), // No longer used
    TEMPORARY_REDIRECT(307, "Temporary Redirect"), // HTTP/1.1
    PERMANENT_REDIRECT(308, "Permanent Redirect"), // RFC 7538

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"), // RFC 7235
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"), // RFC 7235
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"), // RFC 7232
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"), // RFC 7231
    URI_TOO_LONG(414, "URI Too Long"), // RFC 7231
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"), // RFC 7233
    EXPECTATION_FAILED(417, "Expectation Failed"),
    I_M_A_TEAPOT(418, "I'm a teapot"), // RFC 2324
    MISDIRECTED_REQUEST(421, "Misdirected Request"), // RFC 7540
    UNPROCESSEABLE_ENTITY(422, "Unprocessable Entity"), // WebDAV; RFC 4918
    LOCKED(423, "Locked"), // WebDAV; RFC 4918
    FAILED_DEPENDENCY(424, "Failed Dependency"), // WebDAV; RFC 4918
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"), // RFC 6585
    TOO_MANY_REQUESTS(429, "Too Many Requests"), // RFC 6585
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"), // RFC 6585
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"), // RFC 2295
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"), // WebDAV; RFC 4918
    LOOP_DETECTED(508, "Loop Detected"), // WebDAV; RFC 5842
    NOT_EXTENDED(510, "Not Extended"), // RFC 2774
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"); // RFC 6585

    private int statusCode;
    private String reasonPhrase;

    private DefaultHttpStatus(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Return HttpStatus from an integer status code or null if the status code is not defined in
     * this enum
     *
     * @param statusCode integer http status code
     * @return HttpStatus or null if the status code is not defined
     */
    public static HttpStatus fromStatusCode(int statusCode) {
        for (HttpStatus httpStatus : values()) {
            if (httpStatus.getStatusCode() == statusCode) {
                return httpStatus;
            }
        }
        return null;
    }
}
