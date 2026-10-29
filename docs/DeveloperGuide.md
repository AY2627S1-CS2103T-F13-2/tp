---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TuteeDex Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* TuteeDex is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit 5](https://github.com/junit-team/junit5).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** and **`MainApp`** are in charge of the app launch and shut down. [`Main`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/Main.java) is the Java entry point that launches [`MainApp`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/MainApp.java), which initializes and connects the other components.
* During initialization, `MainApp` loads user preferences and address-book data through `Storage`, creates `Model` and `Logic`, and then creates `UI` with the `Logic` API.
* During shutdown, `MainApp` saves the current user preferences through `Storage`.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

`UiManager` implements `Ui` and starts the JavaFX interface by creating and showing a `MainWindow`, then filling its placeholders. It also handles fatal errors during UI initialization. `UiManager` and `MainWindow` both keep a reference to `Logic`.

`MainWindow` assembles the TuteeDex interface:

* The header contains the application title, the local date (`DD  -  MM`), and the local 24-hour time (`HH  :  MM`). A JavaFX `Timeline` refreshes the clock every second while the main window is shown and stops when it is hidden. The clock can be supplied in tests.
* `ResultDisplay` shows either the TOTAL/PAID summary or command feedback, with line breaks replaced by ` | `. Feedback that exceeds the normal display width switches to a wrapping two-line display, with vertical scrolling for additional text. Resizing re-evaluates the available width. Result displays have no tooltip.
* `PersonListPanel` displays the filtered `ObservableList<Person>` obtained from `Logic`. Its custom `PersonListViewCell` creates a `PersonCard` for each non-empty cell, so changes to the observable list update the displayed students.
* `PersonCard` shows a three-digit, zero-padded list index, the student's name, and subjects. Long names and subject text are abbreviated; tooltips expose the full profile and subjects, and accessible text includes the profile details.
* `CommandBox` accepts commands at the bottom of the window. The FIND button calls `prepareFind()`, which inserts `find ` only when the input is blank, then focuses the input and moves the caret to its end without executing a command.
* The exit button and `exit` command close the application window. Help is available through the `help` command and F1, using a `HelpWindow` created with `MainWindow` and reused when requested.

`MainWindow`, `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonCard`, and `HelpWindow` inherit from the abstract `UiPart<T>` class. `UiPart` loads an FXML layout, sets the controller, and exposes the root JavaFX object through `getRoot()`. `StatusBarFooter` and its FXML file remain in the source tree, but the current `MainWindow` does not instantiate or display a status bar; it is omitted from the diagram above.

The layouts are defined in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) defines the main layout and loads `TuteeDex.css`, while `PersonListCard.fxml` defines the layout for `PersonCard`. The main window also loads the bundled pixel fonts. `HelpWindow` has its own FXML layout and stylesheet.

`HelpWindow` renders command formats as single-line JavaFX labels in a scroll pane,
with full text in tooltips. Gradle bundles `docs/UserGuide.md` as a classpath resource;
the help window reads the inline-code entries on its `Format:` lines, so the guide
is the single source for the displayed formats, including sorting. Its browser
callback comes from the running `MainApp`'s `HostServices`, through `UiManager`
and `MainWindow`; it does not embed a browser. The close button and Escape call
`Stage.close()`. MainWindow uses the same pixel close icon but retains its application
exit handler. `HelpWindowTest` tests format extraction with small fixtures and verifies
the rendered tooltips, hyperlink, scrolling, and close/reopen behavior without
comparing a second hard-coded list against the guide.

The UI depends on `Model` classes to display `Person` data and to identify students whose outstanding amount is zero for the PAID count. Commands and access to the filtered list and GUI settings go through `Logic`.

The following sequence diagram shows submission of a successful `list` command and the resulting summary:

<puml src="diagrams/UiCommandSequenceDiagram.puml" alt="UI command execution and list summary"/>

1. When Enter is pressed, `CommandBox` ignores an empty string; otherwise, it passes the input to its `CommandExecutor`. `MainWindow` supplies this callback as `this::executeCommand`, keeping `CommandBox` independent of `Logic`.
1. `MainWindow` calls `Logic.execute(commandText)` and displays the returned `CommandResult` feedback. For a successful `list` command, it then replaces that feedback with the TOTAL/PAID summary. `showListSummary()` reads the filtered list: TOTAL is its size, and PAID counts students whose outstanding amount equals `OutstandingAmount.ZERO`. The summary is calculated at startup and after successful `list` commands; it is not continuously bound to list changes.
1. `MainWindow` also checks the result's help and exit flags. Help opens or focuses the existing `HelpWindow`; exit saves the current window size and position through `Logic.setGuiSettings()`, then hides the help and main windows. The exit button uses the same exit handler, and F1 uses the help handler.
1. When execution succeeds, `CommandBox` clears the input. If `Logic` throws a `CommandException` or `ParseException`, `MainWindow` displays the exception message and rethrows it. `CommandBox` retains the input and applies its error style, which is removed when the text next changes.

The sequence diagram omits error handling, help/exit flag handling, and the observable list's JavaFX cell updates for clarity.

For List commands, text such as `Listed N contacts.`, `No contacts match the specified subjects.`,
`No contacts match the specified tags.`, and `No archived contacts.` remains in the `CommandResult`
and is logged by `MainWindow`, but the visible result is the TOTAL/PAID banner. The optional `-summary`
changes the command's returned feedback; it does not opt into a different GUI display.
This applies to plain, filtered, archived and subject-grouped List modes. Empty results show both counts
as zero. Grouped counts use unique students, including subjectless students, rather than grouped rows.
Invalid List input follows the exception path instead: error feedback is shown and the current list remains.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

The partial class diagram below shows the command-execution boundary of the `Logic` component.
`LogicManager` implements `Logic` and keeps one reference each to `AddressBookParser`, `Model`, and `Storage`.
It parses command text into a `Command`, executes the command against the model, and saves the address book.

<puml src="diagrams/LogicClassDiagram.puml" width="800" alt="Partial class diagram of the Logic component"/>

`DeleteCommand` represents the concrete command subclasses in this diagram; its note lists other examples.
Command-specific parser classes and utility classes are omitted here. `CommandResult` carries feedback and the
`showHelp` and `exit` flags, which the UI uses after a successful execution.

The sequence diagram below follows a successful `execute("delete 1")` call. It assumes that the displayed list
contains at least one student and that saving succeeds. `index` is an `Index` object with a one-based value of 1.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Successful Delete command execution and saving through Logic" />

How the `Logic` component works:

1. `LogicManager.execute(commandText)` calls `AddressBookParser.parseCommand(commandText)`. For `delete 1`,
   the parser creates `DeleteCommandParser`, which validates the argument and returns a `DeleteCommand`.
2. `LogicManager` calls the command's `execute(model)` method. `DeleteCommand` reads the filtered person list
   and checks that the requested index exists before selecting the `Person` to delete. The index therefore
   refers to the currently displayed list, including search results or an archived list.
