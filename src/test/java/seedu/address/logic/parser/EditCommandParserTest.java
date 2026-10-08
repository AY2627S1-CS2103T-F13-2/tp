package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.StartDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_startDateSpecified_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withStartDate(VALID_START_DATE_BOB).build();
        assertParseSuccess(parser, INDEX_FIRST_PERSON.getOneBased() + START_DATE_DESC_BOB,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_invalidStartDate_failure() {
        assertParseFailure(parser, INDEX_FIRST_PERSON.getOneBased() + INVALID_START_DATE_DESC,
                StartDate.MESSAGE_CONSTRAINTS);
    }
}
