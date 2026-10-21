# Issue 6 — Add Step Execution Handlers (Implementation Plan)

> Source: `WORKFLOW_MVP_BACKLOG.md` → Issue 6 "Add step execution handlers".
> Builds on the Issue 5 engine (`docs/workflow-json-schema.md` is the current schema reference).
> Approach: extend the existing `StepHandler` framework — no engine changes required.

## 1. Current state (post-Issue 5)

- `StepHandler` (`engine/steps/StepHandler.java`): `getType()` + `execute(config, context) -> StepResult`.
- `StepResult` (`engine/steps/StepResult.java`): `ok(output)` / `failed(message)`.
- `WorkflowContext` (`engine/context/WorkflowContext.java`): run-scoped `put`/`get`/`containsKey`/`asMap`.
- `StepHandlerRegistry`: auto-collects all `StepHandler` Spring beans, fails fast on duplicate `type`.
- `WorkflowEngine`: looks up a handler by `type`; unknown type → `fail(... "Unsupported step type: <type>")`.
  On success it persists the output **and** merges it into the shared context
  (`Map` output is merged key-by-key; any other output is stored under the step name).
- Built-in handlers today: **`log`** and **`delay`** (2). Both live in `engine/steps/impl/` and are `@Component`s.

**Gap:** only two built-in types; the backlog asks for "at least three".

## 2. Goal

Deliver four built-in step types (`log`, `delay`, **`set`**, **`http`**) so Issue 6's acceptance
criteria are met, with unit coverage per handler and coverage that outputs land in the shared context.

## 3. Acceptance criteria → how each is satisfied

| Acceptance criterion | Satisfied by |
|---|---|
| At least three built-in step types implemented | `log`, `delay` (existing) + new `set`, `http` = **4** |
| Each step type can be executed independently | Focused unit test per handler (`LogStepHandlerTest`, `DelayStepHandlerTest`, `SetStepHandlerTest`, `HttpStepHandlerTest`) |
| Step outputs are written into the workflow context | `set` returns its `{key: value}` map (engine merges into context); engine-level test asserts a later step reads it |
| Unsupported step types fail gracefully with a clear error | Existing engine behavior; add an explicit assertion for the message |

## 4. Design decisions

1. **No new dependencies.** HTTP uses the JDK's `java.net.http.HttpClient`. `set` uses plain Java.
2. **Two new handlers only** (`set`, `http`) — the "≥ three" bar is cleared at four, keeping surface small.
3. **`set` returns a single-entry map `{key: value}`** rather than writing `context.put(...)` itself.
   The engine already merges map outputs into the context, so this keeps the engine the single writer
   and makes the value part of the persisted step output (`ExecutionStepResult.output`).
   (A `Map.of` cannot hold a `null` value, so use a `LinkedHashMap`.)
4. **`set` supports static JSON values only** (string/number/boolean/null/object/array passed through
   unchanged). Config templating/interpolation is explicitly out of scope (candidate for a later issue).
5. **`http` success = HTTP 2xx.** Non-2xx responses fail the step with the status code in the message
   (body included for debugging). Connection/timeout/IO errors fail with a clear message; on
   `InterruptedException` the interrupt flag is restored, matching `DelayStepHandler`.
6. **`http` client is an injected seam.** A single `HttpClient` `@Bean` is defined so tests can supply
   a stub/point at a local server. The handler does not create its own client.
7. **`http` methods: `GET` and `POST`** for the MVP (default `GET`); any other method fails clearly.
8. **Unknown-type handling is unchanged** — the engine already fails gracefully; Issue 6 only adds
   regression coverage, no engine edit.
9. **Type names are lowercase** (`set`, `http`), matching `log`/`delay` and the schema doc examples.

## 5. New step type specifications

### `set`
Purpose: write a value into the shared workflow context for later steps.

| Config key | Type | Required | Description |
|---|---|---|---|
| `key` | string | Yes | Context key to write. Must be non-blank. |
| `value` | any JSON value | Yes | Value to store (null allowed). Passed through unchanged. |

- **Success output:** `{ "<key>": <value> }` → engine merges to `context.put(key, value)`.
- **Failure:** missing/blank `key`; `value` key absent.

### `http`
Purpose: call an external HTTP endpoint; expose the response to later steps.

| Config key | Type | Required | Default | Description |
|---|---|---|---|---|
| `url` | string | Yes | — | Must start with `http://` or `https://`. |
| `method` | string | No | `GET` | `GET` or `POST` (case-insensitive). |
| `headers` | object(string→string) | No | `{}` | Request headers. |
| `body` | string | No | none | Request body (sent for `POST`). |
| `timeoutSeconds` | number | No | `10` | Per-request timeout; must be > 0. |

- **Success output:** `{ "status": <int>, "body": <string>, "url": <string> }`.
- **Failure (clear message each):** missing/blank or non-http `url`; unsupported `method`; invalid
  `timeoutSeconds`; non-2xx status (`http step '<url>' returned status <code>: <body>`);
  `IOException`/timeout; `InterruptedException` (interrupt restored).