3. The command passes that `Person` to `model.deletePerson(personToDelete)` and constructs a `CommandResult`
   containing the success feedback. Message-formatting helpers and list-access calls are omitted from the
   diagram to keep the component interactions readable.
4. After the command returns, `LogicManager` obtains the address book from `Model` and calls
   `Storage.saveAddressBook(addressBook)`. Only after saving succeeds does it return the result to the caller.

Invalid command syntax produces a `ParseException`; an out-of-range Delete index produces a `CommandException`
before any student is deleted. These failure paths are omitted from the success sequence. A storage failure
also becomes a `CommandException`, but occurs after command execution and does not roll back its model changes.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* `AddressBookParser` first separates the command word from its arguments and dispatches the input to the matching command parser. The current command-specific parsers are `AddCommandParser`, `EditCommandParser`, `DeleteCommandParser`, `ClearCommandParser`, `PaidCommandParser`, `ChargeCommandParser`, `FindCommandParser`, `ListCommandParser`, `SortCommandParser`, `ArchiveCommandParser`, `UnarchiveCommandParser`, `ExportCommandParser`, `ImportCommandParser`, and `ViewCommandParser`.
* Commands without arguments (`exit` and `help`) are constructed directly by `AddressBookParser`. All other supported command words are delegated to a parser that creates the corresponding concrete `Command` object.
* The command parsers implement the generic `Parser<T extends Command>` interface. Prefix-based parsers use `CliSyntax` and `Prefix` to identify fields, `ArgumentTokenizer` to split the input, and `ArgumentMultimap` to collect repeated values. `ParserUtil` validates and converts values such as indexes, subjects, tags, notes, names, and contact fields before the command is created.
* Invalid command words, malformed arguments, unsupported prefixes, duplicate fields, and invalid field values result in a `ParseException`; the command is not executed.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores each student's subjects as `Subject` objects. Each `Subject` owns one required `LessonSlot`,
  which records a `DayOfWeek` and a `LocalTime` start time.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (the GUI settings and the last list view). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` interface is implemented by `StorageManager`, which delegates student-data access to
`JsonAddressBookStorage` and user-preference access to `JsonUserPrefsStorage`.

* `JsonAddressBookStorage` reads and writes `JsonSerializableAddressBook`, which contains zero or more
  `JsonAdaptedPerson` records. Each record stores the student's core profile fields, notes, archive status,
  zero or more tags represented by `JsonAdaptedTag`, and zero or more scheduled subjects represented by
  `JsonAdaptedSubject`.
* `JsonAdaptedSubject` stores the subject name, weekly lesson day, and lesson start time. It reconstructs
  a `Subject` with a non-null `LessonSlot` when data is loaded.
* The `toModelType()` methods validate stored values and reconstruct the corresponding model objects.
  `JsonSerializableAddressBook.toModelType()` also rejects null student entries and duplicates under the
  legacy five-field rule (name, phone, address, start date and amount). It then recovers balance-only
  duplicates under the current four-field identity rule by appending ` Recovered` to later names,
  repeating until neither an original nor a recovered identity collides. All original identities are
  checked before recovery, so the suffix cannot take a name needed by a later record.
  Recovery logs a warning, preserves every balance and all other fields, and does not rewrite the
  source file on read. Normal saving persists the recovered names; subsequent loads need no recovery.
  Users review the visible suffixes and correct distinguishing identity details with `edit`; amounts
  are never automatically merged.
  These conversions explain the Storage component's dependency on the Model component; model classes
  are omitted from this diagram.
* `JsonUtil.readJsonFile()` rejects a JSON root of `null` through `DataLoadingException`, rather than
  treating it as a missing file. `JsonAddressBookStorage` also translates adapter validation failures into
  `DataLoadingException`. `MainApp` handles that exception by creating an empty in-memory address book;
  loading itself does not rewrite the invalid student-data file or load only the valid entries.
* Student records without any `subjects` field load with an empty subject set. Records from v1.4 that store
  subjects as plain strings are incompatible because they do not contain a lesson day or start time.
  Older records without tags, notes, or archive status still load with empty tags, empty notes, and
  `archived = false`. User preferences are serialized directly without a separate adapter.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Sorting criteria

<puml src="diagrams/SortingClassDiagram.puml" width="1000" alt="Sorting criteria, comparisons and display rows" />

The diagram shows the sorting types and their main dependencies; UI, storage and comparator parameter
lists are omitted for readability.

`SortOrder` is an immutable, ordered list of field/direction pairs. Its first criterion has the highest
priority. Fields cannot repeat, and an empty order represents the original stored order. The supported
fields are student name, outstanding amount, start date, and upcoming lesson slot. A lesson slot is a
combined day/time criterion; each lesson row associates that schedule with its subject.

`SortOrderParser` validates criteria arguments such as `o/desc n/asc` or `slot/asc`, preserving their order
and independent directions. It rejects malformed arguments, unknown fields, repeated fields, and directions
other than lowercase `asc` or `desc`. The criteria model and parser do not mutate profiles or user preferences.
`SortCommandParser` handles the current-order query and reset, and the registered `SortCommand` applies
criteria to the session's `ModelManager`. Saving and restoring state belongs to the separate persistence feature.
`SortCommand` overrides `Command#requiresAddressBookSave()` so `LogicManager` does not write profile JSON
for apply, reset or query. A storage permission error therefore cannot make session-only sorting fail.
Other commands keep their existing save behaviour; preference persistence remains separate.

`SortComparators` compares names without regard to case, amounts numerically, and start dates as annual
month/day values. Invalid existing dates and absent lessons stay last in both directions. `LessonRow`
references a profile and one of its subjects without duplicating stored data. Upcoming lesson comparisons
use one captured local date/time: a start equal to that time is due now, while a start that has already
passed is compared using its next weekly occurrence. `representativeLesson()` selects the minimum row under
the requested comparison: nearest for ascending slots and furthest for descending slots. Student-level
criteria are constant within one profile, so the slot direction selects its representative even in a mixed
order. Equal slots retain subject insertion order; subjectless profiles use a null subject.

`ModelManager` exposes a `SortedList` over the existing filtered students. The stored address-book list is
not reordered. Original stored positions explicitly resolve complete ties, including after an earlier sort.
The unique-student order compares representative lessons using all supplied criteria; it supplies the
printed index used by View/Edit/Delete and by both displays. `getStudentRows()` exposes one representative
per student in that same order. With slot sorting, `LessonListPanel` numbers these rows by their displayed
position, so every student appears once with the subject/slot actually used for sorting. Counts remain
unique. Indexed commands continue to target this unique-student list without a separate lesson-index map.

The default clock uses the computer's local zone. An injected clock deliberately defines the local zone
for that model; tests compare Singapore and UTC at the same instant to verify this contract. Lesson slots
remain weekly local times rather than timezone-bearing events.

