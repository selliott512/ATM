package org.selliott.atm.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.ATMException;

/**
 * In Intermediate representation of commands that is helpful when reading
 * commands via SAX.
 */
public class Command {
    static Map<String, CommandImpl> implMap = new HashMap<String, CommandImpl>();
    private static final Logger log = Logger.getLogger(Main.class);
    static {
        implMap.put(GetBalanceImpl.NAME, new GetBalanceImpl());
        implMap.put(DepositImpl.NAME, new DepositImpl());
        implMap.put(WithdrawImpl.NAME, new WithdrawImpl());
    }
    public ATMError error = ATMError.SUCCESS; // public for JUnit

    Map<String, String> fieldMap = new HashMap<String, String>();
    String name;

    /**
     * Create a new command given the command's name.
     * 
     * @param name
     *            The name.
     */
    public Command(String name) {
        this.name = name;
    }

    /**
     * Process the command. This means to do whatever needs to be done for the
     * command and then produce a result.
     * 
     * @return The result.
     * @throws ATMException
     *             Application level error.
     */
    public Result process() throws ATMException {
        CommandImpl commandImpl = implMap.get(name);
        if (commandImpl == null) {
            throw new ATMException("Uknown command \"" + name + "\".");
        }
        Result result = null;
        DAL dal = DAL.getInstance();
        try {
            if (!dal.verifyPIN(fieldMap.get("num"), fieldMap.get("pin"))) {
                error = ATMError.INVALID_PIN;
                // No need to throw an exception for this relatively common
                // error.
                log.error("Invalid PIN entered for account \""
                        + fieldMap.get("num") + "\".");
                return null;
            }
            result = commandImpl.process(fieldMap);
            dal.commit();
        } catch (ATMException e) {
            log.error("Unable to process a command.  Rollback: " + e);
            error = commandImpl.geResult().getError();
            dal.rollback();
        }
        return result;
    }

    /**
     * Add a name/value pair to the command. This corresponds to "field"
     * elements.
     * 
     * @param name
     *            The name of the field.
     * @param value
     *            The value of the field.
     */
    public void setField(String name, String value) {
        fieldMap.put(name, value);
    }
}
