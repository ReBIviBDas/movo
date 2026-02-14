const express = require('express');
const router = express.Router();
const SplitRequest = require('../models/SplitRequest');
const Rental = require('../models/Rental');
const User = require('../models/User');
const tokenChecker = require('../middlewares/tokenChecker');

router.use(tokenChecker);

function formatSplitResponse(split) {
    return {
        id: split._id,
        rental_id: split.rental_id,
        status: split.status,
        participants: split.participants.map(p => ({
            user_id: p.user_id,
            percentage: p.percentage,
            amount_cents: p.amount_cents,
            status: p.status
        })),
        created_at: split.created_at,
        expires_at: split.expires_at
    };
}

// POST /api/v1/payments/split-request
router.post('/split-request', async (req, res) => {
    try {
        const { rental_id, participants, split_mode } = req.body;

        if (!rental_id || !participants || !Array.isArray(participants) || participants.length < 1) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'rental_id and at least one participant are required'
            });
        }

        if (participants.length > 4) {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'Maximum 4 participants allowed'
            });
        }

        const rental = await Rental.findById(rental_id);
        if (!rental) {
            return res.status(404).json({ type: 'not_found', detail: 'Rental not found' });
        }

        if (rental.user_id.toString() !== req.loggedUser.id) {
            return res.status(403).json({
                type: 'authorization_error',
                detail: 'Only the rental owner can request a cost split'
            });
        }

        for (const p of participants) {
            const participantUser = await User.findById(p.user_id);
            if (!participantUser) {
                return res.status(404).json({
                    type: 'not_found',
                    detail: `Participant user ${p.user_id} not found`
                });
            }
        }

        const totalCostCents = Math.round((rental.total_cost || 0) * 100);
        const isAutoSplit = split_mode === 'automatic';
        const totalPeople = participants.length + 1;

        let splitParticipants;

        if (isAutoSplit) {
            const equalPercentage = Math.floor(100 / totalPeople);
            const remainder = 100 - (equalPercentage * totalPeople);
            splitParticipants = participants.map((p, index) => {
                const pct = index === 0 ? equalPercentage + remainder : equalPercentage;
                return {
                    user_id: p.user_id,
                    percentage: pct,
                    amount_cents: Math.round(totalCostCents * pct / 100),
                    status: 'pending'
                };
            });
        } else {
            const totalPercentage = participants.reduce((sum, p) => sum + p.percentage, 0);
            if (totalPercentage > 99) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: 'Total participant percentage cannot exceed 99'
                });
            }

            for (const p of participants) {
                if (p.percentage < 1 || p.percentage > 99) {
                    return res.status(400).json({
                        type: 'validation_error',
                        title: 'Bad Request',
                        detail: `Each participant percentage must be between 1 and 99`
                    });
                }
            }

            splitParticipants = participants.map(p => ({
                user_id: p.user_id,
                percentage: p.percentage,
                amount_cents: Math.round(totalCostCents * p.percentage / 100),
                status: 'pending'
            }));
        }

        const expiresAt = new Date();
        expiresAt.setHours(expiresAt.getHours() + 24);

        const splitRequest = new SplitRequest({
            rental_id,
            requester_id: req.loggedUser.id,
            status: 'pending',
            participants: splitParticipants,
            expires_at: expiresAt
        });

        await splitRequest.save();

        console.log(`[MOCK PUSH NOTIFICATION] Split request ${splitRequest._id} sent to ${participants.length} participants`);

        res.status(201).json(formatSplitResponse(splitRequest));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error creating split request' });
    }
});

// POST /api/v1/payments/split-request/:splitId/respond
router.post('/split-request/:splitId/respond', async (req, res) => {
    try {
        const { accept } = req.body;
        if (typeof accept !== 'boolean') {
            return res.status(400).json({
                type: 'validation_error',
                title: 'Bad Request',
                detail: 'accept field (boolean) is required'
            });
        }

        const splitRequest = await SplitRequest.findById(req.params.splitId);
        if (!splitRequest) {
            return res.status(404).json({ type: 'not_found', detail: 'Split request not found' });
        }

        if (splitRequest.status !== 'pending') {
            return res.status(409).json({
                type: 'conflict',
                title: 'Conflict',
                detail: 'This split request is no longer pending'
            });
        }

        if (new Date() > splitRequest.expires_at) {
            splitRequest.status = 'expired';
            await splitRequest.save();
            return res.status(409).json({
                type: 'conflict',
                title: 'Conflict',
                detail: 'This split request has expired'
            });
        }

        const participant = splitRequest.participants.find(
            p => p.user_id.toString() === req.loggedUser.id
        );

        if (!participant) {
            return res.status(403).json({
                type: 'authorization_error',
                detail: 'You are not a participant in this split request'
            });
        }

        if (participant.status !== 'pending') {
            return res.status(409).json({
                type: 'conflict',
                title: 'Conflict',
                detail: 'You have already responded to this split request'
            });
        }

        participant.status = accept ? 'accepted' : 'rejected';

        if (!accept) {
            splitRequest.status = 'rejected';
        } else {
            const allAccepted = splitRequest.participants.every(p => p.status === 'accepted');
            if (allAccepted) {
                splitRequest.status = 'accepted';
                console.log(`[MOCK PAYMENT] Processing split payments for request ${splitRequest._id}`);
            }
        }

        await splitRequest.save();
        res.json(formatSplitResponse(splitRequest));
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error responding to split request' });
    }
});

module.exports = router;