`LogicManager` brackets command execution with `Model#beginCommand()` and `endCommand()` in a `finally`
block. The first sorting refresh captures a reference; further refreshes in that command reuse it. Thus
Edit's profile update and filter reset cannot straddle a lesson boundary with different references.
Query/View capture no new time. No timer reorders rows while a tutor is entering an indexed command.
A repeated Sort or List refreshes upcoming occurrences; indexed targets are resolved before mutation refreshes.

Student and grouped-lesson row snapshots are built lazily and reused until profile data, the filter,
criteria or reference time changes. An address-book listener updates the stored-position map before
transformation listeners compare added/edited profiles. Tie breaks therefore use constant-time lookups
rather than scanning the stored list during every comparison. Profile changes also invalidate cached rows.

#### Design considerations

| Decision | Reason |
| --- | --- |
| `SortedList` over the filtered students | Changes display order while retaining stored/CSV order and the existing unique-student command indexes. Reordering the address book would mix presentation with data changes. |
| One captured local reference per command | Keeps comparisons consistent at lesson boundaries. Reading the clock inside each comparison could change keys while sorting. |
| One representative row per ungrouped student | Keeps natural indexes and whole-profile commands simple. Subject groups retain every lesson because each subject has its own schedule. |
| Lazy snapshots and a stored-position map | Avoid repeated snapshot sorting and linear tie-break scans. Invalidation is explicit when profiles or sorting inputs change. |

#### Sorting within subject groups

`SubjectGrouping` keeps alphabetical subject headings and the printed index of each unique student.
When a sort is active, it maps the already ordered `ModelManager#getLessonRows()` to positions and uses
those positions to order entries within each group. This API still includes every lesson; it does not
collapse to representatives. Comparisons use the same reference time and criterion rules as the ungrouped
display, but each grouped card retains its own subject and slot. Complete lesson ties explicitly fall back
to stored student positions, independently of representative ordering. A student can appear under several
subjects with the same printed student index; deleting that index removes their whole profile and all lessons.
Without active criteria, grouping retains its original alphabetical student order. Subjectless students
remain part of the unique-student list and counts, but do not produce subject groups.

`CommandResult.ListPresentation.REFRESH` requests rebuilding the current presentation without selecting
contacts or subject groups. Applying/resetting Sort uses this request; a bare Sort query uses `UNCHANGED`.
`MainWindow` remembers the current grouped presentation and rebuilds the appropriate panel after an apply
or reset. View, Help, Export and a bare Sort query preserve the existing panel and its scroll position.
Find, other List options and successful profile mutations select the ungrouped presentation, as before.
The active sorting criteria remain available through Logic/Model for the separate persistence feature.

### Weekly subject lessons

`Subject` contains its validated display name and one required `LessonSlot`. `LessonSlot` contains a
`DayOfWeek` and `LocalTime`, which prevents incomplete subjects inside the model. Add and Edit accept one
ordered `s/SUBJECT day/DAY time/TIME` triplet per subject. `ParserUtil` validates the triplet counts and
order, accepts the documented three-letter day names, requires strict 24-hour `HH:mm` times, and rejects
duplicate subject names case-insensitively. List filters continue to compare only the subject name so that
`list s/Maths` works regardless of when each student's Maths lesson begins.

### Combining subject and tag filters

`ListCommandParser` tokenizes both `s/` and `t/` prefixes together, accepting either category order
and interleaved repeated values. It reuses `ParserUtil` validation and `PersonHasAnyTagPredicate`.
`ListCommand` combines its subject-name match with the supplied tag predicate using AND, then applies
active-student visibility. Each category uses OR internally; an omitted category adds no restriction.
The command updates the existing filtered list, so students appear once and the current sort order
and displayed indexes still apply. Archived-only and subject-grouped modes remain standalone.
`Subject.normalizeName()` trims boundary whitespace and collapses runs of ordinary ASCII spaces before
validation and construction; the 1–50 character limit applies to the normalized name. It preserves case
and the lesson slot, and does not permit internal tabs or line breaks. `ParserUtil.parseSubjectName()`
reuses this rule for Add, Edit, List and CSV import. Equality, hashing, name matching and UI grouping
therefore use consistent names. Each student's subject names remain unique, ignoring case, even when
their lesson slots differ. Different students can share a subject heading while retaining their own slots.

JSON adapters construct the same normalized `Subject` model. Reading legacy spaced names does not
rewrite the file; subsequent saves serialize canonical names. If legacy entries for one student collide
after normalization, `JsonAdaptedPerson` rejects the record with `IllegalValueException`, translated by
storage into `DataLoadingException`. Loading fails rather than silently losing a lesson. This is an
upgrade constraint: back up and resolve colliding entries before using the file.

### Shared tag data

Tags are optional descriptive labels, separate from subjects. Add accepts repeated `t/TAG` values
using `ParserUtil.parseTags()`. This validates every occurrence before adding the profile, combines
repeated tags, and produces an empty set when no tags are supplied. The shared contract is:

* `Tag` accepts ASCII letters, digits, spaces, and hyphens, and rejects blank values. It trims surrounding
  whitespace and stores lowercase text using `Locale.ROOT`. Internal spaces are preserved.
* `Person.getTags()` returns an immutable set. Repeated tags and case variants collapse into one value;
  order has no meaning. The five-argument constructor creates a profile without tags, while the
  six-argument constructor accepts a set of tags.
* `Person.isSamePerson()` compares name, phone, address and start date. Outstanding amount, tags,
  notes, subjects and archive state do not distinguish identity. `UniquePersonList`, Add, Edit and Import
  share this rule. Paid and Charge change only the balance and cannot create an identity collision.
  Full names alone need not be unique: different phone, address or start date distinguishes students.
* `Person.equals()` and `hashCode()` include tags, so changes to stored data remain detectable even when
  the student identity stays the same.
* JSON stores a `tags` array of normalized strings in alphabetical order. A missing or null array loads
  as an empty set, preserving compatibility with the previous student-profile schema. Invalid or null
  elements reject the stored profile. For example: `"tags": ["a-level", "needs revision"]`.
* Edit replaces the whole tag set when `t/` is supplied and keeps the existing tags otherwise. A single
  blank `t/` clears all tags. `list t/TAG` filters with `PersonHasAnyTagPredicate`, which reuses the same
  normalization, so `t/A-Level` matches the stored `a-level`.
* `view INDEX` shows the stored lowercase tags. No tag length or count limit is enforced.

`LogicManagerTest` verifies Add with real temporary JSON storage: normalized tags survive loading into
a new model, and invalid tags or duplicate profiles leave saved data and search results unchanged.

### CSV import

`import FILE_PATH [confirm]` reads the CSV format written by `export`, so a file exported from one copy of
TuteeDex can be imported into another. The work is split into three small classes:

