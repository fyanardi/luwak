package io.luwak.http.message;

/**
 * Interface for all HTTP status codes
 *
 * @author Fredy Yanardi
 *
 */
public interface HttpStatus {

    /**
     * Get the numeric HTTP status code
     *
     * @return the numeric HTTP status code
     */
    public int getStatusCode();

    /**
     * Return the Reason-Phrase of this HTTP status code. The Reason-Phrase is intended to give a
     * short textual description of the Status-Code.
     *
     * @return the reason phrase of this HTTP status code
     */
    public String getReasonPhrase();

}
