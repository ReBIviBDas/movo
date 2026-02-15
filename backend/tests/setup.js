const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

let mongoServer;

/**
 * Connect to in-memory MongoDB before all tests.
 * Disconnects from any existing connection first (from app.js auto-connect).
 */
async function connectDB() {
    // Close any existing connection from app.js
    if (mongoose.connection.readyState !== 0) {
        await mongoose.disconnect();
    }

    mongoServer = await MongoMemoryServer.create();
    const uri = mongoServer.getUri();
    await mongoose.connect(uri);
}

/**
 * Drop all collections and close connection after all tests.
 */
async function disconnectDB() {
    if (mongoose.connection.readyState !== 0) {
        await mongoose.connection.dropDatabase();
        await mongoose.disconnect();
    }
    if (mongoServer) {
        await mongoServer.stop();
    }
}

/**
 * Clear all collections between tests.
 */
async function clearDB() {
    const collections = mongoose.connection.collections;
    for (const key in collections) {
        await collections[key].deleteMany({});
    }
}

/**
 * Generate a valid JWT token for testing.
 */
function generateToken(user) {
    const payload = {
        id: user._id || user.id,
        email: user.email,
        role: user.role || 'user'
    };
    return jwt.sign(payload, process.env.SUPER_SECRET || 'test-secret', {
        expiresIn: 86400
    });
}

/**
 * Create a test user directly in the DB and return the user + token.
 */
async function createTestUser(overrides = {}) {
    const User = require('../models/User');
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash('TestPass123!', salt);

    const userData = {
        first_name: 'Test',
        last_name: 'User',
        email: `test${Date.now()}@test.com`,
        password: hashedPassword,
        phone: '+39 333 1234567',
        date_of_birth: new Date('1995-05-15'),
        fiscal_code: `TSTCDF${Date.now().toString().slice(-10)}`,
        accept_terms: true,
        accept_privacy: true,
        status: 'active',
        role: 'user',
        ...overrides
    };

    const user = await User.create(userData);
    const token = generateToken(user);
    return { user, token };
}

/**
 * Create a test vehicle directly in the DB.
 */
async function createTestVehicle(overrides = {}) {
    const Vehicle = require('../models/Vehicle');
    const vehicleData = {
        plate: `AB${Date.now().toString().slice(-5)}CD`,
        model: 'Test Model EV',
        type: 'car',
        status: 'available',
        battery_level: 85,
        location: {
            type: 'Point',
            coordinates: [11.1219, 46.0677] // Trento
        },
        range_km: 200,
        price_per_minute: 0.35,
        ...overrides
    };
    return await Vehicle.create(vehicleData);
}

/**
 * Create a test payment method directly in the DB.
 */
async function createTestPaymentMethod(userId, overrides = {}) {
    const PaymentMethod = require('../models/PaymentMethod');
    const methodData = {
        user_id: userId,
        card_type: 'visa',
        last_four: '4242',
        expiry_month: 12,
        expiry_year: 2028,
        token: `tok_test_${Date.now()}`,
        is_default: true,
        ...overrides
    };
    return await PaymentMethod.create(methodData);
}

// Set test environment variable
process.env.SUPER_SECRET = process.env.SUPER_SECRET || 'test-jwt-secret-key';
process.env.NODE_ENV = 'test';

module.exports = {
    connectDB,
    disconnectDB,
    clearDB,
    generateToken,
    createTestUser,
    createTestVehicle,
    createTestPaymentMethod
};