* `CsvReader` (in `commons.util`) turns the text into rows of fields. It follows `CsvExporter`'s quoting rules,
  ignores the byte order mark that Excel adds, accepts LF and CRLF, and keeps line breaks inside quoted fields.
  Blank lines are kept as blank rows so that row numbers in error messages match what a spreadsheet shows.
* `CsvPersonParser` (in `logic.parser`) turns one row into a `Person`. It reuses the `ParserUtil` methods used by
  `add`, so imported values follow exactly the same rules and produce the same error messages. Because `Subject`
  holds a `LessonSlot`, a subject cell such as `Maths (Mon 09:00); Physics (Tue 10:00)` is split on semicolons and
  each part is parsed into a name, a day and a start time, matching `Subject#toString()`, which the export uses.
* `ImportCommand` checks the header against `CsvExporter.HEADER`, parses every row, and collects all problems
  before changing anything.

**Duplicates.** The model already rejects two students with the same identity (`Person#isSamePerson()`). A row
that is equal in every field to a stored student, or to an earlier row, is skipped, which makes importing the same
file twice harmless. A row with the same identity but different details is an error, because adding it would break
the uniqueness rule and silently keeping either version could lose data.

**All or nothing.** Without `confirm`, the command only returns a preview. With `confirm`, it copies the current
`AddressBook`, adds every new student to the copy and then calls `Model#setAddressBook()` once. Existing students
are kept, and if any row is invalid the command throws before the copy is made, so the stored data never ends up
half imported. The filter is then reset to active students so the new students are visible.

**Data sandbox.** The course constraints do not allow the app to read files outside its home folder.
`ImportCommand` resolves the path against the folder TuteeDex runs from, the same folder `export` writes to,
and rejects any path that ends up outside it, including `..` steps and links that lead out of the folder.
Tests pass in a temporary folder as the home folder through a package-private constructor.

**Alternatives considered.** Keeping the preview in memory and confirming it with a separate command would avoid
reading the file twice, but it adds state that can go stale if the file or the address book changes in between.
Reading and checking the file again on `confirm` is simpler and always uses the current data.

### Restoring the last view

When TuteeDex opens, it shows the same list the user was looking at when they closed it, in the same order,
such as the subject groups from `list -subjects` sorted by `sort o/desc`.

**What is saved.** `UserPrefs` has two strings. `lastViewCommand` is the `list` or `find` command that last decided
which students are shown, and `lastSortCommand` is the `sort` command that last set the order. An empty string
means the normal list or the default order. Both are saved with the GUI settings in `preferences.json`, because
they are preferences about the display rather than student data.

**When it changes.** After each successful command, `LogicManager#execute()` calls `saveLastView()`:

* A `SortCommand` that applies criteria saves its own command text, and `sort reset` clears the saved order.
  A bare `sort` only reports the order (its result is `ListPresentation.UNCHANGED`), so nothing changes.
  Sorting keeps the current presentation, so it never changes the saved view.
* A `ListCommand` or `FindCommand` saves its own command text.
* A command whose result has `ListPresentation.UNCHANGED` (`view`, `help`, `export`), or that exits the app,
  keeps the saved view, because the display does not change.
* Any other command clears the saved view only if it changed what is on screen. `LogicManager#execute()` reads
  `Model#getFilteredPersonListPredicate()` before the command runs, and `saveLastView()` compares it with the
  filter afterwards. Commands that reset the filter, such as `add`, `edit`, `archive` and `import`, clear the saved
  view. `delete` and an `edit` with no changes keep the filter, so the saved view is kept. The saved view is also
  cleared when the subject groups are replaced by the contact list, which `LogicManager` tracks from each result's
  `ListPresentation`. The saved order stays, because sorting carries over to the normal list.

A command that fails throws before `saveLastView()` runs, so it never changes the saved view. `MainApp#stop()`
already writes `UserPrefs` to disk when the app closes, so no new storage code is needed.

**Restoring.** At the end of `MainWindow#fillInnerParts()`, `restoreLastView()` runs the saved sort command and
then the saved view command through the normal `executeCommand()` path, so the list, the subject groups and the
result box are updated in the same way as when the user types the commands. The sort runs first so that the view
is built in the saved order. The preferences file can be edited by hand, so `LogicManager#getLastSortCommand()`
and `LogicManager#getLastViewCommand()` parse the saved text and return it only if it is a `sort` command, or a
`list` or `find` command. Anything else, such as `delete 1`, is logged and ignored.
A restored `find` can show no students, for example after its only match was deleted, so `restoreLastView()` puts
`MainWindow.MESSAGE_LAST_VIEW_RESTORED` before the find result. A restored `list` shows the student counts instead
of the result box, so it needs no message.
Like `sort`, `list` and `find` override `Command#requiresAddressBookSave()` to return `false`, because they
only change the display. Otherwise, if the data file failed to load and the app started empty, replaying the
saved view would write that empty address book over the user's file.

**Alternatives considered.** Saving the filter itself is not practical, because the filters are `Predicate`
objects built from lambdas and cannot be written to JSON without a separate data form for every kind of filter.
Saving the command text reuses the existing parsers, so new `list` options are restored without extra code.
Making `delete` reset the filter would also keep the saved view and the screen in step, but the index numbers on
screen would change after every deletion, so a following `delete 2` could remove a different student.

### Archiving students

`Person` has an `archived` flag, which is saved in the JSON file and in the CSV export. Records without the flag
load as active. `archive INDEX` and `unarchive INDEX` pick the student from the currently displayed list, like
`delete`, and replace the profile with `Person#withArchived()` through `Model#setPerson()`. Archiving an already
archived student, or unarchiving an active one, is rejected with a `CommandException`.

`Model.PREDICATE_SHOW_ACTIVE_PERSONS` hides archived students. Plain `list`, the subject and tag filters, `find`,
`list -subjects` and the TOTAL/PAID counts all apply it, and `add`, `edit`, `archive` and `unarchive` reset the
filter to it. Only `list -archived` shows the archived students instead.

**Alternatives considered.** Moving archived students to a second list in `AddressBook` would keep them out of
every filter automatically, but `edit`, `export`, `import` and duplicate checks would then have to look in two
places. A flag on `Person` keeps one list and one duplicate check.

### Balance commands

The `paid` and `charge` commands use the index from the currently displayed student list. Their parsers validate
the index and amount before creating a command. `paid INDEX` replaces the balance with zero, while
`paid INDEX AMOUNT` subtracts a positive amount. `charge INDEX AMOUNT` adds a positive amount. Amount arithmetic
is performed by the immutable `OutstandingAmount` value object, so balances remain nonnegative and limited to two
decimal places. Payments greater than the current balance are rejected with a command error before the profile
is modified.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a private tutor who teaches multiple students one-on-one
* needs to keep track of student contact details, weekly lesson slots, and fee payments
* juggles many students at once and needs an easy way to avoid losing track of details
* prefers desktop apps over other types of applications
* is reasonably comfortable using CLI apps

