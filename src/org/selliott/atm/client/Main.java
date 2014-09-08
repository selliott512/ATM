package org.selliott.atm.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;
import org.selliott.atm.common.ATMError;
import org.selliott.atm.common.Config;

/**
 * Main entry point for the Swing client GUI.
 */
public class Main {
    private static final String APP_NAME = "ATM";
    private static final int BORDER = 5;
    private static final String INSTRUCTIONS = "Press one of the buttons to begin a transaction.";
    private static final Logger log = Logger.getLogger(Main.class);
    // TODO: Handle additional cases such as "$", commas, missing zero to the
    // left of ".", etc.
    private static final Pattern MONEY_RE = Pattern
            .compile("\\d{1,6}\\.\\d{2}");
    private static final Pattern NUMBER_RE = Pattern.compile("\\d{1,10}");
    private static final int SCREEN_SIZE = 500;
    private static final String SEP = "****************************************";
    private static final int TRIES = 3;
    private static final String WELCOME = "Welcome to the";

    public static void main(String[] args) throws Exception {
        log.info("Begin main.");

        // TODO: getInstance is called here not to get the instance, but so that
        // the configuration parameters can be accessed directly. Perhaps there
        // is a more encapsulated way of doing this.
        Config.getInstance(args);

        // Display the GUI and immediately return.
        new Main();

        log.info("End main.");
    }

    private Client client;
    private JFrame frame;
    private String num;
    private String pin;
    private JTextArea screen;

    /**
     * Construct the GUI. TODO: This probably should not be entirely in one
     * method.
     */
    public Main() {
        client = new Client();

        frame = new JFrame(APP_NAME);

        JPanel pan = new JPanel();
        pan.setBorder(new LineBorder(Color.LIGHT_GRAY, BORDER));
        frame.setContentPane(pan);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        screen = new JTextArea();
        Font font = screen.getFont();
        screen.setFont(new Font(font.getName(), font.getStyle(),
                (int) (1.5 * font.getSize())));

        JScrollPane scrollTA = new JScrollPane(screen);

        welcome();

        // Stretch components to take up available area.
        gbc.fill = GridBagConstraints.BOTH;

        // Size of the data entry screen.
        gbc.ipadx = SCREEN_SIZE;
        gbc.ipady = SCREEN_SIZE;

        // The screen should take up as much space as possible when the window
        // is resized.
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 10;
        gbc.gridheight = 10;
        frame.add(scrollTA, gbc);

        // Reset the constraints for buttons.
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        frame.add(new JLabel());
        JButton balanceB = new JButton("Balance");
        balanceB.setMnemonic(KeyEvent.VK_B);
        balanceB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clickBalance();
            }
        });
        gbc.gridy = 11;
        frame.add(balanceB, gbc);

        JButton depositB = new JButton("Deposit");
        depositB.setMnemonic(KeyEvent.VK_D);
        depositB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clickDeposit();
            }
        });
        gbc.gridy = 12;
        frame.add(depositB, gbc);

        JButton withdrawB = new JButton("Withdraw");
        withdrawB.setMnemonic(KeyEvent.VK_W);
        withdrawB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clickWithdraw();
            }

        });
        gbc.gridy = 13;
        frame.add(withdrawB, gbc);

        JButton logoutB = new JButton("Logout");
        logoutB.setMnemonic(KeyEvent.VK_L);
        logoutB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clickLogout();
            }
        });
        gbc.gridy = 14;
        frame.add(logoutB, gbc);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Clean sensitive information, such as the PIN.
     */
    private void clean() {
        num = pin = null;
    }

    /**
     * Process the balance button being clicked on.
     */
    private void clickBalance() {
        log.info("Begin balance.");
        screen.append("\n" + SEP + "\n");
        screen.append("Getting the balance.\n");
        if (getCredentials()) {
            BigDecimal balance = client.getBalance(num, pin);
            processBalance(balance, "get the balance");
        }
        screen.append(SEP + "\n");
        log.info("End balance.");
    }

    /**
     * Process the deposit button being clicked on.
     */
    private void clickDeposit() {
        log.info("Begin deposit.");
        screen.append("\n" + SEP + "\n");
        screen.append("Deposit.\n");
        if (getCredentials()) {
            String amount = getUserItem("the amount to deposit", MONEY_RE);
            screen.append("Amount: " + amount + "\n");
            BigDecimal balance = client.deposit(num, pin,
                    new BigDecimal(amount));
            processBalance(balance, "deposit");
        }
        screen.append(SEP + "\n");
        log.info("End deposit.");
    }

    /**
     * Process the logout button being clicked on.
     */
    private void clickLogout() {
        log.info("Begin logout.");
        screen.append("\nLogging out.\n");
        clean();
        welcome();
        log.info("End logout.");
    }

    /**
     * Process the withdraw button being clicked on.
     */
    private void clickWithdraw() {
        log.info("Begin withdraw.");
        screen.append("\n" + SEP + "\n");
        screen.append("Withdraw.\n");
        if (getCredentials()) {
            String amount = getUserItem("the amount to withdraw", MONEY_RE);
            screen.append("Amount: " + amount + "\n");
            BigDecimal balance = client.withdraw(num, pin, new BigDecimal(
                    amount));
            processBalance(balance, "withdraw");
        }
        screen.append(SEP + "\n");
        log.info("End withdraw.");
    }

    /**
     * Prompt for the credentials. Clean the credentials if user fails to
     * provide valid credentials.
     * 
     * @return True if the user successfully provided credentials.
     */
    private boolean getCredentials() {
        if (num == null) {
            num = getUserItem("your account number", NUMBER_RE);
        }
        if ((num != null) && (pin == null)) {
            pin = getUserItem("your PIN", NUMBER_RE);
        }

        boolean success = (num != null) && (pin != null);
        if (!success) {
            // TODO: This could be said in a more user friendly way.
            screen.append("Unable to get the required credentials.\n");
            clean();
        }

        return success;
    }

    /**
     * Prompt for a particular item, such as the PIN.
     * 
     * @param item
     *            Phrase describing the item.
     * @param allowed
     *            Regular expression describing allowed patterns.
     * @return The item or null if the user failed to enter a valid value.
     */
    private String getUserItem(String item, Pattern allowed) {
        String value = null;
        for (int t = 1; t <= TRIES; t++) {
            value = JOptionPane.showInputDialog("Please enter " + item);
            if ((value != null) && (allowed.matcher(value).matches())) {
                break;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid " + item + "."
                        + ((t < TRIES) ? " Please try again." : ""));
                value = null;
            }
        }
        frame.requestFocus();
        return value;
    }

    /**
     * Process the balance returned by all commands to tell the user the balance
     * as result of the command and also to further verify the data returned.
     * 
     * @param balance
     *            TODO
     * @param action
     *            A phrase that describes the action.
     */
    private void processBalance(BigDecimal balance, String action) {
        if (balance == null) {
            ATMError atmError = client.getATMError();
            screen.append("Unable to " + action + ": "
                    + atmError.getDescription() + "\n");
            if (atmError == ATMError.INVALID_PIN) {
                clean();
            }
        } else {
            screen.append("Balance: " + balance + "\n");
        }
    }

    /**
     * Sets the main text area to a welcome message.
     */
    private void welcome() {
        screen.setText(SEP + "\n" + WELCOME + " " + APP_NAME + ".\n"
                + INSTRUCTIONS + "\n" + SEP + "\n");
    }
}
