package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_START_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.START_DATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StartDate;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {
    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, VALID_NAME_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "", EditCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "0" + NAME_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 some random string", EditCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY + PHONE_DESC_BOB
                + ADDRESS_DESC_AMY + START_DATE_DESC_BOB;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_AMY)
                .withStartDate(VALID_START_DATE_BOB)
                .build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_eachFieldSpecifiedIndividually_success() {
        assertParseSuccess(parser, "1" + NAME_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build()));
        assertParseSuccess(parser, "1" + PHONE_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build()));
        assertParseSuccess(parser, "1" + ADDRESS_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build()));
        assertParseSuccess(parser, "1" + START_DATE_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON,
                        new EditPersonDescriptorBuilder().withStartDate(VALID_START_DATE_AMY).build()));
    }

    @Test
    public void parse_invalidFieldValues_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_START_DATE_DESC, StartDate.MESSAGE_CONSTRAINTS);
    }

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

    @Test
    public void parse_invalidIndexes_failure() {
        String[] invalidIndexes = {"", " ", "-1", "0", "1.5", "abc", "1 2", "+1", "2147483648"};
        for (String invalidIndex : invalidIndexes) {
            assertParseFailure(parser, invalidIndex + PHONE_DESC_AMY, EditCommandParser.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_unknownOrUppercasePrefix_failure() {
        String[] arguments = {"1 x/value", "1 a/Main Street x/value", "1 x/value" + PHONE_DESC_AMY,
            "1 P/91234567", "1 a/Main Street N/Amy", "1 e/student@example.com", "1 t/friend"};
        for (String argument : arguments) {
            assertParseFailure(parser, argument, EditCommandParser.MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_eachSingleValuedFieldRepeated_failure() {
        String[] repeatedFields = {
            "1 n/Amy n/Bob",
            "1 p/11111111 p/22222222",
            "1 a/First Street a/Second Street",
            "1 d/10-Aug d/19-Sep"
        };
        for (String arguments : repeatedFields) {
            assertParseFailure(parser, arguments, EditCommandParser.MESSAGE_DUPLICATE_FIELDS);
        }
    }

    @Test
    public void parse_trimmedIndexAndValues_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Block 1, #02/03")
                .withPhone(VALID_PHONE_AMY)
                .build();
        assertParseSuccess(parser, "  1  a/  Block 1, #02/03   p/  " + VALID_PHONE_AMY + "  ",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }
}
