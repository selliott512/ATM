package org.selliott.atm.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.ATMException;
import org.selliott.atm.common.Config;
import org.selliott.atm.common.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An interface for the client to talk to the server. This class does not throw
 * exceptions for commands. Instead it returns null for the balance in the error
 * case combined with setting "error" to the appropriate error. NOTE: Instances
 * of this class are not thread safe.
 */
public class Client {
    private static final Logger log = Logger.getLogger(Client.class);
    private ATMError atmError = ATMError.SUCCESS;
    private Element atmRequestEl;
    private Element commandEl;
    private Document request;

    /**
     * Add a name/value pair.
     * 
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    void addField(String name, String value) {
        Element feildEl = request.createElement("field");
        feildEl.setAttribute("name", name);
        feildEl.setAttribute("value", value);
        commandEl.appendChild(feildEl);
    }

    /**
     * Create a rew request element.
     * 
     * @throws ATMException
     *             The XML API failed to create a new document.
     */
    private void createRequest() throws ATMException {
        request = Util.newXmlDoc();
        atmRequestEl = request.createElement("atmRequest");
        request.appendChild(atmRequestEl);
        commandEl = request.createElement("command");
        atmRequestEl.appendChild(commandEl);
    }

    /**
     * Deposit funds.
     * 
     * @param num
     *            The account number.
     * @param pin
     *            The PIN.
     * @param amount
     *            The amount to deposit.
     * @return The new balance.
     */
    public BigDecimal deposit(String num, String pin, BigDecimal amount) {
        try {
            atmError = ATMError.SUCCESS;
            createRequest();
            // TODO: It would be nice if it was possible to refer to
            // DepositImpl.NAME here, but the client and server are in separate
            // JARs.s
            commandEl.setAttribute("name", "deposit");

            addField("num", num);
            addField("pin", pin);
            addField("amount", "" + amount);

            return sendRequest();
        } catch (Exception e) {
            return processEx(e);
        }
    }

    /**
     * The error from the last communication.
     * 
     * @return
     */
    public ATMError getATMError() {
        return atmError;
    }

    /**
     * Get the balance.
     * 
     * @param num
     *            The account number.
     * @param pin
     *            The PIN.
     * @return The balance.
     */
    public BigDecimal getBalance(String num, String pin) {
        try {
            atmError = ATMError.SUCCESS;
            createRequest();
            commandEl.setAttribute("name", "get-balance");

            addField("num", num);
            addField("pin", pin);

            return sendRequest();
        } catch (Exception e) {
            return processEx(e);
        }
    }

    /**
     * Parse the response XML and extract the balance. Successfully doing so
     * assures the the server responded in a valid way.
     * 
     * @param response
     *            The balance, if it could be extracted, or null.
     * @return
     */
    private BigDecimal getBalanceFromResponse(Document response) {
        Element resultEl = (Element) response.getElementsByTagName("result")
                .item(0);
        String error = resultEl.getAttribute("error");
        try {
            atmError = ATMError.valueOf(error);
        } catch (IllegalArgumentException e) {
            atmError = ATMError.XML_FORMATTING;
        }
        if (atmError != ATMError.SUCCESS) {
            log.error("Result had non-zero error " + atmError);
            return null;
        }

        NodeList fields = resultEl.getElementsByTagName("field");
        for (int i = 0; i < fields.getLength(); i++) {
            Element fieldEl = (Element) fields.item(i);
            if (fieldEl.getAttribute("name").equals("balance")) {
                String balance = fieldEl.getAttribute("value");
                return new BigDecimal(balance);
            }
        }
        // We should have found at least one balance element.
        return null;
    }

    /**
     * Process exceptions encountered when attempting commands. If "error" has
     * not been set it's set based on the exception caught. TODO: It's a bit odd
     * that null is always returned, but it's helpful.
     * 
     * @param e
     *            Exception encountered
     * @return Null for the balance.
     */
    private BigDecimal processEx(Exception e) {
        log.error("Unable to process command.", e);
        if (atmError == ATMError.SUCCESS) {
            if (e instanceof IOException || e instanceof ConnectException) {
                atmError = ATMError.CONNECTIVITY;
            } else {
                atmError = ATMError.CLIENT;
            }
        }
        return null;
    }

    /**
     * Send the previously formed request.
     * 
     * @return The balance after the transaction.
     * @throws IOException
     *             Low level communication issue.
     * @throws MalformedURLException
     *             In correct URL.
     * @throws ATMException
     *             Other application level error.
     */
    private BigDecimal sendRequest() throws IOException, MalformedURLException,
            ATMException {
        URLConnection conn = new URL(Config.cfg.serverURL).openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/xml");

        try (OutputStream out = conn.getOutputStream();) {
            Util.xmlFormat(request, new BufferedWriter(new OutputStreamWriter(
                    out)));
        }

        try (InputStream in = conn.getInputStream()) {
            Document response = Util.xmlDocParse(in);
            return getBalanceFromResponse(response);
        }
    }

    /**
     * Withdraw funds.
     * 
     * @param num
     *            The account number.
     * @param pin
     *            The PIN.
     * @param amount
     *            The amount to withdraw.
     * @return The new balance.
     */
    public BigDecimal withdraw(String num, String pin, BigDecimal amount) {
        try {
            atmError = ATMError.SUCCESS;
            createRequest();
            commandEl.setAttribute("name", "withdraw");

            addField("num", num);
            addField("pin", pin);
            addField("amount", "" + amount);

            return sendRequest();
        } catch (Exception e) {
            return processEx(e);
        }
    }
}
