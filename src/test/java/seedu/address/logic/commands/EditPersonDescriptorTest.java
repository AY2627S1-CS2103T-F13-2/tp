package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_START_DATE_BOB;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.OutstandingAmount;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different start date -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withStartDate(VALID_START_DATE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void outstandingAmountOnly_countsAsEditAndSurvivesCopy() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withOutstandingAmount("600").build();
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);

        assertTrue(copy.isAnyFieldEdited());
        assertEquals(new OutstandingAmount("600"), copy.getOutstandingAmount().orElseThrow());
        assertEquals(descriptor, copy);
        assertFalse(descriptor.equals(new EditPersonDescriptor()));
        assertFalse(descriptor.equals(new EditPersonDescriptorBuilder().withOutstandingAmount("20").build()));
    }

    @Test
    public void tags_copyInputAndExposeImmutableSet() {
        Set<Tag> source = new HashSet<>(Set.of(new Tag("A-Level")));
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setTags(source);
        source.clear();
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);

        assertEquals(Set.of(new Tag("a-level")), copy.getTags().orElseThrow());
        assertEquals(descriptor, copy);
        assertTrue(copy.isAnyFieldEdited());
        assertThrows(UnsupportedOperationException.class, () -> copy.getTags().orElseThrow().clear());
        descriptor.setTags(Set.of(new Tag("other")));
        assertEquals(Set.of(new Tag("a-level")), copy.getTags().orElseThrow());
        assertFalse(descriptor.equals(copy));
    }

    @Test
    public void tags_omittedAndEmptyAreDistinct() {
        EditPersonDescriptor omitted = new EditPersonDescriptor();
        assertTrue(omitted.getTags().isEmpty());
        assertFalse(omitted.isAnyFieldEdited());

        EditPersonDescriptor clear = new EditPersonDescriptorBuilder().withTags().build();
        assertTrue(clear.getTags().orElseThrow().isEmpty());
        assertTrue(clear.isAnyFieldEdited());
        assertFalse(clear.equals(omitted));
        assertEquals(clear, new EditPersonDescriptor(clear));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", startDate="
                + editPersonDescriptor.getStartDate().orElse(null) + ", outstandingAmount="
                + editPersonDescriptor.getOutstandingAmount().orElse(null) + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
