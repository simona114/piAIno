---
name: delegate-implementation
description: |
 Use this skill when an implementation task is large enough to split into focused, independently-ownable sub-problems. Breaks the task down, delegates each sub-problem to the most appropriate available subagent, coordinates parallel and sequential execution via the task list, and verifies and integrates the results.
---

# Delegate Implementation

## Core Principle

Delegation only pays off when a sub-problem is narrower and more self-contained than the whole task — small enough for a specialist with no memory of this conversation to own outright. Decomposition is the real work here; dispatching agents is just mechanical follow-through. A bad split produces specialists that silently disagree at the boundary or redo each other's work.

---

## Workflow

1. **Decide whether to delegate at all.** If the task has one obvious target file/area, just do it directly — don't manufacture sub-problems to justify using this skill. Reserve it for tasks that genuinely span modules/specialties or contain pieces that can be worked independently.

2. **Decompose and track.** Name each sub-problem concretely: what changes, in which files/module, what "done" looks like. Create a `TaskCreate` entry per sub-problem, and wire up dependencies with `addBlockedBy`/`addBlocks` via `TaskUpdate` for any sub-problem that needs another's output first.

3. **Map each sub-problem to a specialist.** See "Choosing a Specialist" below.

4. **Batch by dependency, not convenience.** Sub-problems with no `blockedBy` and no file/interface overlap can run in parallel — dispatch them as multiple `Agent` calls in a single message. Anything blocked waits until its dependency's task is `completed` and verified.

5. **Write a self-contained brief per sub-problem.** The agent has no memory of this conversation. State the exact goal, exact files/types involved, and what "done" means — restate the relevant facts rather than saying "based on the above, implement it."

6. **Dispatch.** Set the task's owner to the agent type when you dispatch it and mark it `in_progress`.

7. **Verify before marking done.** An agent's summary describes intent, not necessarily what happened — read the actual diff/files. Only mark a task `completed` once verified. If verification finds an issue, resume the same agent via `SendMessage` (it keeps prior context) rather than respawning fresh. If the brief implied crossing one of the specialist's own Boundaries, stop and surface that to the user instead of completing silently.

8. **Integrate at the seams.** Where two sub-problems meet — a shared type, a module boundary — check it by hand. Don't assume two independently-briefed agents agreed on the interface.

9. **Final whole-task check.** Once everything is integrated, run the build/test commands relevant to every module touched — pull these from each dispatched specialist's own "Tools You Can Use" section rather than guessing one — before calling the task done.

---

## Choosing a Specialist

For each sub-problem, in order:

1. **A project specialist agent** (`.claude/agents/*.md`) whose `description` covers the sub-problem's module. Read its `## Boundaries` section before writing the brief.
2. **No specialist covers it** (e.g. Piano Domain math/preferences work has no dedicated agent today) — use `general-purpose` for implementation, and name the gap explicitly in the brief so it draws conventions from the relevant skill file (e.g. `android-code-quality`) instead of improvising.
3. **The sub-problem is research or design, not implementation** — use `Explore` or `Plan`; they cannot Edit/Write, so never assign them implementation work.
4. **Nothing fits and the work is generic** (build config, docs, tooling) — `general-purpose` or `claude`.

Don't force a sub-problem onto a specialist just because it's the closest available one — a mismatch (e.g. the UI agent reimplementing domain math) recreates exactly the coupling the module boundaries exist to prevent.

---

## Guardrails

- Brief every agent like a self-contained handoff: exact paths, exact constraints, exact done-condition. Never write "based on the research/design above, implement it."
- Treat an agent's return message as a claim, not a fact — open the actual files/diff before marking its task `completed`.
- Read each specialist's `## Boundaries` (⚠️/🚫) before dispatching; if the sub-problem would cross one, surface it to the user first instead of letting the subagent or this skill decide alone.
- Never let a delegated sub-problem add a new dependency, change a schema, or perform a destructive/hard-to-reverse operation without the user confirming — the orchestrator inherits this responsibility even when the specialist would normally ask first.
- Don't decompose a task that already has one obvious target — that's busywork, not delegation.
- Never run two sub-problems in parallel if they touch the same file, the same shared interface, or one feeds the other's input — sequence them via `addBlockedBy` instead.
- If no specialist's description matches, say so explicitly and fall back to `general-purpose` (or `Explore`/`Plan` for non-implementation steps) rather than stretching a mismatched agent's scope.
- Reconcile shared interfaces/module boundaries yourself after delegates return — independent agents do not coordinate with each other.
- Run a whole-task build/test pass after integration even when every sub-problem's own checks passed individually.
- If a sub-problem turns out bigger or different than its brief assumed, stop and re-scope rather than letting the agent improvise past what it was asked.
