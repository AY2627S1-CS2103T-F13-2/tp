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

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

* The header contains the application title and the File and Help menus.
* `ResultDisplay` shows either the TOTAL/PAID summary or command feedback. Feedback appears on a horizontally scrollable line, with line breaks replaced by ` | `; a tooltip preserves the original message.
* `PersonListPanel` displays the filtered `ObservableList<Person>` obtained from `Logic`. Its custom `PersonListViewCell` creates a `PersonCard` for each non-empty cell, so changes to the observable list update the displayed students.
* `PersonCard` shows a three-digit, zero-padded list index, the student's name, and subjects. Long names and subject text are abbreviated; tooltips expose the full profile and subjects, and accessible text includes the profile details.
* `CommandBox` accepts commands at the bottom of the window. The FIND button calls `prepareFind()`, which inserts `find ` only when the input is blank, then focuses the input and moves the caret to its end without executing a command.
* The exit button and File > Exit close the application window. Help is available through the Help menu and F1, using a `HelpWindow` created with `MainWindow` and reused when requested.

`MainWindow`, `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonCard`, and `HelpWindow` inherit from the abstract `UiPart<T>` class. `UiPart` loads an FXML layout, sets the controller, and exposes the root JavaFX object through `getRoot()`. `StatusBarFooter` and its FXML file remain in the source tree, but the current `MainWindow` does not instantiate or display a status bar; it is omitted from the diagram above.

The layouts are defined in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F13-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) defines the main layout and loads `TuteeDex.css`, while `PersonListCard.fxml` defines the layout for `PersonCard`. The main window also loads the bundled pixel fonts. `HelpWindow` has its own FXML layout and stylesheet.

The UI depends on `Model` classes to display `Person` data and to identify students whose outstanding amount is zero for the PAID count. Commands and access to the filtered list and GUI settings go through `Logic`.

The following sequence diagram shows submission of a successful `list` command and the resulting summary:

<puml src="diagrams/UiCommandSequenceDiagram.puml" alt="UI command execution and list summary"/>

1. When Enter is pressed, `CommandBox` ignores an empty string; otherwise, it passes the input to its `CommandExecutor`. `MainWindow` supplies this callback as `this::executeCommand`, keeping `CommandBox` independent of `Logic`.
1. `MainWindow` calls `Logic.execute(commandText)` and displays the returned `CommandResult` feedback. For a successful `list` command, it then replaces that feedback with the TOTAL/PAID summary. `showListSummary()` reads the filtered list: TOTAL is its size, and PAID counts students whose outstanding amount equals `OutstandingAmount.ZERO`. The summary is calculated at startup and after successful `list` commands; it is not continuously bound to list changes.
1. `MainWindow` also checks the result's help and exit flags. Help opens or focuses the existing `HelpWindow`; exit saves the current window size and position through `Logic.setGuiSettings()`, then hides the help and main windows. The menu and exit button use the same handlers.
1. When execution succeeds, `CommandBox` clears the input. If `Logic` throws a `CommandException` or `ParseException`, `MainWindow` displays the exception message and rethrows it. `CommandBox` retains the input and applies its error style, which is removed when the text next changes.

The sequence diagram omits error handling, help/exit flag handling, and the observable list's JavaFX cell updates for clarity.

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
* `AddressBookParser` first separates the command word from its arguments and dispatches the input to the matching command parser. The current command-specific parsers are `AddCommandParser`, `EditCommandParser`, `DeleteCommandParser`, `FindCommandParser`, `ListCommandParser`, `ArchiveCommandParser`, `UnarchiveCommandParser`, `ExportCommandParser`, and `ViewCommandParser`.
* Commands without arguments (`clear`, `exit`, and `help`) are constructed directly by `AddressBookParser`. All other supported command words are delegated to a parser that creates the corresponding concrete `Command` object.
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
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
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
  `JsonSerializableAddressBook.toModelType()` also rejects duplicate students. These conversions explain
  the Storage component's dependency on the Model component; model classes are omitted from this diagram.
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

`SortOrder` is an immutable, ordered list of field/direction pairs. Its first criterion has the highest
priority. Fields cannot repeat, and an empty order represents the original stored order. The supported
fields are student name, outstanding amount, start date, and upcoming lesson slot. A lesson slot is a
combined day/time criterion; each lesson row associates that schedule with its subject.

`SortOrderParser` validates criteria arguments such as `o/desc n/asc` or `slot/asc`, preserving their order
and independent directions. It rejects malformed arguments, unknown fields, repeated fields, and directions
other than lowercase `asc` or `desc`. The criteria model and parser do not mutate profiles or user preferences.
`SortCommandParser` handles the current-order query and reset, and the registered `SortCommand` applies
criteria to the session's `ModelManager`. Saving and restoring state belongs to the separate persistence feature.

