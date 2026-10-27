package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.sort.SortOrder;

/** Parses sorting criteria, a query, or a reset without changing the model. */
public class SortCommandParser implements Parser<SortCommand> {
    @Override
    public SortCommand parse(String args) throws ParseException {
        String trimmed = requireNonNull(args).trim();
        if (trimmed.isEmpty()) {
            return new SortCommand();
        }
        if (trimmed.equals("reset")) {
            return new SortCommand(SortOrder.NONE);
        }
        return new SortCommand(new SortOrderParser().parse(trimmed));
    }
}
