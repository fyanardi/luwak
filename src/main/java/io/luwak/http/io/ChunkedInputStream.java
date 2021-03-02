package io.luwak.http.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Input stream to read chunked message body as specified in RFC 2616 Section 3
 * 
 * @author Fredy Yanardi
 *
 */
public class ChunkedInputStream extends FilterInputStream {

    private static final int READ_BUFFER = 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkedInputStream.class);

    /**
     * This variable holds number of bytes left in current chunk that hasn't been read by the
     * previous read() invocation
     */
    private int chunkLeft;

    /**
     * Creates a ChunkedInputStream from an underlying input stream.
     *
     * @param in an input stream that streams contents in chunked encoding
     */
    public ChunkedInputStream(InputStream in) {
        super(in);
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
        LOGGER.debug("read() chunkLeft={}", chunkLeft);
        if (chunkLeft == 0) {
            chunkLeft = findChunkSize(in);
            LOGGER.debug("new chunk size: {}", chunkLeft);
        }
        else if (chunkLeft == -1) {
            return -1;
        }
        int rlen = 0;
        while (chunkLeft != -1) {
            while (rlen < len && chunkLeft > 0) {
                int read = in.read(b, off + rlen, Math.min(Math.min(READ_BUFFER, len - rlen),
                        chunkLeft));
                if (read == -1) {
                    return rlen > 0 ? rlen : -1;
                }
                else {
                    rlen += read;
                    chunkLeft -= read;
                }
            }
            if (rlen == len) {
                if (chunkLeft == 0) {
                    ensureChunkTermination(in);
                }
                break;
            }
            if (chunkLeft == 0) {
                ensureChunkTermination(in);
                int chunkSize = chunkLeft = findChunkSize(in);
                if (chunkSize == 0) {
                    ensureChunkTermination(in);
                    chunkLeft = -1;
                    break;
                }
            }
        }

        return rlen > 0 ? rlen : -1;
    }

    /**
     * Consume InputStream and make sure the first two bytes are CR + LF.
     *
     * @param in InputStream to be checked
     * @throws IOException thrown if the first two bytes of the InputStream are not CR + LF
     */
    private void ensureChunkTermination(InputStream in) throws IOException {
        int cr = -1;
        int lf = -1;
        if ((cr = in.read()) == -1 || cr != '\r' || (lf = in.read()) == -1 || lf != '\n') {
            throw new IOException("Chunk is not terminated properly");
        }
    }

    private int findChunkSize(final InputStream in) throws IOException {
        // Assume max 32 bytes for chunk size + extension + CRLF
        byte[] buf = new byte[32];
        byte[] pattern = new byte[] { '\r', '\n' };
        int index = 0;
        boolean found = false;
        int rlen = 0;

        int b = -1;
        while ((b = in.read()) != -1) {
            buf[rlen++] = (byte) b;
            if (b == pattern[index]) {
                if (index == pattern.length - 1) {
                    found = true;
                    break;
                }
                index++;
            }
            else {
                index = 0;
            }
        }

        if (found) {
            // Doesn't support extension yet
            byte[] size = new byte[rlen - 2];
            System.arraycopy(buf, 0, size, 0, size.length);
            try {
                return Integer.parseInt(new String(size), 16);
            }
            catch (NumberFormatException e) {
                throw new IOException("Invalid hexadecimal chunk size: \"" + new String(size) + "\"",
                        e);
            }
        }
        return -1;
    }

}
