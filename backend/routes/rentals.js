const express = require('express');
const router = express.Router();
const Rental = require('../models/Rental');
const Booking = require('../models/Booking');
const Vehicle = require('../models/Vehicle');
const PaymentMethod = require('../models/PaymentMethod');
const paymentService = require('../services/paymentService');
const locationService = require('../services/locationService');
const tokenChecker = require('../middlewares/tokenChecker');

// All routes require authentication
router.use(tokenChecker);

// ============================================================================
// RENTAL ENDPOINTS
// ============================================================================

// GET /api/v1/rentals/active - Get user's active rental
router.get('/active', async (req, res) => {
    try {
        const rental = await Rental.findOne({ 
            user_id: req.loggedUser.id, 
            status: 'active' 
        }).populate('vehicle_id');
        
        if (!rental) {
            return res.json({ active_rental: null });
        }
        
        const vehicle = rental.vehicle_id;
        const durationMs = Date.now() - rental.started_at.getTime();
        const durationMinutes = Math.floor(durationMs / 60000);
        const currentCost = durationMinutes * rental.price_per_minute;
        
        res.json({
            active_rental: {
                id: rental._id,
                status: rental.status,
                started_at: rental.started_at,
                duration_minutes: durationMinutes,
                current_cost: Math.round(currentCost * 100) / 100,
                price_per_minute: rental.price_per_minute,
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    model: vehicle.model,
                    type: vehicle.type,
                    battery_level: vehicle.battery_level
                }
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching rental' });
    }
});

// POST /api/v1/rentals/start - Unlock vehicle and start rental
router.post('/start', async (req, res) => {
    try {
        const { booking_id, user_location } = req.body;
        
        if (!booking_id) {
            return res.status(400).json({ type: 'validation_error', detail: 'booking_id is required' });
        }
        
        // Check if user already has active rental
        const existingRental = await Rental.findOne({ 
            user_id: req.loggedUser.id, 
            status: 'active' 
        });
        
        if (existingRental) {
            return res.status(409).json({
                type: 'conflict',
                detail: 'You already have an active rental'
            });
        }
        
        // Find active booking
        const booking = await Booking.findOne({ 
            _id: booking_id, 
            user_id: req.loggedUser.id,
            status: 'active'
        }).populate('vehicle_id');
        
        if (!booking) {
            return res.status(404).json({ type: 'not_found', detail: 'Active booking not found' });
        }
        
        // Check if booking expired
        if (new Date() > booking.expires_at) {
            booking.status = 'expired';
            await booking.save();
            await Vehicle.findByIdAndUpdate(booking.vehicle_id, { status: 'available' });
            
            return res.status(410).json({ 
                type: 'expired', 
                detail: 'Booking has expired' 
            });
        }
        
        const vehicle = booking.vehicle_id;
        
        // Check proximity (mock or real)
        const vehicleLocation = {
            lat: vehicle.location.coordinates[1],
            lng: vehicle.location.coordinates[0]
        };
        
        const proximityCheck = await locationService.checkProximity(
            user_location || vehicleLocation, // Use vehicle location if not provided (for testing)
            vehicleLocation,
            30 // 30 meters max distance
        );
        
        if (!proximityCheck.withinRange) {
            return res.status(400).json({
                type: 'too_far',
                detail: proximityCheck.message,
                distance: proximityCheck.distance
            });
        }
        
        // Get default payment method
        const paymentMethod = await PaymentMethod.findOne({ 
            user_id: req.loggedUser.id, 
            is_default: true 
        });
        
        if (!paymentMethod) {
            return res.status(400).json({
                type: 'payment_required',
                detail: 'No default payment method found'
            });
        }
        
        // Create rental
        const rental = new Rental({
            user_id: req.loggedUser.id,
            vehicle_id: vehicle._id,
            booking_id: booking._id,
            started_at: new Date(),
            start_location: vehicleLocation,
            price_per_minute: vehicle.price_per_minute,
            payment_method_id: paymentMethod._id,
            vehicle_snapshot: {
                plate: vehicle.plate,
                model: vehicle.model,
                type: vehicle.type,
                battery_level: vehicle.battery_level
            }
        });
        
        await rental.save();
        
        // Update booking status
        booking.status = 'completed';
        await booking.save();
        
        // Update vehicle status
        vehicle.status = 'rented';
        await vehicle.save();
        
        res.status(201).json({
            message: 'Vehicle unlocked! Rental started.',
            rental: {
                id: rental._id,
                started_at: rental.started_at,
                price_per_minute: rental.price_per_minute,
                vehicle: {
                    plate: vehicle.plate,
                    model: vehicle.model
                }
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error starting rental' });
    }
});

// POST /api/v1/rentals/:id/end - End rental and process payment
router.post('/:id/end', async (req, res) => {
    try {
        const { end_location } = req.body;
        
        const rental = await Rental.findOne({ 
            _id: req.params.id, 
            user_id: req.loggedUser.id,
            status: 'active'
        }).populate('vehicle_id').populate('payment_method_id');
        
        if (!rental) {
            return res.status(404).json({ type: 'not_found', detail: 'Active rental not found' });
        }
        
        const vehicle = rental.vehicle_id;
        const vehicleLocation = end_location || {
            lat: vehicle.location.coordinates[1],
            lng: vehicle.location.coordinates[0]
        };
        
        // Validate parking zone (mock or real)
        const parkingCheck = await locationService.validateParkingZone(vehicleLocation);
        
        if (!parkingCheck.valid) {
            const nearestZones = await locationService.getNearestZones(vehicleLocation);
            return res.status(400).json({
                type: 'invalid_parking',
                detail: 'Vehicle is not in an authorized parking zone',
                nearest_zones: nearestZones
            });
        }
        
        // Calculate cost
        const endedAt = new Date();
        const durationMs = endedAt - rental.started_at;
        const durationMinutes = Math.ceil(durationMs / 60000); // Round up
        const subtotal = durationMinutes * rental.price_per_minute;
        const totalCost = Math.round(subtotal * 100) / 100;
        
        // Process payment
        const paymentMethod = rental.payment_method_id;
        const chargeResult = await paymentService.chargeCard(
            paymentMethod.token,
            totalCost,
            `MOVO Rental - ${vehicle.plate} - ${durationMinutes} min`
        );
        
        if (!chargeResult.success) {
            return res.status(402).json({
                type: 'payment_failed',
                detail: chargeResult.error || 'Payment processing failed'
            });
        }
        
        // Update rental
        rental.status = 'completed';
        rental.ended_at = endedAt;
        rental.end_location = vehicleLocation;
        rental.duration_minutes = durationMinutes;
        rental.subtotal = subtotal;
        rental.total_cost = totalCost;
        rental.payment_status = 'charged';
        rental.charge_id = chargeResult.chargeId;
        
        await rental.save();
        
        // Update vehicle
        vehicle.status = 'available';
        vehicle.location = {
            type: 'Point',
            coordinates: [vehicleLocation.lng, vehicleLocation.lat]
        };
        await vehicle.save();
        
        res.json({
            message: 'Rental completed successfully!',
            receipt: {
                rental_id: rental._id,
                vehicle: {
                    plate: vehicle.plate,
                    model: vehicle.model
                },
                started_at: rental.started_at,
                ended_at: endedAt,
                duration_minutes: durationMinutes,
                price_per_minute: rental.price_per_minute,
                subtotal: subtotal,
                discount: 0,
                total_cost: totalCost,
                payment_method: `**** ${paymentMethod.last_four}`,
                charge_id: chargeResult.chargeId,
                parking_zone: parkingCheck.zoneName
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error ending rental' });
    }
});

// GET /api/v1/rentals/history - Get rental history
router.get('/history', async (req, res) => {
    try {
        const { page = 1, limit = 10 } = req.query;
        
        const rentals = await Rental.find({ 
            user_id: req.loggedUser.id,
            status: 'completed'
        })
            .sort({ ended_at: -1 })
            .skip((page - 1) * limit)
            .limit(parseInt(limit));
        
        const total = await Rental.countDocuments({ 
            user_id: req.loggedUser.id, 
            status: 'completed' 
        });
        
        res.json({
            rentals: rentals.map(r => ({
                id: r._id,
                vehicle: r.vehicle_snapshot,
                started_at: r.started_at,
                ended_at: r.ended_at,
                duration_minutes: r.duration_minutes,
                total_cost: r.total_cost
            })),
            pagination: {
                page: parseInt(page),
                limit: parseInt(limit),
                total,
                pages: Math.ceil(total / limit)
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching history' });
    }
});

module.exports = router;
