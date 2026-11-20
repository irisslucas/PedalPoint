# AGENTS.md

Instructions for AI coding agents working in this repository. Humans stay accountable for every change: agents never merge, and a teammate approves every pull request.

## Project
PedalPoint: fleet-as-a-service for a university campus (fictional client Ria Nova University). Java 21, Spring Boot, PostgreSQL, Flyway. See [README.md](README.md).

## Layout
- `backend/` Spring Boot service (package `tqs.ua.pedalpoint.backend`, one sub-package per domain area, e.g. `hub`)
- `frontend/`, `simulator/` not started yet
- `docs/` reports; `.github/` workflows and PR template

## Commands (run from `backend/`)
- Build, unit and integration tests: `./mvnw -B verify` (Docker must be running: integration tests use Testcontainers)
- Unit tests only: `./mvnw -B test`
- Run the stack: `docker compose up --build` (from the repository root)

## Code style
- Constructor injection; no field `@Autowired`.
- Schema is owned by Flyway (`src/main/resources/db/migration`, `V<n>__description.sql`); never rely on Hibernate DDL.
- Return DTOs (records) from controllers, never entities.
- Package names must match directories.

## Tests (definition of done)
- Unit tests (`*Test`): business logic, no Spring context, mocks for collaborators.
- Integration tests (`*IT`): run by failsafe in `verify`, real PostgreSQL via Testcontainers; pin image versions (no `latest`).
- Acceptance criteria of core stories become Gherkin scenarios (Cucumber).
- Tests must be able to fail on a plausible bug; do not add tests that only restate the implementation.

## Workflow
- One branch per story, named with the Jira key: `PDP-<n>-short-description`; the key also goes in commit messages and the PR title.
- Open a pull request using `.github/pull_request_template.md`; CI must be green.
- Never commit secrets or personal data; use synthetic test data.
- Do not push or merge unless a human asked for it.
