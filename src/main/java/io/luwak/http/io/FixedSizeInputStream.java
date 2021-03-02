package io.luwak.http.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream to read fixed sized message body, once the full message is read, this input stream
 * will stop reading the underlying input stream.
 * 
 * @author Fredy Yanardi
 *
 */
public class FixedSizeInputStream extends FilterInputStream {

    private long size;

    /**
     * Create a FixedSizeInputStream from an underlying input stream.
     *
     * @param in an input stream that streams contents in a fixed size byte array
     */
    public FixedSizeInputStream(InputStream in, long size) {
        super(in);
        this.size = size;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int rlen = read(b);
        return rlen != -1 ? (((int) b[0]) & 0xFF) : -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (size < 0) {
            return -1;
        }
        int rlen = in.read(b, 0, (int) Math.min(size, b.length));
        if (rlen > 0) {
            size -= rlen;
            byte[] buffer = new byte[rlen];
            System.arraycopy(b, 0, buffer, 0, rlen);
            return rlen;
        }
        else {
            size = -1;
            return -1;
        }
    }

}
