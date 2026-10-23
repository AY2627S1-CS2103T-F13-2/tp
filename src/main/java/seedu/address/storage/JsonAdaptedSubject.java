package seedu.address.storage;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonSlot;
import seedu.address.model.person.Subject;

/** Jackson-friendly representation of a scheduled subject. */
class JsonAdaptedSubject {
    private final String name;
    private final String day;
    private final String time;

    @JsonCreator
    public JsonAdaptedSubject(@JsonProperty("name") String name, @JsonProperty("day") String day,
            @JsonProperty("time") String time) {
        this.name = name;
        this.day = day;
        this.time = time;
    }

    public JsonAdaptedSubject(Subject source) {
        name = source.value;
        day = source.getLessonSlot().getDay().name();
        time = source.getLessonSlot().getStartTime().toString();
    }

    public String getName() {
        return name;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    /** Converts this stored representation into the model type. */
    public Subject toModelType() throws IllegalValueException {
        if (name == null || !Subject.isValidSubject(name)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (day == null) {
            throw new IllegalValueException(ParserUtil.MESSAGE_INVALID_LESSON_DAY);
        }
        if (time == null) {
            throw new IllegalValueException(ParserUtil.MESSAGE_INVALID_LESSON_TIME);
        }
        try {
            DayOfWeek modelDay = DayOfWeek.valueOf(day);
            LocalTime modelTime = ParserUtil.parseLessonTime(time);
            return new Subject(name, new LessonSlot(modelDay, modelTime));
        } catch (IllegalArgumentException | ParseException exception) {
            String message = exception instanceof ParseException
                    ? exception.getMessage() : ParserUtil.MESSAGE_INVALID_LESSON_DAY;
            throw new IllegalValueException(message);
        }
    }
}
