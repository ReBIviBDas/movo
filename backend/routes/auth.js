const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { sendRegistrationConfirmation } = require('../services/emailService');

// Helper function to calculate age
function calculateAge(dateOfBirth) {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
}

// POST /api/v1/auth/register
// Follows OpenAPI spec for request/response format
router.post('/register', async (req, res) => {
    try {
        // 1. Check required fields
        const { email, password, first_name, last_name, date_of_birth, fiscal_code, phone, accept_terms, accept_privacy } = req.body;

        if (!accept_terms || !accept_privacy) {
            return res.status(400).json({ 
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'You must accept terms of service and privacy policy'
            });
        }

        // 2. Age verification - must be 18+ to register
        const age = calculateAge(date_of_birth);
        if (age < 18) {
            return res.status(400).json({ 
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Devi avere almeno 18 anni per registrarti'
            });
        }

        // 3. Check if user with this email or fiscal_code already exists
        let user = await User.findOne({ 
            $or: [{ email }, { fiscal_code }] 
        });

        if (user) {
            return res.status(409).json({ 
                type: 'conflict',
                title: 'Conflict',
                detail: 'User with this email or fiscal code already exists'
            });
        }

        // 4. Hash the password (Security RNF5)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 5. Determine status based on whether driving_license is provided
        // If documents are uploaded, status is pending_approval; otherwise active (passenger only)
        const hasDrivingLicense = req.body.driving_license ? true : false;
        const accountStatus = hasDrivingLicense ? 'pending_approval' : 'active';

        // 6. Create the new user
        let newUser = new User({
            first_name,
            last_name,
            email,
            password: hashedPassword,
            phone,
            date_of_birth: new Date(date_of_birth),
            fiscal_code: fiscal_code.toUpperCase(), // Normalize fiscal code
            address: req.body.address || '',
            accept_terms,
            accept_privacy,
            accept_cookies: req.body.accept_cookies || false,
            status: accountStatus
        });

        // 7. Save to DB
        await newUser.save();

        // 8. Send confirmation email (mock)
        await sendRegistrationConfirmation(newUser);

        // 9. Respond (matching OpenAPI spec)
        res.status(201).json({ 
            user_id: newUser._id,
            email: newUser.email,
            status: newUser.status,
            message: accountStatus === 'pending_approval' 
                ? 'Registrazione completata. In attesa di approvazione documenti.'
                : 'Registrazione completata. Email di conferma inviata.'
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ 
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Server error during registration'
        });
    }
});

// POST /api/v1/auth/login
// Follows OpenAPI spec for AuthResponse format
router.post('/login', async (req, res) => {
    try {
        // 1. Find the user by email
        const user = await User.findOne({ email: req.body.email });
        
        if (!user) {
            return res.status(401).json({ 
                type: 'authentication_error',
                title: 'Unauthorized',
                detail: 'Authentication failed. User not found.'
            });
        }

        // 2. Check the password
        const checkPassword = await bcrypt.compare(req.body.password, user.password);

        if (!checkPassword) {
            return res.status(401).json({ 
                type: 'authentication_error',
                title: 'Unauthorized',
                detail: 'Authentication failed. Wrong password.'
            });
        }

        // 3. Generate the Token (matching OpenAPI spec)
        const payload = { 
            id: user._id, 
            email: user.email,
            role: user.role 
        };

        const expiresInSeconds = 86400; // 24 hours
        const token = jwt.sign(payload, process.env.SUPER_SECRET, {
            expiresIn: expiresInSeconds
        });

        // 4. Return the token (matching OpenAPI AuthResponse schema)
        res.status(200).json({
            access_token: token,
            token_type: 'Bearer',
            expires_in: expiresInSeconds,
            user: {
                id: user._id,
                email: user.email,
                first_name: user.first_name,
                last_name: user.last_name,
                role: user.role,
                status: user.status
            }
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ 
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Server error during login'
        });
    }
});

// POST /api/v1/auth/login/google
// Handle Google OAuth - receives authorization code from frontend
router.post('/login/google', async (req, res) => {
    try {
        const { credential } = req.body;
        
        if (!credential) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Google credential token is required'
            });
        }

        // Decode the Google ID token (in production, verify with Google API)
        // Google uses base64url encoding, need to convert to standard base64
        const base64Payload = credential.split('.')[1];
        // Replace base64url chars with standard base64 chars
        const base64 = base64Payload.replace(/-/g, '+').replace(/_/g, '/');
        // Add padding if needed
        const padded = base64 + '='.repeat((4 - base64.length % 4) % 4);
        const payload = JSON.parse(Buffer.from(padded, 'base64').toString('utf8'));
        
        console.log('Google login payload decoded:', { email: payload.email, sub: payload.sub });

        const { email, given_name, family_name, sub: googleId } = payload;

        // Check if user exists
        let user = await User.findOne({ email });

        if (!user) {
            // Create new user from Google data
            // Generate a random password (user won't need it for Google login)
            const randomPassword = require('crypto').randomBytes(32).toString('hex');
            const hashedPassword = await bcrypt.hash(randomPassword, 10);

            user = new User({
                first_name: given_name || 'Google',
                last_name: family_name || 'User',
                email,
                password: hashedPassword,
                phone: '+39000000000', // Placeholder for Google users
                date_of_birth: new Date('2000-01-01'), // Placeholder, user should update
                fiscal_code: `GOOGLE${googleId.substring(0, 10).toUpperCase()}`,
                accept_terms: true,
                accept_privacy: true,
                status: 'active',
                google_id: googleId
            });

            await user.save();
            await sendRegistrationConfirmation(user);
        }

        // Generate JWT token
        const jwtPayload = { 
            id: user._id, 
            email: user.email,
            role: user.role 
        };

        const expiresInSeconds = 86400;
        const token = jwt.sign(jwtPayload, process.env.SUPER_SECRET, {
            expiresIn: expiresInSeconds
        });

        res.status(200).json({
            access_token: token,
            token_type: 'Bearer',
            expires_in: expiresInSeconds,
            user: {
                id: user._id,
                email: user.email,
                first_name: user.first_name,
                last_name: user.last_name,
                role: user.role,
                status: user.status
            }
        });

    } catch (err) {
        console.error('Google login error:', err);
        
        // Provide more specific error messages
        let detail = 'Server error during Google authentication';
        if (err.message && err.message.includes('JSON')) {
            detail = 'Invalid Google credential format';
        } else if (err.message && err.message.includes('base64')) {
            detail = 'Unable to decode Google credential';
        }
        
        res.status(500).json({ 
            type: 'server_error',
            title: 'Internal Server Error',
            detail: detail
        });
    }
});

// POST /api/v1/auth/logout
// Follows OpenAPI spec
router.post('/logout', (req, res) => {
    // For JWT-based auth, logout is handled client-side by deleting the token
    // This endpoint is here for API completeness and future token blacklisting
    res.status(204).send();
});

module.exports = router;