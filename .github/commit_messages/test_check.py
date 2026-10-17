"""Regression tests for blocking rules, advisory rules, and history selection."""

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from check import escape_annotation, inspect_message, introduced_commits


class MessageTests(unittest.TestCase):
    def errors(self, message):
        return [item for item in inspect_message(message) if item.level == "error"]

    def test_good_message(self):
        self.assertEqual([], inspect_message("Add student notes\n\nTutors need teaching context.\n\nStore optional notes."))

    def test_subject_hard_limit(self):
        self.assertFalse(self.errors("A" * 72))
        self.assertTrue(self.errors("A" * 73))

    def test_subject_soft_limit_is_advisory(self):
        findings = inspect_message("A" * 51)
        self.assertTrue(any("50 characters" in item.message for item in findings))
        self.assertFalse(self.errors("A" * 51))

    def test_lowercase_unscoped_subject(self):
        self.assertTrue(self.errors("add notes"))

    def test_scopes_allow_guide_examples(self):
        for subject in ("chore: Update release date", "bug fix: Add space after name",
                        "Find command: make matching case-insensitive", "Main.java: Remove blank lines"):
            with self.subTest(subject=subject):
                self.assertFalse(self.errors(subject))

    def test_period(self):
        self.assertTrue(self.errors("Add notes."))

    def test_non_imperative_is_advisory(self):
        for subject in ("Added notes", "Updated notes", "scope: Adding notes"):
            with self.subTest(subject=subject):
                self.assertFalse(self.errors(subject))
                self.assertTrue(any("non-imperative" in item.message for item in inspect_message(subject)))

    def test_blank_subject(self):
        self.assertTrue(self.errors(""))

    def test_missing_separator(self):
        self.assertTrue(self.errors("Add notes\nTutors need context."))

    def test_body_limit(self):
        self.assertFalse(self.errors("Add notes\n\n" + "word " * 14 + "ok"))
        self.assertTrue(self.errors("Add notes\n\n" + "word " * 15))

    def test_long_url_is_advisory(self):
        self.assertFalse(self.errors("Add notes\n\nhttps://example.com/" + "a" * 100))

    def test_trailer_is_not_explanation(self):
        findings = inspect_message("Add notes\n\nCo-authored-by: Person <person@example.com>")
        self.assertTrue(any("No explanatory body" in item.message for item in findings))

    def test_missing_body_is_advisory(self):
        self.assertFalse(self.errors("Add notes"))
        self.assertTrue(any(item.level == "warning" for item in inspect_message("Add notes")))

    def test_annotation_escaping(self):
        self.assertEqual("a%0A%25", escape_annotation("a\n%"))
        self.assertEqual("a%2Cb%3Ac", escape_annotation("a,b:c", True))


class HistoryTests(unittest.TestCase):
    def test_ignore_base_history_and_merge_commits_but_check_all_new_commits(self):
        with tempfile.TemporaryDirectory() as directory:
            def run(*arguments):
                return subprocess.check_output(["git", "-C", directory, *arguments], text=True).strip()

            run("init", "-b", "master")
            run("config", "user.name", "Test")
            run("config", "user.email", "test@example.com")
            run("commit", "--allow-empty", "-m", "bad inherited message.")
            run("checkout", "-b", "feature")
            run("commit", "--allow-empty", "-m", "Add notes")
            feature = run("rev-parse", "HEAD")
            run("checkout", "master")
            run("commit", "--allow-empty", "-m", "Update unrelated field")
            updated_base = run("rev-parse", "HEAD")
            run("checkout", "feature")
            run("merge", "--no-ff", "master", "-m", "Merge master")
            with patch("check.git", side_effect=lambda *args: run(*args)):
                self.assertEqual([feature], introduced_commits(updated_base, run("rev-parse", "HEAD")))

    def test_reject_non_sha_arguments(self):
        with self.assertRaises(ValueError):
            introduced_commits("--all", "a" * 40)

    def test_cli_fails_errors_but_passes_warnings(self):
        script = str(Path(__file__).with_name("check.py").resolve())
        with tempfile.TemporaryDirectory() as directory:
            def run(*arguments):
                return subprocess.check_output(["git", "-C", directory, *arguments], text=True).strip()

            run("init", "-b", "master")
            run("config", "user.name", "Test")
            run("config", "user.email", "test@example.com")
            run("commit", "--allow-empty", "-m", "bad inherited subject.")
            base = run("rev-parse", "HEAD")
            run("commit", "--allow-empty", "-m", "Added notes")
            head = run("rev-parse", "HEAD")
            warning_run = subprocess.run(
                [sys.executable, script, "--base", base, "--head", head],
                cwd=directory, capture_output=True, text=True
            )
            self.assertEqual(0, warning_run.returncode, warning_run.stderr)
            self.assertIn("::warning", warning_run.stdout)
            self.assertNotIn("::error", warning_run.stdout)
            run("commit", "--allow-empty", "-m", "Update notes.")
            error_run = subprocess.run(
                [sys.executable, script, "--base", base, "--head", run("rev-parse", "HEAD")],
                cwd=directory, capture_output=True, text=True
            )
            self.assertEqual(1, error_run.returncode, error_run.stderr)
            self.assertIn("::error", error_run.stdout)
            self.assertIn("Do not end the subject with a period", error_run.stdout)


if __name__ == "__main__":
    unittest.main()
