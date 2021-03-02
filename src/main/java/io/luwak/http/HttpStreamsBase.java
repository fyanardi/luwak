package io.luwak.http;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Parent class for HTTP Request and Response Streams 
 *
 * Some part of the codes are adapted from the NanoHTTPD project
 *
 */
public abstract class HttpStreamsBase {

    /** Maximum HTTP Header size */
    public static final int BUFSIZE = 8192;
    public static final int MEMORY_STORE_LIMIT = 500 * 1024;
    public static final int REQUEST_BUFFER_LEN = 512;

    protected InputStream in;
    protected OutputStream out;

    public HttpStreamsBase(InputStream in, OutputStream out) {
        this.in = new BufferedInputStream(in, BUFSIZE);
        this.out = out;
    }

    protected byte[] readHeader() throws IOException {
        try {
            // Read the first 8192 bytes.
            // The full header should fit in here.
            // Apache's default header limit is 8KB.
            // Do NOT assume that a single read will get the entire header at once!
            byte[] buf = new byte[BUFSIZE];
            int splitbyte = 0;
            int rlen = 0;

            int read = -1;
            this.in.mark(BUFSIZE);
            try {
                read = this.in.read(buf, 0, BUFSIZE);
            } catch (IOException e) {
                this.in.close();
                throw e;
            }

            if (read == -1) {
                // socket has been closed
                this.in.close();
                throw new SocketException("Remote host closes socket connection");
            }

            while (read > 0) {
                rlen += read;
                splitbyte = findHeaderEnd(buf, rlen);
                if (splitbyte > 0) {
                    break;
                }
                read = this.in.read(buf, rlen, BUFSIZE - rlen);
            }

            System.out.println("rlen: " + rlen + ", splitbyte: " + splitbyte);
            if (splitbyte < rlen) {
                this.in.reset();
                this.in.skip(splitbyte);
            }

            byte[] header = new byte[splitbyte];
            System.arraycopy(buf, 0, header, 0, splitbyte);
            return header;
        } catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw e;
        } catch (SocketTimeoutException ste) {
            // treat socket timeouts the same way we treat socket exceptions
            // i.e. close the stream & finalAccept object by throwing the
            // exception up the call stack.
            throw ste;
        }
    }

    protected final void safeClose(Object closeable) {
        try {
            if (closeable != null) {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else if (closeable instanceof Socket) {
                    ((Socket) closeable).close();
                } else if (closeable instanceof ServerSocket) {
                    ((ServerSocket) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not close");
            e.printStackTrace();
        }
    }

    /**
     * Find byte index separating header from body. It must be the last byte of the first two sequential new lines.
     *
     */
    private static int findHeaderEnd(final byte[] buf, int rlen) {
        int splitbyte = 0;
        while (splitbyte + 1 < rlen) {

            // RFC2616
            if (buf[splitbyte] == '\r' && buf[splitbyte + 1] == '\n' && splitbyte + 3 < rlen &&
                    buf[splitbyte + 2] == '\r' && buf[splitbyte + 3] == '\n') {
                return splitbyte + 4;
            }

            // tolerance
            if (buf[splitbyte] == '\n' && buf[splitbyte + 1] == '\n') {
                return splitbyte + 2;
            }
            splitbyte++;
        }
        return 0;
    }
}
