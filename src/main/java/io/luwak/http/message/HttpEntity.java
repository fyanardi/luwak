package io.luwak.http.message;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.luwak.http.io.ChunkedInputStream;
import io.luwak.http.io.ChunkedOutputStream;
import io.luwak.http.io.FixedSizeInputStream;

/**
 * A representation of an HTTP Entity (body) message. The body message may come from either a fixed
 * size byte array or a file. The entity may be sent in uncompressed or gzip compressed. Furthermore,
 * message may also be sent in chunked encoding.
 *
 * @author Fredy Yanardi
 *
 */
public class HttpEntity {

    private static final int BUFFER_SIZE = 2 * 1024;
    private static final int MEMORY_CACHE_LIMIT = 100 * 1024;
    private static final int REQUEST_BUFFER_LEN = 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEntity.class);

    private final boolean chunked;
    private boolean gzip;

    private byte[] outBytes;
    private File outFile;

    private File contentFile;
    private byte[] contentBytes;

    /**
     * Create a new HttpEntity from the given file. The file contains un-compressed and un-chunked
     * data. The other parameters specify whether the content needs to be chunked or gzip compressed
     * when it is written to the output stream (via {@link #writeTo(OutputStream)}.
     *
     * @param file file content
     * @param chunked whether the content is to be chunked
     * @param gzip whether the content is to be gzip compressed
     * @throws FileNotFoundException if the specified file does not exist
     */
    public HttpEntity(File file, boolean chunked, boolean gzip) throws FileNotFoundException {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException(
                    file.getName() + " (The system cannot find the file specified)");
        }
        this.contentFile = file;
        this.chunked = chunked;
        this.gzip = gzip;
    }

    /**
     * Create a new HttpEntity from the given byte array. The byte array contains un-compressed and
     * un-chunked data. The other parameters specify whether the content needs to be chunked or gzip
     * compressed when it is written to the output stream (via {@link #writeTo(OutputStream)}.
     *
     * @param buffer byte array content
     * @param chunked whether the content is to be chunked
     * @param gzip whether the content is to be gzip compressed
     */
    public HttpEntity(byte[] buffer, boolean chunked, boolean gzip) {
        if (buffer == null) {
            throw new NullPointerException("buffer is null");
        }
        this.contentBytes = buffer;
        this.chunked = chunked;
        this.gzip = gzip;
    }

    /**
     * Return true if this HTTP Entity is chunked, false otherwise.
     * 
     * @return true if this HTTP Entity is chunked, false otherwise.
     */
    public boolean isChunked() {
        return chunked;
    }

    /**
     * Return true if this HTTP Entity is gzip compressed, false otherwise.
     * 
     * @return true if this HTTP Entity is gzip compressed, false otherwise.
     */
    public boolean isGzip() {
        return gzip;
    }

    /**
     * Get the content of the entity. It is the responsibility of the caller to close the returned
     * input stream once the content has been read successfully. This method will always return a
     * new input stream based on the underlying message (byte array or file).
     *
     * @return content input stream
     */
    public InputStream getContent() {
        try {
            return contentBytes != null ?
                    new ByteArrayInputStream(contentBytes) : new FileInputStream(contentFile);
        }
        catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * Clear any temporary file cache associated with this entity. 
     */
    public void clearTempCache() {
        if (outFile != null) {
            try {
                outFile.delete();
            }
            catch (Exception e) {
            }
            outFile = null;
        }
    }

    /**
     * Return content-length for this entity based on the actual content to be transferred. Hence if
     * this entity is to be gzip compressed, the length returned will the length of the
     * gzip-compressed content.
     *
     * If the content is to be transferred in chunked encoding, this method will return -1.
     *
     * @return content-length or -1 if the content is to be transferred in chunked encoding
     * @throws IOException if there is an exception while calculating the content length
     */
    public long getLength() throws IOException {
        if (chunked) {
            return -1;
        }

        ensureCache();

        if (outBytes != null) {
            return outBytes.length;
        }
        else if (this.outFile != null) {
            return outFile.length();
        }

        return -1;
    }

    private void ensureCache() throws IOException {
        if (outBytes == null && outFile == null) {
            InputStream in = null;
            OutputStream out = null;
            ByteArrayOutputStream baos = null;

            if (contentBytes != null) {
                in = new ByteArrayInputStream(contentBytes);
                baos = new ByteArrayOutputStream(MEMORY_CACHE_LIMIT);
                out = gzip ? new GZIPOutputStream(baos) : baos;
            }
            else {
                if (gzip) {
                    in = new FileInputStream(contentFile);

                    outFile = getTmpFile(); 
                    FileOutputStream fout = new FileOutputStream(outFile);
                    out = gzip ? new GZIPOutputStream(fout) : fout;
                }
                else {
                    outFile = contentFile;
                }
            }

            if (in != null) {
                byte[] buffer = new byte[REQUEST_BUFFER_LEN];
                int n = 0;
                while (-1 != (n = in.read(buffer))) {
                    out.write(buffer, 0, n);
                }

                if (baos != null) {
                    outBytes = baos.toByteArray();
                }

                out.flush();
                out.close();
                in.close();
            }
        }
    }

    /**
     * Transfers (write) the content of this entity into the specified output stream. This method
     * will write to the output stream correctly based on the transfer encoding (chunked/non-chunked)
     * and compression method (gzip/non-compressed) for this entity.
     *
     * @param out the output stream to write this entity to
     * @throws IOException
     */
    public void writeTo(OutputStream out) throws IOException {
        try {
            LOGGER.debug("Write to {} chunked={} gzip={}", out, chunked, gzip);

            ensureCache();

            InputStream in = null;
            if (outBytes != null) {
                in = new ByteArrayInputStream(outBytes);
            }
            else if (outFile != null) {
                in = new FileInputStream(outFile);
            }

            OutputStream os = chunked ? new ChunkedOutputStream(out) : out;

            byte[] buff = new byte[BUFFER_SIZE];

            int read = -1;
            while ((read = in.read(buff)) != -1) {
                os.write(buff, 0, read);
                os.flush();
            }

            if (chunked) {
                ((ChunkedOutputStream) os).finish();
            }

            os.flush();
            in.close();

            // clearTempCache();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a chunked HTTP entity and return a new HttpEntity instance which contains the entity in
     * non-chunked and non-gzipped format.
     * 
     * @param in the InputStream that is holding the HTTP Entity
     * @param gzip whether the InputStream contains gzipped chunked entity
     * @return a new HttpEntity instance that contains the non-chunked message
     * @throws IOException if IOException occurs during reading of the entity
     */
    public static HttpEntity fromChunkedInputStream(InputStream in, boolean gzip)
            throws IOException {
        ChunkedInputStream chunkedIn = new ChunkedInputStream(in);

        File tmpFile = getTmpFile();

        InputStream contentIn = gzip ? new GZIPInputStream(chunkedIn) : chunkedIn;
        FileOutputStream fout = new FileOutputStream(tmpFile);

        int rlen = 0;
        byte[] buf = new byte[REQUEST_BUFFER_LEN];
        while (rlen >= 0) {
            rlen = contentIn.read(buf);
            if (rlen > 0) {
                fout.write(buf, 0, rlen);
            }
        }

        contentIn.close();
        fout.close();

        return new HttpEntity(new File(tmpFile.getAbsolutePath()), true, gzip);
    }

    public static HttpEntity fromInputStream(InputStream in, long size, boolean gzip)
            throws IOException {
        File tmpFile = null;
        OutputStream out = null;

        // If the content is gzipped, the FixedSizeInputStream must be enclosed in another
        // BufferedInputStream, Otherwise GZIPInputStream will fail with this exception:
        // java.util.zip.ZipException: invalid block type
        //     at java.util.zip.InflaterInputStream.read(InflaterInputStream.java:164)
        //     at java.util.zip.GZIPInputStream.read(GZIPInputStream.java:117)
        //     at com.ibm.taiss.http.messages.entity.HttpEntity.fromInputStream(HttpEntity.java:301)
        FixedSizeInputStream fixedIn = new FixedSizeInputStream(in, size);
        InputStream contentIn = gzip ?
                new GZIPInputStream(new BufferedInputStream(fixedIn)) : fixedIn;

        // Store the request in memory or a file, depending on size.
        // For gzip, force into file (as depending on the content, gzipped content might achieve
        // very high compression ratio
        if (size < MEMORY_CACHE_LIMIT && !gzip) {
            out = new ByteArrayOutputStream();
        }
        else {
            tmpFile = getTmpFile();
            out = new FileOutputStream(tmpFile);
        }

        // Read all the body and write it to request_data_output
        int rlen = 0;
        byte[] buf = new byte[REQUEST_BUFFER_LEN];
        while (rlen >= 0) {
            rlen = contentIn.read(buf, 0, REQUEST_BUFFER_LEN);
            if (rlen > 0) {
                out.write(buf, 0, rlen);
            }
        }

        contentIn.close();
        out.close();

        if (out instanceof ByteArrayOutputStream) {
            byte[] content = ((ByteArrayOutputStream) out).toByteArray();
            return new HttpEntity(content, false, gzip);
        }
        else {
            // randomAccessFile.close();
            return new HttpEntity(new File(tmpFile.getAbsolutePath()), false, gzip);
        }
    }

    private static File getTmpFile() throws IOException {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        if (!tmpdir.exists()) {
            tmpdir.mkdirs();
        }
        File tmpFile = File.createTempFile(HttpEntity.class.getSimpleName(), null, tmpdir);
        return tmpFile;
    }
}
