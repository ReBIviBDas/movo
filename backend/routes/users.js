const express = require('express');
const router = express.Router();
const User = require('../models/User');
const tokenChecker = require('../middlewares/tokenChecker');
const roleChecker = require('../middlewares/roleChecker');
const upload = require('../middlewares/upload');
const { sendApprovalNotification } = require('../services/emailService');

// ============================================================================
// USER ROUTES (Authenticated users)
// ============================================================================

// GET /api/v1/users/me - Get current user profile
router.get('/me', tokenChecker, async (req, res) => {
    try {
        const user = await User.findById(req.loggedUser.id).select('-password');
        if (!user) {
            return res.status(404).json({
                type: 'not_found',
                title: 'Not Found',
                detail: 'User not found'
            });
        }
        res.json(user);
    } catch (err) {
        console.error(err);
        res.status(500).json({
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Error fetching user profile'
        });
    }
});

// POST /api/v1/users/me/documents - Upload driving license and/or ID
router.post('/me/documents', 
    tokenChecker, 
    upload.fields([
        { name: 'driving_license', maxCount: 1 },
        { name: 'identity_document', maxCount: 1 }
    ]),
    async (req, res) => {
        try {
            const user = await User.findById(req.loggedUser.id);
            if (!user) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'User not found'
                });
            }

            // Update document paths
            if (req.files['driving_license']) {
                user.driving_license = req.files['driving_license'][0].path;
            }
            if (req.files['identity_document']) {
                user.identity_document = req.files['identity_document'][0].path;
            }

            // If documents uploaded, set status to pending_approval
            if (req.files['driving_license'] || req.files['identity_document']) {
                user.status = 'pending_approval';
            }

            await user.save();

            res.json({
                message: 'Documenti caricati con successo. In attesa di approvazione.',
                status: user.status
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error uploading documents'
            });
        }
    }
);

// ============================================================================
// OPERATOR ROUTES (Operators and Admins only)
// ============================================================================

// GET /api/v1/users/pending - List users pending approval
router.get('/pending', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const users = await User.find({ status: 'pending_approval' })
                .select('-password')
                .sort({ created_at: -1 });
            
            res.json({
                count: users.length,
                users: users
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching pending users'
            });
        }
    }
);

// GET /api/v1/users/:id - Get user details (operator only)
router.get('/:id', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const user = await User.findById(req.params.id).select('-password');
            if (!user) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'User not found'
                });
            }
            res.json(user);
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching user'
            });
        }
    }
);

// POST /api/v1/users/:id/approve - Approve user
router.post('/:id/approve', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const user = await User.findById(req.params.id);
            if (!user) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'User not found'
                });
            }

            user.status = 'active';
            user.driving_enabled = true;
            user.driving_license_verified = true;
            await user.save();

            // Send notification email
            await sendApprovalNotification(user, true);

            res.json({
                message: 'Utente approvato con successo',
                user_id: user._id,
                status: user.status
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error approving user'
            });
        }
    }
);

// POST /api/v1/users/:id/reject - Reject user
router.post('/:id/reject', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const user = await User.findById(req.params.id);
            if (!user) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'User not found'
                });
            }

            const reason = req.body.reason || 'Documenti non validi o non leggibili';
            
            user.status = 'active'; // Revert to active (passenger) status
            user.driving_enabled = false;
            user.driving_license_verified = false;
            // Clear document paths so they can upload again
            user.driving_license = null;
            user.identity_document = null;
            await user.save();

            // Send notification email
            await sendApprovalNotification(user, false, reason);

            res.json({
                message: 'Utente rifiutato. Notifica inviata.',
                user_id: user._id,
                status: user.status
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error rejecting user'
            });
        }
    }
);

// GET /api/v1/users - List all users (operator only)
router.get('/', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const { status, page = 1, limit = 20 } = req.query;
            
            const query = {};
            if (status) {
                query.status = status;
            }

            const users = await User.find(query)
                .select('-password')
                .sort({ created_at: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit));
            
            const total = await User.countDocuments(query);

            res.json({
                users,
                pagination: {
                    page: parseInt(page),
                    limit: parseInt(limit),
                    total,
                    pages: Math.ceil(total / limit)
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching users'
            });
        }
    }
);

module.exports = router;
