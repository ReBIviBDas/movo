# 🚀 D2 Development Guide: From Zero to Deploy

**Goal:** Build a functional prototype of MOVO, implementing the 10 User Stories defined in D1, documented via OpenAPI, tested, and deployed.

## 🛠 Phase 0: The Setup (Do this immediately)

Before writing code, everyone in the team needs these tools installed:

1.  **Node.js (LTS version):** Download from [nodejs.org](https://nodejs.org/). This allows you to run JavaScript outside the browser.
2.  **VS Code:** The standard code editor. Install the "Prettier" and "Vetur/Volar" extensions.
3.  **Git:** For version control.
4.  **Postman:** To test your backend APIs without a frontend.
5.  **MongoDB Compass:** A visual tool to look inside your database.

---

## 📂 Phase 1: Repository & Organization

You need to set up your GitHub repository. Following the slides, keep it clean.

1.  **Clone your Repo:** `git clone <your-repo-url>`
2.  **Create Folder Structure:** You need two distinct folders inside your main folder.
    *   `/backend` (For the Node.js API)
    *   `/frontend` (For the Vue.js App)
3.  **Git Branching Strategy (Important for Grade):**
    *   Never work directly on `main`.
    *   Create a branch for each feature: `git switch -b feature/login-page`.
    *   When done, push and do a **Pull Request** on GitHub. Merge only when it works.

---

## 🗄 Phase 2: Database (MongoDB)

Do not install MongoDB locally. Use the Cloud (MongoDB Atlas) as suggested in the slides.

1.  Go to **MongoDB Atlas** and create a free account.
2.  Create a **Cluster** (Free tier).
3.  **Database Access:** Create a user (e.g., `movo_admin`) and password. **Save this password!**
4.  **Network Access:** Allow access from anywhere (`0.0.0.0/0`).
5.  **Get Connection String:** Click "Connect" -> "Drivers" -> Copy the string (looks like `mongodb+srv://...`).
6.  **Compass:** Open MongoDB Compass on your PC and paste that string to ensure you can connect.

---

## ⚙️ Phase 3: Backend Development (Node.js + Express)

This is the "Brain" of MOVO.

**1. Initialization:**
*   Open terminal in `/backend`.
*   Run `npm init -y` (creates `package.json`).
*   Install dependencies:
    ```bash
    npm install express mongoose dotenv cors jsonwebtoken
    npm install --save-dev nodemon jest supertest
    ```

**2. Setup `app.js` (Entry Point):**
Create an `app.js` file. This needs to:
*   Import Express.
*   Connect to MongoDB (using `mongoose`).
*   Use `cors` (to allow Frontend to talk to Backend).
*   Use `express.json()` (to understand JSON data).

**3. Environment Variables:**
*   Create a `.env` file in `/backend`.
*   Add: `DB_URL=your_mongodb_connection_string` and `SUPER_SECRET=somethingsecret`.
*   **Crucial:** Add `.env` to your `.gitignore` file. Never upload passwords to GitHub.

**4. The MVC Structure (Keep it organized):**
*   `/models`: Define what your data looks like (Mongoose Schemas).
    *   *Example:* `User.js`, `Vehicle.js`, `Rental.js`.
*   `/controllers`: The logic. "What happens when I request to rent a car?".
*   `/routes`: The URLs. E.g., `router.post('/rentals', rentalController.create)`.

**5. Authentication (Hardest Part):**
*   You must implement **JWT (JSON Web Token)**.
*   **Login Flow:** User sends email/password -> Server checks DB -> Server signs a Token (using `jsonwebtoken`) -> Server sends Token back.
*   **Protection:** Create a middleware `tokenChecker.js`. Put this before any route that requires login (like booking a car). It checks if the Token is valid.

---

## 📜 Phase 4: API Documentation (OpenAPI/Swagger)

This is **mandatory** for D2. Do not leave this for the end.

1.  **Install:** `npm install swagger-ui-express swagger-jsdoc`.
2.  **Create `oas3.yaml`:** This file describes your API.
    *   List every endpoint (e.g., `GET /vehicles`, `POST /login`).
    *   Describe inputs (request body) and outputs (response JSON).
3.  **Serve it:** In `app.js`, add the code from the slides to serve this file at `http://localhost:3000/api-docs`.
    *   *Why?* The professor will check this URL to understand how to use your backend.

---

## 🎨 Phase 5: Frontend Development (Vue.js)

This is the "Face" of MOVO.

**1. Initialization:**
*   Open terminal in `/frontend`.
*   Run `npm init vue@latest`. (Select "No" for TypeScript/Testing for simplicity unless you feel confident).
*   Run `npm install`.
*   **Install UI Framework (Required):**
    ```bash
    npm install -D tailwindcss postcss autoprefixer
    npx tailwindcss init -p
    npm install daisyui
    ```
    *   *Note:* Configure `tailwind.config.js` to include DaisyUI as a plugin.

**2. Structure:**
*   `/views`: Full pages (e.g., `HomeView.vue`, `LoginView.vue`, `MapView.vue`).
*   `/components`: Reusable parts (e.g., `NavBar.vue`, `VehicleCard.vue`).
*   `/router`: Defines URLs (e.g., `/login` goes to `LoginView`).

**3. Connecting to Backend:**
*   Use `fetch()` inside your Vue components.
*   *Example:* To get vehicles, put a `fetch('https://your-backend.com/api/v1/vehicles')` inside `onMounted`.
*   **Auth State:** When a user logs in, save the **Token** in `localStorage`. Read it from there to make authenticated requests.

**4. Google Auth:**
*   Use the Google Identity Services script (referenced in slides) in your `index.html` or a Vue component to handle "Sign in with Google".

---

## 🧪 Phase 6: Testing (Jest)

You need to prove your backend works automatically.

1.  **Configuration:** Ensure `package.json` has a test script: `"test": "jest"`.
2.  **Write Tests:** Create files like `auth.test.js`.
    *   Use `supertest` to send fake HTTP requests to your app.
    *   *Example:* Send a POST to `/login` with wrong password -> Expect 401 Error.
    *   *Example:* Send a GET to `/vehicles` -> Expect 200 OK and a list of cars.
3.  **Run:** `npm test`. Take screenshots of green ticks for the D2 report.

---

## 🚀 Phase 7: Deployment (Render.com)

You need your app online.

**1. Backend:**
*   Create "Web Service" on Render.
*   Connect GitHub Repo.
*   Root Directory: `backend`.
*   Build Command: `npm install`.
*   Start Command: `node app.js`.
*   **Environment Variables:** Add your `DB_URL` and `SUPER_SECRET` here in the Render dashboard.

**2. Frontend:**
*   Create "Static Site" on Render.
*   Connect GitHub Repo.
*   Root Directory: `frontend`.
*   Build Command: `npm run build`.
*   Publish Directory: `dist`.
*   **Important:** You might need to tell your frontend *where* the backend is. Usually, we use an env variable `VITE_API_URL` pointing to your Render Backend URL.

---

## 📝 Checklist for D2 Submission

1.  [ ] **OpenAPI Spec:** Accessible at `/api-docs` on the live backend.
2.  [ ] **Working Backend:** Responds to JSON requests.
3.  [ ] **Working Frontend:** Vue app looks like the mockups in D1.
4.  [ ] **Database:** Data persists in MongoDB Atlas.
5.  [ ] **User Stories:** The 10 selected stories are implemented.
6.  [ ] **Auth:** Login/Register works (JWT).
7.  [ ] **Tests:** `npm test` passes.
8.  [ ] **Deployed:** Both links (frontend and backend) are active on Render.

**Pro Tip:** Start with **one** vertical slice. Make the "Login" work fully (Backend -> DB -> Frontend -> Deploy). Once that pipeline is clear, doing the other features is just repetition. Good luck!