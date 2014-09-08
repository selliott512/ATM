package org.selliott.atm.common;

/**
 * Common types of errors that may be sent form the server to the client.
 */
public enum ATMError {
    CLIENT("Client side error"), CONNECTIVITY(
            "Unable to communicate with the server"), INSUFFICIENT_FUNDS(
            "Insufficient funds"), INVALID_PIN(
            "Invalid account and PIN combination"), SUCCESS("Success"), XML_FORMATTING(
            "Received XML that was not correctly formatted");

    private String msg;

    /**
     * Create a new ATM error given a descriptive message.
     * 
     * @param msg
     *            The description.
     */
    private ATMError(String msg) {
        this.msg = msg;
    }

    /**
     * Get the description that includes both the message and the message
     * identifier.
     * 
     * @return The description.
     */
    public String getDescription() {
        return msg + " (error: " + this + ")";
    }

    /**
     * Get the message.
     * 
     * @return The message.
     */
    public String getMsg() {
        return msg;
    }
}
