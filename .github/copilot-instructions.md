# KADAI – Copilot Coding Agent Instructions

## Project Overview

KADAI is an open-source Java task management library. It can be embedded in an application or run standalone. It adds workbaskets and classifications to control and monitor large numbers of tasks within organizations.

- **Website:** http://kadai.io/
- **License:** Apache License 2.0
- **Current version:** see root `pom.xml`

---

## Repository Structure

```
kadai/
├── lib/               # Core Java libraries (task management engine)
│   ├── kadai-core     # Core task management logic, services, SPI interfaces
│   ├── kadai-spring   # Spring Framework integration
│   └── ...
├── rest/              # REST API (Spring Boot)
│   ├── kadai-rest-spring              # REST controllers and models
│   ├── kadai-rest-spring-example-boot # Runnable Spring Boot example
│   └── ...
├── common/            # Shared utilities (logging, security, data, testing)
├── routing/           # Task routing to workbaskets (DMN-based)
├── history/           # Task history and audit trail
│   ├── kadai-simplehistory-provider
│   └── kadai-simplehistory-rest-spring
├── web/               # Angular frontend (workplace, admin, monitor)
│   └── src/app/
│       ├── workplace       # Task list and task processing UI
│       ├── administration  # Workbasket & classification management
│       ├── monitor         # KPI dashboards and service level monitoring
│       ├── history         # Task history viewer
│       └── shared          # Shared components, services, NGXS state
├── ci/                # SonarCloud coverage aggregation
├── docs/              # Architecture diagrams and images
└── .github/           # GitHub Actions workflows, Dependabot, PR template
```

---

## Technology Stack

### Backend
- **Language:** Java 17+ (compiled with Java 25 in CI)
- **Build tool:** Maven (use `./mvnw` wrapper)
- **Framework:** Spring Boot 4.x
- **Persistence:** MyBatis with SQL providers for H2, PostgreSQL, and DB2
- **Databases:** H2 (tests), PostgreSQL 14+, IBM DB2 11.5.6+
- **Decision engine:** Camunda DMN (for task routing)
- **Testing:** JUnit 5, AssertJ, TestContainers, ArchUnit
- **Code quality:** Checkstyle (Google Java Style), JaCoCo, SonarCloud
- **Architecture validation:** ArchUnit

### Frontend (`web/`)
- **Framework:** Angular (latest) with standalone components
- **Language:** TypeScript
- **State management:** NGXS
- **UI components:** Angular Material
- **Package manager:** Yarn 4 (use `yarn` commands, not `npm`)
- **Unit tests:** Vitest
- **E2E tests:** Cypress
- **Linting:** ESLint
- **Formatting:** Prettier

---

## Building and Testing

### Backend

```bash
# Compile without running tests
./mvnw -B install -DskipTests -Djacoco.skip -T 1C

# Run all tests
./mvnw -B test -T 1C

# Full build including tests and quality checks
./mvnw clean install

# Build a specific module
./mvnw -B install -pl lib/kadai-core -DskipTests
```

### Frontend

```bash
cd web

# Install dependencies
yarn install

# Development build
yarn build

# Production build
yarn build:prod

# Run unit tests
yarn test

# Run E2E tests (requires a running backend)
yarn e2e-dev

# Lint
yarn lint

# Format code
yarn format
```

---

## Coding Conventions

### Java / Backend

- Follow the **Google Java Style Guide** (enforced via Checkstyle).
- Maximum line length: **100 characters**.
- All source files **must** include the Apache 2.0 license header. Check existing files for the exact format.
- Package structure: `io.kadai.<module>.<layer>`, e.g., `io.kadai.task.api`, `io.kadai.task.internal`.
- Layers per module:
  - `api/` – public interfaces and exceptions
  - `internal/` – implementation classes (not part of the public API)
  - `models/` – data model / entity classes
- Use the **SQL Provider pattern** for MyBatis dynamic SQL (see existing `*SqlProvider` classes).
- Respect the **SPI (Service Provider Interface)** design: extensibility points are in `kadai-core` and documented in the README.
- Write tests with **JUnit 5** and **AssertJ** assertions.
- Use **TestContainers** for database integration tests.
- **ArchUnit** tests enforce the layer rules; do not break them when restructuring packages.

### TypeScript / Angular Frontend

- Use **standalone Angular components** (no NgModules).
- Manage application state exclusively via **NGXS store**; do not use component-local mutable state for shared data.
- Format with **Prettier** and lint with **ESLint** before committing.
- Path alias `@task-routing/*` maps to `app/v2/task-routing/*`.
- Target: ES2022 modules.

---

## Key Domain Concepts

### Task lifecycle (state machine)

```
READY → CLAIMED → READY_FOR_REVIEW → IN_REVIEW
         ↓                 ↓
     COMPLETED / CANCELLED / TERMINATED
```

Non-final end states (`COMPLETED`, `CANCELLED`) can be reopened via `reopen()`; `TERMINATED` is a final end state and cannot be reopened.

### Workbaskets

Containers that hold tasks and represent responsibility:
- **Personal** – assigned to a single user
- **Group/Team** – shared among multiple users
- **Topic** – thematic grouping

### Classifications

Define task types and drive derived attributes such as service level and priority.

### Routing

Incoming tasks are automatically routed to workbaskets using DMN decision tables (Camunda engine). Custom routing can be added via the `TaskRoutingProvider` SPI.

### Service Provider Interfaces (SPIs)

Extensibility hooks in `kadai-core` (`io.kadai.spi.*`):
- `CreateTaskPreprocessor` – mutate a task before it is persisted
- `ReviewRequiredProvider` – decide if a review step is needed
- `PriorityServiceProvider` – custom priority calculation
- `TaskRoutingProvider` – custom task routing logic
- `KadaiHistory` – custom history tracking back-end
- Pre/post processors for task state changes

---

## Pull Request Guidelines

All PRs must satisfy the **Definition of Done** in `.github/pull_request_template.md`:

- The corresponding issue is linked and in state *In Review*.
- Commit message format: `Closes #ISSUE_ID - PROBLEM/SOLUTION` (either as a single commit or squashed on merge).
- The SonarCloud Quality Gate must pass.
- If documentation needs an update, a link is included.

---

## CI / CD

- Pipeline defined in `.github/workflows/continuous-integration.yml`.
- Triggered on push to `master`, on pull requests, and on version tags (`v*.*.*`).
- Steps: build → unit tests → integration tests → SonarCloud analysis.
- Dependabot updates Maven, GitHub Actions, Docker, and npm dependencies daily.