`SortComparators` compares names without regard to case, amounts numerically, and start dates as annual
month/day values. Invalid existing dates and absent lessons stay last in both directions. `LessonRow`
references a profile and one of its subjects without duplicating stored data. Upcoming lesson comparisons
use one captured local date/time: a start equal to that time is due now, while a start that has already
passed is compared using its next weekly occurrence. Complete ties retain input order.

`ModelManager` exposes a `SortedList` over the existing filtered students. The stored address-book list is
not reordered. Original stored positions explicitly resolve complete ties, including after an earlier sort.
The unique-student order compares the student attributes only; it supplies the printed index used by
View/Edit/Delete. When slot sorting is active, `LessonListPanel` displays separately sorted `LessonRow`
references, so one student can have multiple rows sharing that index. Counts continue to use unique students.
Deleting a printed student index removes the profile and therefore all its lesson rows.

The default clock is the computer's local clock; tests inject a fixed clock through the model constructor.
Sort, filter changes, and profile mutations capture a fresh reference time. Query/View do not recalculate
time-based ordering. No timer reorders rows while a tutor is entering an indexed command. A repeated Sort
or List refreshes upcoming occurrences, and indexed command targets are resolved before mutation refreshes.

#### Sorting within subject groups

`SubjectGrouping` keeps alphabetical subject headings and the printed index of each unique student.
When a sort is active, it maps the already ordered `ModelManager#getLessonRows()` to positions and uses
those positions to order entries within each group. Comparisons therefore use the same reference time,
priority rules and stable ties as the ungrouped display. Each grouped card shows its own subject and slot.
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

### Shared tag data

Tags are optional descriptive labels, separate from subjects. Add accepts repeated `t/TAG` values
using `ParserUtil.parseTags()`. This validates every occurrence before adding the profile, combines
repeated tags, and produces an empty set when no tags are supplied. The shared contract is:

* `Tag` accepts ASCII letters, digits, spaces, and hyphens, and rejects blank values. It trims surrounding
  whitespace and stores lowercase text using `Locale.ROOT`. Internal spaces are preserved.
* `Person.getTags()` returns an immutable set. Repeated tags and case variants collapse into one value;
  order has no meaning. The five-argument constructor creates a profile without tags, while the
  six-argument constructor accepts a set of tags.
* `Person.isSamePerson()` compares name, phone, address, start date, and outstanding amount, ignoring tags.
  Profiles with the same core fields but different tags are duplicates. The outstanding-amount identity
  discussion in issue #73 remains separate and this change preserves its current behavior.
* `Person.equals()` and `hashCode()` include tags, so changes to stored data remain detectable even when
  the student identity stays the same.
* JSON stores a `tags` array of normalized strings in alphabetical order. A missing or null array loads
  as an empty set, preserving compatibility with the previous student-profile schema. Invalid or null
  elements reject the stored profile. For example: `"tags": ["a-level", "needs revision"]`.
