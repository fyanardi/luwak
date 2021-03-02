package io.luwak.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import io.luwak.http.message.HttpEntity;
import io.luwak.http.message.HttpRequest;
import io.luwak.http.message.HttpResponse;
import io.luwak.http.message.HttpResponseException;
import io.luwak.http.message.HttpStatus;

/**
 * Class to send outgoing HTTP Request and read incoming HTTP Response.
 *
 * Some part of the codes are adapted from the NanoHTTPD project
 */
public class HttpClientStreams extends HttpStreamsBase {

    public HttpClientStreams(InputStream in, OutputStream out) {
        super(in, out);
    }

    /**
     * Send a HTTP request specified by the argument
     *
     * @param httpRequest the HTTP request to be sent
     * @throws IOException when IO exception occurs while trying to send the HTTP request
     */
    public void send(HttpRequest httpRequest) throws IOException {
        // TODO: encoding
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out, "UTF-8"));
            PrintWriter pw = new PrintWriter(bw, false);

            // Request-Line
            pw.append(httpRequest.getMethod().toString()).append(" ")
                    .append(httpRequest.getRequestUri()).append(" ")
                    .append(httpRequest.getHttpVersion()).append("\r\n");

            // Request-Headers
            for (Map.Entry<String, String> entry : httpRequest.getHeaders().entrySet()) {
                pw.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            pw.append("\r\n");
            pw.flush();

            if (httpRequest.getEntityBody() != null) {
                // System.out.println("Sending entity body");
                httpRequest.getEntityBody().writeTo(this.out);
            }
            // System.out.println("Request sent");

            this.out.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse read() throws IOException, HttpResponseException {
        try {
            byte[] header = readHeader();

            // Create a BufferedReader for parsing the header.
            InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(header));
            BufferedReader hin = new BufferedReader(isr);

            return decodeHeader(hin);
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

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     *
     * @return 
     */
    private HttpResponse decodeHeader(BufferedReader in) throws HttpResponseException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return null;
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new HttpResponseException("BAD RESPONSE: Syntax error, missing Status-Line");
            }

            String protocolVersion = st.nextToken();

            if (!st.hasMoreTokens()) {
                throw new HttpResponseException("BAD RESPONSE: Syntax error, missing Status-Code");
            }

            String statusCodeStr = st.nextToken();
            int statusCode = 0;
            try {
                statusCode = Integer.parseInt(statusCodeStr);
            } catch (Exception e) {
                throw new HttpResponseException("BAD RESPONSE: Syntax error, non-numeric Status-Code");
            }

            if (!st.hasMoreTokens()) {
                throw new HttpResponseException("BAD RESPONSE: Syntax error, missing Reason-Phrase");
            }
            String reasonPhrase = st.nextToken();

            final int finalStatusCode = statusCode;

            HttpStatus status = new HttpStatus() {

                @Override
                public int getStatusCode() {
                    return finalStatusCode;
                }

                @Override
                public String getReasonPhrase() {
                    return reasonPhrase;
                }
            };

            Map<String, String> headers = new LinkedHashMap<>();
            String line = in.readLine();
            while (line != null && !line.trim().isEmpty()) {
                int p = line.indexOf(':');
                if (p >= 0) {
                    headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                }
                line = in.readLine();
            }

            HttpEntity body = readHttpRequestBody(headers);
            return new HttpResponse(protocolVersion, status, headers, body); 
        }
        catch (IOException ioe) {
            throw new HttpResponseException("SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Read http message body from the input stream.
     *
     * @param headers
     * @return
     * @throws IOException
     */
    public HttpEntity readHttpRequestBody(Map<String, String> headers) throws IOException {
        long contentLength = -1;
        boolean gzip = false;
        boolean chunked = false;

        if (headers.containsKey("content-length")) {
            contentLength = Long.parseLong(headers.get("content-length"));
        }
        if (headers.containsKey("content-encoding")) {
            String contentEnc = headers.get("content-encoding");
            System.out.println("Content encoding: " + contentEnc);
            gzip = contentEnc.equalsIgnoreCase("gzip");
        }
        if (headers.containsKey("transfer-encoding")) {
            String transferEncoding = headers.get("transfer-encoding");
            chunked = transferEncoding.equalsIgnoreCase("chunked");
        }

        // No body
        if (contentLength == -1 && !chunked) {
            return null;
        }

        if (contentLength != -1) {
            return HttpEntity.fromInputStream(this.in, contentLength, gzip);
        }
        if (chunked) {
            return HttpEntity.fromChunkedInputStream(this.in, gzip);
        }
        // Neither content-length nor transfer-encoding=chunked available, assume no body
        return null;
    }

    /**
     * Deduce body length in bytes. Either from "content-length" header or
     * read bytes.
     */
    /*private long getBodySize(Map<String, String> headers) {
        if (headers.containsKey("content-length")) {
            return Long.parseLong(headers.get("content-length"));
        }
        else if (this.splitbyte < this.rlen) {
            return this.rlen - this.splitbyte;
        }
        return 0;
    }*/

    /**
     * Decode percent encoded String values.
     * 
     * @param str the percent encoded String
     * @return expanded form of the input, for example "foo%20bar" becomes "foo bar"
     */
    protected static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            System.err.println("Encoding not supported, ignored");
            ignored.printStackTrace();
        }
        return decoded;
    }
}
