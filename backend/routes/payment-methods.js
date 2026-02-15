const express = require('express');
const router = express.Router();
const PaymentMethod = require('../models/PaymentMethod');
const Rental = require('../models/Rental');
const paymentService = require('../services/paymentService');
const tokenChecker = require('../middlewares/tokenChecker');

// All routes require authentication
router.use(tokenChecker);

// ============================================================================
// PAYMENT METHODS CRUD
// ============================================================================

// GET /api/v1/payment-methods - List user's payment methods
router.get('/', async (req, res) => {
    try {
        const methods = await PaymentMethod.find({ user_id: req.loggedUser.id })
            .sort({ is_default: -1, created_at: -1 });
        
        res.json({
            count: methods.length,
            payment_methods: methods.map(m => ({
                id: m._id,
                card_type: m.card_type,
                last_four: m.last_four,
                expiry_month: m.expiry_month,
                expiry_year: m.expiry_year,
                is_default: m.is_default,
                nickname: m.nickname,
                created_at: m.created_at
            }))
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching payment methods' });
    }
});

// POST /api/v1/payment-methods - Add new payment method
router.post('/', async (req, res) => {
    try {
        const { card_number, expiry_month, expiry_year, cvv, nickname } = req.body;
        
        // Validate required fields
        if (!card_number || !expiry_month || !expiry_year || !cvv) {
            return res.status(400).json({
                type: 'validation_error',
                detail: 'card_number, expiry_month, expiry_year, and cvv are required'
            });
        }
        
        // Create token via payment service
        const tokenResult = await paymentService.createToken({
            card_number,
            expiry_month: parseInt(expiry_month),
            expiry_year: parseInt(expiry_year),
            cvv
        });
        
        if (!tokenResult.success) {
            return res.status(400).json({
                type: 'validation_error',
                detail: tokenResult.error
            });
        }
        
        // Check if this is the first method (make it default)
        const existingCount = await PaymentMethod.countDocuments({ user_id: req.loggedUser.id });
        
        // Create payment method
        const method = new PaymentMethod({
            user_id: req.loggedUser.id,
            card_type: tokenResult.cardType,
            last_four: tokenResult.lastFour,
            expiry_month: parseInt(expiry_month),
            expiry_year: parseInt(expiry_year),
            token: tokenResult.token,
            is_default: existingCount === 0,
            nickname
        });
        
        await method.save();
        
        res.status(201).json({
            message: 'Payment method added successfully',
            payment_method: {
                id: method._id,
                card_type: method.card_type,
                last_four: method.last_four,
                is_default: method.is_default
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error adding payment method' });
    }
});

// PATCH /api/v1/payment-methods/:id/default - Set as default
router.put('/:id/default', async (req, res) => {
    try {
        const method = await PaymentMethod.findOne({ 
            _id: req.params.id, 
            user_id: req.loggedUser.id 
        });
        
        if (!method) {
            return res.status(404).json({ type: 'not_found', detail: 'Payment method not found' });
        }
        
        method.is_default = true;
        await method.save(); // Pre-save hook handles unsetting others
        
        res.json({ message: 'Default payment method updated' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error updating payment method' });
    }
});

// DELETE /api/v1/payment-methods/:id - Remove payment method
router.delete('/:id', async (req, res) => {
    try {
        const method = await PaymentMethod.findOne({ 
            _id: req.params.id, 
            user_id: req.loggedUser.id 
        });
        
        if (!method) {
            return res.status(404).json({ type: 'not_found', detail: 'Payment method not found' });
        }
        
        // Check if user has active rentals
        const activeRental = await Rental.findOne({ 
            user_id: req.loggedUser.id, 
            status: 'active' 
        });
        
        if (activeRental) {
            return res.status(409).json({
                type: 'conflict',
                detail: 'Cannot delete payment method during an active rental'
            });
        }
        
        // Check if this is the only method
        const count = await PaymentMethod.countDocuments({ user_id: req.loggedUser.id });
        if (count === 1) {
            return res.status(409).json({
                type: 'conflict',
                detail: 'Cannot delete your only payment method. Add another method first.'
            });
        }
        
        const wasDefault = method.is_default;
        await PaymentMethod.findByIdAndDelete(req.params.id);
        
        // If deleted method was default, set another as default
        if (wasDefault) {
            const remaining = await PaymentMethod.findOne({ user_id: req.loggedUser.id });
            if (remaining) {
                remaining.is_default = true;
                await remaining.save();
            }
        }
        
        res.json({ message: 'Payment method deleted' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error deleting payment method' });
    }
});

module.exports = router;
