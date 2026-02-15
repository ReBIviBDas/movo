# MOVO Project Structure

```
movo/
├── .gitignore
├── README.md
├── mongodb-credentials.txt
│
├── backend/                        # Express.js REST API
│   ├── .env                        # Environment variables (MongoDB, JWT, Google OAuth)
│   ├── index.js                    # Server entry point
│   ├── app.js                      # Express app configuration
│   ├── package.json                # Dependencies (express, mongoose, jwt, etc.)
│   ├── package-lock.json
│   ├── openapi.yaml                # Full API specification
│   ├── seed_users.js               # Database seeding script for users
│   ├── seed_vehicles.js            # Database seeding script for vehicles
│   │
│   ├── models/                     # Mongoose schemas (7 models)
│   │   ├── User.js                 # User accounts & authentication
│   │   ├── Vehicle.js              # Fleet vehicles with GeoJSON location
│   │   ├── Booking.js              # Vehicle reservations with 15-min timer
│   │   ├── Rental.js               # Active & completed rentals
│   │   ├── PaymentMethod.js        # Saved payment methods
│   │   ├── Report.js               # User-reported vehicle problems
│   │   └── FleetAuditLog.js        # Fleet change tracking & audit logs
│   │
│   ├── routes/                     # API endpoints (8 route files)
│   │   ├── auth.js                 # Login, Register, Google OAuth
│   │   ├── users.js                # User management & approval
│   │   ├── vehicles.js             # Public vehicle listing
│   │   ├── fleet.js                # Operator fleet management & audit logs
│   │   ├── bookings.js             # Booking CRUD & timer management
│   │   ├── rentals.js              # Rental lifecycle (start, unlock, end)
│   │   ├── payment-methods.js      # Payment method CRUD
│   │   └── reports.js              # Problem reporting & operator triage
│   │
│   ├── services/                   # Business logic
│   │   ├── paymentService.js       # Mock payment processing (Stripe-ready)
│   │   ├── locationService.js      # Mock GPS/geofencing & proximity checks
│   │   └── emailService.js         # Mock email notifications
│   │
│   ├── middlewares/                # Auth & file upload
│   │   ├── tokenChecker.js         # JWT validation
│   │   ├── roleChecker.js          # Role-based access control
│   │   └── upload.js               # Multer file upload for documents/images
│   │
│   └── uploads/                    # Uploaded files storage
│
├── frontend/                       # Vue 3 SPA
│   ├── .env                        # Environment variables (API URL, Google OAuth)
│   ├── index.html                  # HTML entry point
│   ├── package.json                # Dependencies (vue, vue-router, axios, leaflet, etc.)
│   ├── package-lock.json
│   ├── vite.config.js              # Vite build configuration
│   ├── tailwind.config.js          # Tailwind CSS configuration
│   ├── postcss.config.js           # PostCSS configuration
│   │
│   └── src/
│       ├── main.js                 # Vue app entry point
│       ├── App.vue                 # Root component with navigation
│       │
│       ├── views/                  # Page components
│       │   ├── HomeView.vue        # Landing page with vehicle map
│       │   ├── LoginView.vue       # Auth with Google OAuth option
│       │   ├── RegisterView.vue    # User registration with role selection
│       │   │
│       │   ├── user/               # User dashboard views (7 views)
│       │   │   ├── DashboardView.vue        # User overview & quick actions
│       │   │   ├── WalletView.vue           # Payment methods management
│       │   │   ├── ActiveBookingView.vue    # Current booking with countdown
│       │   │   ├── ActiveRentalView.vue     # Active rental with unlock & end
│       │   │   ├── RentalHistoryView.vue    # Past rentals & costs
│       │   │   ├── ReportProblemView.vue    # Report vehicle issues
│       │   │   └── MyReportsView.vue        # User's submitted reports
│       │   │
│       │   └── operator/           # Operator dashboard views (6 views)
│       │       ├── DashboardView.vue        # Operator overview & analytics
│       │       ├── FleetView.vue            # Fleet management & vehicle CRUD
│       │       ├── VehicleDetailView.vue    # Individual vehicle details & editing
│       │       ├── PendingUsersView.vue     # User approval workflow
│       │       ├── ReportsView.vue          # Problem reports triage
│       │       └── ReportDetailView.vue     # Report details & status updates
│       │
│       ├── components/
│       │   └── VehicleMap.vue      # Leaflet map with vehicle markers
│       │
│       ├── router/
│       │   ├── index.js            # Vue Router configuration
│       │   └── guards.js           # Route guards for auth & roles
│       │
│       ├── states/
│       │   └── auth.js             # Simple reactive auth state
│       │
│       ├── config/
│       │   └── api.js              # API base URL configuration
│       │
│       └── assets/
│           └── main.css            # Global CSS styles
│
└── course_context/                 # Course materials & documentation (15 files)
    ├── Authentication in RESTful APIs.md
    ├── Continuous Integration & Deployment.md
    ├── Express.js.md
    ├── Frontend.md
    ├── MongoDB and Mongoose.md
    ├── OpenAPI.md
    ├── RESTful WebAPIs.md
    ├── Testing and Architecture.md
    ├── d2 scaletta.md
    ├── deployment.md
    ├── git.md
    ├── javascript and node.js.md
    ├── movo d1 contents.md
    ├── user-stories-short.md
    └── user-stories.md
```

## Key Features Implemented

### Backend (Express.js + MongoDB)
- **7 Mongoose models**: User, Vehicle, Booking, Rental, PaymentMethod, Report, FleetAuditLog
- **8 API route files**: Complete CRUD operations for all resources
- **Authentication**: JWT-based auth + Google OAuth integration
- **Role-based access**: User vs Operator permissions
- **Mock services**: Payment processing, GPS/geofencing, email notifications
- **File uploads**: Document and image upload support

### Frontend (Vue 3 + Vite + Tailwind)
- **3 main views**: Home (landing + map), Login, Register
- **7 user views**: Dashboard, Wallet, Active Booking, Active Rental, Rental History, Report Problem, My Reports
- **6 operator views**: Dashboard, Fleet Management, Vehicle Details, Pending Users, Reports Triage, Report Details
- **Leaflet map integration**: Real-time vehicle location display
- **Reactive state management**: Simple auth state with Vue reactivity
- **Route guards**: Protected routes based on authentication and roles

### Configuration Files
- **Backend .env**: MongoDB URI, JWT secret, Google OAuth credentials
- **Frontend .env**: API base URL, Google OAuth client ID
- **.gitignore**: Node modules and environment files excluded
- **Tailwind**: Custom theme and responsive design configuration
- **Vite**: Modern build tooling with hot module replacement
