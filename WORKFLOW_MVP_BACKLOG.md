# Workflow Orchestration MVP Backlog

This backlog translates the 4-week MVP plan into GitHub-style issues with clear acceptance criteria.

## Epic 1 — Project Foundation

### Issue 1: Bootstrap backend and frontend skeleton

**Labels:** setup, backend, frontend

**Description**
Create the initial project structure for the workflow orchestration MVP, including a Spring Boot backend and a React frontend.

**Acceptance Criteria**

- [x] A Spring Boot project is initialized and runs locally.
- [x] A React app is initialized and runs locally.
- [x] The project structure clearly separates backend and frontend code.
- [x] A README describes how to start both applications.

---

### Issue 2: Define core domain model

**Labels:** backend, domain

**Description**
Model the core entities needed to represent workflows, executions, and step results.

**Acceptance Criteria**

- [x] A Workflow entity exists.
- [x] A WorkflowExecution entity exists.
- [x] An ExecutionStep entity exists.
- [x] Relationships between entities are defined clearly.
- [x] The model supports status values such as pending, running, completed, and failed.

---

### Issue 3: Set up persistence and database configuration

**Labels:** backend, database

**Description**
Configure PostgreSQL and persistence for the workflow engine.

**Acceptance Criteria**

- [x] PostgreSQL is configured for local development.
- [x] JPA repositories are created for core entities.
- [x] Database tables are created successfully.
- [x] The application starts with the database connection configured.

---

## Epic 2 — Workflow Engine

### Issue 4: Define workflow JSON format

**Labels:** backend, engine

**Description**
Define a simple JSON schema for workflow definitions that can be loaded by the engine.

**Acceptance Criteria**

- [x] A sample workflow JSON file is added to the repository.
- [x] The format supports workflow name, description, and steps.
- [x] Each step includes a type and configuration fields.
- [x] The schema is documented for future contributors.

---

### Issue 5: Implement workflow execution engine

**Labels:** backend, engine

**Description**
Build the engine that executes workflow steps sequentially and tracks shared context. See `docs/issue-5-engine-plan.md` (including its Phase 0 blockers).

**Acceptance Criteria**

- [x] A workflow definition is loaded from `classpath*:workflows/*.json` and validated (name, steps, and each step's type/config).
- [x] Steps execute sequentially in JSON order, and persisted step results record that order (`stepOrder`).
- [x] A shared workflow context is created per run and step outputs are written into it for later steps to read.
- [x] `WorkflowExecution.status` transitions PENDING -> RUNNING -> COMPLETED/FAILED with `startedAt`/`finishedAt` set.
- [x] A failed step (handler error or unsupported type) stops execution; the failed step and the execution are persisted as FAILED with a clear error message (each repository save commits independently).
- [x] A workflow can run without being pre-registered; the `Workflow` row is found or created by name with `createdAt` set.

**Notes**
- Optional step `name` (fallback `type#index`) was added to the Issue 4 schema/sample in plan Phase 0.
- Covered by `WorkflowEngineTest`, `WorkflowEnginePersistenceTest`, `WorkflowEngineStepHandlingTest`, and the loader/context/registry unit tests.

---

### Issue 6: Add step execution handlers

**Labels:** backend, engine

**Description**
Implement the first built-in step types for the MVP.

**Acceptance Criteria**

- [ ] At least three built-in step types are implemented.
- [ ] Each step type can be executed independently.
- [ ] Step outputs are written into the workflow context.
- [ ] Unsupported step types fail gracefully with a clear error.

**Notes**
- Issue 5 ships the minimum handlers required by `sample.json` (`log`, `delay`); this issue delivers the full set of three or more.

---

## Epic 3 — API and Persistence

### Issue 7: Create workflow management API

**Labels:** backend, api

**Description**
Expose workflow functionality through REST endpoints.

**Acceptance Criteria**

- [ ] An endpoint exists to create or register a workflow.
- [ ] An endpoint exists to list workflows.
- [ ] An endpoint exists to trigger a workflow execution.
- [ ] An endpoint exists to fetch workflow execution status.
- [ ] API responses use clear JSON structures.

---

### Issue 8: Persist execution history and step results

**Labels:** backend, persistence

**Description**
Save workflow run history and detailed step results for later inspection.

**Acceptance Criteria**

- [ ] Each workflow execution is stored in the database.
- [ ] Each step result is stored with status and timestamps.
- [ ] Step results preserve their execution order (`stepOrder`).
- [ ] Execution history can be retrieved through the API.
- [ ] Failed runs preserve enough detail for debugging.

---

## Epic 4 — UI and Demo Readiness

### Issue 9: Build basic workflow dashboard

**Labels:** frontend, ui

**Description**
Create a simple React dashboard to view workflows and workflow runs.

**Acceptance Criteria**

- [ ] The UI lists available workflows.
- [ ] The UI shows the latest execution status for each workflow.
- [ ] The UI allows a user to trigger a workflow run.
- [ ] The UI displays basic execution details for a selected run.

---

### Issue 10: Add sample workflow and demo data

**Labels:** demo, samples

**Description**
Provide an example workflow that demonstrates the MVP end-to-end.

**Acceptance Criteria**

- [ ] A sample invoice-processing workflow is included.
- [ ] The sample workflow runs successfully with default data.
- [ ] The workflow demonstrates at least three steps.
- [ ] The sample is documented in the README.

---

### Issue 11: Add tests for core engine behavior

**Labels:** testing, backend

**Description**
Cover the workflow engine with automated tests to ensure reliability.

**Acceptance Criteria**

- [ ] Unit tests cover successful workflow execution.
- [ ] Unit tests cover failed workflow execution.
- [ ] Tests verify context propagation between steps.
- [ ] Tests pass in CI or local execution.

---

### Issue 12: Add Docker and local run support

**Labels:** devops, setup

**Description**
Make the MVP easy to run locally using Docker Compose.

**Acceptance Criteria**

- [ ] Docker Compose starts the app and database.
- [ ] The app can be started without manual environment setup.
- [ ] The README includes startup instructions.
- [ ] The local setup is reproducible on a fresh machine.

---

## Suggested Implementation Order

1. Bootstrap backend and frontend
2. Define core domain model
3. Configure database persistence
4. Implement workflow JSON format
5. Build workflow engine
6. Add step handlers
7. Create API endpoints
8. Persist execution history
9. Build UI dashboard
10. Add tests and Docker support

## Definition of Done for the MVP

The MVP is complete when:

- [ ] A workflow can be defined in JSON.
- [ ] The workflow engine can execute it end-to-end.
- [ ] Workflow runs are stored and retrievable.
- [ ] A basic UI allows triggering and viewing runs.
- [ ] The project can be demonstrated locally in a few steps.
