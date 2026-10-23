package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OUTSTANDING_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.Notes;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_START_DATE,
                        PREFIX_OUTSTANDING_AMOUNT, PREFIX_SUBJECT, PREFIX_LESSON_DAY, PREFIX_LESSON_TIME,
                        PREFIX_TAG, PREFIX_NOTES);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_START_DATE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        if (argMultimap.getAllValues(PREFIX_SUBJECT).isEmpty()) {
            throw new ParseException("At least one subject must be supplied.");
        }
        ParserUtil.verifySubjectScheduleOrder(args);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_START_DATE,
                PREFIX_OUTSTANDING_AMOUNT, PREFIX_NOTES);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        StartDate startDate = ParserUtil.parseStartDate(argMultimap.getValue(PREFIX_START_DATE).get());
        OutstandingAmount outstandingAmount = argMultimap.getValue(PREFIX_OUTSTANDING_AMOUNT).isPresent()
                ? ParserUtil.parseOutstandingAmount(argMultimap.getValue(PREFIX_OUTSTANDING_AMOUNT).get())
                : OutstandingAmount.ZERO;
        Set<Subject> subjects = ParserUtil.parseScheduledSubjects(
                argMultimap.getAllValues(PREFIX_SUBJECT), argMultimap.getAllValues(PREFIX_LESSON_DAY),
                argMultimap.getAllValues(PREFIX_LESSON_TIME));

        Notes notes = argMultimap.getValue(PREFIX_NOTES).map(ParserUtil::parseNotes).orElse(Notes.EMPTY);
        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        Person person = new Person(name, phone, address, startDate, outstandingAmount, tags, notes, subjects);

        return new AddCommand(person);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
