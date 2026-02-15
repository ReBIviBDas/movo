const express = require('express');
const router = express.Router();
const User = require('../models/User');
const tokenChecker = require('../middlewares/tokenChecker');
const upload = require('../middlewares/upload');

// All routes require authentication
router.use(tokenChecker);

// ============================================================================
// USER ROUTES (Authenticated users)
// ============================================================================

// GET /api/v1/users/me - Get current user profile
router.get('/me', async (req, res) => {
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

module.exports = router;
