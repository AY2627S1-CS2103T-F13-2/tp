package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.PersonHasAnyTagPredicate;
import seedu.address.model.tag.Tag;

/** Tests plain List and repeated tag filters. */
public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

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
        for (String args : new String[] {" t/a-level x/value", " T/a-level", " s/Maths", " foo t/a-level",
            " t/a-level notes/hello", " --summary", " t/a-level S/Maths"}) {
            assertParseFailure(parser, args, ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        }
    }

    @Test
    public void parse_argsGiven_throwsParseException() {
        assertParseFailure(parser, " 3", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
        assertParseFailure(parser, " foo", ListCommandParser.MESSAGE_INVALID_PARAMETERS);
    }
}
