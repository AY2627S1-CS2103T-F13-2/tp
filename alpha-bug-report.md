# Alpha bug-testing report

Tester: Alpha  
Artifact: `TuteeDex.jar` in the project root  
Date: 24 October 2026

## Scope

The assigned features are:

- Add student contact, address, and payment information
- View each contact/profile
- Edit contact (SH)
- Exit
- New UI/theme across all functions
- List by tag(s)
- Edit command supporting new fields

## Environment and test method

- Ubuntu Linux, OpenJDK 25.0.4.1
- Tested the JAR directly with `java -jar TuteeDex.jar`.
- The JAR cannot display its JavaFX window in this headless environment (`Unable to open DISPLAY`).
- Command handling was additionally exercised through the JAR's `LogicManager` classes with an isolated temporary data file, so the confirmed command defect below is independent of the missing display.

## Confirmed defects

### ALPHA-001 — Documented Add command rejects valid subject lesson

- **Severity:** High
- **Feature:** Add student contact, address, and payment information
- **Steps to reproduce:**
  1. Start the JAR.
  2. Enter: `add n/Alpha Student p/91234567 a/Block 1 d/10-Aug s/Maths day/MON time/10:00`
- **Expected:** A student profile is added and appears in the contact list. This command matches the format and example in `docs/UserGuide.md`.
- **Actual:** The command is rejected with: `Subject must be 1 to 50 characters long, contain only English letters, digits, spaces or hyphens, and include at least one letter or digit.`
- **Impact:** A normal valid Add operation cannot create a profile. Consequently, View, Edit, Edit-with-new-fields, and List-by-tag cannot be exercised through the normal end-to-end workflow because they require an existing profile.

## Blocked / not independently verifiable in this environment

- **View each contact/profile:** blocked by ALPHA-001 and the unavailable JavaFX display.
- **Edit contact (SH):** blocked by ALPHA-001 and the unavailable JavaFX display.
- **Exit:** the application starts JavaFX and terminates before presenting a window because no display server is available; the Exit control could not be clicked.
- **New UI/theme across all functions:** not visually verifiable without a display server.
- **List by tag(s):** no profile could be created using the documented Add command; therefore no meaningful tag-filter result could be observed.
- **Edit command supporting new fields:** a direct command attempt after the failed Add produced an invalid-index result; no existing profile was available for a meaningful edit assertion.

## Verification notes

The repository's automated checks completed successfully:

- `./gradlew checkstyleMain checkstyleTest`
- `./gradlew test`

These passing checks do not invalidate ALPHA-001: the defect was observed against the supplied root JAR, while the repository tests run against the current source/build configuration.

## Follow-up edge-case testing for `list -subjects`

The grouped-list implementation was tested against empty data, active students without
subjects, archived students, students taking multiple subjects, repeated/case-variant
subject names, different lesson slots, invalid flag combinations, and switching back with
plain `list`.

### Fixed during follow-up testing

- **Subjectless active contacts:** `list -subjects` previously reported active contacts but
  rendered an empty grouped panel. It now reports `No subjects found for the active contacts.`
  and the UI displays the same message.
- **Repeated grouped contact indexes:** a student appearing in multiple subject groups could
  receive a different displayed index in each group. Grouped cards now use the student’s
  index from the normal contact list consistently.

No additional unresolved defects were found in the tested grouped-list paths.
