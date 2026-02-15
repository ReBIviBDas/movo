const request = require('supertest');
const { connectDB, disconnectDB, clearDB, createTestUser, createTestVehicle, createTestPaymentMethod } = require('./setup');
const Booking = require('../models/Booking');

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
// Test 10: Sblocco veicolo (inizio noleggio)
// ============================================================================
describe('POST /api/v1/rentals/unlock', () => {
    it('Test 10 - should unlock vehicle and start rental from valid booking', async () => {
        // Setup: user, vehicle, payment method, and booking
        const { user, token } = await createTestUser();
        const vehicle = await createTestVehicle({ status: 'available' });
        await createTestPaymentMethod(user._id);

        // Create a booking first
        const bookingRes = await request(app)
            .post('/api/v1/bookings')
            .set('Authorization', `Bearer ${token}`)
            .send({ vehicle_id: vehicle._id.toString() });

        expect(bookingRes.status).toBe(201);
        const bookingId = bookingRes.body.booking.id;

        // Unlock the vehicle (user location near the vehicle in Trento)
        const res = await request(app)
            .post('/api/v1/rentals/unlock')
            .set('Authorization', `Bearer ${token}`)
            .send({
                booking_id: bookingId,
                user_location: {
                    lat: 46.0677,
                    lng: 11.1219
                }
            });

        expect(res.status).toBe(201);
        expect(res.body).toHaveProperty('message');
        expect(res.body).toHaveProperty('rental');
        expect(res.body.rental).toHaveProperty('id');
        expect(res.body.rental).toHaveProperty('started_at');
        expect(res.body.rental).toHaveProperty('vehicle');
    });
});