**Value proposition**: Organizes the chaos that comes with tutoring many students privately — track each student's contact details, lesson schedule, and fee payments in one place, so private tutors spend less time on admin and less time chasing missed payments.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                    | I want to …                 | So that I can…                                                                                                         |
|----------|--------------------------------------------|------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `* * *`  | new user                                   | see usage instructions       | refer to instructions when I forget how to use the App                                                                 |
| `* * *`  | private tutor                              | add a student                | keep track of the students I teach                                                                                     |
| `* * *`  | private tutor                              | view a list of all my students | see all my students in one place                                                                                       |
| `* * *`  | private tutor                              | view a student's profile     | quickly access the student's information                                                                               |
| `* * *`  | private tutor                              | edit a student's details     | keep my records accurate when information changes                                                                      |
| `* * *`  | private tutor                              | delete a student             | remove records that are no longer needed                                                                               |
| `* * *`  | private tutor                              | check if a student has paid fees this month | track my payments in a timely manner                                                                                   |
| `* * *`  | private tutor                              | search for a student by name | quickly find a student profile without scrolling                                                                       |
| `* * *`  | private tutor                              | view the current outstanding balance on a student's profile | know exactly how much they owe me                                                                                      |
| `* *`    | private tutor                              | record a student's address   | know where they live, to travel to their home and give lessons / chase money                                           |
| `* *`    | private tutor                              | import students from a file exported by TuteeDex | move my students to another computer without typing them in again                                                       |
| `* *`    | private tutor                              | have TuteeDex open on the list I was last using | continue where I left off without typing the same `list`, `find` or `sort` again                                       |
| `* * *`  | private tutor                              | record each student's subjects with a weekly lesson day and time | know when I teach each student                                                                     |
| `* *`    | private tutor                              | list students by subject     | prepare for lessons of one subject at a time                                                                           |
| `* *`    | private tutor                              | group students under their subjects | see who I teach for each subject at a glance                                                                    |
| `* *`    | private tutor                              | tag students, such as `needs revision`, and list them by tag | focus on students who need the same kind of attention                                                  |
| `* *`    | private tutor                              | add a note to a student      | remember things about the student that do not fit other fields                                                         |
| `* *`    | private tutor                              | sort students by name, balance, start date or next lesson | put the most urgent students first                                                                    |
| `* *`    | private tutor                              | record payments and new charges | keep each student's outstanding balance up to date                                                                  |
| `* *`    | private tutor                              | archive a student who has stopped lessons | hide them from my active list without losing their records                                               |
| `* *`    | private tutor                              | export my students to a CSV file | keep a copy of my records outside the app                                                                          |
| `*`      | private tutor                              | clear all students after a confirmation | start over without deleting the data file by hand                                                             |

### Use cases

(For all use cases below, the **System** is `TuteeDex` and the **Actor** is the `private tutor`.)

**Use case: Add a student profile**

**MSS**

1. Tutor requests to add a student profile with the student's details.
2. TuteeDex validates the supplied details.
3. TuteeDex saves the profile and confirms that it was added.

    Use case ends.

**Extensions**

* 2a. One or more supplied details are invalid.

  * 2a1. TuteeDex shows the relevant validation error and does not add the profile.

    Use case resumes at step 1.

* 2b. An identical profile already exists.

  * 2b1. TuteeDex rejects the duplicate and displays `This contact already exists in TuteeDex`.

    Use case ends.

**Use case: Locate and view a student profile**

**MSS**

1. Tutor requests to list all students or search for a student by name.
2. TuteeDex displays the matching students with their indexes, names and subjects.
3. Tutor selects a student using the index shown in the displayed list.
4. Tutor requests to view the selected profile.
5. TuteeDex displays the student's full profile, including available contact, subject, fee, and note details.

    Use case ends.

**Extensions**

* 2a. No students match the search, or the list is empty.

  * 2a1. TuteeDex informs the tutor that there are no matching students.

    Use case ends.

* 3a. The selected index is invalid or outside the displayed list.

  * 3a1. TuteeDex shows an error and does not display a profile.

    Use case resumes at step 2.

**Use case: Update a student profile**

**MSS**

1. Tutor searches for a student by name.
2. TuteeDex displays matching students.
3. Tutor selects a student using the index shown in the displayed list and supplies the details to update.
4. TuteeDex validates the supplied details and applies the changes while preserving fields the tutor did not specify.
5. TuteeDex refreshes the displayed profile and confirms the update.

    Use case ends.

**Extensions**

* 4a. The index or one or more supplied details are invalid, or the edit would create an identical profile.

  * 4a1. TuteeDex shows the relevant error and leaves the profile unchanged.

    Use case resumes at step 2.

**Use case: Delete a student profile**

**MSS**

1. Tutor requests to list all students or search for a student by name.
2. TuteeDex displays the matching students.
3. Tutor requests to delete a student using the index shown in the displayed list.
4. TuteeDex permanently deletes the selected profile without a confirmation prompt.
5. TuteeDex refreshes and re-numbers the displayed list, then confirms the deletion.

    Use case ends.

**Extensions**

* 2a. No students match the search, or the list is empty.

  * 2a1. TuteeDex informs the tutor that there are no matching students.

    Use case ends.

* 3a. The selected index is invalid or outside the displayed list.

  * 3a1. TuteeDex shows an error and leaves all profiles unchanged.

    Use case resumes at step 2.

### Non-Functional Requirements

1.  **Platform compatibility:** TuteeDex should work on Windows, macOS, and Linux.
2.  **Java version:** TuteeDex should run on a computer with Java 25 installed.
3.  **Command-based usability:** A private tutor who types quickly should be able to complete most common student-management tasks faster with typed commands than with a GUI-only workflow.
4.  **Screen-size usability:** The GUI should work well at resolutions of 1920 x 1080 or higher with 100% or 125% display scaling. It should remain usable at resolutions of 1280 x 720 or higher with 150% scaling.
5.  **Single-user operation:** TuteeDex should support one private tutor using their own local data. It should not depend on shared data files or a multi-user service.
6.  **Local, editable storage:** Student data should be stored locally in a human-editable text file and remain available after the application is restarted.
7.  **Standalone distribution:** TuteeDex should work without an installer or a team-operated remote server. Its deliverable should be packaged as one JAR file no larger than 100 MB.

### Glossary

