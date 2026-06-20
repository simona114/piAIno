---
description: Analyze uncommitted changes and propose splitting them into atomic commits; create them only after approval
---

Analyze all current uncommitted changes in this repository and split them into multiple meaningful, atomic commits.

1. Run `git status` and `git diff` (staged and unstaged) to see every changed, added, deleted, and untracked file.
2. Group the changes into logically atomic units — each commit should represent one coherent, self-contained change. Don't mix unrelated changes (e.g. a doc update and an unrelated code change) into one commit, and don't split a single logical change across multiple commits just because it touches several files.
3. For each proposed commit, determine:
   - Exactly which files (or hunks, if a file contains two unrelated changes) belong in it
   - A concise, why-focused commit message, matching this repo's existing style (check `git log` for tone/format)
4. Determine the dependency order between the proposed commits before sequencing them. If one commit relies on something another commit introduces or changes (e.g. it edits a file the other renames/creates, references a config/symbol the other adds, or would leave the repo in a broken/inconsistent state without the other applied first), the commit being depended on must come first, and the dependent commit comes after it. Order all commits so that after each one is applied in sequence, the repo is in a valid, self-consistent state — never order a dependent commit before what it depends on.
5. Present the full proposed commit structure to the user as a numbered list, in the dependency-respecting order determined above — for each commit: files included, draft message, and (if relevant) a one-line note on why it must come before/after another proposed commit. Explicitly ask for approval. Do not stage or commit anything before approval.
6. Only after the user approves, create the commits one at a time in the proposed (dependency-respecting) order. Stage only the files belonging to that commit (`git add <specific paths>` — never `git add -A` or `git add .`). If one file contains changes that belong in two different commits, stage the relevant hunks individually rather than the whole file.
7. After all commits are created, run `git log --on
8. eline` (limited to the commits just created) and `git status` to confirm the result, and report back what was committed.

If the working tree has no uncommitted changes, say so and stop — don't invent commits.
