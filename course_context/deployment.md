## 1. Cloud Service Models

### IaaS vs. PaaS vs. SaaS
Understanding the responsibility shift between you and the provider.

| Model    | Name                        | Description                                                                                                                             | Examples                                  |
| :------- | :-------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------- | :---------------------------------------- |
| **IaaS** | Infrastructure as a Service | Provides virtualized computing resources (VMs, Storage, Networking). You manage OS, Runtime, & App.                                     | AWS EC2, Google Compute Engine, Azure VMs |
| **PaaS** | Platform as a Service       | Provides a platform allowing customers to develop, run, and manage applications without building infrastructure. You manage Data & App. | **Render**, Heroku, Google App Engine     |
| **SaaS** | Software as a Service       | Software is licensed on a subscription basis and is centrally hosted. You manage nothing.                                               | Gmail, Dropbox, Salesforce                |

---

## 2. Deploying Backend (Node.js/Express)

For this course, we use **Render** (PaaS) to host the Node.js backend.

### Preparation
1.  **Configure `package.json`:** Ensure you have start scripts defined.
    ```json
    "scripts": {
        "start": "node index.js"
    }
    ```
2.  **Environment Variables:** Do not hardcode ports or secrets.
    ```javascript
    const PORT = process.env.PORT || 8080;
    app.listen(PORT, ...);
    ```

### Deployment Steps (Render)
1.  Push your code to **GitHub/GitLab**.
2.  Log in to [Render.com](https://render.com/).
3.  Create a **New Web Service**.
4.  Connect your repository.
5.  **Configure Settings:**
    *   **Build Command:** `npm install`
    *   **Start Command:** `npm start`
6.  **Environment Variables:** Add `DB_URL` (MongoDB Atlas connection string) and `SUPER_SECRET` (JWT secret) in the Render dashboard settings.

---

## 3. Deploying Frontend (Vue.js)

There are two main strategies for deploying the frontend.

### Strategy A: Static Site Hosting (Recommended for Frontend-only)
Deploy the Vue app as a static site separate from the backend.

1.  **Configure `package.json`:**
    ```json
    "scripts": {
        "build": "vite build"
    }
    ```
2.  **Environment Configuration:**
    The frontend needs to know where the backend API is living.
    *   Create `.env.production`: `VITE_API_URL=https://your-backend.onrender.com`
    *   In code: `const API_URL = import.meta.env.VITE_API_URL;`
3.  **Deploy on Render:**
    *   Create a **Static Site**.
    *   **Build Command:** `npm run build`
    *   **Publish Directory:** `dist`

### Strategy B: Serving from Backend (Monolith Style)
Build the frontend and serve it as static files from the Node.js Express server.

1.  **Build Frontend:** Run `npm run build` locally or in CI. This creates a `dist` folder.
2.  **Configure Express:**
    ```javascript
    const path = require('path');
    
    // Serve static files from the Vue dist folder
    app.use('/', express.static(path.join(__dirname, '../frontend/dist')));
    
    // Catch-all handler for SPA routing (Important for Vue Router History Mode)
    app.get('*', (req, res) => {
        res.sendFile(path.join(__dirname, '../frontend/dist/index.html'));
    });
    ```
3.  **Deploy Backend:** Deploying the backend now effectively deploys the whole app.

### Strategy C: GitHub Pages
Host the frontend for free on GitHub Pages.

1.  **Build Script (`deploy.sh`):**
    ```bash
    #!/usr/bin/env sh
    set -e
    npm run build
    cd dist
    git init
    git add -A
    git commit -m 'deploy'
    git push -f git@github.com:<USERNAME>/<REPO>.git main:gh-pages
    cd -
    ```
2.  **Run:** `sh deploy.sh`
3.  **Note:** Requires a dedicated repository for the frontend or specific configuration to push the `dist` folder to a `gh-pages` branch.

---

## 4. Modern Web Architecture Recap

### Serverless Architecture
Backend logic is split into individual functions (e.g., AWS Lambda) triggered by events (HTTP requests, database changes).
*   **Pros:** Highly scalable, cost-effective (pay per execution).
*   **Cons:** Harder to debug (Cold starts, distributed complexity).

### Course Architecture (SPA + REST API)
*   **Frontend:** Vue.js SPA loaded from a CDN/Static Host.
*   **Backend:** Node.js REST API on PaaS (Render).
*   **Database:** MongoDB Atlas (DBaaS).
*   **Communication:** JSON over HTTP.