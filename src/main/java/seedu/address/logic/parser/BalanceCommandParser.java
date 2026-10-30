package seedu.address.logic.parser;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.OutstandingAmount;

/** Parses the common index and optional amount arguments for balance commands. */
public class BalanceCommandParser {

    /** Parses a command argument list into its index and optional amount. */
    public static ParsedBalanceArguments parse(String args, String usage, boolean amountRequired)
            throws ParseException {
        List<String> tokens = List.of(args.trim().split("\\s+"));
        if (args.trim().isEmpty()) {
            throw new ParseException(String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, usage));
        }
        if (amountRequired && tokens.size() != 2) {
            throw new ParseException(String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, usage));
        }
        if (!amountRequired && tokens.size() > 2) {
            throw new ParseException(String.format(BalanceCommand.MESSAGE_INVALID_FORMAT, usage));
        }

        Index index = ParserUtil.parseIndex(tokens.get(0));
        OutstandingAmount amount = tokens.size() == 2 ? parseAmount(tokens.get(1)) : null;
        if (amount != null && amount.equals(OutstandingAmount.ZERO)) {
            throw new ParseException(BalanceCommand.MESSAGE_AMOUNT_MUST_BE_POSITIVE);
        }
        return new ParsedBalanceArguments(index, amount);
    }

    // A payment or charge gets its own message, since the outstanding-amount message describes the balance.
    private static OutstandingAmount parseAmount(String amount) throws ParseException {
        if (!OutstandingAmount.isValidOutstandingAmount(amount)) {
            throw new ParseException(BalanceCommand.MESSAGE_INVALID_AMOUNT);
        }
        return ParserUtil.parseOutstandingAmount(amount);
    }

    /** Parsed balance command arguments. */
    public record ParsedBalanceArguments(Index index, OutstandingAmount amount) {}
}
