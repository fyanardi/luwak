package io.luwak.httpd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.luwak.http.message.DefaultHttpStatus;
import io.luwak.http.message.HttpEntity;
import io.luwak.http.message.HttpRequest;
import io.luwak.http.message.HttpResponse;

/**
 * Default HTTP Session which will try to locate and load the request URI as a file from local
 * document root and return it as HTTP response. The default local document root is 'doc-root' under
 * the current working directory.
 * 
 * @author Fredy Yanardi
 *
 */
public class DefaultHttpSession implements HttpSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpSession.class);

    public static final String DEFAULT_DOC_ROOT_LOCATION = "doc-root";
    public static final String DEFAULT_404_NOT_FOUND =
            "<html><head><title>404 Page Not Found</title><body><h1>404 Page Not Found</h1><h2>The requested URL %s was not found on this server</h2></body></html>";
    public static final String INDEX_HTML = "index.html";

    private final String docRootLocation;

    /**
     * Constructs a DefaultHttpSession instance with the default doc root location
     */
    public DefaultHttpSession() {
        docRootLocation = DEFAULT_DOC_ROOT_LOCATION;
    }

    /**
     * Constructs a DefaultHttpSession instance with the specified document root location
     *
     * @param docRootLocation the document root location
     */
    public DefaultHttpSession(String docRootLocation) {
        this.docRootLocation = docRootLocation;
    }

    @Override
    public HttpResponse serve(HttpRequest httpRequest) {
        String uri = httpRequest.getRequestUri();
        LOGGER.info("Serve URI={}", uri);

        if (uri == null) {
            return new HttpResponse(HttpResponse.DEFAULT_HTTP_VERSION, DefaultHttpStatus.BAD_REQUEST,
                    new HashMap<>(), null);
        }

        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            String query = uri.substring(queryIndex + 1);
            uri = uri.substring(0, queryIndex);
            LOGGER.debug("Query parameters '{}' ignored", query);
        }

        if (uri.equals("/")) {
            uri += INDEX_HTML;
        }

        HttpResponse httpResponse = null;
        String fileName = this.docRootLocation + File.separator + uri.replaceFirst("^/", "");
        File file = new File(fileName);

        if (file.exists()) {
            try {
                HttpEntity httpEntity = new HttpEntity(file, false, false);
                Map<String, String> headers = new HashMap<String, String>();

                String contentLength = Long.toString(httpEntity.getLength());
                String contentType = guessContentType(file);
                LOGGER.debug("File '{}' content-length={} content-type={}", file,
                        httpEntity.getLength(), contentType);

                headers.put("Content-Length", contentLength);
                headers.put("Content-type", guessContentType(file));

                httpResponse = new HttpResponse(HttpResponse.DEFAULT_HTTP_VERSION,
                        DefaultHttpStatus.OK, headers, httpEntity);
            }
            catch (IOException e) {
                LOGGER.error("Error constructing HttpResponse for file '" + file.getAbsolutePath() +
                        "'", e);
            }
        }
        else {
            LOGGER.error("File '{}' not found", file);
        }

        if (httpResponse == null) {
            httpResponse = buildHttpResponseNotFound(uri);
        }
        return httpResponse;
    }

    @Override
    public void onResponseSent(HttpResponse httpResponse) {
    }

    private String guessContentType(File file) {
        Path path = file.toPath();
        String contentType = null;
        try {
            contentType = Files.probeContentType(path);
        }
        catch (IOException e) {
            contentType = "application/octet-stream";
            LOGGER.warn("Failed to get Content-Type from file '{}' set to '{}'", file, contentType);
        }
        return contentType;
    }

    private HttpResponse buildHttpResponseNotFound(String uri) {
        HttpEntity httpEntity = new HttpEntity(String.format(DEFAULT_404_NOT_FOUND, uri).getBytes(),
                false, false);
        Map<String, String> headers = new HashMap<String, String>();
        try {
            headers.put("Content-Length", Long.toString(httpEntity.getLength()));
            headers.put("Content-type", "text/html; charset=UTF-8");

            return new HttpResponse(HttpResponse.DEFAULT_HTTP_VERSION, DefaultHttpStatus.NOT_FOUND,
                    headers, httpEntity);
        }
        catch (IOException e) {
            return new HttpResponse(HttpResponse.DEFAULT_HTTP_VERSION,
                    DefaultHttpStatus.INTERNAL_SERVER_ERROR, new HashMap<>(), null);
        }
    }

    /*private String[] getUriPaths(String uri) {
        return uri.replaceFirst("^/", "").split("/");
    }*/
}