* **Private tutor**: The single user who uses TuteeDex to manage students they teach.
* **Student (tutee)**: A learner whose tutoring and contact information is recorded in TuteeDex.
* **Student profile**: A record for one student. It contains the student's name, address, phone number, tuition start date, subjects with weekly lesson slots, outstanding balance, tags, notes, and archive status.
* **Outstanding balance**: The amount, in Singapore dollars (SGD), that remains owed for a student's tuition. The amount cannot be negative. It represents the current balance, not a history of individual payments.
* **Archived student**: A student hidden from the active list without being deleted. `list -archived` shows them, and `unarchive` makes them active again.
* **Displayed index**: The one-based number shown beside a student in the current displayed list. After a search, the index refers to that search result list.
* **Duplicate profile**: A profile with the same name, phone, address, start date and outstanding amount as an existing profile. TuteeDex rejects it even if subjects, tags or notes differ; two profiles may share a name if one of those five fields differs.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar "TuteeDex.jar"`.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app with the same command.<br>
       Expected: The most recent window size and location are retained.

### Deleting a student

1. Deleting a student while all active students are being shown

   1. Prerequisites: List all active students using the `list` command, with multiple students in the list.

   1. Test case: `delete 1`<br>
      Expected: The first student is deleted from the list. The status message shows the deleted student's details.

   1. Test case: `delete 0`<br>
      Expected: No student is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Listing students by tags

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm` in this test copy, then add three profiles:
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/A-Level`,
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00 t/Needs revision`, and
   `add n/Cara Dee a/Block 3 p/33333333 d/20-Oct s/Maths day/WED time/11:00`.
2. Run `find Nobody`, then `list t/A-Level t/Needs revision`.<br>
   Expected: Amy and Bob are displayed in that order, with TOTAL 2 / PAID 2.
   This checks OR matching across the full address book, including students hidden by Find.
3. Run `list t/NEEDS REVISION t/needs revision`, then `view 1`.<br>
   Expected: List displays only Bob, with TOTAL 1 / PAID 1. View then replaces the banner with Bob's details.
   The repeated case variants do not duplicate Bob in the results, and the displayed index is used.
4. Run `list t/revision`.<br>
   Expected: An empty list with TOTAL 0 / PAID 0. A substring is not a complete tag match.
5. Run `list`.<br>
   Expected: Amy, Bob, and untagged Cara return, with TOTAL 3 / PAID 3.
6. Run `find Bob`, then try `list t/`, `list t/A-Level t/`, `list t/A-Level x/value`, and `list T/A-Level`.<br>
   Expected: Each command reports an error. Only Bob remains displayed, and no profile fields change.
7. Exit and restart from the same folder, then run `list t/A-Level` and `list`.<br>
   Expected: Amy still matches the tag filter, and all three original profiles remain stored.
8. Compare `list`, `list -summary`, `list t/A-Level`, and `list -summary t/A-Level`.<br>
   Expected: The first two show the same three students and TOTAL 3 / PAID 3. The last two show only
   Amy and TOTAL 1 / PAID 1. Adding the optional flag does not change the visible result.

### Summary flag ordering

`ListCommandParser` recognises one standalone `-summary` before, between, or after filter arguments.
It removes only the flag before delegating subject/tag values to the existing validators.
The flag must precede another prefix/summary flag or end the input, so a hyphenated value such as
`t/Pre-summary` and text inside a multiword value such as `t/Needs -summary revision` are preserved.
Repeated summary flags and combinations with the standalone `-archived` or `-subjects` modes are rejected.

Using the profiles above:

1. Compare `list -summary s/Maths`, `list s/Maths -summary`, and `list s/Maths -summary s/Maths`.<br>
   Expected: Amy and Cara are displayed, with identical TOTAL/PAID counts for all three commands.
2. Compare `list -summary t/A-Level t/Needs revision`, `list t/A-Level -summary t/Needs revision`,
   and `list t/A-Level t/Needs revision -summary`.<br>
   Expected: Amy and Bob are displayed, with identical TOTAL/PAID counts for all three commands.
3. Run `find Bob`, then try `list t/A-Level -summary -summary`, `list t/ -summary`,
   `list -subjects -summary`, and `list -archived -summary`.<br>
   Expected: Each command fails; Bob remains the displayed student and stored profiles are unchanged.

### Combining subject and tag filters

Use the three profiles from the preceding tag-list test in a disposable test copy.

1. Run `find Nobody`, then `list s/Maths s/Physics t/A-Level t/Needs revision`.<br>
   Expected: Amy and Bob appear once each; the GUI banner shows TOTAL 2 / PAID 2.
   Untagged Cara does not match both categories.
2. Run `list t/Needs revision s/Maths s/Physics`.<br>
   Expected: Only Bob appears. Prefix order does not change the matching rule.
3. Run `list -summary s/Maths t/Needs revision`.<br>
   Expected: An empty list with TOTAL 0 / PAID 0. Cara and Amy have Maths but neither has the required tag.
4. Run `sort n/desc`, then `list s/Maths s/Physics t/A-Level t/Needs revision`, and `view 1`.<br>
   Expected: Bob appears before Amy, and View shows Bob. Listing retains the current sort order.
5. Try `list s/Maths t/`, `list t/A-Level s/`, `list -archived s/Maths t/A-Level`, and
   `list -subjects t/A-Level s/Maths`.<br>
   Expected: Each fails without changing the displayed students, sort order, or stored profiles.

### Listing archived students

Use a fresh test copy of the JAR in an empty writable folder.

1. Add two students with different names, then archive the second with `archive 2`.
2. Run `find Nobody`, then `list -archived`.<br>
   Expected: only the archived student appears, with TOTAL 1. PAID is 1 if that student's outstanding
   amount is zero, otherwise 0.
3. Run `view 1`.<br>
   Expected: the archived student's details, not the first stored active student.
4. Run `list`, `list s/Maths`, or `list t/A-Level` with suitable test data.<br>
   Expected: each list excludes archived students. `list -summary` counts active students only.
5. Run `list -archived`, then try `list -archived -summary` and `list t/A-Level -archived`.<br>
   Expected: both invalid combinations fail without changing the archived list or saved profiles.
6. Restart the app, then run `list -archived`.<br>
   Expected: the same archived student remains available. Run `unarchive 1` and repeat
   `list -archived`; expect an empty list with TOTAL 0 / PAID 0.

