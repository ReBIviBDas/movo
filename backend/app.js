const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const swaggerUi = require('swagger-ui-express');
const yaml = require('js-yaml');
const fs = require('fs');
const path = require('path');
const authRoutes = require('./routes/auth');
const usersRoutes = require('./routes/users');
const vehiclesRoutes = require('./routes/vehicles');
const fleetRoutes = require('./routes/fleet');
const tokenChecker = require('./middlewares/tokenChecker');
require('dotenv').config();

const app = express();

// 1. Middleware (Parsers)
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

// Serve uploaded files statically (for document preview)
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// 2. Swagger / OpenAPI Setup
// We try to load the file named 'openapi.yaml' or 'oas3.yaml'
// CHECK: Make sure the file name below matches what you have in your folder!
const oasFile = fs.existsSync(path.join(__dirname, 'openapi.yaml')) ? 'openapi.yaml' : 'oas3.yaml';

try {
    const oas3 = yaml.load(fs.readFileSync(path.join(__dirname, oasFile), 'utf8'));
    app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(oas3));
    console.log(`✅ Swagger docs available at /api-docs (loaded ${oasFile})`);
} catch (e) {
    console.error("⚠️  Warning: Could not load OpenAPI file. Check if openapi.yaml or oas3.yaml exists in backend/ folder.");
}

// Routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', usersRoutes);
app.use('/api/v1/vehicles', vehiclesRoutes);
app.use('/api/v1/fleet', fleetRoutes);

// PROTECTED ROUTE EXAMPLE (deprecated - use /users/me instead)
app.get('/api/v1/profile', tokenChecker, (req, res) => {
    res.json({ 
        message: 'You are seeing this because you are logged in!', 
        yourData: req.loggedUser 
    });
});

// 3. Database Connection
// This connects to the URL you put in .env
mongoose.connect(process.env.DB_URL)
    .then(() => console.log("✅ Connected to MongoDB Atlas"))
    .catch((err) => console.error("❌ DB Connection Error:", err));

// 4. Basic Health Check Route
app.get('/api/v1/health', (req, res) => {
    res.json({ status: 'OK', message: 'MOVO Backend is running' });
});

module.exports = app;