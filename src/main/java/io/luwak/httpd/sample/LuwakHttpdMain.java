package io.luwak.httpd.sample;

import java.util.EnumSet;

import io.luwak.http.message.HttpMethod;
import io.luwak.httpd.DefaultHttpSessionFactory;
import io.luwak.httpd.DefaultHttpdContext;
import io.luwak.httpd.LuwakHttpd;

/**
 * A main method for a simple plain web server that serves files from a specified root location
 *
 * @author Fredy Yanardi
 *
 */
public class LuwakHttpdMain {

    public static void main(String[] args) {
        // Create a default httpd context with the default http session factory
        DefaultHttpdContext httpdContext = new DefaultHttpdContext();
        httpdContext.addHttpSessionFactory("*", EnumSet.of(HttpMethod.GET),
                new DefaultHttpSessionFactory());

        /* If SSL is needed
        System.setProperty("javax.net.ssl.keyStore", "res\\keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "p@ssw0rd");
        */

        // Start a HTTP server at localhost port 8080 with max 100 threads
        LuwakHttpd httpd = new LuwakHttpd("localhost", 8080, 100, httpdContext);
        httpd.start();
    }
}