### Editing a student profile

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm` in this test copy to remove sample profiles, then add two test profiles:
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 o/20` and
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00 o/600`.
2. Run `find Bob`, then `edit 1 p/91234567 o/20.50`.<br>
   Expected: `Edited contact: Bob Choo`. The full list returns, and Bob's name, address, and start date
   remain unchanged.
3. Run `view 2`.<br>
   Expected: Bob's details include phone `91234567` and outstanding amount `SGD 20.50`.
4. Run `find Bob`, then `edit 1 o/20.5`.<br>
   Expected: `No changes made to contact: Bob Choo`. Only Bob remains displayed.
5. Run `edit 1 p/87654321 o/-1`, then `edit 1 o/`.<br>
   Expected: Both commands show the outstanding-amount validation error. Bob's details and search results
   remain unchanged. Try `edit 1 o/20 o/30` too; it should show the repeated-field error.
6. Run `edit 1 n/Amy Bee a/Block 1 p/11111111 d/10-Aug o/20.00`.<br>
   Expected: `This contact already exists in TuteeDex.` Bob remains unchanged in the search results.
7. Run `edit 1 o/0`.<br>
   Expected: `Edited contact: Bob Choo`. The full list returns, and `view 2` shows `SGD 0.00`.
8. Exit and restart the app from the same folder, then run `view 2`.<br>
   Expected: Bob's phone is still `91234567` and outstanding amount is still `SGD 0.00`.

### Editing student tags

Use a fresh copy of the app in an empty folder. Inspect saved tags in `data/addressbook.json`
after successful edits; these checks do not require changes to the profile-card display.

1. Run `clear confirm`, then add
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/Old notes/Keep note` and
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00`.
2. Run `find Amy`, then `edit 1 t/A-Level t/Needs revision t/a-level`.<br>
   Expected: `Edited contact: Amy Bee`, and both students return to the list.
   Amy's stored tags are `a-level` and `needs revision`; `old` is removed and the repeated tag is combined.
   `view 1` still shows subject `Maths` and notes `Keep note`, with all five core fields unchanged.
3. Run `find Amy`, then `edit 1 t/needs revision t/A-LEVEL`.<br>
   Expected: `No changes made to contact: Amy Bee`. Only Amy remains displayed.
4. Try `edit 1 p/87654321 t/A-Level t/` and `edit 1 T/A-Level`.<br>
   Expected: Both commands fail, and Amy's phone, tags, and current search results remain unchanged.
5. Run `edit 1 t/`, then `find Amy` and `edit 1 t/` again.<br>
   Expected: The first edit clears Amy's tags and restores both students. The second reports no changes
   and preserves the search results. Amy's other profile fields remain unchanged.
6. Exit and restart from the same folder.<br>
   Expected: Amy's tags remain empty, and both profiles retain their other details.

### Editing student subjects

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm`, then add
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/A-Level notes/Amy note` and
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/English day/TUE time/10:00 o/600 t/Needs revision notes/Bob note`.
2. Run `find Bob`, then `edit 1 s/Physics day/WED time/15:30 s/H2 Chemistry day/FRI time/17:00`.<br>
   Expected: `Edited contact: Bob Choo`, and both students return to the list.
   `view 2` shows `Subjects: Physics (Wed 15:30), H2 Chemistry (Fri 17:00)` and notes `Bob note`; the five core fields and tags are preserved.
3. Run `find Bob`, then `edit 1 s/h2 chemistry day/FRI time/17:00 s/physics day/WED time/15:30`.<br>
   Expected: `No changes made to contact: Bob Choo`. Only Bob remains displayed.
   `view 1` retains the original subject spelling and order from step 2.
4. Try `edit 1 s/`, `edit 1 s/Maths day/MON`, and `edit 1 p/87654321 t/A-Level s/--- day/MON time/09:00`.<br>
   Expected: Each command fails; Bob's subjects, phone, tags, and current search results remain unchanged.
5. Run `edit 1 n/Amy Bee a/Block 1 p/11111111 d/10-Aug o/0 s/Physics day/WED time/15:30`.<br>
   Expected: `This contact already exists in TuteeDex.` Different subjects, tags, and notes do not bypass duplicate checks.
6. Run `edit 1 p/87654321`, then exit and restart from the same folder.<br>
   Expected: `view 2` shows Bob's updated phone and still lists both scheduled subjects, with his original notes.

### Normalizing subject spaces

Use a fresh copy of the app in an empty folder.

1. Add `add n/Amy Bee p/11111111 a/Block 1 d/10-Aug s/H2   Maths day/MON time/09:00`.<br>
   Expected: `view 1` shows `H2 Maths (Mon 09:00)`. Both `list s/H2 Maths` and
   `list s/h2   maths` find Amy.
2. Run `edit 1 s/h2  maths day/MON time/09:00`.<br>
   Expected: No changes are made, and the original capitalization is retained.
3. Run `edit 1 s/H2 Maths day/MON time/09:00 s/h2   Maths day/TUE time/10:00`.<br>
   Expected: Duplicate-subject error; Amy's original lesson remains intact.
4. Add `add n/Bob Choo p/22222222 a/Block 2 d/19-Sep s/h2  maths day/TUE time/10:00`,
   then run `list -subjects`.<br>
   Expected: One subject heading contains both students with their different slots. Restart and check
   their profiles; names have single spaces and both schedules are preserved.

### Editing student notes

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm`, then add
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/A-Level notes/Original note` and
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00`.
2. Run `find Amy`, then `edit 1 notes/Revise fractions, graphs & 中文!`.<br>
   Expected: `Edited contact: Amy Bee`, and both students return to the list.
   `view 1` shows `Notes: Revise fractions, graphs & 中文!`; all other profile fields are unchanged.
3. Run `find Amy`, then repeat `edit 1 notes/Revise fractions, graphs & 中文!`.<br>
   Expected: `No changes made to contact: Amy Bee`. Only Amy remains displayed.
4. Try `edit 1 notes/One notes/Two`, `edit 1 notes/Replacement p/invalid`, and `edit 1 notes/Review x/value`.<br>
   Expected: Each command fails; Amy's saved note, other fields, and search results remain unchanged.
5. Run `edit 1 notes/Call parent p/87654321`.<br>
   Expected: Amy's note becomes `Call parent` and her phone becomes `87654321`.
   A recognized prefix starts another field rather than becoming literal note text.
6. Run `find Amy`, then `edit 1 notes/`, `find Amy`, and `edit 1 notes/` again.<br>
   Expected: The first edit clears the note and returns the full list. The second reports no changes
   and preserves the search results. View shows an empty `Notes:` value.
7. Exit and restart from the same folder.<br>
   Expected: Amy's note remains empty, her phone is `87654321`, and her subjects and tags remain unchanged.

### Viewing help

1. Run `help`, or use **F1**. Expect the command-format window.
2. Scroll to the last command and hover over a long format such as `add`.
   Expect all commands to be reachable and the full format to appear in a tooltip.
3. Select the blue `here` link. Expect the online User Guide in the default browser.
4. Close help with its pixel button, reopen it, then press Escape.
   Expect only the help window to close each time; the main window stays open.
5. Resize help to its minimum size. Expect scrolling and the close button to remain usable.
6. Select the matching pixel close button in the main window. Expect the application
   to exit and retain its window size and position on the next launch.

### Header clock and command results

1. Compare the header date and time with the computer's local clock. Expect a numeric
   day/month and 24-hour time, with two spaces around the hyphen and colon.
   Leave the app open across a minute boundary; the time should update without a command.
2. Run `view 1` for an existing student. Expect long feedback to wrap into a two-line
   display; scroll vertically to read the remainder. Hovering must not cover it with a tooltip.
3. Resize the window. Feedback should switch between one line and the wrapping display
   according to the available width. Run `list` to restore the TOTAL/PAID summary.

### Sorting students

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm`, then add
   `add n/Amy Bee p/11111111 a/Block 1 d/10-Aug o/2 s/Maths day/MON time/09:00 s/Physics day/FRI time/10:00`
   and `add n/Bob Choo p/22222222 a/Block 2 d/01-Jan o/10 s/Maths day/TUE time/11:00`.
