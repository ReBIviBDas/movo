const express = require('express');
const router = express.Router();
const Vehicle = require('../models/Vehicle');
const tokenChecker = require('../middlewares/tokenChecker');
const roleChecker = require('../middlewares/roleChecker');

// ============================================================================
// OPERATOR ROUTES (Fleet Management)
// ============================================================================

// GET /api/v1/fleet - List all vehicles (any status)
router.get('/', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const { status, type, page = 1, limit = 20 } = req.query;
            
            const query = {};
            if (status) query.status = status;
            if (type) query.type = type;
            
            const vehicles = await Vehicle.find(query)
                .sort({ last_updated: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit));
            
            const total = await Vehicle.countDocuments(query);
            
            res.json({
                vehicles: vehicles.map(v => ({
                    id: v._id,
                    plate: v.plate,
                    model: v.model,
                    type: v.type,
                    status: v.status,
                    battery_level: v.battery_level,
                    range_km: v.range_km,
                    price_per_minute: v.price_per_minute,
                    location: {
                        lat: v.location.coordinates[1],
                        lng: v.location.coordinates[0]
                    },
                    last_updated: v.last_updated,
                    created_at: v.created_at
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
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching fleet'
            });
        }
    }
);

// POST /api/v1/fleet - Add new vehicle
router.post('/', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const { plate, model, type, battery_level, lat, lng, range_km, price_per_minute } = req.body;
            
            // Validate required fields
            if (!plate || !model || !lat || !lng) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: 'plate, model, lat, and lng are required'
                });
            }
            
            // Check for duplicate plate
            const existing = await Vehicle.findOne({ plate: plate.toUpperCase() });
            if (existing) {
                return res.status(409).json({
                    type: 'conflict',
                    title: 'Conflict',
                    detail: 'Vehicle with this plate already exists'
                });
            }
            
            const vehicle = new Vehicle({
                plate: plate.toUpperCase(),
                model,
                type: type || 'car',
                battery_level: battery_level || 100,
                location: {
                    type: 'Point',
                    coordinates: [parseFloat(lng), parseFloat(lat)]
                },
                range_km: range_km || 200,
                price_per_minute: price_per_minute || 0.35
            });
            
            await vehicle.save();
            
            res.status(201).json({
                message: 'Vehicle added successfully',
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    model: vehicle.model,
                    type: vehicle.type,
                    status: vehicle.status
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error adding vehicle'
            });
        }
    }
);

// PATCH /api/v1/fleet/:id - Update vehicle
router.patch('/:id', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const vehicle = await Vehicle.findById(req.params.id);
            
            if (!vehicle) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'Vehicle not found'
                });
            }
            
            const { status, battery_level, lat, lng, range_km, price_per_minute } = req.body;
            
            if (status) vehicle.status = status;
            if (battery_level !== undefined) vehicle.battery_level = battery_level;
            if (range_km !== undefined) vehicle.range_km = range_km;
            if (price_per_minute !== undefined) vehicle.price_per_minute = price_per_minute;
            
            if (lat !== undefined && lng !== undefined) {
                vehicle.location = {
                    type: 'Point',
                    coordinates: [parseFloat(lng), parseFloat(lat)]
                };
            }
            
            await vehicle.save();
            
            res.json({
                message: 'Vehicle updated successfully',
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    status: vehicle.status,
                    battery_level: vehicle.battery_level
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error updating vehicle'
            });
        }
    }
);

// DELETE /api/v1/fleet/:id - Remove vehicle
router.delete('/:id', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const vehicle = await Vehicle.findById(req.params.id);
            
            if (!vehicle) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'Vehicle not found'
                });
            }
            
            // Prevent deletion if vehicle is rented
            if (vehicle.status === 'rented') {
                return res.status(409).json({
                    type: 'conflict',
                    title: 'Conflict',
                    detail: 'Cannot delete a vehicle that is currently rented'
                });
            }
            
            await Vehicle.findByIdAndDelete(req.params.id);
            
            res.json({
                message: 'Vehicle deleted successfully',
                vehicle_id: req.params.id
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error deleting vehicle'
            });
        }
    }
);

module.exports = router;
