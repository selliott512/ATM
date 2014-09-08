package org.selliott.atm.server;

import java.math.BigDecimal;
import java.util.Map;

import org.selliott.atm.common.ATMException;

/**
 * Implementation of the command that withdraws money.
 */
public class DepositImpl extends CommandImpl {

    static final String NAME = "deposit";

    /**
     * Create a new DepositImpl.
     */
    DepositImpl() {
        super(NAME);
    }

    @Override
    public Result process(Map<String, String> commandFields)
            throws ATMException {
        DAL dal = DAL.getInstance();
        BigDecimal balance = dal.deposit(commandFields.get("num"),
                commandFields.get("pin"),
                new BigDecimal(commandFields.get("amount")));
        result.setField("balance", "" + balance);
        return result;
    }
}
