const request = require('supertest');
const { connectDB, disconnectDB, clearDB, createTestUser, createTestVehicle, createTestPaymentMethod } = require('./setup');

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
// Test 7: Creazione prenotazione valida
// ============================================================================
describe('POST /api/v1/bookings', () => {
    it('Test 7 - should create a booking for an available vehicle', async () => {
        const { user, token } = await createTestUser();
        const vehicle = await createTestVehicle({ status: 'available' });
        await createTestPaymentMethod(user._id);

        const res = await request(app)
            .post('/api/v1/bookings')
            .set('Authorization', `Bearer ${token}`)
            .send({ vehicle_id: vehicle._id.toString() });

        expect(res.status).toBe(201);
        expect(res.body).toHaveProperty('message');
        expect(res.body).toHaveProperty('booking');
        expect(res.body.booking).toHaveProperty('id');
        expect(res.body.booking).toHaveProperty('expires_at');
        expect(res.body.booking).toHaveProperty('vehicle');
        expect(res.body.booking.vehicle.id).toBe(vehicle._id.toString());
    });

    // ========================================================================
    // Test 8: Prenotazione veicolo non disponibile
    // ========================================================================
    it('Test 8 - should reject booking for unavailable vehicle', async () => {
        const { user, token } = await createTestUser();
        const vehicle = await createTestVehicle({ status: 'maintenance' });
        await createTestPaymentMethod(user._id);

        const res = await request(app)
            .post('/api/v1/bookings')
            .set('Authorization', `Bearer ${token}`)
            .send({ vehicle_id: vehicle._id.toString() });

        expect(res.status).toBe(409);
        expect(res.body.type).toBe('conflict');
    });
});
