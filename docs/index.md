---
layout: default.md
title: "Home"
---

<link rel="stylesheet" href="stylesheets/landing.css">
<script src="scripts/landing.js" defer></script>

<main class="landing-page">
  <div class="landing-masthead">
    <a class="landing-wordmark" href="index.html" aria-label="TuteeDex home"><span aria-hidden="true">&gt;</span> TuteeDex</a>
    <span>MADE FOR PRIVATE TUTORS</span>
  </div>
  <section class="landing-hero" aria-labelledby="landing-title">
    <div class="landing-intro">
      <p class="landing-eyebrow">YOUR TUTORING DESK</p>
      <h1 id="landing-title">Less admin.<br>More <span>teaching.</span></h1>
      <p class="landing-lead">Your students, lesson schedules, and tuition balances.<br class="landing-desktop-break">
        Together in one desktop app.</p>
      <p class="landing-description">TuteeDex pairs quick, typed commands with a clear visual overview,
        so you can find a student, plan your next lesson, and keep track of what is owed.</p>
      <div class="landing-actions">
        <a class="landing-button landing-button-primary" href="https://github.com/AY2627S1-CS2103T-F13-2/tp/releases">Get TuteeDex <span aria-hidden="true">↗</span></a>
        <a class="landing-button landing-button-secondary" href="UserGuide.html">Read the User Guide <span aria-hidden="true">→</span></a>
      </div>
      <p class="landing-platform">Java 25 · Windows, macOS &amp; Linux · Desktop CLI + GUI</p>
    </div>
    <figure class="landing-preview">
      <div class="landing-preview-label"><span class="landing-status-dot" aria-hidden="true"></span> TUTEEDEX / DESKTOP PREVIEW</div>
      <a href="images/TuteeDexLanding.png" aria-label="Open the full-size TuteeDex screenshot">
        <img src="images/TuteeDexLanding.png" width="962" height="563"
          alt="TuteeDex showing example students grouped by subject, with weekly lesson slots and a total and paid summary.">
      </a>
      <figcaption>The current desktop interface, with fictional example students. Click to enlarge.</figcaption>
    </figure>
  </section>

  <section class="landing-features" aria-labelledby="landing-features-title">
    <div class="landing-section-heading">
      <p class="landing-eyebrow">EXPLORE YOUR TOOLKIT</p>
      <h2 id="landing-features-title">Keep the details. Find the overview.</h2>
      <p>Tools for the everyday work around your lessons.</p>
    </div>
    <div class="landing-feature-selector" role="group" aria-label="Explore TuteeDex features" hidden>
      <button type="button" aria-pressed="true" aria-controls="feature-profiles">001 / Profiles</button>
      <button type="button" aria-pressed="false" aria-controls="feature-lessons">002 / Lessons</button>
      <button type="button" aria-pressed="false" aria-controls="feature-focus">003 / Focus</button>
      <button type="button" aria-pressed="false" aria-controls="feature-priorities">004 / Priorities</button>
      <button type="button" aria-pressed="false" aria-controls="feature-records">005 / Records</button>
      <button type="button" aria-pressed="false" aria-controls="feature-data">006 / Your data</button>
    </div>
    <div class="landing-feature-grid" aria-live="polite" aria-atomic="true">
      <article class="landing-feature" id="feature-profiles">
        <span class="landing-feature-number">001 / PROFILES</span>
        <h3>Know each student</h3>
        <p>Keep contact details, tuition start dates, subjects, tags, notes, and outstanding balances
          in one profile. View the details or edit just the fields that need changing.</p>
        <div class="landing-feature-example"><span>OPEN A STUDENT PROFILE</span><code>view 1</code></div>
      </article>
      <article class="landing-feature" id="feature-lessons">
        <span class="landing-feature-number">002 / LESSONS</span>
        <h3>See who you teach, and when</h3>
        <p>Give each subject a weekly lesson day and start time. Group students by subject while
          keeping each student's own lesson slot visible.</p>
        <div class="landing-feature-example"><span>PUT UPCOMING LESSONS FIRST</span><code>sort slot/asc n/asc</code></div>
      </article>
      <article class="landing-feature" id="feature-focus">
        <span class="landing-feature-number">003 / FOCUS</span>
        <h3>Find the right students</h3>
        <p>Search by name, filter by subject, or bring up a tag such as <code>needs revision</code>.
          Return to the active student list whenever you need the full picture.</p>
        <div class="landing-feature-example"><span>FOCUS ON A SHARED TAG</span><code>list t/Needs revision</code></div>
      </article>
      <article class="landing-feature" id="feature-priorities">
        <span class="landing-feature-number">004 / PRIORITIES</span>
        <h3>Put your next lesson first</h3>
        <p>Sort by upcoming lesson, name, tuition start date, or outstanding amount. Combine
          criteria and choose ascending or descending order for each.</p>
        <div class="landing-feature-example"><span>SORT STUDENTS BY NAME</span><code>sort n/asc</code></div>
      </article>
      <article class="landing-feature" id="feature-records">
        <span class="landing-feature-number">005 / RECORDS</span>
        <h3>Keep balances and records clear</h3>
        <p>See outstanding tuition amounts and total/paid student counts. Archive past students
          without deleting their profiles, and restore them when they return.</p>
        <div class="landing-feature-example"><span>ARCHIVE A PAST STUDENT</span><code>archive 1</code></div>
      </article>
      <article class="landing-feature" id="feature-data">
        <span class="landing-feature-number">006 / YOUR DATA</span>
        <h3>Bring your records with you</h3>
        <p>Export profiles to CSV for a copy outside the app. Preview a CSV import before
          confirming it. Your student records are stored locally on your computer.</p>
        <div class="landing-feature-example"><span>EXPORT YOUR STUDENT RECORDS</span><code>export</code></div>
      </article>
    </div>
  </section>

  <section class="landing-workflow" aria-labelledby="landing-workflow-title">
    <div>
      <p class="landing-eyebrow">THE COMMAND DESK</p>
      <h2 id="landing-workflow-title">A focused view is<br>just a command away.</h2>
      <p>Type in the command box and press Enter. The student list updates in the same window;
        there is no need to work through a series of forms.</p>
      <p>TuteeDex remembers your last list or find view and applied sort order when you close it,
        so you can pick up where you left off.</p>
      <a class="landing-text-link" href="UserGuide.html#command-summary">Explore the commands <span aria-hidden="true">→</span></a>
    </div>
    <div class="landing-command-panel" aria-label="Example TuteeDex commands">
      <div class="landing-command-example">
        <p><span aria-hidden="true">›</span> <code>list t/Needs revision</code></p>
        <span>Focus on students with this tag.</span>
      </div>
      <div class="landing-command-example">
        <p><span aria-hidden="true">›</span> <code>sort slot/asc n/asc</code></p>
        <span>Order upcoming lessons, then break ties by name.</span>
      </div>
      <div class="landing-command-example">
        <p><span aria-hidden="true">›</span> <code>view 1</code></p>
        <span>Open the first displayed student's full profile.</span>
      </div>
    </div>
  </section>

  <section class="landing-start" aria-labelledby="landing-start-title">
    <div class="landing-section-heading">
      <p class="landing-eyebrow">YOUR FIRST SESSION</p>
      <h2 id="landing-start-title">Set up your tutoring desk.</h2>
    </div>
    <ol class="landing-steps">
      <li><h3>Get Java 25 ready</h3><p>Check the setup instructions for your operating system in the
        <a href="UserGuide.html#quick-start">Quick Start guide</a>.</p></li>
      <li><h3>Download the app</h3><p>Choose a JAR from the
        <a href="https://github.com/AY2627S1-CS2103T-F13-2/tp/releases">team's releases</a>
        and put it in a folder where the app can save its files.</p></li>
      <li><h3>Launch and explore</h3><p>From that folder, run <code>java -jar "TuteeDex.jar"</code>
        using your downloaded filename. Enter <code>help</code> to see the commands.</p></li>
    </ol>
    <p class="landing-release-note">This site describes the current development version.
      Check the release notes for the features included in your download.</p>
  </section>

  <section class="landing-contribute" aria-labelledby="landing-contribute-title">
    <div>
      <p class="landing-eyebrow">BUILT TOGETHER</p>
      <h2 id="landing-contribute-title">Meet the people behind TuteeDex.</h2>
      <p>A CS2103T team project at the National University of Singapore,
        built on AddressBook-Level3 by the <a href="https://se-education.org">SE-EDU initiative</a>.</p>
    </div>
    <div class="landing-resource-links">
      <a href="AboutUs.html">Meet the team <span aria-hidden="true">↗</span></a>
      <a href="DeveloperGuide.html">Developer Guide <span aria-hidden="true">↗</span></a>
      <a href="https://github.com/AY2627S1-CS2103T-F13-2/tp">Source on GitHub <span aria-hidden="true">↗</span></a>
    </div>
  </section>
  <p class="landing-credits">Built with Java and JavaFX, with Jackson and JUnit.
    <a href="https://github.com/AY2627S1-CS2103T-F13-2/tp/actions/workflows/gradle.yml">Build status</a>
    <span aria-hidden="true">·</span>
    <a href="https://codecov.io/gh/AY2627S1-CS2103T-F13-2/tp">Coverage report</a>
  </p>
</main>
