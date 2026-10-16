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

## Notes
- Steps execute sequentially.
- `config` values should be JSON-serializable primitives/objects as needed by the step type.
- Unknown step types should fail gracefully with a clear error.
