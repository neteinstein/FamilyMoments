# Agent orchestration

Family Moments defines four specialized agent roles — **Developer**, **QA**, **Architect**, and **Security Manager** — usable from both Claude Code and GitHub Copilot. Each role is defined once, canonically, in this directory; the two tools each get their own thin frontmatter wrapper because their config formats differ (Claude Code subagents vs. Copilot custom agents aren't interchangeable file formats), but both wrappers point back to the same canonical instructions so the roles can't drift apart.

## File map

| Role | Canonical instructions | Claude Code subagent | Copilot custom agent |
|---|---|---|---|
| Developer | [`developer.md`](./developer.md) | [`.claude/agents/developer.md`](../../.claude/agents/developer.md) | [`.github/agents/developer.agent.md`](../../.github/agents/developer.agent.md) |
| QA | [`qa.md`](./qa.md) | [`.claude/agents/qa.md`](../../.claude/agents/qa.md) | [`.github/agents/qa.agent.md`](../../.github/agents/qa.agent.md) |
| Architect | [`architect.md`](./architect.md) | [`.claude/agents/architect.md`](../../.claude/agents/architect.md) | [`.github/agents/architect.agent.md`](../../.github/agents/architect.agent.md) |
| Security Manager | [`security-manager.md`](./security-manager.md) | [`.claude/agents/security-manager.md`](../../.claude/agents/security-manager.md) | [`.github/agents/security-manager.agent.md`](../../.github/agents/security-manager.agent.md) |

Claude Code discovers subagents by scanning `.claude/agents/`; GitHub Copilot (CLI, VS Code, and the cloud coding agent) discovers custom agents by scanning `.github/agents/*.agent.md`. Neither tool supports a cross-file `include`, so each wrapper's body tells the agent to read its canonical file at the start of the session — that's the "thin wrapper" part. The frontmatter (tool access, model, delegation permissions) still has to be declared per-tool because the schemas aren't compatible.

## Who delegates to whom

Architect and Security Manager are the two orchestrator roles: they're the ones expected to plan/review and then hand work off rather than doing all the typing themselves. Developer and QA are workers — they don't spawn other agents.

```
Architect ──┬──> Developer   (implement the design)
            └──> QA          (define/verify the test strategy)

Security Manager ──┬──> Developer   (remediate a finding)
                    └──> QA         (add a regression test proving the fix holds)
```

- **Claude Code**: only `architect` and `security-manager` get the `Agent` tool in their frontmatter, so only they can spawn `developer`/`qa`/each other via the `Agent` tool, `@agent-<name>` mentions, or plain natural language ("use the qa subagent to..."). `developer` and `qa` omit `Agent` entirely — they're leaves.
- **Copilot**: only `architect.agent.md` and `security-manager.agent.md` include the `agent` tool alias, which lets the Copilot runtime delegate to another custom agent (intent-matched by that agent's `description`, or explicitly via `/agent <name>`). They also declare `handoffs` — VS Code's guided "hand off to another agent" buttons — pointing at `developer` and `qa`/`security-manager` so a human reviewing the Architect or Security Manager's output has a one-click way to continue the same delegation Claude Code does via the `Agent` tool.

## Invoking a role

**Claude Code:**
- Natural language: "Use the architect subagent to plan this feature module."
- Explicit: `@agent-architect ...`
- Whole session as that role: `claude --agent architect`

**GitHub Copilot** (CLI, VS Code, or the cloud coding agent):
- Automatic: Copilot intent-matches your prompt against each agent's `description` and delegates on its own (this is the "reactive" trigger — no extra config needed, since none of these agents set `disable-model-invocation: true`).
- Explicit: `/agent architect` in Copilot CLI, or pick it from the agent picker in VS Code.

## Keeping the two wrappers in sync

Edit `docs/agents/<role>.md` for anything about *what the role should do*. Only touch `.claude/agents/<role>.md` or `.github/agents/<role>.agent.md` for tool-specific concerns: which tools/model to grant, delegation edges, or (Copilot only) `handoffs`. The `description` frontmatter field is duplicated by necessity (each tool uses its own for delegation matching) — when you change a role's purpose enough that the one-line summary goes stale, update `description` in both wrapper files.

## Out of scope here

Reactively triggering an agent *from a GitHub event* (e.g. auto-assigning Copilot's cloud coding agent to a newly opened issue, or running an agent on a PR label) is a repository/organization setting, not something declared in an agent's own frontmatter. These files only define the roles and their delegation edges; wiring them to GitHub events is a separate decision left to whoever owns this repo's Copilot settings.
