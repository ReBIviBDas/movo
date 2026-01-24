## 1. Concepts
**CI/CD** automates the software lifecycle from development to production.
*   **Continuous Integration (CI):** Automates building and testing code every time a developer pushes changes.
*   **Continuous Deployment (CD):** Automates the release of the application to the production server (e.g., Render) if the CI tests pass.

**Tools used:**
*   **GitHub Actions:** For the workflow automation.
*   **Render:** For hosting the application (PaaS).

---

## 2. Pre-requisites: Git Hygiene
Before setting up automation, ensure your repository is clean.

### .gitignore
Never commit dependencies, secrets, or build artifacts.
*   **Tools:** Use [gitignore.io](https://www.gitignore.io) to generate standard templates (e.g., for Node, Windows, VSCode).
*   **Crucial ignores:**
    *   `node_modules/` (Heavy dependencies)
    *   `coverage/` (Test reports)
    *   `.env` (Secrets/Passwords)

```bash
# Add to .gitignore
node_modules
coverage
.env
dist
```

---

## 3. GitHub Actions Workflow
Workflows are defined in YAML files located in `.github/workflows/`.

### Basic Node.js CI Workflow
This workflow triggers on a push to `master`, installs dependencies, builds, and runs tests.

**File:** `.github/workflows/node.js.yml`

```yaml
name: Node.js CI

on:
  push:
    branches: [ master ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        node-version: [14.x] # Define versions to test against

    steps:
    - uses: actions/checkout@v3
    
    - name: Use Node.js ${{ matrix.node-version }}
      uses: actions/setup-node@v3
      with:
        node-version: ${{ matrix.node-version }}
        cache: 'npm'
        
    - run: npm ci 
      # Note: 'npm ci' is cleaner/faster than 'npm install' for CI environments
      
    - run: npm run build --if-present
    - run: npm test
```

---

## 4. Managing Secrets
**Never** hardcode secrets (API keys, DB URLs) in your YAML file. Use GitHub Secrets.

### Setup
1.  Go to GitHub Repository Settings > **Secrets and variables** > **Actions**.
2.  Add `New repository secret`.

### Accessing Secrets in YAML
Use the `${{ secrets.VAR_NAME }}` syntax.

```yaml
env:
  SUPER_SECRET: ${{ secrets.SUPER_SECRET }}
  DB_URL: ${{ secrets.DB_URL }}
```

---

## 5. Continuous Deployment to Render
If the `test` job passes, we trigger a deployment to Render.

### Required Secrets
You need to get these from your Render Dashboard and save them in GitHub Secrets:
1.  **RENDER_API_KEY:** Found in Account Settings > API Keys.
2.  **RENDER_SERVICE_ID:** Found in the Dashboard URL of your service (starts with `srv-...`).

### Deployment Job (Add to YAML)
Add this `deploy` job below the `test` job in your workflow file. Note the `needs: test` line—this ensures deployment only happens if tests pass.

```yaml
  deploy:
    name: Deploy to Render
    runs-on: ubuntu-latest
    needs: test # CRITICAL: Only deploy if tests pass
    
    steps:
    - name: Trigger deployment
      uses: sws2apps/render-deployment@main
      with:
        serviceId: ${{ secrets.RENDER_SERVICE_ID }}
        apiKey: ${{ secrets.RENDER_API_KEY }}
        multipleDeployment: false # Optional
```

### Alternative Action
Sometimes specific actions are preferred. Another popular option is `bounceapp/render-action`:

```yaml
    - name: Wait for Render Deployment
      uses: bounceapp/render-action@0.6.0
      with:
        render-token: ${{ secrets.RENDER_API_KEY }}
        github-token: ${{ secrets.GITHUB_TOKEN }} # Built-in GitHub token
        service-id: ${{ secrets.RENDER_SERVICE_ID }}
        retries: 20
        wait: 16000
```

---

## 6. Full Workflow Summary
1.  **Develop:** Write code and tests locally.
2.  **Push:** `git push origin master`.
3.  **Trigger:** GitHub Actions starts the `Node.js CI` workflow.
4.  **Test Job:**
    *   Installs dependencies (`npm ci`).
    *   Runs unit tests (`npm test`).
5.  **Deploy Job:**
    *   Waits for `Test Job` to succeed.
    *   Calls Render API to update the live service.
6.  **Result:** Live application updated automatically.