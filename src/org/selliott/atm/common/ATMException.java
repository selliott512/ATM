package org.selliott.atm.common;

/**
 * The main exception class for the ATM application.
 */
public class ATMException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new ATMException
     */
    public ATMException() {
        super();
    }

    /**
     * Create a new ATMException
     * 
     * @param message
     *            Exception message
     */
    public ATMException(String message) {
        super(message);
    }

    /**
     * Create a new ATMException
     * 
     * @param message
     *            Exception message
     * @param cause
     *            Exception that caused this one
     */
    public ATMException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new ATMException
     * 
     * @param cause
     *            Exception that caused this one
     */
    public ATMException(Throwable cause) {
        super(cause);
    }
}
