package io.luwak.httpd;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.luwak.http.message.HttpMethod;

/**
 * Default implementation of the HttpdContext.
 * 
 * @author Fredy Yanardi
 *
 */
public class DefaultHttpdContext implements HttpdContext {

    static class HttpSessionFactoryInfo {
        Pattern uriPattern;
        EnumSet<HttpMethod> httpMethods;
        HttpSessionFactory httpSessionFactory;

        public HttpSessionFactoryInfo(Pattern uriPattern, EnumSet<HttpMethod> httpMethods,
                HttpSessionFactory httpSessionFactory) {
            this.uriPattern = uriPattern;
            this.httpMethods = httpMethods;
            this.httpSessionFactory = httpSessionFactory;
        }
    }

    private final List<HttpSessionFactoryInfo> httpSessionFactoryList = new LinkedList<>();

    /**
     * Add an implementation of HttpSessionFactory based on URI pattern and HTTP methods that can be
     * handled by the specified HttpSessionFactory
     *
     * @param uriPattern URI pattern
     * @param httpMethods a set of HTTP methods
     * @param httpSessionFactory an instance of HttpSessionFactory that handles the specified URI
     *      pattern
     */
    public void addHttpSessionFactory(String uriPattern, EnumSet<HttpMethod> httpMethods,
            HttpSessionFactory httpSessionFactory) {
        Pattern pattern = Pattern.compile(wildcardToRegex(uriPattern));
        httpSessionFactoryList.add(new HttpSessionFactoryInfo(pattern, httpMethods, httpSessionFactory));
    }

    @Override
    public HttpSessionFactory getHttpSessionFactory(String uri, HttpMethod httpMethod) {
        for (HttpSessionFactoryInfo httpSessionFactory : httpSessionFactoryList) {
            Matcher matcher = httpSessionFactory.uriPattern.matcher(uri);
            if (matcher.matches()) {
                if (httpSessionFactory.httpMethods.contains(httpMethod)) {
                    return httpSessionFactory.httpSessionFactory;
                }
            }
        }
        return null;
    }

    private String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
            case '*':
                s.append(".*");
                break;
            case '?':
                s.append(".");
                break;
                // escape special regexp-characters
            case '(': case ')': case '[': case ']': case '$':
            case '^': case '.': case '{': case '}': case '|':
            case '\\':
                s.append("\\");
                s.append(c);
                break;
            default:
                s.append(c);
                break;
            }
        }
        s.append('$');
        return(s.toString());
    }
}