## 6. Implementation phases

### Phase 0 — Prep (no behavior change)
- Confirm JDK `HttpClient` and `com.sun.net.httpserver.HttpServer` (test-only) are available under
  Java 24 / Gradle; no `build.gradle` change expected.
- Add the shared `HttpClient` bean (`engine/steps/HttpClientConfig.java` or a `@Bean` method placed
  alongside the handler).

### Phase 1 — `set` handler
- Add `engine/steps/impl/SetStepHandler.java` (`@Component`, type `"set"`): validate `key`, build a
  `LinkedHashMap` with `key → config.get("value")`, return `StepResult.ok(map)`.
- Add `src/test/java/.../engine/steps/impl/SetStepHandlerTest.java` (type name, writes value,
  missing key fails, blank key fails, null value allowed, object value passed through).

### Phase 2 — `http` handler
- Add `engine/steps/impl/HttpStepHandler.java` (`@Component`, type `"http"`): build `HttpRequest`
  (method, URI, headers, body, timeout), `client.send(..., BodyHandlers.ofString())`, map 2xx →
  `StepResult.ok({status, body, url})`, otherwise fail.
- Add `HttpStepHandlerTest` using a local `com.sun.net.httpserver.HttpServer` on `127.0.0.1:0`
  (offline, deterministic): 200 GET, POST with body/headers, 500 → failure, unsupported method,
  bad URL, unreachable port, timeout.

### Phase 3 — Wiring, integration & docs
- Update `BuiltInStepHandlersTest` to assert all four types (`log`, `delay`, `set`, `http`).
- Add an engine-level fixture + test proving a `set` step's value reaches a later step through the
  shared context. Add `src/test/resources/workflows/set-context.json` and, reusing the existing
  `capture` test handler in `WorkflowEngineStepHandlingTest`, assert the captured value.
- Document the built-in types and their config tables in `docs/workflow-json-schema.md`
  (new "Built-in step types" section), and note `set`/`http` there.
- (Optional) extend `workflows/sample.json` with a `set` step to showcase context, or leave it as-is
  and reserve richer samples for Issue 10.

## 7. Files

**New**
- `apps/backend/src/main/java/com/example/workflowbackend/engine/steps/impl/SetStepHandler.java`
- `apps/backend/src/main/java/com/example/workflowbackend/engine/steps/impl/HttpStepHandler.java`
- `apps/backend/src/main/java/com/example/workflowbackend/engine/steps/HttpClientConfig.java`
- `apps/backend/src/test/java/com/example/workflowbackend/engine/steps/impl/SetStepHandlerTest.java`
- `apps/backend/src/test/java/com/example/workflowbackend/engine/steps/impl/HttpStepHandlerTest.java`
- `apps/backend/src/test/resources/workflows/set-context.json`

**Changed**
- `apps/backend/src/test/java/com/example/workflowbackend/engine/steps/BuiltInStepHandlersTest.java`
- `apps/backend/src/test/java/com/example/workflowbackend/engine/WorkflowEngineStepHandlingTest.java`
  (add one test reusing the existing `capture` handler)
- `docs/workflow-json-schema.md` (built-in step type reference)
- `WORKFLOW_MVP_BACKLOG.md` (tick Issue 6 boxes + note handlers, on completion)

## 8. Test plan

- **Unit (per handler):** `LogStepHandlerTest`, `DelayStepHandlerTest` (existing), `SetStepHandlerTest`,
  `HttpStepHandlerTest` — including each failure branch.
- **Registry:** `BuiltInStepHandlersTest` asserts `set` and `http` are discovered.
- **Engine integration:** `set-context.json` run end-to-end; assert step output persisted and a later
  step reads the value from the context.
- **Failure regression:** assert an unknown `type` still yields `Unsupported step type: <type>`
  (extend the existing `unsupported-step` fixture assertions if not already covered).

## 9. Verification

```
cd apps/backend
./gradlew.bat test        # Windows; ./gradlew test on Unix
```

Expected: all tests green, including the new handler tests and the updated registry test.

## 10. Risks / out of scope

- **`http` determinism:** handler tests use a local in-JVM server only; no external network, so CI stays
  offline and stable.
- **No templating:** `set`/`http` values and URLs are static config; dynamic interpolation is out of scope.
- **No branching:** the engine is strictly sequential; `http` failures stop the run (Issue 5 semantics).
- **No auth/retry:** basic method/headers/body/timeout only.

## 11. Definition of done

- [ ] `SetStepHandler` and `HttpStepHandler` implemented and registered.
- [ ] Four built-in types: `log`, `delay`, `set`, `http`.
- [ ] Each handler has focused unit tests including failure paths.
- [ ] Engine-level test proves a step output is readable from the shared context.
- [ ] Unknown step type still fails with a clear message (covered).
- [ ] Built-in types documented in `docs/workflow-json-schema.md`.
- [ ] `./gradlew test` passes; backlog Issue 6 boxes ticked.
