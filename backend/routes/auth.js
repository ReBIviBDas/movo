const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { sendRegistrationConfirmation } = require('../services/emailService');

const pendingUploadDir = path.join(__dirname, '../uploads/pending');
if (!fs.existsSync(pendingUploadDir)) fs.mkdirSync(pendingUploadDir, { recursive: true });

const registrationStorage = multer.diskStorage({
    destination: function (req, file, cb) {
        const requestDir = path.join(pendingUploadDir, `reg-${Date.now()}`);
        if (!fs.existsSync(requestDir)) fs.mkdirSync(requestDir, { recursive: true });
        req._uploadDir = requestDir;
        cb(null, requestDir);
    },
    filename: function (req, file, cb) {
        const ext = path.extname(file.originalname);
        cb(null, `${file.fieldname}${ext}`);
    }
});

const registrationFileFilter = (req, file, cb) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp'];
    if (allowedTypes.includes(file.mimetype)) cb(null, true);
    else cb(new Error('Invalid file type. Only JPEG, PNG, and WebP images are allowed.'), false);
};

const registrationUpload = multer({
    storage: registrationStorage,
    fileFilter: registrationFileFilter,
    limits: { fileSize: 5 * 1024 * 1024 }
});

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

function parseBoolean(value) {
    if (typeof value === 'boolean') return value;
    if (typeof value === 'string') return value.toLowerCase() === 'true';
    return Boolean(value);
}

// POST /api/v1/auth/register (multipart/form-data)
router.post('/register',
    registrationUpload.fields([
        { name: 'driving_license', maxCount: 1 },
        { name: 'identity_document', maxCount: 1 }
    ]),
    async (req, res) => {
    try {
        const { email, password, first_name, last_name, date_of_birth, fiscal_code, phone } = req.body;
        const accept_terms = parseBoolean(req.body.accept_terms);
        const accept_privacy = parseBoolean(req.body.accept_privacy);
        const accept_cookies = parseBoolean(req.body.accept_cookies);

        if (!accept_terms || !accept_privacy) {
            return res.status(400).json({ 
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'You must accept terms of service and privacy policy'
            });
        }

        const age = calculateAge(date_of_birth);
        if (age < 18) {
            return res.status(400).json({ 
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Devi avere almeno 18 anni per registrarti'
            });
        }

        let user = await User.findOne({ 
            $or: [{ email }, { fiscal_code: fiscal_code.toUpperCase() }] 
        });

        if (user) {
            return res.status(409).json({ 
                type: 'conflict',
                title: 'Conflict',
                detail: 'User with this email or fiscal code already exists'
            });
        }

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        const hasDrivingLicense = req.files && req.files['driving_license'] && req.files['driving_license'].length > 0;
        const hasIdentityDocument = req.files && req.files['identity_document'] && req.files['identity_document'].length > 0;

        if (hasDrivingLicense && !hasIdentityDocument) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Identity document is required when providing a driving license'
            });
        }

        const accountStatus = hasDrivingLicense ? 'pending_approval' : 'active';

        let newUser = new User({
            first_name,
            last_name,
            email,
            password: hashedPassword,
            phone,
            date_of_birth: new Date(date_of_birth),
            fiscal_code: fiscal_code.toUpperCase(),
            address: req.body.address || '',
            accept_terms,
            accept_privacy,
            accept_cookies,
            status: accountStatus
        });

        await newUser.save();

        if (req.files && Object.keys(req.files).length > 0) {
            const userDir = path.join(__dirname, '../uploads', newUser._id.toString());
            if (!fs.existsSync(userDir)) fs.mkdirSync(userDir, { recursive: true });

            if (req.files['driving_license']) {
                const src = req.files['driving_license'][0].path;
                const dest = path.join(userDir, req.files['driving_license'][0].filename);
                fs.renameSync(src, dest);
                newUser.driving_license = `uploads/${newUser._id}/${req.files['driving_license'][0].filename}`;
            }
            if (req.files['identity_document']) {
                const src = req.files['identity_document'][0].path;
                const dest = path.join(userDir, req.files['identity_document'][0].filename);
                fs.renameSync(src, dest);
                newUser.identity_document = `uploads/${newUser._id}/${req.files['identity_document'][0].filename}`;
            }

            await newUser.save();

            if (req._uploadDir && fs.existsSync(req._uploadDir)) {
                fs.rmSync(req._uploadDir, { recursive: true, force: true });
            }
        }

        await sendRegistrationConfirmation(newUser);

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