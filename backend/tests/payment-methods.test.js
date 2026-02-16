const request = require('supertest');
const { connectDB, disconnectDB, clearDB, createTestUser } = require('./setup');

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
// Test 10: Aggiunta metodo di pagamento
// ============================================================================
describe('POST /api/v1/payments/methods', () => {
    it('Test 12 - should add a payment method (first card becomes default)', async () => {
        const { token } = await createTestUser();

        const res = await request(app)
            .post('/api/v1/payments/methods')
            .set('Authorization', `Bearer ${token}`)
            .send({
                card_number: '4242424242424242',
                expiry_month: 12,
                expiry_year: 2028,
                cvv: '123',
                nickname: 'My Visa'
            });

        expect(res.status).toBe(201);
        expect(res.body).toHaveProperty('message');
        expect(res.body).toHaveProperty('payment_method');
        expect(res.body.payment_method).toHaveProperty('id');
        expect(res.body.payment_method).toHaveProperty('card_type');
        expect(res.body.payment_method).toHaveProperty('last_four');
        expect(res.body.payment_method.is_default).toBe(true);
    });
});
