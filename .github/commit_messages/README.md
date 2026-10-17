# Commit message checks

Every PR to `master` is checked, including draft PRs and PRs from personal
forks. New pushes to an open PR rerun the same check. No local hook or
change of Git workflow is required. Unpushed commits cannot be checked.

The `Commit message conventions` check inspects non-merge commits in
`base..head`, not the PR title, inherited history, or GitHub's synthetic
merge commit. Normal upstream merges are excluded from message checks.

Rules follow https://se-education.org/guides/conventions/git.html:

| Blocking errors | Advisory warnings (check still passes) |
| --- | --- |
| Blank subject or subject longer than 72 characters | Subject longer than the suggested 50 characters |
| Lowercase initial on an unscoped subject | Likely past-tense or progressive subject |
| Subject ending with a period | Missing explanatory body; reviewer judges whether needed |
| No blank line between subject and body | Overlong trailers or lines containing unbreakable tokens |
| Ordinary body line longer than 72 characters | Other grammar, clarity, and what/why issues need human review |

Scope/category prefixes follow the guide's examples; lowercase prefixes
and lowercase action words after a scope are allowed. Warnings are not
an exemption from the guide. Branch naming is outside this check.

Open the failed check's Details and job summary to find the commit SHA,
message line, and reason. Adding a later fix commit does not correct an
earlier commit message. Do not rewrite merged team history. For messages
already pushed on unmerged branches, follow the tutor-approved policy
on rewording and timestamps rather than automatically rebasing or
force-pushing. Contact the team lead if correction is unclear.

Example:

```text
Add optional student notes

Tutors need to retain student-specific teaching information.

Store an optional note with each student and preserve it across
saving and loading. Default older records to an empty note.
```

Run locally if desired (full commit SHAs are required):

```sh
python3 -m unittest discover -s .github/commit_messages -p 'test_*.py' -v
python3 .github/commit_messages/check.py --base BASE_SHA --head HEAD_SHA
```

To enforce this on GitHub, require `Commit message conventions` in the
protection/ruleset for `master` after this workflow is available. Running
the Action alone does not block merges. The workflow uses only read
permissions and does not require secrets. It does not assess Java style;
the existing Gradle/Checkstyle workflow remains responsible for that.
