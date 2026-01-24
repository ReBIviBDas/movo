# Git - Versioning and Collaboration
**Tags:** #git #software-engineering #version-control
**Source:** Lecture Slides (Marco Robol)

## 1. What is Version Control?
Version Control Systems (VCS) are tools that help manage changes to source code over time.
*   **Key Features:**
    *   Tracks every modification.
    *   Allows comparing earlier versions ("turning back the clock").
    *   Prevents concurrent work from conflicting.
    *   Allows safe experimentation (undo changes easily).

---

## 2. Setup and Configuration

### Installation
Verify installation in the terminal:
```bash
git version
```
*If not installed, download from [git-scm.com](https://git-scm.com)*.

### First-Time Configuration
You must tell Git who you are (this information is attached to your commits).
```bash
# Set global username
git config --global user.name "First-name Surname"

# Set global email
git config --global user.email "you@email.com"
```

**Config Levels:**
*   `--system`: Applies to all users on the computer.
*   `--global`: Applies to your user account (most common).
*   `--local`: Applies only to the specific repository you are in.

*View all settings:* `git config --list`

---

## 3. The Local Workflow (The 3 Trees)

Git tracks history using three distinct states (trees):

1.  **Working Tree:** The files you are currently editing on your disk.
2.  **Staging Area (Index):** A holding area for changes you want to commit next.
3.  **History (Repository):** The recorded snapshots (commits).

### Basic Commands
| Action | Command | Description |
| :--- | :--- | :--- |
| **Initialize** | `git init` | Turns the current folder into a Git repository (creates `.git` folder). |
| **Check Status** | `git status` | **Crucial command.** Shows what branch you are on, untracked files, and staged changes. |
| **Stage** | `git add <file>` | Moves changes from Working Tree to Staging Area. Use `git add .` for all files. |
| **Commit** | `git commit -m "msg"` | Takes a snapshot of the Staging Area and saves it to History. |

**Commit Message Best Practices:**
*   Short (~50 chars).
*   Describe *what* changed and *why*.
*   Think of it as telling a story of the project's evolution.

### Viewing History & Diff
*   **View Log:**
    ```bash
    git log --oneline --graph --decorate --all  # The "Pro" view
    ```
*   **View Differences:**
    *   `git diff`: Working Tree vs. Staging (What have I changed but not staged?).
    *   `git diff --staged`: Staging vs. Last Commit (What will be included in the next commit?).
    *   `git diff HEAD`: Working Tree vs. Last Commit.

---

## 4. Navigation and Restoration

**Concepts:**
*   **Branch:** A pointer to a specific commit (default is `main` or `master`).
*   **HEAD:** A pointer to the *current* commit/branch you are looking at.

### Moving Around (`git switch`)
*   **Switch Branch:** `git switch <branch-name>`
*   **Time Travel (Detached HEAD):** `git switch <commit-hash>`
    *   `git switch main~` (Go 1 commit back from main).
    *   `git switch HEAD~` (Go 1 commit back from current).

### Undoing Changes (`git restore`)
*   **Unstage files:** `git restore --staged <file>` (Removes from staging, keeps in working tree).
*   **Discard changes:** `git restore <file>` (Reverts working tree file to last commit—**Dangerous**, data loss).

---

## 5. Branching and Merging

Branching allows you to work on features in isolation without breaking the main code.

### Managing Branches
```bash
# Create a new branch
git branch <new-branch>

# Switch to that branch
git switch <new-branch>

# Shortcut: Create AND switch
git switch -b <new-branch>

# Delete a branch (after merging)
git branch -d <branch-name>
```

### Merging
Brings changes from one branch into another.
1.  Switch to the target branch (e.g., `git switch main`).
2.  Run `git merge <feature-branch>`.

**Types of Merges:**
1.  **Fast-forward:** The target branch just moves its pointer forward (linear history).
2.  **Three-way merge:** Branches have diverged. Git creates a new "Merge Commit" to join them.

### Merge Conflicts
Occur when the same line of code was changed differently in both branches.
1.  Git pauses the merge.
2.  Open the file (look for markers `<<<<<<<`, `=======`, `>>>>>>>`).
3.  Edit the file to choose the correct code.
4.  **Stage the resolved file:** `git add <file>`.
5.  **Commit:** `git commit` (no message needed, Git provides a default).

---

## 6. Remote Repositories (Collaboration)

Connecting your local code to a server (e.g., GitHub).

### Basics
*   **Clone:** `git clone <url>` (Download a repo from a server).
*   **Remote:** `git remote -v` (List connected servers).
*   **Add Remote:** `git remote add origin <url>`.

### Syncing
*   **Fetch:** `git fetch` (Downloads changes but does **not** merge them into your code).
*   **Pull:** `git pull` (Equivalent to `git fetch` + `git merge`). Updates your current branch.
*   **Push:** `git push origin <branch-name>` (Uploads your commits to the server).

---

## 7. Advanced: Rewriting History

### Ignoring Files (`.gitignore`)
Create a file named `.gitignore`. List files/folders to hide from Git (e.g., build artifacts, passwords, `node_modules`).
*   *Tip:* If you already committed a file and want to ignore it now:
    ```bash
    git rm --cached <file>
    # Then add to .gitignore and commit
    ```

### Rebasing (`git rebase`)
An alternative to merging. It moves your branch to start from the tip of another branch.
*   **Goal:** Keeps a clean, linear history.
*   **Command:** `git rebase main` (while on your feature branch).
*   **Interactive Rebase:** `git rebase -i HEAD~3` allows you to squash, reword, or drop the last 3 commits.

### Reset (`git reset`)
Moves the branch pointer backward. **Destructive.**
*   `git reset --soft HEAD~1`: Undo commit, keep changes staged.
*   `git reset --mixed HEAD~1`: Undo commit, keep changes in working tree (unstaged). *Default*.
*   `git reset --hard HEAD~1`: Undo commit, **destroy** changes.

### Revert (`git revert`)
The "Safe Undo". Instead of deleting history, it creates a *new* commit that does the opposite of the specified commit.
*   `git revert <commit-hash>`
*   Use this for public/shared branches.

---

## 8. Extras

*   **Stashing:**
    *   `git stash`: Temporarily hide changes to clean the working directory (useful if you need to switch branches quickly but aren't ready to commit).
    *   `git stash pop`: Bring the hidden changes back.
*   **Tagging:**
    *   `git tag -a v1.0 -m "Version 1.0"`: Mark a specific commit as a release.
    *   `git push origin --tags`: Send tags to remote.

---

## Quick Reference Cheat Sheet

| Intent | Command |
| :--- | :--- |
| **Start Repo** | `git init` |
| **Stage All** | `git add .` |
| **Commit** | `git commit -m "message"` |
| **Check State** | `git status` |
| **View History** | `git log --oneline --graph` |
| **New Branch** | `git switch -b <name>` |
| **Update Local** | `git pull` |
| **Upload Code** | `git push origin main` |
| **Undo (Safe)** | `git revert HEAD` |
| **Emergency Hide**| `git stash` |