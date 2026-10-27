package seedu.address.model.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.sort.SortOrder.Criterion;
import seedu.address.model.sort.SortOrder.Direction;
import seedu.address.model.sort.SortOrder.Field;

public class SortOrderTest {
    private static final Criterion NAME_ASCENDING = new Criterion(Field.NAME, Direction.ASCENDING);
    private static final Criterion AMOUNT_DESCENDING = new Criterion(Field.OUTSTANDING_AMOUNT, Direction.DESCENDING);

    @Test
    public void constructor_copiesCriteriaAndPreventsExternalMutation() {
        List<Criterion> input = new ArrayList<>(List.of(AMOUNT_DESCENDING, NAME_ASCENDING));
        SortOrder order = new SortOrder(input);
        input.clear();

        assertEquals(List.of(AMOUNT_DESCENDING, NAME_ASCENDING), order.criteria());
        assertThrows(UnsupportedOperationException.class, () -> order.criteria().clear());
    }

    @Test
    public void constructor_duplicateFieldWithEitherDirection_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SortOrder(List.of(NAME_ASCENDING, NAME_ASCENDING)));
        assertThrows(IllegalArgumentException.class, () -> new SortOrder(List.of(NAME_ASCENDING,
                AMOUNT_DESCENDING, new Criterion(Field.NAME, Direction.DESCENDING))));
    }

    @Test
    public void constructor_nullValues_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortOrder(null));
        assertThrows(NullPointerException.class, () -> new SortOrder(Arrays.asList(NAME_ASCENDING, null)));
        assertThrows(NullPointerException.class, () -> new Criterion(null, Direction.ASCENDING));
        assertThrows(NullPointerException.class, () -> new Criterion(Field.NAME, null));
    }

    @Test
    public void equals_andHashCode_respectPriorityAndDirection() {
        SortOrder order = new SortOrder(List.of(AMOUNT_DESCENDING, NAME_ASCENDING));
        SortOrder equalOrder = new SortOrder(List.of(AMOUNT_DESCENDING, NAME_ASCENDING));

        assertEquals(order, equalOrder);
        assertEquals(order.hashCode(), equalOrder.hashCode());
        assertNotEquals(order, new SortOrder(List.of(NAME_ASCENDING, AMOUNT_DESCENDING)));
        assertNotEquals(order, new SortOrder(List.of(
                new Criterion(Field.OUTSTANDING_AMOUNT, Direction.ASCENDING), NAME_ASCENDING)));
        assertNotEquals(order, SortOrder.NONE);
        assertEquals(new SortOrder(List.of()), SortOrder.NONE);
    }
}
