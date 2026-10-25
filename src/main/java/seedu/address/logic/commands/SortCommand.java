package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.sort.SortOrder;
import seedu.address.model.sort.SortOrder.Field;

/** Changes or queries the session's display order without changing its filter or stored profiles. */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = "Usage: sort n/asc o/desc d/asc slot/asc (choose any distinct fields), "
            + "sort reset, or sort to show the current order. Each direction must be asc or desc.";
    public static final String MESSAGE_DEFAULT = "Sorting: default display order.";

    private final Optional<SortOrder> requestedOrder;

    /** Creates a read-only query of the current order. */
    public SortCommand() {
        requestedOrder = Optional.empty();
    }

    /** Creates a request to apply an order, including NONE to reset it. */
    public SortCommand(SortOrder order) {
        requestedOrder = Optional.of(requireNonNull(order));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        requestedOrder.ifPresent(model::setSortOrder);
        SortOrder order = model.getSortOrder();
        String feedback = order.equals(SortOrder.NONE) ? MESSAGE_DEFAULT : "Sorting: " + order.criteria().stream()
                .map(criterion -> label(criterion.field()) + " "
                        + criterion.direction().name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", ")) + ".";
        return new CommandResult(feedback, false, false, requestedOrder.isPresent()
                ? CommandResult.ListPresentation.REFRESH : CommandResult.ListPresentation.UNCHANGED);
    }

    private String label(Field field) {
        return switch (field) {
            case NAME -> "name";
            case OUTSTANDING_AMOUNT -> "outstanding amount";
            case START_DATE -> "start date";
            case LESSON_SLOT -> "upcoming lesson";
        };
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand command && requestedOrder.equals(command.requestedOrder);
    }
}
