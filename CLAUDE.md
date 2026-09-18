# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A generated Java client for the Zenodo REST API (deposit, files, and actions endpoints), built from a hand-maintained Swagger 2.0 spec since Zenodo does not publish its own OpenAPI/Swagger description. Published by Dockstore for use as a Maven dependency.

- The API spec (source of truth for client code) lives at `src/main/resources/zenodo-1.0.0-swagger-2.0.yaml`. Edit this file to add/change API operations or models — the Java client classes are code-generated from it, not hand-written.
- `src/main/java/io/dockstore/EntryCreatorExample.java` is a runnable example/manual-test program exercising a typical Zenodo workflow (create deposit, upload file, set metadata, publish, create a new version). It takes `args[0]` = Zenodo base path (e.g. `https://sandbox.zenodo.org/api`) and `args[1]` = an API token.
- `src/test/java/io/dockstore/ZenodoClientTest.java` contains JUnit tests, several `@Disabled` because they require a personal Zenodo token or hit flaky/undocumented sandbox endpoints.

## Dependency conventions

Prefer, in order: (1) built-in Java features, (2) a third-party library already pulled into the project (e.g. `commonmark-java`, jersey/jackson already present via the parent BOM), (3) a new third-party dependency — only reach for a new one when neither of the above covers the need.

## Build

Generated client code is produced during the Maven build — there is no separate codegen step to run manually. Always invoke the Maven wrapper (`./mvnw`), never a system-installed `mvn`, so the build uses the project's pinned Maven version.

```
./mvnw clean install          # full build: generates client from the YAML spec, compiles, runs tests
./mvnw clean compile          # just generate + compile, skip tests
./mvnw test                   # run tests (generation happens first via the normal lifecycle)
./mvnw test -Dtest=ZenodoClientTest#testConceptDoi   # run a single test method
```

Generated sources land under `target/generated-sources/swagger/...` and are added to the build via `build-helper-maven-plugin`. Requires Java 21 to build (CI uses `21.0.2+13.0.LTS`), though the `maven-compiler-plugin` `release` target is 17.

## Architecture notes

- Code generation uses `io.swagger:swagger-codegen-maven-plugin` (Swagger Codegen 2.x, `language=java`, `library=jersey2`) against the YAML spec. Generated packages: `io.swagger.zenodo.client` (ApiClient/ApiException), `io.swagger.zenodo.client.api` (`DepositsApi`, `FilesApi`, `ActionsApi`, `PreviewApi`, etc.), `io.swagger.zenodo.client.model` (request/response DTOs like `Deposit`, `DepositMetadata`, `Author`, `RelatedIdentifier`).
- After generation, a `maven-replacer-plugin` step rewrites `javax.*` package references (`javax.annotation`, `javax.validation`, `javax.ws`, `javax.servlet`, `javax.xml.bind`) to their `jakarta.*` equivalents in the generated sources, since the generator emits `javax` imports but the project depends on Jakarta EE artifacts (`jakarta.ws.rs-api`, jersey3-line dependencies).
- Checkstyle and SpotBugs are configured to run but **not** fail the build on generated code (`skip`/`failOnError`/`failOnViolation` disabled for those plugins) — they still apply to hand-written code under `src/main/java` and `src/test/java`.
- `flatten-maven-plugin` writes a flattened POM to `generated/src/main/resources/pom.xml` on `validate` (used for downstream publishing). Like the codegen output, any `generated/` directory is build output, not source — don't hand-edit it; to change a dependency/version that flows into it, edit the root `pom.xml` (or, for shared versions, `dockstore-core.version`/the `bom-internal` BOM it pulls in) instead.
- Parent BOM: `io.dockstore:bom-internal` (via `dockstore-core.version`) supplies shared dependency versions, checkstyle config (`checkstyle.xml`, `checkstyle-suppressions.xml`), and plugin defaults — those files aren't in this repo.
- Descriptions/notes sent to Zenodo only support a restricted HTML tag allowlist (see comment in `EntryCreatorExample.java`); Markdown content intended for those fields should be rendered to HTML via `commonmark-java` before submission, as shown in the example.
- CI (`.github/workflows/mvn.yml`) runs `./mvnw -B -ntp clean install` on every push, after installing `git-secrets` for secret scanning.

## Pull requests

When creating a PR, always create it in draft mode. A human developer must be the one to mark it ready for review/move it out of draft state — Claude Code should not do this itself.

Keep the freeform "Description" and "Review Instructions" sections of `.github/PULL_REQUEST_TEMPLATE.md` brief — one paragraph each, or two for a genuinely complicated fix, not multi-paragraph writeups. The "Security and Privacy" checklist section is separate: copy it verbatim, never reword/reformat/condense/annotate an item, and only flip `[ ]` to `[x]` after actually confirming that item for this PR.

## JIRA

When adding comments to JIRA tickets (e.g. `SEAB-` prefixed tickets referenced from a PR's "Issue" field), clearly indicate the comment was written by Claude (e.g. lead with a line like "This comment was generated by Claude (Claude Code).").
