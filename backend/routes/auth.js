const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

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

        // 2. Check if user with this email or fiscal_code already exists
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

        // 3. Hash the password (Security RNF5)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 4. Determine status based on whether driving_license is provided
        // If documents are uploaded, status is pending_approval; otherwise active (passenger only)
        const hasDrivingLicense = req.body.driving_license ? true : false;
        const accountStatus = hasDrivingLicense ? 'pending_approval' : 'active';

        // 5. Create the new user
        let newUser = new User({
            first_name,
            last_name,
            email,
            password: hashedPassword,
            phone,
            date_of_birth: new Date(date_of_birth),
            fiscal_code,
            address: req.body.address || '',
            accept_terms,
            accept_privacy,
            accept_cookies: req.body.accept_cookies || false,
            status: accountStatus
        });

        // 6. Save to DB
        await newUser.save();

        // 7. Respond (matching OpenAPI spec)
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

// POST /api/v1/auth/logout
// Follows OpenAPI spec
router.post('/logout', (req, res) => {
    // For JWT-based auth, logout is handled client-side by deleting the token
    // This endpoint is here for API completeness and future token blacklisting
    res.status(204).send();
});

module.exports = router;