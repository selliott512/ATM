package org.selliott.atm.server;

import java.math.BigDecimal;
import java.util.Map;

import org.selliott.atm.common.ATMException;

/**
 * Implementation of the command that gets the balance.
 */
public class GetBalanceImpl extends CommandImpl {
    static final String NAME = "get-balance";

    /**
     * Create a new GetBalanceImpl.
     */
    GetBalanceImpl() {
        super(NAME);
    }

    @Override
    public Result process(Map<String, String> commandFields)
            throws ATMException {
        DAL dal = DAL.getInstance();
        BigDecimal balance = dal.getBalance(commandFields.get("num"),
                commandFields.get("pin"));
        result.setField("balance", "" + balance);
        return result;
    }
}
