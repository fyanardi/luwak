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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import io.luwak.http.message.DefaultHttpStatus;
import io.luwak.http.message.HttpEntity;
import io.luwak.http.message.HttpMethod;
import io.luwak.http.message.HttpRequest;
import io.luwak.http.message.HttpRequestException;
import io.luwak.http.message.HttpResponse;

/**
 * Class to read incoming HTTP Request and send outgoing HTTP Response.
 *
 * Some part of the codes are adapted from the NanoHTTPD project
 */
public class HttpServerStreams extends HttpStreamsBase {

    /**
     * Constructs a HttpServerStreams from the given input and output streams
     *
     * @param in the input stream
     * @param out the output stream
     */
    public HttpServerStreams(InputStream in, OutputStream out) {
        super(in, out);
    }

    /**
     * Read and parse HTTP request
     * 
     * @return parsed HTTP request
     * @throws IOException
     * @throws SocketException
     * @throws HttpRequestException
     */
    public HttpRequest read() throws IOException, SocketException, HttpRequestException {
        try {
            byte[] header = readHeader();
            // Create a BufferedReader for parsing the header.
            BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(header)));

            return decodeRequestHeader(hin);
        }
        catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw e;
        }
        catch (SocketTimeoutException ste) {
            // treat socket timeouts the same way we treat socket exceptions
            // i.e. close the stream & finalAccept object by throwing the
            // exception up the call stack.
            throw ste;
        }
        catch (IOException ioe) {
            throw new HttpRequestException(DefaultHttpStatus.INTERNAL_SERVER_ERROR,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    public void send(HttpResponse httpResponse) throws IOException {
        // TODO: encoding
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.out, "UTF-8"));
            PrintWriter pw = new PrintWriter(bw, false);

            // System.out.println("HTTP Response: " + httpResponse.getHttpVersion() + " " +
            //        Integer.toString(httpResponse.getStatus().getStatusCode()) + " " + httpResponse.getStatus().getReasonPhrase());
            // Status-Line
            if (httpResponse.getStatus().getReasonPhrase() != null) {
                pw.append(httpResponse.getHttpVersion()).append(" ")
                        .append(Integer.toString(httpResponse.getStatus().getStatusCode()))
                        .append(" ").append(httpResponse.getStatus().getReasonPhrase())
                        .append("\r\n");
            }
            else {
                pw.append(httpResponse.getHttpVersion()).append(" ")
                        .append(Integer.toString(httpResponse.getStatus().getStatusCode()))
                        .append(" \r\n");
            }
            // Header
            // System.out.println("Headers: " + httpResponse.getHeaders().entrySet());
            for (Map.Entry<String, String> entry : httpResponse.getHeaders().entrySet()) {
                pw.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            pw.append("\r\n");
            pw.flush();

            if (httpResponse.getEntityBody() != null) {
                // httpResponse.getHeaders().put("Content-Encoding", "gzip");
                String contentEncoding = httpResponse.getHeaders().get("content-encoding");
                if (contentEncoding != null && contentEncoding.equals("gzip")) {
                    httpResponse.getEntityBody().writeTo(this.out);
                }
                else {
                    httpResponse.getEntityBody().writeTo(this.out);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } 
    }

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     *
     * @return 
     */
    private HttpRequest decodeRequestHeader(BufferedReader in) throws HttpRequestException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return null;
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new HttpRequestException(DefaultHttpStatus.BAD_REQUEST,
                        "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            String method = st.nextToken();
            HttpMethod httpMethod = HttpMethod.fromString(method);
            if (httpMethod == null) {
                throw new HttpRequestException(DefaultHttpStatus.BAD_REQUEST,
                        "BAD REQUEST: Syntax error. HTTP verb " + method + " unhandled.");
            }

            if (!st.hasMoreTokens()) {
                throw new HttpRequestException(DefaultHttpStatus.BAD_REQUEST,
                        "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            String uri = st.nextToken();
            Map<String, String> parms = new LinkedHashMap<>();
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParms(uri.substring(qmi + 1), parms);
                uri = uri.substring(0, qmi); // decodePercent(uri.substring(0, qmi));
            }

            String protocolVersion = null;
            // If there's another token, its protocol version, followed by HTTP headers.
            // NOTE: this now forces header names lower case since they are case insensitive and
            // vary by client.
            if (st.hasMoreTokens()) {
                protocolVersion = st.nextToken();
            }
            else {
                protocolVersion = "HTTP/1.1";
                System.out.println("No protocol version specified, strange. Assuming HTTP/1.1.");
            }

            Map<String, String> headers = new HashMap<String, String>();
            String line = in.readLine();
            while (line != null && !line.trim().isEmpty()) {
                int p = line.indexOf(':');
                if (p >= 0) {
                    headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                }
                line = in.readLine();
            }

            // Read HTTP Request body (if any) based on information from the HTTP header
            HttpEntity body = readHttpRequestBody(headers);
            return new HttpRequest(httpMethod, uri, parms, protocolVersion, headers, body);
        }
        catch (IOException ioe) {
            throw new HttpRequestException(DefaultHttpStatus.INTERNAL_SERVER_ERROR, 
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    private HttpEntity readHttpRequestBody(Map<String, String> headers) throws IOException {
        long contentLength = -1;
        boolean gzip = false;
        boolean chunked = false;

        if (headers.containsKey("content-length")) {
            contentLength = Long.parseLong(headers.get("content-length"));
        }
        if (headers.containsKey("content-encoding")) {
            String contentEnc = headers.get("content-encoding");
            gzip = contentEnc.equalsIgnoreCase("gzip");
        }
        if (headers.containsKey("transfer-encoding")) {
            String transferEncoding = headers.get("transfer-encoding");
            chunked = transferEncoding.equalsIgnoreCase("chunked");
        }

        if (contentLength != -1) {
            return HttpEntity.fromInputStream(this.in, contentLength, gzip);
        }
        if (chunked) {
            return HttpEntity.fromChunkedInputStream(this.in, gzip);
        }
        // Neither content-length nor transfer-encoding=chunked available, assume no body & return null
        return null;
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
     * Map. NOTE: this doesn't support multiple identical keys due to the
     * simplicity of Map.
     */
    private static void decodeParms(String parms, Map<String, String> p) {
        if (parms == null) {
            return;
        }

        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
            } else {
                p.put(decodePercent(e).trim(), "");
            }
        }
    }

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
