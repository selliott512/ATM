package org.selliott.atm.server;

import java.util.HashMap;
import java.util.Map;

import org.selliott.atm.common.ATMError;

/**
 * The result produced by processing a command.
 */
public class Result {
    ATMError error = ATMError.SUCCESS;
    public Map<String, String> fieldMap = new HashMap<String, String>(); // public
                                                                         // for
                                                                         // JUnit
    String name;

    /**
     * Create a new result given a name.
     * 
     * @param name
     *            The name
     */
    public Result(String name) {
        this.name = name;
    }

    public ATMError getError() {
        return error;
    }

    /**
     * Add a name/value pair to the result. This corresponds to "field"
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
