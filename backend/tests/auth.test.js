const request = require('supertest');
const { connectDB, disconnectDB, clearDB } = require('./setup');

// Must require app AFTER setup so mongoose can be redirected
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
// Test 1: Registrazione con dati validi
// ============================================================================
describe('POST /api/v1/auth/register', () => {
    it('Test 1 - should register a new user with valid data', async () => {
        const userData = {
            email: 'newuser@test.com',
            password: 'SecurePass123!',
            first_name: 'Mario',
            last_name: 'Rossi',
            date_of_birth: '1995-05-15',
            fiscal_code: 'RSSMRA95E15H501Z',
            phone: '+39 333 1234567',
            accept_terms: true,
            accept_privacy: true
        };

        const res = await request(app)
            .post('/api/v1/auth/register')
            .send(userData);

        expect(res.status).toBe(201);
        expect(res.body).toHaveProperty('message');
        expect(res.body.message).toMatch(/[Rr]egistrazione/);
    });

    // ========================================================================
    // Test 2: Registrazione con email duplicata
    // ========================================================================
    it('Test 2 - should reject registration with duplicate email', async () => {
        const userData = {
            email: 'duplicate@test.com',
            password: 'SecurePass123!',
            first_name: 'Mario',
            last_name: 'Rossi',
            date_of_birth: '1995-05-15',
            fiscal_code: 'RSSMRA95E15H501A',
            phone: '+39 333 1234567',
            accept_terms: true,
            accept_privacy: true
        };

        // First registration should succeed
        await request(app)
            .post('/api/v1/auth/register')
            .send(userData);

        // Second registration with same email should fail
        const res = await request(app)
            .post('/api/v1/auth/register')
            .send({
                ...userData,
                fiscal_code: 'RSSMRA95E15H501B' // different fiscal code
            });

        expect(res.status).toBe(409);
        expect(res.body.type).toBe('conflict');
    });
});

// ============================================================================
// Test 3: Login con credenziali corrette
// ============================================================================
describe('POST /api/v1/auth/login', () => {
    const testUser = {
        email: 'logintest@test.com',
        password: 'SecurePass123!',
        first_name: 'Test',
        last_name: 'Login',
        date_of_birth: '1995-05-15',
        fiscal_code: 'TSTLGN95E15H501Z',
        phone: '+39 333 9876543',
        accept_terms: true,
        accept_privacy: true
    };

    beforeEach(async () => {
        // Register the user first
        await request(app)
            .post('/api/v1/auth/register')
            .send(testUser);
    });

    it('Test 3 - should login with correct credentials', async () => {
        const res = await request(app)
            .post('/api/v1/auth/login')
            .send({
                email: testUser.email,
                password: testUser.password
            });

        expect(res.status).toBe(200);
        expect(res.body).toHaveProperty('access_token');
        expect(res.body).toHaveProperty('token_type', 'Bearer');
        expect(res.body).toHaveProperty('expires_in');
        expect(res.body).toHaveProperty('user');
        expect(res.body.user.email).toBe(testUser.email);
    });

    // ========================================================================
    // Test 4: Login con password errata
    // ========================================================================
    it('Test 4 - should reject login with wrong password', async () => {
        const res = await request(app)
            .post('/api/v1/auth/login')
            .send({
                email: testUser.email,
                password: 'WrongPassword!'
            });

        expect(res.status).toBe(401);
        expect(res.body.type).toBe('authentication_error');
    });
});