* Existing Edit commands preserve tags while editing the other fields. Tag editing and filtering are
  separate follow-ups (#81 and #82); they should reuse `Tag` normalization and set equality.

Add tag input, shared storage, and validation are implemented in issue #80. Another team member owns
extending View for all new fields. Profile-card changes are deferred to That Li's UI work; neither
display change is part of the Add Tags contribution. Both should read `Person.getTags()` and display
the stored lowercase labels, preferably in alphabetical order. No tag length or count limit is
introduced by this contract.

`LogicManagerTest` verifies Add with real temporary JSON storage: normalized tags survive loading into
a new model, and invalid tags or duplicate profiles leave saved data and search results unchanged.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
* needs to keep track of student contact details, parent contact details, and fee payments
* juggles many students at once and needs an easy way to avoid losing track of details
* prefers desktop apps over other types of applications
* is reasonably comfortable using CLI apps

**Value proposition**: Organizes the chaos that comes with tutoring many students privately — track each student's contact details, parent contacts, and fee payments in one place, so private tutors spend less time on admin and less time chasing missed payments.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                    | I want to …                 | So that I can…                                                                                                         |
|----------|--------------------------------------------|------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `* * *`  | new user                                   | see usage instructions       | refer to instructions when I forget how to use the App                                                                 |
| `* * *`  | user                                       | add a new person             |                                                                                                                        |
| `* * *`  | user                                       | delete a person              | remove entries that I no longer need                                                                                   |
| `* * *`  | user                                       | find a person by name        | locate details of persons without having to go through the entire list                                                 |
| `* *`    | user                                       | hide private contact details | minimize chance of someone else seeing them by accident                                                                |
| `*`      | user with many persons in the address book | sort persons by name         | locate a person easily                                                                                                 |
| `* * *`  | private tutor                              | add a student                | keep track of the students I teach                                                                                     |
| `* * *`  | private tutor                              | view a list of all my students | see all my students in one place                                                                                       |
| `* * *`  | private tutor                              | view a student's profile     | quickly access the student's information                                                                               |
| `* * *`  | private tutor                              | edit a student's details     | keep my records accurate when information changes                                                                      |
| `* * *`  | private tutor                              | delete a student             | remove records that are no longer needed                                                                               |
| `* * *`  | private tutor                              | check if a student has paid fees this month | track my payments in a timely manner                                                                                   |
| `* * *`  | private tutor                              | search for a student by name | quickly find a student profile without scrolling                                                                       |
| `* * *`  | private tutor                              | view the current outstanding balance on a student's profile | know exactly how much they owe me                                                                                      |
| `* *`    | private tutor                              | record a student's address   | know where they live, to travel to their home and give lessons / chase money                                           |

*{More to be added}*

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
2. TuteeDex displays the matching students with their names and phone numbers.
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
* **Student profile**: A record for one student. It can contain the student's name, address, phone number, subjects, tuition start date, outstanding balance, guardian contact, education level, and notes.
* **Guardian contact**: The phone number of a student's parent or guardian, recorded separately from the student's own phone number.
* **Outstanding balance**: The amount, in Singapore dollars (SGD), that remains owed for a student's tuition. The amount cannot be negative. It represents the current balance, not a history of individual payments.
* **Displayed index**: The one-based number shown beside a student in the current displayed list. After a search, the index refers to that search result list.
* **Duplicate profile**: A new profile whose stored details are identical to an existing profile. TuteeDex rejects an exact duplicate; two profiles may have the same student name if their other details differ.

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

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Listing students by tags

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear` in this test copy, then add three profiles:
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 t/A-Level`,
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00 t/Needs revision`, and
   `add n/Cara Dee a/Block 3 p/33333333 d/20-Oct s/Maths day/WED time/11:00`.
2. Run `find Nobody`, then `list t/A-Level t/Needs revision`.<br>
   Expected: Amy and Bob are displayed in that order, with `Listed 2 contacts.`.
   This checks OR matching across the full address book, including students hidden by Find.
3. Run `list t/NEEDS REVISION t/needs revision`, then `view 1`.<br>
   Expected: Only Bob is displayed, with `Listed 1 contact.`. View shows Bob's details.
   The repeated case variants do not duplicate Bob in the results, and the displayed index is used.
4. Run `list t/revision`.<br>
   Expected: An empty list with `No contacts match the specified tags.`. A substring is not a complete tag match.
5. Run `list`.<br>
   Expected: Amy, Bob, and untagged Cara return, with `Listed 3 contacts.`.
6. Run `find Bob`, then try `list t/`, `list t/A-Level t/`, `list t/A-Level x/value`, and `list T/A-Level`.<br>
   Expected: Each command reports an error. Only Bob remains displayed, and no profile fields change.
7. Exit and restart from the same folder, then run `list t/A-Level` and `list`.<br>
   Expected: Amy still matches the tag filter, and all three original profiles remain stored.

### Listing archived students

Use a fresh test copy of the JAR in an empty writable folder.

1. Add two students with different names, then archive the second by its full name.
2. Run `find Nobody`, then `list -archived`.<br>
   Expected: only the archived student appears, with `Listed 1 contact.`.
3. Run `view 1`.<br>
   Expected: the archived student's details, not the first stored active student.
4. Run `list`, `list s/Maths`, or `list t/A-Level` with suitable test data.<br>
   Expected: each list excludes archived students. `list -summary` counts active students only.
5. Run `list -archived`, then try `list -archived -summary` and `list t/A-Level -archived`.<br>
   Expected: both invalid combinations fail without changing the archived list or saved profiles.
6. Restart the app, then run `list -archived`.<br>
   Expected: the same archived student remains available. Unarchive the student and repeat
   `list -archived`; expect an empty list with `No archived contacts.`.

### Editing a student profile

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear` in this test copy to remove sample profiles, then add two test profiles:
   `add n/Amy Bee a/Block 1 p/11111111 d/10-Aug s/Maths day/MON time/09:00 o/20` and
   `add n/Bob Choo a/Block 2 p/22222222 d/19-Sep s/Physics day/TUE time/10:00 o/600`.
2. Run `find Bob`, then `edit 1 p/91234567 o/20.50`.<br>
   Expected: `Edited contact: Bob Choo`. The full list returns; Bob's card immediately shows the new phone
   and `SGD 20.50`, while his name, address, and start date remain unchanged.
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
   Expected: `Edited contact: Bob Choo`. The full list returns, and Bob's card shows `SGD 0.00`.
8. Exit and restart the app from the same folder, then run `view 2`.<br>
   Expected: Bob's phone is still `91234567` and outstanding amount is still `SGD 0.00`.

### Editing student tags

Use a fresh copy of the app in an empty folder. Inspect saved tags in `data/addressbook.json`
after successful edits; these checks do not require changes to the profile-card display.

1. Run `clear`, then add
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

1. Run `clear`, then add
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

### Editing student notes

Use a fresh copy of the app in an empty folder so these checks do not affect real student records.

1. Run `clear`, then add
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

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
