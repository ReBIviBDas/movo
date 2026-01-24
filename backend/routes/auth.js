const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

// POST /api/v1/auth/register
router.post('/register', async (req, res) => {
    try {
        // 1. Check if user with this email or TaxID already exists
        // $or is a MongoDB operator to check multiple conditions
        let user = await User.findOne({ 
            $or: [{ email: req.body.email }, { taxId: req.body.taxId }] 
        });

        if (user) {
            return res.status(409).json({ success: false, message: 'User with this email or Tax ID already exists' });
        }

        // 2. Hash the password (Security RNF5)
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(req.body.password, salt);

        // 3. Create the new user
        let newUser = new User({
            name: req.body.name,
            surname: req.body.surname,
            email: req.body.email,
            password: hashedPassword,
            phone: req.body.phone,
            dateOfBirth: req.body.dateOfBirth,
            taxId: req.body.taxId
        });

        // 4. Save to DB
        await newUser.save();

        // 5. Respond
        res.status(201).json({ success: true, message: 'User registered successfully!' });

    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, message: 'Server error during registration' });
    }
});

// POST /api/v1/auth/login
router.post('/login', async (req, res) => {
    try {
        // 1. Find the user by email
        const user = await User.findOne({ email: req.body.email });
        
        if (!user) {
            return res.status(401).json({ success: false, message: 'Authentication failed. User not found.' });
        }

        // 2. Check the password
        // bcrypt.compare(plainText, hash)
        const checkPassword = await bcrypt.compare(req.body.password, user.password);

        if (!checkPassword) {
            return res.status(401).json({ success: false, message: 'Authentication failed. Wrong password.' });
        }

        // 3. Generate the Token (The "Badge")
        // We put the userId and email inside the token payload
        const payload = { 
            id: user._id, 
            email: user.email,
            role: user.role 
        };

        const token = jwt.sign(payload, process.env.SUPER_SECRET, {
            expiresIn: '24h' // Token dies in 1 day
        });

        // 4. Return the token
        res.status(200).json({
            success: true,
            message: 'Login successful!',
            token: token,
            user: { // Send back some basic info (but NOT the password)
                id: user._id,
                name: user.name,
                email: user.email,
                role: user.role
            }
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, message: 'Server error during login' });
    }
});

module.exports = router;