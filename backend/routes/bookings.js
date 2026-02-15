const express = require('express');
const router = express.Router();
const Booking = require('../models/Booking');
const Vehicle = require('../models/Vehicle');
const PaymentMethod = require('../models/PaymentMethod');
const tokenChecker = require('../middlewares/tokenChecker');

// All routes require authentication
router.use(tokenChecker);

// ============================================================================
// BOOKING ENDPOINTS
// ============================================================================

// GET /api/v1/bookings - Get user's active booking
router.get('/', async (req, res) => {
    try {
        const booking = await Booking.findOne({ 
            user_id: req.loggedUser.id, 
            status: 'active' 
        }).populate('vehicle_id');
        
        if (!booking) {
            return res.json({ active_booking: null });
        }
        
        // Check if expired
        if (new Date() > booking.expires_at) {
            booking.status = 'expired';
            await booking.save();
            
            // Release vehicle
            await Vehicle.findByIdAndUpdate(booking.vehicle_id, { status: 'available' });
            
            return res.json({ active_booking: null, message: 'Previous booking expired' });
        }
        
        const vehicle = booking.vehicle_id;
        res.json({
            active_booking: {
                id: booking._id,
                status: booking.status,
                expires_at: booking.expires_at,
                remaining_seconds: Math.max(0, Math.floor((booking.expires_at - new Date()) / 1000)),
                created_at: booking.created_at,
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    model: vehicle.model,
                    type: vehicle.type,
                    battery_level: vehicle.battery_level,
                    location: {
                        lat: vehicle.location.coordinates[1],
                        lng: vehicle.location.coordinates[0]
                    }
                }
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error fetching booking' });
    }
});

// POST /api/v1/bookings - Create new booking
router.post('/', async (req, res) => {
    try {
        const { vehicle_id } = req.body;
        
        if (!vehicle_id) {
            return res.status(400).json({ type: 'validation_error', detail: 'vehicle_id is required' });
        }
        
        // Check if user has payment method
        const hasPayment = await PaymentMethod.findOne({ user_id: req.loggedUser.id });
        if (!hasPayment) {
            return res.status(400).json({
                type: 'payment_required',
                detail: 'You need to add a payment method before booking'
            });
        }
        
        // Check if user already has active booking
        const existingBooking = await Booking.findOne({ 
            user_id: req.loggedUser.id, 
            status: 'active' 
        });
        
        if (existingBooking) {
            return res.status(409).json({
                type: 'conflict',
                detail: 'You already have an active booking'
            });
        }
        
        // Check vehicle availability
        const vehicle = await Vehicle.findById(vehicle_id);
        
        if (!vehicle) {
            return res.status(404).json({ type: 'not_found', detail: 'Vehicle not found' });
        }
        
        if (vehicle.status !== 'available') {
            return res.status(409).json({
                type: 'conflict',
                detail: 'Vehicle is not available for booking'
            });
        }
        
        // Create booking (15 min for regular users, could be 30 for subscribers)
        const durationMinutes = 15;
        const expiresAt = new Date(Date.now() + durationMinutes * 60 * 1000);
        
        const booking = new Booking({
            user_id: req.loggedUser.id,
            vehicle_id: vehicle._id,
            expires_at: expiresAt,
            duration_minutes: durationMinutes,
            vehicle_snapshot: {
                plate: vehicle.plate,
                model: vehicle.model,
                location: {
                    lat: vehicle.location.coordinates[1],
                    lng: vehicle.location.coordinates[0]
                }
            }
        });
        
        await booking.save();
        
        // Update vehicle status
        vehicle.status = 'booked';
        await vehicle.save();
        
        res.status(201).json({
            message: 'Booking created successfully',
            booking: {
                id: booking._id,
                expires_at: booking.expires_at,
                remaining_seconds: durationMinutes * 60,
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    model: vehicle.model,
                    location: booking.vehicle_snapshot.location
                }
            }
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error creating booking' });
    }
});

// DELETE /api/v1/bookings/:id - Cancel booking
router.delete('/:id', async (req, res) => {
    try {
        const booking = await Booking.findOne({ 
            _id: req.params.id, 
            user_id: req.loggedUser.id,
            status: 'active'
        });
        
        if (!booking) {
            return res.status(404).json({ type: 'not_found', detail: 'Active booking not found' });
        }
        
        // Cancel booking
        booking.status = 'cancelled';
        await booking.save();
        
        // Release vehicle
        await Vehicle.findByIdAndUpdate(booking.vehicle_id, { status: 'available' });
        
        res.json({ message: 'Booking cancelled successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ type: 'server_error', detail: 'Error cancelling booking' });
    }
});

module.exports = router;
