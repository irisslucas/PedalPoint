# PedalPoint

![CI](https://github.com/irisslucas/PedalPoint/actions/workflows/ci.yml/badge.svg)

## 1. Project abstract

**PedalPoint** is a light-mobility fleet-as-a-service platform for a university campus.

PedalPoint Mobility Ltd. runs a fleet of e-bikes and cargo e-bikes for Ria Nova University (both fictional). Students and staff with a @ua.pt account find an e-bike at a campus hub, book it for a period or unlock it on the spot, and return it to any hub with free space, where it charges. They pay with a monthly or semester pass, or per use. The operator handles maintenance, batteries and moving e-bikes between hubs under agreed SLAs. The university sets the rules of use and the prices, and follows the service through KPIs and a monthly statement.

## 2. Project team

| Name | Role |
| --- | --- |
| Íris Lucas | Team Coordinator |
| Francisco Baptista | Product Owner |
| Guilherme Gomes | QA Engineer |
| Bohdan Popov | DevOps Master |

All members are also developers.

## 3. Project bookmarks

- Jira backlog:(https://pedalpoint.atlassian.net/jira/software/projects/PDP/boards/1/backlog)
- API documentation: _(to add)_
- Static analysis dashboard (SonarQube): _(to add)_
- Observability dashboard: _(to add)_
- Deployed application: _(to add)_

## Repository structure

```
backend/     Spring Boot service (Java 21)
frontend/    Web UI
simulator/   Fleet telemetry simulator
docs/        Reports (PDF) and presentation
.github/     CI workflows and pull request template
AGENTS.md    Instructions for AI agents (build commands, style, definition of done)
```

## Running locally

```
docker compose up --build     # backend on :8080, PostgreSQL on :5432
cd backend && ./mvnw verify   # unit + integration tests (needs Docker)
```
