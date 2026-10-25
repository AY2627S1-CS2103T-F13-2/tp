package seedu.address.model.sort;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** An immutable list of sorting criteria, ordered from highest to lowest priority. */
public record SortOrder(List<SortOrder.Criterion> criteria) {

    public static final SortOrder NONE = new SortOrder(List.of());

    /**
     * Copies the criteria and rejects repeated fields. An empty list represents original stored order.
     *
     * @throws IllegalArgumentException if a field appears more than once
     * @throws NullPointerException if the list or any criterion is null
     */
    public SortOrder {
        criteria = List.copyOf(criteria);
        Set<Field> fields = EnumSet.noneOf(Field.class);
        for (Criterion criterion : criteria) {
            if (!fields.add(criterion.field())) {
                throw new IllegalArgumentException("Each sorting field may be specified only once.");
            }
        }
    }

    /** Returns whether the order includes a lesson-slot comparison. */
    public boolean hasLessonSlot() {
        return criteria.stream().anyMatch(criterion -> criterion.field() == Field.LESSON_SLOT);
    }

    /** An attribute that can determine the display order. */
    public enum Field {
        NAME, OUTSTANDING_AMOUNT, START_DATE, LESSON_SLOT
    }

    /** Whether present values are compared in ascending or descending order. */
    public enum Direction {
        ASCENDING, DESCENDING
    }

    /** One field and its independently selected comparison direction. */
    public record Criterion(Field field, Direction direction) {
        /** Creates a criterion with a non-null field and direction. */
        public Criterion {
            requireNonNull(field);
            requireNonNull(direction);
        }
    }
}
