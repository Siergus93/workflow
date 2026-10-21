# Workflow JSON Schema

A workflow definition is a JSON document with the following fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | string | Yes | Unique name of the workflow. |
| `description` | string | No | Human-readable description. |
| `steps` | array | Yes | Ordered list of steps to execute. |

## Step
Each step has:

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | string | No | Step identifier used in execution results. Defaults to `<type>#<index>` when omitted. |
| `type` | string | Yes | Identifier of the step handler (e.g., "log", "delay", "http", "script"). |
| `config` | object | Yes | Step-specific configuration passed to the handler. Keys depend on `type`. |

## Example
See `apps/backend/src/main/resources/workflows/sample.json`.

## Built-in step types
The engine ships the following handlers (see `engine/steps/impl/`). Each step's `config` is a JSON
object whose keys depend on the `type`.

### `log`
Logs a message at INFO level.

| Config key | Type | Required | Description |
|---|---|---|---|
| `message` | string | Yes | Text to log. |

Output: `{ "message": <string> }`.

### `delay`
Pauses the run for a number of seconds.

| Config key | Type | Required | Description |
|---|---|---|---|
| `seconds` | number | Yes | Non-negative number of seconds to pause. |

Output: `{ "delayedSeconds": <number> }`.

### `set`
Writes a value into the shared workflow context for later steps to read.

| Config key | Type | Required | Description |
|---|---|---|---|
| `key` | string | Yes | Context key to write (must be non-blank). |
| `value` | any JSON value | Yes | Value to store; `null` allowed, passed through unchanged. |

Output: `{ "<key>": <value> }`, which the engine also merges into the shared context.

### `http`
Calls an HTTP endpoint and exposes the response to later steps.

| Config key | Type | Required | Description |
|---|---|---|---|
| `url` | string | Yes | Endpoint URL; must use `http` or `https`. |
| `method` | string | No | `GET` or `POST` (default `GET`). |
| `headers` | object | No | Request headers (string values). |
| `body` | string | No | Request body, sent for `POST`. |
| `timeoutSeconds` | number | No | Per-request timeout in seconds (default `10`, must be positive). |

A 2xx response succeeds with output `{ "status": <int>, "body": <string>, "url": <string> }`. Any
other status, an invalid config, a connection error, or a timeout fails the step with a clear message.

## Notes
- Steps execute sequentially.
- `config` values should be JSON-serializable primitives/objects as needed by the step type.
- Unknown step types should fail gracefully with a clear error.
- Step output is merged into the shared context: a `Map` output is merged key-by-key; any other
  output is stored under the step name.
