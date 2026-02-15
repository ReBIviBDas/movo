const request = require('supertest');
const { connectDB, disconnectDB, clearDB, createTestUser, createTestVehicle } = require('./setup');

let app;

beforeAll(async () => {
    await connectDB();
    app = require('../app');
});

afterAll(async () => {
    await disconnectDB();
});

afterEach(async () => {
    await clearDB();
});

// ============================================================================
// Test 5: Lista veicoli pubblici (senza auth)
// ============================================================================
describe('GET /api/v1/vehicles/public', () => {
    it('Test 5 - should return public vehicles without authentication', async () => {
        // Seed a vehicle
        await createTestVehicle({ status: 'available' });

        const res = await request(app)
            .get('/api/v1/vehicles/public');

        expect(res.status).toBe(200);
        expect(res.body).toHaveProperty('vehicles');
        expect(res.body).toHaveProperty('count');
        expect(res.body.count).toBeGreaterThanOrEqual(1);

        // Verify privacy: public endpoint should NOT expose plate
        const vehicle = res.body.vehicles[0];
        expect(vehicle).toHaveProperty('model');
        expect(vehicle).toHaveProperty('type');
        expect(vehicle).toHaveProperty('location');
        expect(vehicle).not.toHaveProperty('plate');
    });
});

// ============================================================================
// Test 6: Accesso protetto senza token
// ============================================================================
describe('GET /api/v1/bookings (protected)', () => {
    it('Test 6 - should reject access without token', async () => {
        const res = await request(app)
            .get('/api/v1/bookings');

        expect(res.status).toBe(401);
        expect(res.body.type).toBe('authentication_error');
        expect(res.body.detail).toMatch(/[Nn]o token/);
    });
});
