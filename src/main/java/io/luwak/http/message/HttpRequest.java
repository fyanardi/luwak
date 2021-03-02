package io.luwak.http.message;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Request representation based on RFC 2616.
 *
 * @author Fredy Yanardi
 */
public class HttpRequest {

    // Only supports HTTP/1.1 for now
    public static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";

    private HttpMethod method;
    private String uriPath;
    private Map<String, String> queries;
    private String httpVersion;
    private Map<String, String> headers;
    private HttpEntity body;

    private String requestUri;

    /**
     * Copy constructor
     *
     * @param httpRequest HttpRequest object to be copied
     */
    public HttpRequest(HttpRequest httpRequest) {
        this.method = httpRequest.method;
        this.uriPath = httpRequest.uriPath;
        this.queries = new HashMap<>(httpRequest.queries);
        this.httpVersion = httpRequest.httpVersion;
        this.headers = new HashMap<>(httpRequest.headers);
        // TODO: copy body

        this.requestUri = httpRequest.requestUri;
    }

    /**
     * Constructs an HTTP request from the given HTTP method, URI path, queries, HTTP version,
     * HTTP request headers and HTTP Entity (body)
     *
     * @param method the HTTP method
     * @param uriPath the URI path of the request
     * @param queries HTTP request queries
     * @param httpVersion HTTP version
     * @param headers HTTP request headers
     * @param body HTTP Entity (body)
     */
    public HttpRequest(HttpMethod method, String uriPath, Map<String, String> queries,
            String httpVersion, Map<String, String> headers, HttpEntity body) {
        this.method = method;
        this.uriPath = uriPath;
        this.queries = queries;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;

        this.requestUri = getRequestUri(uriPath, queries);
    }

    /**
     * Gets the HTTP method of this request
     *
     * @return the HTTP method of this request
     */
    public HttpMethod getMethod() {
        return this.method;
    }

    /**
     * Gets the path portion of the request URI, from the root path but precedes the query string.
     * This path always starts with a "/" character. This path is a decoded path.
     *
     * @return URI path String
     */
    public String getUriPath() {
        return this.uriPath;
    }

    /**
     * Gets the queries of this HTTP request
     */
    public Map<String, String> getQueries() {
        return this.queries;
    }

    /**
     * Get the full Request-URI to be sent in the HTTP request message. This request URI is URL
     * encoded.
     *
     * @return request URI string
     */
    public String getRequestUri() {
        return this.requestUri;
    }

    /**
     * Gets the HTTP version of this HTTP request
     *
     * @return the HTTP version of this HTTP request
     */
    public String getHttpVersion() {
        return this.httpVersion;
    }

    /**
     * Gets the HTTP request headers
     *
     * @return the HTTP request headers
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Gets the HTTP Entity (body)
     *
     * @return the HTTP Entity (body)
     */
    public HttpEntity getEntityBody() {
        return this.body;
    }

    /**
     * Construct the request URI from the URI path and request queries, this method will also
     * encode every entry of the request queries
     *
     * @param uriPath the URI path
     * @param queries the request queries map
     * @return the request URI that contains both URI path and encoded request queries
     */
    private String getRequestUri(String uriPath, Map<String, String> queries) {
        StringBuilder requestUri = new StringBuilder();
        requestUri.append(uriPath);
        if (!queries.isEmpty()) {
            requestUri.append('?');
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                requestUri.append(encodeUrl(entry.getKey()));
                requestUri.append('=');
                requestUri.append(encodeUrl(entry.getValue()));
                requestUri.append('&');
            }
            requestUri.deleteCharAt(requestUri.length() - 1);
        }
        return requestUri.toString();
    }

    private String encodeUrl(String url) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Ignored
        }
        return encoded;
    }
}
