---
  layout: default.md
  title: "Hein Lin Htet's Project Portfolio Page"
---

### Project: TuteeDex

TuteeDex is a desktop application for private tutors to manage their students' profiles, weekly lessons and
outstanding fees. The user interacts with it using a CLI, and it has a GUI created with JavaFX. It is written in Java.

Given below are my contributions to the project.

* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=henry336&breakdown=true)

* **New Feature**: View a student's full profile with `view INDEX` (#52, #95).

* **New Feature**: Import students from a CSV file exported by TuteeDex, with a preview before confirming (#126).
  * Highlights: the whole file is checked first, so an import is all or nothing. Files outside the app folder are
    refused.

* **New Feature**: Reopen TuteeDex with the last list or find view and the last sort order (#159).

* **New Feature**: Recall earlier commands with Up and Down (#181).

* **Enhancements to existing features**:
  * Notes field in `add` (#90). Exit that rejects extra arguments (#51).
  * Find that shows active students only with clearer feedback (#96), and `list -archived` (#97).
  * Archive and unarchive by index, and a confirmation step for `clear` (#173, #181).
  * Data safety: commands that only show information no longer write the data file, and a failed save is rolled
    back so a retried command is not applied twice (#210).
  * Amount limit of SGD 1,000,000 and clearer amount and view messages (#211).
  * Kept the main window and Help window on screen when the saved position is off screen (#192).

* **Documentation**:
  * User Guide: brought the whole guide in line with the product for v1.5 (#190), plus sections for view, import,
    reopening where you left off and saving the data.
  * Developer Guide: Storage diagram (#103), CSV import and last-view implementation sections, and the final DG
    update for v1.6.

* **Project management**:
  * Team lead: planned each milestone, ran weekly meetings, and coordinated alpha and beta testing.
  * Released `v1.4` on GitHub.
  * Added the SE-EDU commit message check that runs on every pull request (#83).
