# Contributing to PedalPoint

Short rules for working on this repository. Agent-specific instructions are in `AGENTS.md`. The full quality strategy and the definition of done are in the QA Manual (`docs/`).

## 1. Workflow

We work directly from `main`. There is no `dev` branch.

1. Pick a story or task in Jira and move it to **In Progress**.
2. Create a branch from an up-to-date `main`:
   ```bash
   git checkout main && git pull
   git checkout -b PDP-<number>-<short-description>
   ```
3. Commit often on your branch. Before pushing, run the tests (see section 4).
4. Open a pull request to `main` and fill in the template.
5. Ask a teammate and Copilot to review it.
6. After the merge, delete the branch and move the Jira item to **Done**.

`main` is protected: nobody pushes to it directly.

## 2. Naming

| What | Format | Example |
|---|---|---|
| Branch | `PDP-<number>-<short-description>` | `PDP-19-login-ua-email` |
| Commit | `PDP-<number>: <what changed>` | `PDP-19: reject non @ua.pt emails on registration` |
| Pull request title | `PDP-<number>: <story title>` | `PDP-19: Register and log in with a @ua.pt email` |

The Jira key must appear in all three, so the work shows up in Jira. Dependabot pull requests are the only exception.

## 3. Merge policy

A pull request can only be merged when:

- [ ] the branch and the title include the Jira key;
- [ ] CI is green (`./mvnw -B verify`: build, unit and integration tests);
- [ ] the quality gate passed;
- [ ] at least one other team member approved it (the author does not count);
- [ ] the AI review findings are listed in the PR as accepted or rejected;
- [ ] the acceptance criteria of the story are covered by tests.

Merge with **Squash and merge**, so each story becomes one commit on `main` with the PR title.

Dependabot pull requests follow the same rules. Do not merge updates that change the Java version (for example, Temurin 25 or 26): the project uses Java 21.

## 4. Running the tests

From `backend/`:

```bash
./mvnw -B test     # unit tests only
./mvnw -B verify   # unit + integration tests (Docker must be running)
```

## 5. Code style

**Backend**

- Base package `tqs.ua.pedalpoint.backend`, with one sub-package per domain area (`hub`, `bike`, `booking`, ...).
- Constructor injection only. No `@Autowired` on fields.
- Controllers only handle HTTP and return DTOs (Java records), never entities. Business rules live in services.
- The database schema is owned by Flyway: every change is a new migration in `src/main/resources/db/migration`, named `V<n>__description.sql`. Never edit a migration that is already on `main`.
- External systems (telemetry, payments) are only used through their interfaces.
- Document every endpoint for the OpenAPI documentation.
- No secrets in the code. Use environment variables (see `docker-compose.yml`).

**Tests**

- Unit tests: `*Test.java`, no Spring context, collaborators mocked.
- Integration tests: `*IT.java`, real PostgreSQL through Testcontainers, pinned image versions.
- Test names say what is expected, for example `rejectsBookingWhenBatteryBelowMinimum`.
- Gherkin scenarios of core stories live in `src/test/resources/features/`, one file per story.

**Frontend and simulator**

- Style rules will be added here when these parts start.

## 6. Review checklist

When reviewing a pull request, check that:

- [ ] the change does what the story asks, and nothing unrelated;
- [ ] the business rules match the specification;
- [ ] there are tests for the new behaviour, including error cases;
- [ ] names are clear and there is no dead or commented-out code;
- [ ] no secrets or personal data were added;
- [ ] new endpoints are documented;
- [ ] AI-generated code was read and understood by the author.

Leave at least one comment on every pull request you review.

## 7. References

- Agent instructions: `AGENTS.md`
- QA Manual and Product Specification: `docs/`
- Jira backlog: https://pedalpoint.atlassian.net/jira/software/projects/PDP/boards/1/backlog
