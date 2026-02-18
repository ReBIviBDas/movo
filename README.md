# movo

[![CI Backend](https://github.com/ReBIviBDas/movo/actions/workflows/ci-backend.yml/badge.svg)](https://github.com/ReBIviBDas/movo/actions/workflows/ci-backend.yml)
[![CI Frontend](https://github.com/ReBIviBDas/movo/actions/workflows/ci-frontend.yml/badge.svg)](https://github.com/ReBIviBDas/movo/actions/workflows/ci-frontend.yml)
[![CI Mobile](https://github.com/ReBIviBDas/movo/actions/workflows/ci-mobileapp.yml/badge.svg)](https://github.com/ReBIviBDas/movo/actions/workflows/ci-mobileapp.yml)

Car sharing platform for the software engineering course exam at unitn, built as a monorepo with a Node.js backend, Vue.js web frontend, and Kotlin Multiplatform mobile app.

Backend and frontend are deployed on Render at [movo-frontend.onrender.com](movo-frontend.onrender.com) and [movo-backend-hvlw.onrender.com/api/v1](https://movo-backend-hvlw.onrender.com/api/v1).

API documentation is served via Swagger at [https://app.swaggerhub.com/apis/none-afc-206/MovoCarSharing/1.0.0](https://app.swaggerhub.com/apis/none-afc-206/MovoCarSharing/1.0.0).

## Repository structure

```
movo/
  backend/        Node.js + Express + MongoDB API
  frontend/       Vue 3 + Vite + Tailwind web app
  mobileapp/      Kotlin Multiplatform + Compose Multiplatform (Android/iOS)
  .github/        CI/CD workflows
```

## Local setup

### Prerequisites

- Node.js 20+
- Java 21+ (for mobile app)
- MongoDB instance (local or Atlas)

### Backend

```bash
cd backend
npm install
```

Create a `.env` file in `backend/`:

```
DB_URL=mongodb://localhost:27017/movo
SUPER_SECRET=your_jwt_secret_here
PORT=10000
```

Start the development server:

```bash
npm run dev
```

Run tests (uses an in-memory MongoDB instance, no external database required):

```bash
npm test
```

### Frontend

```bash
cd frontend
npm install
```

Create a `.env` file in `frontend/` (see `.env.example`):

```
VITE_API_URL=http://localhost:10000/api/v1
VITE_GOOGLE_CLIENT_ID=your_google_client_id
```

Start the development server:

```bash
npm run dev
```

### Mobile app

```bash
cd mobileapp
./gradlew :androidApp:assembleRelease
```

The mobile app targets Android and iOS through Kotlin Multiplatform. The API base URL is configured in `composeApp/build.gradle.kts`.

## Authentication

The API uses JWT tokens for authentication. Tokens are issued by the `/api/v1/auth/login` endpoint and must be included in subsequent requests via the `Authorization` header.

```
Authorization: Bearer <token>
```

Tokens are signed with the `SUPER_SECRET` environment variable and expire after 24 hours. The payload contains the user's `id`, `email`, and `role`.

Protected routes use the `tokenChecker` middleware (`backend/middlewares/tokenChecker.js`) to validate the token before granting access.

### User roles

| Role | Description |
|------|-------------|
| `user` | Standard user. Can register, book vehicles, start rentals, manage wallet. |
| `operator` | Fleet manager. Can manage vehicles, verify users, resolve issues. |
| `admin` | Full access. |

## CI/CD

CI and CD pipelines are split per component with path filters, so only the affected component runs on each change.

| Workflow | Trigger | What it does |
|----------|---------|--------------|
| `ci-backend.yml` | Push/PR to `main` touching `backend/**` | Runs `npm test` |
| `ci-frontend.yml` | Push/PR to `main` touching `frontend/**` | Runs `npm run build` |
| `ci-mobileapp.yml` | Push/PR to `main` touching `mobileapp/**` | Runs `./gradlew :composeApp:jvmTest` |
| `cd-backend.yml` | After CI Backend succeeds on `main` | Deploys backend to Render |
| `cd-frontend.yml` | After CI Frontend succeeds on `main` | Deploys frontend to Render |

## Environment variables

### Backend (`backend/.env`)

| Variable | Description |
|----------|-------------|
| `DB_URL` | MongoDB connection string |
| `SUPER_SECRET` | Secret used to sign and verify JWT tokens |
| `PORT` | Server port (defaults to 10000) |

### Frontend (`frontend/.env`)

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Backend API base URL |
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth client ID for social login |

### CI/CD secrets (GitHub)

| Secret | Description |
|--------|-------------|
| `RENDER_API_KEY` | Render platform API key |
| `RENDER_BACKEND_SERVICE_ID` | Render service ID for backend deployment |
| `RENDER_FRONTEND_SERVICE_ID` | Render service ID for frontend deployment |
