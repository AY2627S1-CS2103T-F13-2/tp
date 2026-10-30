package seedu.address.logic.commands;

/** Shared constants for commands that modify outstanding balances. */
public final class BalanceCommand {
    public static final String MESSAGE_INVALID_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_AMOUNT_MUST_BE_POSITIVE = "Amount must be greater than 0.";
    public static final String MESSAGE_INVALID_AMOUNT =
            "Amount must be a positive number with at most two decimal places, such as 50 or 20.50.";

    private BalanceCommand() {}
}
