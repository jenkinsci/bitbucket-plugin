# Repository guidance

## Project

This is the Jenkins Bitbucket plugin (`bitbucket`), a Maven project packaged as
an HPI. It receives Bitbucket webhooks at `/bitbucket-hook/`, matches repositories
to Jenkins jobs, and triggers SCM polling or multibranch indexing. See `README.md`
for configuration, supported payloads, and Job DSL examples.

## Where to work

- `src/main/java/com/cloudbees/jenkins/plugins/`: webhook receiver, payload parsing
  and processing, signature validation, triggers, and repository links.
- `src/main/java/com/cloudbees/jenkins/plugins/extensions/dsl/`: Job DSL integration.
- `src/main/resources/`: Jelly configuration views and HTML field help. Resource
  directories mirror their Java classes.
- `src/test/java/com/cloudbees/jenkins/plugins/`: unit and Jenkins harness tests.
- `src/test/resources/`: webhook JSON fixtures.
- `pom.xml`, `.mvn/`, and `Jenkinsfile`: dependencies, build configuration, and CI.

## Build and validation

Use Maven from the repository root; there is no checked-in Maven wrapper.
JDK 21 is a suitable local choice, matching the Windows CI job. `Jenkinsfile`
also tests Linux with JDK 25; these build JDKs do not define the Java language
level. The current build compiles for Java 17. Preserve the compiler release
selected by the Maven parent and Jenkins baseline; avoid newer Java APIs just
because the local JDK supports them. The Jenkins baseline and dependency BOM are
defined in `pom.xml`; keep changes compatible with them.

```sh
# Run one test class or method (quote the method selector for the shell).
mvn --batch-mode --no-transfer-progress -Dtest=BitbucketTriggerTest test
mvn --batch-mode --no-transfer-progress '-Dtest=BitbucketTriggerTest#configRoundTripBitbucketTrigger' test

# Run the test suite.
mvn --batch-mode --no-transfer-progress test

# Build the plugin and run lifecycle verification checks.
mvn --batch-mode --no-transfer-progress verify

# Start a local Jenkins instance with this plugin for manual testing.
mvn hpi:run
```

Start with tests covering the changed behavior. Run `verify` for code or build
changes before handing off when practical, and report any checks that could not
run. Documentation-only changes need a diff and whitespace review, not a full
Maven build. Maven resolves dependencies from the Jenkins repository; distinguish
dependency or network failures from test failures. Do not disable checks to make
a build pass. Treat `target/` and `work/` as generated output.

## Implementation conventions

- Follow the surrounding Java style and avoid unrelated formatting changes.
  Existing names use both `BitBucket` and `Bitbucket`; preserve those names.
- Use JUnit Jupiter (`org.junit.jupiter`), not JUnit 4. For Jenkins integration
  tests, follow the existing `@WithJenkins` and injected `JenkinsRule` pattern.
  Tests that need a separate Jenkins process use `RealJenkinsExtension`, as in
  `BitbucketOptionalDependencyTest`. Use Mockito where appropriate for unit tests.
- Add regression coverage for behavioral fixes. Reuse webhook fixtures and test
  helpers where they fit; keep tests independent of live Bitbucket services.
- Preserve saved Jenkins job configuration, the `bitbucketPush` Pipeline/Job DSL
  interface, and payload environment variables when modifying triggers or models.
- Account for Cloud, Server, and legacy form-encoded webhook paths when changing
  payload handling. Keep signature validation tied to the original request bytes.
- Keep webhook signature checks and the CSRF exclusion narrowly scoped. Avoid
  logging secrets or adding payload logging that could expose sensitive data.
- Preserve optional dependency behavior for Job DSL and Bitbucket Branch Source;
  `BitbucketOptionalDependencyTest` covers operation without Bitbucket Branch
  Source, and `BitbucketLinkUtilsTest` checks for hard links to its classes.
  Do not assume those tests also cover operation without Job DSL.
- Update the relevant Jelly view, HTML help, or README when user-facing
  configuration or behavior changes.
