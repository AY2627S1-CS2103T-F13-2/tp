package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/** Tests plain List and repeated subject or tag filters. */
public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_archivedFlag_returnsArchivedList() {
        assertParseSuccess(parser, " -archived ", ListCommand.forArchivedStudents());
    }

    @Test
    public void parse_subjectsFlag_returnsSubjectGroupedList() {
        assertParseSuccess(parser, " -subjects ", ListCommand.forSubjectGroups());
    }

    @Test
    public void parse_archivedFlagWithOtherOptions_failure() {
        String[] invalid = {"-archived -summary", "-summary -archived", "-archived s/Maths",
            "t/A-Level -archived", "s/Maths -archived", "-archived -archived", "-Archived", "-archived extra"};
        for (String args : invalid) {
            assertParseFailure(parser, args, ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        }
    }

    @Test
    public void parse_noArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
    }

    @Test
    public void parse_blankArgs_returnsListCommand() {
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_tags_normalizesAndCombinesRepeatedValues() {
        ListCommand expected = new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("a-level"), new Tag("needs revision"))));
        assertParseSuccess(parser, " t/ A-Level  t/Needs revision t/a-level ", expected);
        assertParseSuccess(parser, "t/A-Level t/Needs revision", expected);
    }

    @Test
    public void parse_blankOrInvalidTag_failure() {
        for (String args : new String[] {" t/", " t/   ", " t/a-level t/", " t/a,b", " t/中文"}) {
            assertParseFailure(parser, args, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unknownPrefixOrPreamble_failure() {
        for (String args : new String[] {" t/a-level x/value", " T/a-level", " foo t/a-level",
            " t/a-level notes/hello", " --summary", " t/a-level S/Maths",
            " t/a-level s/Maths"}) {
            assertParseFailure(parser, args, ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        }
    }
    @Test
    public void parse_summaryFlag_returnsSummaryListCommand() {
        assertParseSuccess(parser, " -summary ", new ListCommand(true));
    }

    @Test
    public void parse_summaryWithEitherFilter_returnsFilteredSummaryCommand() {
        assertParseSuccess(parser, " -summary s/Maths ",
                new ListCommand(Set.of("Maths"), true));
        assertParseSuccess(parser, " -summary t/A-Level ",
                new ListCommand(new PersonHasAnyTagPredicate(Set.of(new Tag("a-level"))), true));
    }

    @Test
    public void parse_summaryAtAnySubjectFilterBoundary_returnsSameCommand() {
        ListCommand expected = new ListCommand(Set.of("Maths", "Physics"), true);
        for (String args : new String[] {"-summary s/Maths s/Physics", "s/Maths -summary s/Physics",
            "s/Maths s/Physics -summary", "s/Maths\t-summary\ts/Physics"}) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_summaryAtAnyTagFilterBoundary_returnsSameCommand() {
        ListCommand expected = new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("a-level"), new Tag("needs revision"))), true);
        for (String args : new String[] {"-summary t/A-Level t/Needs revision",
            "t/A-Level -summary t/Needs revision", "t/A-Level t/Needs revision -summary",
            "\t-summary\tt/A-Level t/Needs revision"}) {
            assertParseSuccess(parser, args, expected);
        }
    }

    @Test
    public void parse_summaryTextWithinValues_preservesValues() {
        assertParseSuccess(parser, "s/Pre-summary", new ListCommand(Set.of("Pre-summary")));
        assertParseSuccess(parser, "s/Maths -summary revision",
                new ListCommand(Set.of("Maths -summary revision")));
        assertParseSuccess(parser, "t/Pre-summary", new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("pre-summary")))));
        assertParseSuccess(parser, "t/Needs -summary revision", new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("needs -summary revision")))));
        assertParseSuccess(parser, "t/-summary", new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("-summary")))));
        ListCommand expected = new ListCommand(new PersonHasAnyTagPredicate(
                Set.of(new Tag("needs -summary revision"))), true);
        assertParseSuccess(parser, "-summary t/Needs -summary revision", expected);
        assertParseSuccess(parser, "t/Needs -summary revision -summary", expected);
    }

    @Test
    public void parse_repeatedSummaryFlags_failure() {
        for (String args : new String[] {"-summary -summary", "-summary -summary s/Maths",
            "-summary s/Maths -summary",
            "s/Maths -summary -summary", "t/A-Level -summary t/Needs revision -summary"}) {
            assertParseFailure(parser, args, ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        }
    }

    @Test
    public void parse_summaryWithInvalidFilters_failure() {
        assertParseFailure(parser, "s/ -summary", "Subject must not be blank.");
        assertParseFailure(parser, "t/ -summary", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/Maths -summary x/value", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, "s/Maths -summary t/A-Level", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        for (String args : new String[] {"-summary -subjects", "t/A-Level -summary -subjects",
            "-subjects -summary", "s/Maths -subjects -summary", "t/A-Level -archived -summary"}) {
            assertParseFailure(parser, args, ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        }
    }

    @Test
    public void parse_argsGiven_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " foo", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -summary -summary", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -Summary", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -subjects -summary", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -subjects s/Maths", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " -subjects extra", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }

    @Test
    public void parse_subjects_returnsFilteredListCommand() {
        assertParseSuccess(parser, " s/Maths s/H2 Chemistry",
                new ListCommand(Set.of("maths", "H2 Chemistry")));
        assertParseSuccess(parser, " s/ Maths  s/maths s/ H2 Chemistry ",
                new ListCommand(Set.of("Maths", "H2 Chemistry")));
    }

    @Test
    public void parse_invalidSubject_failure() {
        assertParseFailure(parser, " s/Maths s/", "Subject must not be blank.");
        assertParseFailure(parser, " s/Maths, Physics", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " s/---", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " s/Maths t/A-Level", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }
}
