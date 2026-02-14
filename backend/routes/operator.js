const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const User = require('../models/User');
const Rental = require('../models/Rental');
const tokenChecker = require('../middlewares/tokenChecker');
const roleChecker = require('../middlewares/roleChecker');
const UserAuditLog = require('../models/UserAuditLog');
const { sendApprovalNotification } = require('../services/emailService');

router.use(tokenChecker);
router.use(roleChecker('operator', 'admin'));

async function getOperator(req) {
    return await User.findById(req.loggedUser.id).select('email');
}

function buildUserOperatorResponse(user, stats) {
    return {
        id: user._id,
        email: user.email,
        first_name: user.first_name,
        last_name: user.last_name,
        date_of_birth: user.date_of_birth,
        fiscal_code: user.fiscal_code,
        phone: user.phone,
        address: user.address,
        language: user.language,
        role: user.role,
        status: user.status,
        driving_enabled: user.driving_enabled,
        driving_license_verified: user.driving_license_verified,
        driving_license_expiry: user.driving_license_expiry || null,
        identity_document_expiry: user.identity_document_expiry || null,
        created_at: user.created_at,
        updated_at: user.updated_at,
        total_rentals: stats.total_rentals || 0,
        total_spent: stats.total_spent || 0,
        penalties_count: stats.penalties_count || 0,
        documents: {
            driving_license_url: user.driving_license
                ? `/${user.driving_license}`
                : null,
            identity_document_url: user.identity_document
                ? `/${user.identity_document}`
                : null
        }
    };
}

async function getUserStats(userId) {
    const rentals = await Rental.find({ user_id: userId, status: 'completed' });
    return {
        total_rentals: rentals.length,
        total_spent: rentals.reduce((sum, r) => sum + (r.total_cost || 0), 0),
        penalties_count: 0
    };
}

// GET /api/v1/operator/users
router.get('/users', async (req, res) => {
    try {
        const { status, search, documents_expiring_within_days, cursor, limit: rawLimit } = req.query;
        const limit = Math.min(Math.max(parseInt(rawLimit) || 20, 1), 100);

        const filter = {};
        if (status) filter.status = status;
        if (cursor && mongoose.Types.ObjectId.isValid(cursor)) {
            filter._id = { $lt: new mongoose.Types.ObjectId(cursor) };
        }
        if (search) {
            const regex = new RegExp(search, 'i');
            filter.$or = [
                { first_name: regex },
                { last_name: regex },
                { email: regex },
                { fiscal_code: regex }
            ];
        }
        if (documents_expiring_within_days) {
            const days = parseInt(documents_expiring_within_days);
            const expiryThreshold = new Date();
            expiryThreshold.setDate(expiryThreshold.getDate() + days);
            filter.$or = filter.$or || [];
            filter.$or.push(
                { driving_license_expiry: { $lte: expiryThreshold, $ne: null } },
                { identity_document_expiry: { $lte: expiryThreshold, $ne: null } }
            );
        }

        const users = await User.find(filter)
            .select('-password')
            .sort({ _id: -1 })
            .limit(limit + 1);

        const hasMore = users.length > limit;
        const pageUsers = hasMore ? users.slice(0, limit) : users;

        const data = await Promise.all(
            pageUsers.map(async (user) => {
                const stats = await getUserStats(user._id);
                return buildUserOperatorResponse(user, stats);
            })
        );

        res.json({
            data,
            pagination: {
                next_cursor: hasMore ? pageUsers[pageUsers.length - 1]._id.toString() : null,
                has_more: hasMore,
                limit
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching users' });
    }
});

// GET /api/v1/operator/users/:userId
router.get('/users/:userId', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId).select('-password');
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching user details' });
    }
});

// POST /api/v1/operator/users/:userId/approve
router.post('/users/:userId/approve', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        if (!user.driving_license || !user.identity_document) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Cannot approve a user without uploaded driving license and identity document'
            });
        }

        const { notes } = req.body || {};

        const previousStatus = user.status;
        user.status = 'active';
        user.driving_enabled = true;
        user.driving_license_verified = true;
        if (notes) user.approval_notes = notes;
        await user.save();

        const operator = await getOperator(req);
        await UserAuditLog.logAction({
            targetUser: user,
            action: 'approved',
            operator,
            changes: { status: { from: previousStatus, to: 'active' }, driving_enabled: { from: false, to: true } },
            reason: notes
        });

        await sendApprovalNotification(user, true);

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error approving user' });
    }
});

// POST /api/v1/operator/users/:userId/reject
router.post('/users/:userId/reject', async (req, res) => {
    try {
        const { reason } = req.body;
        if (!reason || reason.length < 10) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Reason is required and must be at least 10 characters'
            });
        }

        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        const previousStatus = user.status;
        const previousDrivingEnabled = user.driving_enabled;
        user.status = 'active';
        user.driving_enabled = false;
        user.driving_license_verified = false;
        user.driving_license = null;
        user.identity_document = null;
        await user.save();

        const operator = await getOperator(req);
        await UserAuditLog.logAction({
            targetUser: user,
            action: 'rejected',
            operator,
            changes: { status: { from: previousStatus, to: 'active' }, driving_enabled: { from: previousDrivingEnabled, to: false } },
            reason
        });

        await sendApprovalNotification(user, false, reason);

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error rejecting user' });
    }
});

// POST /api/v1/operator/users/:userId/suspend
router.post('/users/:userId/suspend', async (req, res) => {
    try {
        const { reason, duration_days } = req.body;
        if (!reason) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Reason is required'
            });
        }

        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        const previousStatus = user.status;
        user.status = 'suspended';
        await user.save();

        const operator = await getOperator(req);
        await UserAuditLog.logAction({
            targetUser: user,
            action: 'suspended',
            operator,
            changes: { status: { from: previousStatus, to: 'suspended' }, duration_days: duration_days || null },
            reason
        });

        console.log(`[MOCK] User ${user.email} suspended. Reason: ${reason}. Duration: ${duration_days || 'indefinite'} days.`);

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error suspending user' });
    }
});

// POST /api/v1/operator/users/:userId/reactivate
router.post('/users/:userId/reactivate', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        const previousStatus = user.status;
        user.status = 'active';
        await user.save();

        const operator = await getOperator(req);
        await UserAuditLog.logAction({
            targetUser: user,
            action: 'reactivated',
            operator,
            changes: { status: { from: previousStatus, to: 'active' } }
        });

        console.log(`[MOCK] User ${user.email} reactivated.`);

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error reactivating user' });
    }
});

// POST /api/v1/operator/users/:userId/block
router.post('/users/:userId/block', async (req, res) => {
    try {
        const { reason } = req.body;
        if (!reason || reason.length < 10) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Reason is required and must be at least 10 characters'
            });
        }

        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ type: 'not_found', detail: 'User not found' });

        const previousStatus = user.status;
        user.status = 'blocked';
        user.driving_enabled = false;
        await user.save();

        const operator = await getOperator(req);
        await UserAuditLog.logAction({
            targetUser: user,
            action: 'blocked',
            operator,
            changes: { status: { from: previousStatus, to: 'blocked' }, driving_enabled: { from: user.driving_enabled, to: false } },
            reason
        });

        console.log(`[MOCK NOTIFICATION] User ${user.email} permanently blocked. Reason: ${reason}`);

        const stats = await getUserStats(user._id);
        res.json(buildUserOperatorResponse(user, stats));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error blocking user' });
    }
});

module.exports = router;
