"""Check SE-EDU commit-message conventions without third-party dependencies."""

import argparse
import html
import os
import re
import subprocess
from dataclasses import dataclass
from pathlib import Path


GUIDE = "https://se-education.org/guides/conventions/git.html"
TRAILER = re.compile(r"^(?:Co-authored-by|Signed-off-by|Reviewed-by|Acked-by):", re.I)
SCOPE = re.compile(r"^[^:\n]+:\s+")
NON_IMPERATIVE = re.compile(
    r"^(?:Added|Adding|Updated|Updating|Fixed|Fixing|Changed|Changing|"
    r"Removed|Removing|Implemented|Implementing|Resolved|Resolving|"
    r"Created|Creating|Modified|Modifying|Refactored|Refactoring|"
    r"Improved|Improving|Tested|Testing|Documented|Documenting)\b", re.I
)


@dataclass(frozen=True)
class Finding:
    level: str
    line: int
    message: str


def inspect_message(message):
    """Return objective errors and advisory findings; never infer grammar as an error."""
    lines = message.splitlines()
    subject = lines[0] if lines else ""
    findings = []

    def add(level, line, explanation):
        findings.append(Finding(level, line, explanation))

    if not subject.strip():
        add("error", 1, "Provide a nonblank commit subject.")
    if len(subject) > 72:
        add("error", 1, f"Subject has {len(subject)} characters; the hard limit is 72.")
    elif len(subject) > 50:
        add("warning", 1, "Consider shortening the subject to the suggested 50 characters.")
    if subject.rstrip().endswith("."):
        add("error", 1, "Do not end the subject with a period.")

    scope = SCOPE.match(subject)
    action = subject[scope.end():] if scope else subject
    # The guide explicitly permits lowercase category prefixes and lowercase
    # action words after scopes (e.g. 'chore: Update' and 'Find command: make').
    if not scope and subject and subject[0].islower():
        add("error", 1, "Capitalize the first letter of an unscoped subject.")
    if NON_IMPERATIVE.match(action):
        add("warning", 1, "Likely non-imperative wording; consider 'Add', 'Update', 'Fix', etc.")

    if len(lines) > 1 and lines[1].strip():
        add("error", 2, "Separate the subject and body with a blank line.")

    body = lines[1:]
    explanatory_body = [line for line in body if line.strip() and not TRAILER.match(line)]
    if not explanatory_body:
        add("warning", 1, "No explanatory body: if this change is non-trivial, explain what and why.")

    for number, line in enumerate(body, start=2):
        if len(line) <= 72:
            continue
        if TRAILER.match(line) or any(len(token) > 72 for token in line.split()):
            add("warning", number,
                f"Line has {len(line)} characters but contains a trailer or unbreakable token; review manually.")
        else:
            add("error", number, f"Body line has {len(line)} characters; wrap it at 72.")
    return findings


def git(*arguments):
    return subprocess.check_output(
        ["git", *arguments], encoding="utf-8", errors="replace"
    ).rstrip("\r\n")


def introduced_commits(base, head):
    """Exclude shared history and merge commits, including merged upstream history."""
    for revision in (base, head):
        if not re.fullmatch(r"[0-9a-fA-F]{40,64}", revision):
            raise ValueError("Base and head must be full commit SHAs.")
        git("cat-file", "-e", revision + "^{commit}")
    return git("rev-list", "--reverse", "--no-merges", f"{base}..{head}").splitlines()


def escape_annotation(value, property_value=False):
    value = value.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    if property_value:
        value = value.replace(":", "%3A").replace(",", "%2C")
    return value


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", required=True)
    parser.add_argument("--head", required=True)
    args = parser.parse_args()
    summaries = ["# Commit message conventions", "", f"Based on the [SE-EDU guide]({GUIDE}).", "",
                 "Only commits introduced by this PR are checked. Merge commits are excluded.", "",
                 "Warnings do not fail this check. They are not a certification of full compliance.", ""]
    errors = warnings = 0
    commits = introduced_commits(args.base, args.head)
    for sha in commits:
        message = git("show", "--no-patch", "--no-show-signature", "--format=%B", sha)
        findings = inspect_message(message)
        subject = message.splitlines()[0] if message else "(empty subject)"
        print(f"Commit {sha[:12]}: {subject}")
        summaries.extend([f"## {sha[:12]}", "", f"<code>{html.escape(subject)}</code>", ""])
        if not findings:
            summaries.append("Passed.")
        for finding in findings:
            errors += finding.level == "error"
            warnings += finding.level == "warning"
            title = escape_annotation(f"Commit {sha[:12]}, message line {finding.line}", True)
            print(f"::{finding.level} title={title}::{escape_annotation(finding.message)}")
            summaries.append(f"- **{finding.level.title()}**, message line {finding.line}: "
                             + html.escape(finding.message))
        summaries.append("")
    result = f"Checked {len(commits)} commit(s): {errors} error(s), {warnings} warning(s)."
    print(result)
    summaries.append(result)
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        Path(os.environ["GITHUB_STEP_SUMMARY"]).write_text("\n".join(summaries) + "\n", encoding="utf-8")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
