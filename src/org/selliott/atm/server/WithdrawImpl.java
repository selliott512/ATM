package org.selliott.atm.server;

import java.math.BigDecimal;
import java.util.Map;

import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.ATMException;

/**
 * Implementation of the command that withdraws money.
 */
public class WithdrawImpl extends CommandImpl {

    static final String NAME = "withdraw";

    /**
     * Create a new WithdrawImpl.
     */
    WithdrawImpl() {
        super(NAME);
    }

    @Override
    public Result process(Map<String, String> commandFields)
            throws ATMException {
        DAL dal = DAL.getInstance();
        // The following intentionally uses deposit with a negative value.
        BigDecimal balance = dal.deposit(commandFields.get("num"),
                commandFields.get("pin"),
                new BigDecimal(commandFields.get("amount")).negate());
        if (balance.signum() == -1) {
            // This will cause a rollback in order to prevent a negative
            // balance.
            result.error = ATMError.INSUFFICIENT_FUNDS;
            throw new ATMException("Insufficient funds");
        }
        result.setField("balance", "" + balance);
        return result;
    }
}
