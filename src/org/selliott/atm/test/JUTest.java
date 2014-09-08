package org.selliott.atm.test;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.ATMException;
import org.selliott.atm.common.Config;
import org.selliott.atm.server.Command;
import org.selliott.atm.server.Result;

public class JUTest {
    private static final BigDecimal BALANCE = new BigDecimal("100.00");
    private static final String BIG_AMOUNT = "200.00";
    private static final String SMALL_AMOUNT = "12.34";

    @Before
    public void before() throws ATMException {
        Config.getInstance(new String[] { "resources/atm.properties" });
        // It would be helpful if the test data was setup here in the following
        // way:
        // atm=> table account;
        // id | name | num | pin | balance
        // ----+------+------+------+---------
        // 1 | Bob | 1234 | 4321 | 100.00
        // (1 row)
    }

    @Test
    public void depositWithdrawTest() throws ATMException {
        // Deposit some money, which should result in a larger balance.
        Command command = new Command("deposit");
        command.setField("num", "1234");
        command.setField("pin", "4321");
        command.setField("amount", SMALL_AMOUNT);
        Result result = command.process();
        BigDecimal balance1 = new BigDecimal(result.fieldMap.get("balance"));
        Assert.assertEquals(1, balance1.compareTo(BALANCE));

        // Withdraw the same amount of money, which should result in the
        // original balance.
        command = new Command("withdraw");
        command.setField("num", "1234");
        command.setField("pin", "4321");
        command.setField("amount", SMALL_AMOUNT);
        result = command.process();
        BigDecimal balance2 = new BigDecimal(result.fieldMap.get("balance"));
        Assert.assertEquals(0, balance2.compareTo(BALANCE));
    }

    @Test
    public void getBalanceTest() throws ATMException {
        Command command = new Command("get-balance");
        command.setField("num", "1234");
        command.setField("pin", "4321");
        Result result = command.process();
        BigDecimal balance = new BigDecimal(result.fieldMap.get("balance"));
        Assert.assertEquals(BALANCE, balance);
    }

    @Test
    public void withdrawInsufficientTest() throws ATMException {
        // Deposit some money, which should result in a larger balance.
        Command command = new Command("withdraw");
        command.setField("num", "1234");
        command.setField("pin", "4321");
        command.setField("amount", BIG_AMOUNT);
        Result result = command.process();
        // TODO: It's odd that in this exception case that the result returned
        // in null even though the result exists in the command.
        Assert.assertNull(result);
        Assert.assertEquals(ATMError.INSUFFICIENT_FUNDS, command.error);

        // Confirm that the balance is unaffected by the failed withdraw.
        // TODO: It seems odd for one test to call another.
        getBalanceTest();
    }
}
