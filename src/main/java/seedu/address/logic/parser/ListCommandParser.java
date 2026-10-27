package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.tag.Tag;

/** Parses plain lists, summary lists, and repeated subject or tag filters. */
public class ListCommandParser implements Parser<ListCommand> {
    public static final String MESSAGE_INVALID_PARAMETERS = "Invalid command format! Use list, list -summary, "
                + "list s/SUBJECT [s/SUBJECT]..., list t/TAG [t/TAG]..., list -archived, or list -subjects. "
                + "Use -archived and -subjects on their own.";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([a-zA-Z][a-zA-Z0-9_-]*/)");
    private static final Pattern EXCLUSIVE_FLAG_PATTERN = Pattern.compile("(?:^|\\s)-(?:archived|subjects)(?=\\s|$)");
    // Recognise a complete option before another option or at the end, not text inside a filter value.
    private static final Pattern SUMMARY_FLAG_PATTERN = Pattern.compile(
            "(?<!\\S)-summary(?=\\s+(?:[a-zA-Z][a-zA-Z0-9_-]*/|-summary(?:\\s|$))|\\s*$)\\s*");

    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            return new ListCommand();
        }
        if (trimmed.equals(ListCommand.ARCHIVED_FLAG)) {
            return ListCommand.forArchivedStudents();
        }
        if (trimmed.equals(ListCommand.SUBJECTS_FLAG)) {
            return ListCommand.forSubjectGroups();
        }
        if (EXCLUSIVE_FLAG_PATTERN.matcher(trimmed).find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        Matcher summaryFlag = SUMMARY_FLAG_PATTERN.matcher(trimmed);
        boolean isSummary = summaryFlag.find();
        if (summaryFlag.find()) {
            throw new ParseException(MESSAGE_INVALID_PARAMETERS);
        }
        String filterArgs = summaryFlag.replaceFirst(" ").trim();
        if (filterArgs.isEmpty()) {
            return new ListCommand(isSummary);
        }

        String filterPrefix = null;
        Matcher matcher = PREFIX_PATTERN.matcher(filterArgs);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            if (!PREFIX_SUBJECT.toString().equals(prefix) && !PREFIX_TAG.toString().equals(prefix)) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            if (filterPrefix != null && !filterPrefix.equals(prefix)) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            filterPrefix = prefix;
        }
        if (PREFIX_SUBJECT.toString().equals(filterPrefix)) {
            ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + filterArgs, PREFIX_SUBJECT);
            if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_SUBJECT).isEmpty()) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            Set<String> subjectNames = ParserUtil.parseSubjectNames(values.getAllValues(PREFIX_SUBJECT));
            return new ListCommand(subjectNames, isSummary);
        }
        if (PREFIX_TAG.toString().equals(filterPrefix)) {
            ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + filterArgs, PREFIX_TAG);
            if (!values.getPreamble().isEmpty() || values.getAllValues(PREFIX_TAG).isEmpty()) {
                throw new ParseException(MESSAGE_INVALID_PARAMETERS);
            }
            Set<Tag> tags = ParserUtil.parseTags(values.getAllValues(PREFIX_TAG));
            return new ListCommand(new PersonHasAnyTagPredicate(tags), isSummary);
        }
        throw new ParseException(MESSAGE_INVALID_PARAMETERS);
    }
}