2. Run `sort o/desc n/asc`.<br>
   Expected: Bob is row `001` and Amy is row `002`. `view 1` shows Bob's full profile.
3. Run `sort slot/asc`, then `sort slot/desc`.<br>
   Expected: Two student rows remain. Each row shows the student's nearest upcoming subject for ascending,
   or furthest upcoming subject for descending, relative to the computer's current local time. Compare the
   selected subject with that student's full schedule in View. All subjects remain stored in the profile.
4. Run `list -subjects`, then `sort slot/asc n/asc`.<br>
   Expected: Maths and Physics headings stay alphabetical; each group uses its own lessons' schedules.
   Amy appears in both groups with the same printed index. View preserves the panel and scroll position.
5. Run `sort reset`, then `list`.<br>
   Expected: Reset keeps grouping and restores alphabetical students within groups. List returns to one
   ungrouped row per student in stored order. Applying slot sorting again keeps two ungrouped student rows.
6. Try `sort o/desc,n/asc`, `sort n/up`, `sort n/asc n/desc`, and `sort RESET`.<br>
   Expected: Each command fails without changing the selected order, filter or displayed panel. The comma
   error explains the criteria format, while an unsupported direction explains the allowed directions.

### Importing students

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear confirm`, then add
   `add n/Amy Bee a/Block 1, #01-01 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/A-Level notes/Revise "fractions"`
   and `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00 o/120.50`. Run `archive 2`.
2. Run `export` and note the file name in the result, for example `contacts-20261024-175424-176.csv`.
3. Run `clear confirm`, then `import contacts-20261024-175424-176.csv`.<br>
   Expected: A preview names Amy Bee and Bob Choo, and the list is still empty.
4. Run `import contacts-20261024-175424-176.csv confirm`.<br>
   Expected: `Imported 2 student(s) ...`. Amy is listed; `list -archived` shows Bob. `view 1` shows Amy's
   subject `Maths (Mon 09:00)`, tag and note exactly as before.
5. Run the same confirm command again.<br>
   Expected: `No new students to import...`, and the list does not change.
6. Open the file in a text editor, change Amy's phone to `123` and Bob's subjects to `Physics`, and save it as a
   new `.csv` file. Import it with `confirm`.<br>
   Expected: Two numbered row errors, and no student is added.
7. Try `import missing.csv`, `import notes.txt` and `import` with no path.<br>
   Expected: Each command fails with a specific message.

### Recording payments and charges

Use a fresh copy of the app in an empty folder.

1. Run `clear confirm`, then `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 o/50`.
2. Run `charge 1 20.50`.<br>
   Expected: `Amy Bee's outstanding balance is now SGD 70.50.`
3. Run `paid 1 0.50`, then `paid 1 100`.<br>
   Expected: The first leaves `SGD 70.00`. The second is rejected because it is more than the balance.
4. Run `paid 1`.<br>
   Expected: The balance is `SGD 0.00`, and `list` shows PAID 1.
5. Try `charge 1`, `charge 1 0`, `charge 1 -5`, `paid 1 1.234` and `paid 2`.<br>
   Expected: Each command fails, and Amy's balance stays `SGD 0.00`.

### Archiving students

1. Run `clear confirm`, then add two students and run `archive 2`.<br>
   Expected: `Archived <name>.`, and only the first student remains in the list.
2. Run `find` with the archived student's name.<br>
   Expected: No students are listed.
3. Run `list -archived`, then `archive 1`.<br>
   Expected: `<name> is already archived.`
4. Run `unarchive 1`.<br>
   Expected: `Unarchived <name>.`, and both students are in the active list again.
5. Try `archive 0`, `archive x` and `archive 9`.<br>
   Expected: Each command fails without changing any student.

### Clearing all students

1. Run `clear`.<br>
   Expected: A message says how many students would be deleted. The list does not change.
2. Run `clear 3` and `clear Confirm`.<br>
   Expected: Both are rejected with the usage message. No student is deleted.
3. Run `clear confirm`.<br>
   Expected: `Deleted all N student(s) from TuteeDex.`, and `list` and `list -archived` are both empty.

### Restoring the last view

1. Run `list -subjects`, then close TuteeDex with `exit`. Open it again.<br>
   Expected: The subject groups are shown, as after typing `list -subjects`.
2. Run `find Alex`, close the window and open TuteeDex again.<br>
   Expected: Only the students matching `Alex` are listed.
3. Run `list -archived`, then `view 1`, then `exit`. Open TuteeDex again.<br>
   Expected: The archived students are listed, because `view` does not change the saved view.
4. Run `list -subjects`, then add any student with `add`, then `exit`. Open TuteeDex again.<br>
   Expected: The normal list is shown.
5. Close TuteeDex. In `preferences.json`, change `lastViewCommand` to `"delete 1"` and open TuteeDex.<br>
   Expected: The normal list is shown, and no student is deleted.
6. Run `sort o/desc n/asc`, then `list -subjects`, then `exit`. Open TuteeDex again.<br>
   Expected: The subject groups are shown, with the largest balances first in each group.
7. Run `sort reset`, then `exit`. Open TuteeDex again.<br>
   Expected: Students are in the default order.
8. Run `find` with the name of one student, then `delete 1`, then `exit`. Open TuteeDex again.<br>
   Expected: No students are listed, and the result box says this is the view from when TuteeDex was last closed.

### Saving data

Use a disposable copy of the built JAR in an empty writable folder. Close the application before editing files.

1. Create `data/addressbook.json` containing only `null`, then launch the JAR.<br>
   Expected: The application opens with an empty address book and logs that the data file could not be loaded,
   rather than failing with an uncaught `NullPointerException`. Close the window using its close button,
   without entering any commands, and check that the student-data file still contains `null`.
2. Repeat with `{"persons":[null]}`, `{"persons":null}`, and `{}`.<br>
   Expected: Each invalid file follows the same recovery path and is preserved during loading and shutdown.
3. Start with a valid exported JSON student record, then insert `null` beside it in the `persons` array.<br>
   Expected: The entire file is rejected; the valid student is not loaded on its own and the original file remains intact.
4. Replace the file with `{"persons":[]}` and launch again.<br>
   Expected: The valid empty address book loads without a data-loading warning.
5. Move the student-data file out of the test folder and launch again.<br>
   Expected: Missing-file behaviour is unchanged: the application shows its sample students.

These checks stop before executing commands that save the address book; such commands can overwrite the data file.
Keep a backup before editing or attempting to recover real student data.
