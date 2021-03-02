package io.luwak.httpd;

import java.net.Socket;

/**
 * Default HTTP Session Factory implementation which will just return a new instance of the
 * Default HTTP Session
 * 
 * @see DefaultHttpSession
 *
 * @author Fredy Yanardi
 *
 */
public class DefaultHttpSessionFactory implements HttpSessionFactory {

    @Override
    public HttpSession newInstance(Socket acceptSocket) {
        return new DefaultHttpSession();
    }
}
