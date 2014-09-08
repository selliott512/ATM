package org.selliott.atm.server;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.selliott.atm.common.ATMException;
import org.selliott.atm.common.Config;

/**
 * Data abstraction layer. It's assumed that using callable statements is a
 * sufficient abstraction.
 */
public class DAL {
    private static DAL dal;
    private static final Logger log = Logger.getLogger(DAL.class);

    public static synchronized DAL getInstance() throws ATMException {
        if (dal == null) {
            dal = new DAL();
        }
        return dal;
    }

    private Connection conn;

    // Callable statements.
    private CallableStatement depositCS;
    private CallableStatement getBalanceCS;
    private CallableStatement verifyPINCS;

    /**
     * Create a new DAL.
     * 
     * @throws ATMException
     *             Application level error.
     */
    private DAL() throws ATMException {
        try {
            conn = DriverManager.getConnection(Config.cfg.dbURL);
            // Handle transactions explicitly.
            conn.setAutoCommit(false);

            // Initialize callable statements.
            getBalanceCS = conn.prepareCall("{? = CALL get_balance(?, ?)}");
            depositCS = conn.prepareCall("{? = CALL deposit(?, ?, ?)}");
            verifyPINCS = conn.prepareCall("{? = CALL verify_pin(?, ?)}");
        } catch (SQLException e) {
            throw new ATMException(
                    "Unable to create a connection to the database", e);
        }
    }

    /**
     * Commit the database connection. Typically there is one transaction per
     * command.
     * 
     * @throws ATMException
     *             Application level error.
     */
    public void commit() throws ATMException {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new ATMException("Unable to commit.", e);
        }
    }

    /**
     * Deposit the specified amount of money. Note that this is also used for
     * withdraws, but a negative amount.
     * 
     * @param num
     *            Account number
     * @param pin
     *            PIN
     * @param amount
     *            Amount to deposit.
     * @return Balance after the deposit.
     * @throws ATMException
     *             An application level error.
     */
    public BigDecimal deposit(String num, String pin, BigDecimal amount)
            throws ATMException {
        try {
            depositCS.registerOutParameter(1, Types.NUMERIC);
            depositCS.setString(2, num);
            depositCS.setString(3, pin);
            depositCS.setBigDecimal(4, amount);
            depositCS.execute();
            return depositCS.getBigDecimal(1);
        } catch (SQLException e) {
            throw new ATMException("Unable to deposit.", e);
        }
    }

    protected void finalize() {
        // TODO: Come up with a more reliable way for the client to close the
        // connection.
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Could not close the connection: " + e);
            }
        }
    }

    /**
     * Get the balance.
     * 
     * @param num
     *            Account number
     * @param pin
     *            PIN
     * @return The balance.
     * @throws ATMException
     *             An application level error.
     */
    public BigDecimal getBalance(String num, String pin) throws ATMException {
        try {
            getBalanceCS.registerOutParameter(1, Types.NUMERIC);
            getBalanceCS.setString(2, num);
            getBalanceCS.setString(3, pin);
            getBalanceCS.execute();
            return getBalanceCS.getBigDecimal(1);
        } catch (SQLException e) {
            throw new ATMException("Unable to get the balance.", e);
        }
    }

    /**
     * Rollback the current transaction.
     * 
     * @throws ATMException
     *             An application level error.
     */
    public void rollback() throws ATMException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new ATMException("Unable to rollabck.", e);
        }
    }

    /**
     * Verify the PIN. TODO: Make a distinction between various errors (DB not
     * listening, etc.) and the PIN actually being wrong.
     * 
     * @param num
     *            Account number
     * @param pin
     *            PIN
     * @return True if the PIN is valid.
     * @throws ATMException
     *             Any error (DB connectivity, etc.)
     */
    public boolean verifyPIN(String num, String pin) throws ATMException {
        try {
            verifyPINCS.registerOutParameter(1, Types.BOOLEAN);
            verifyPINCS.setString(2, num);
            verifyPINCS.setString(3, pin);
            verifyPINCS.execute();
            return verifyPINCS.getBoolean(1);
        } catch (SQLException e) {
            throw new ATMException("Unable to verify the PIN.", e);
        }
    }
}
