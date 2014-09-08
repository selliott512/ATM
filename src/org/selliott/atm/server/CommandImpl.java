package org.selliott.atm.server;

import java.util.Map;

import org.selliott.atm.common.ATMException;

/**
 * The implementation of the commands, i.e. a class that is runnable (the
 * process() method) that performs the command. TODO: The term "implementation"
 * is not ideal since this is an abstract class that is implemented by the
 * command implementations.
 */
public abstract class CommandImpl {
    Result result;

    /**
     * Create a new CommandImpl given a name.
     * 
     * @param name
     *            The name.
     */
    CommandImpl(String name) {
        result = new Result(name);
    }

    /**
     * Get a previously generated result.
     * 
     * @return The result.
     */
    public Result geResult() {
        return result;
    }

    /**
     * Process the command - Actually act on the command and produce a result.
     * 
     * @param commandFields
     *            The "field" elements included in the command.
     * @return
     * @throws ATMException
     *             An application level error.
     */
    abstract Result process(Map<String, String> commandFields)
            throws ATMException;
}
