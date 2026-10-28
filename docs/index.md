---
layout: default.md
title: "Home"
---

<link rel="stylesheet" href="stylesheets/landing.css">

<main class="landing-page">
  <section class="landing-hero" aria-labelledby="landing-title">
    <div class="landing-intro">
      <p class="landing-eyebrow">TUTEEDEX / MADE FOR PRIVATE TUTORS</p>
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
      <div class="landing-preview-label"><span class="landing-status-dot" aria-hidden="true"></span> YOUR TUTORING DESK</div>
      <a href="images/TuteeDexLanding.png" aria-label="Open the full-size TuteeDex screenshot">
        <img src="images/TuteeDexLanding.png" width="962" height="563"
          alt="TuteeDex showing example students grouped by subject, with weekly lesson slots and a total and paid summary.">
      </a>
      <figcaption>The current desktop interface, with fictional example students. Click to enlarge.</figcaption>
    </figure>
  </section>

  <section class="landing-features" aria-labelledby="landing-features-title">
    <div class="landing-section-heading">
      <p class="landing-eyebrow">A LITTLE ORGANISATION GOES A LONG WAY</p>
      <h2 id="landing-features-title">Keep the details. Find the overview.</h2>
      <p>Tools for the everyday work around your lessons.</p>
    </div>
    <div class="landing-feature-grid">
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">01 / PROFILES</span>
        <h3>Know each student</h3>
        <p>Keep contact details, tuition start dates, subjects, tags, notes, and outstanding balances
          in one profile. View the details or edit just the fields that need changing.</p>
      </article>
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">02 / LESSONS</span>
        <h3>See who you teach, and when</h3>
        <p>Give each subject a weekly lesson day and start time. Group students by subject while
          keeping each student's own lesson slot visible.</p>
      </article>
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">03 / FOCUS</span>
        <h3>Find the right students</h3>
        <p>Search by name, filter by subject, or bring up a tag such as <code>needs revision</code>.
          Return to the active student list whenever you need the full picture.</p>
      </article>
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">04 / PRIORITIES</span>
        <h3>Put your next lesson first</h3>
        <p>Sort by upcoming lesson, name, tuition start date, or outstanding amount. Combine
          criteria and choose ascending or descending order for each.</p>
      </article>
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">05 / FOLLOW-THROUGH</span>
        <h3>Keep balances and records clear</h3>
        <p>See outstanding tuition amounts and total/paid student counts. Archive past students
          without deleting their profiles, and restore them when they return.</p>
      </article>
      <article class="landing-feature">
        <span class="landing-feature-number" aria-hidden="true">06 / YOUR DATA</span>
        <h3>Bring your records with you</h3>
        <p>Export profiles to CSV for a copy outside the app. Preview a CSV import before
          confirming it. Your student records are stored locally on your computer.</p>
      </article>
    </div>
  </section>

  <section class="landing-workflow" aria-labelledby="landing-workflow-title">
    <div>
      <p class="landing-eyebrow">TYPE A LITTLE. GET THERE QUICKLY.</p>
      <h2 id="landing-workflow-title">A focused view is<br>just a command away.</h2>
      <p>Type in the command box and press Enter. The student list updates in the same window;
        there is no need to work through a series of forms.</p>
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
