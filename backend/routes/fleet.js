const express = require('express');
const router = express.Router();
const Vehicle = require('../models/Vehicle');
const Rental = require('../models/Rental');
const FleetAuditLog = require('../models/FleetAuditLog');
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

// GET /api/v1/fleet/audit - Get all audit logs (paginated)
router.get('/audit', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const { page = 1, limit = 20, action } = req.query;
            
            const query = {};
            if (action) query.action = action;
            
            const logs = await FleetAuditLog.find(query)
                .sort({ timestamp: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit))
                .populate('performed_by', 'first_name last_name email');
            
            const total = await FleetAuditLog.countDocuments(query);
            
            res.json({
                logs: logs.map(log => ({
                    id: log._id,
                    vehicle_id: log.vehicle_id,
                    vehicle_plate: log.vehicle_plate,
                    action: log.action,
                    performed_by: log.performed_by_email,
                    changes: log.changes,
                    notes: log.notes,
                    timestamp: log.timestamp
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
                detail: 'Error fetching audit logs'
            });
        }
    }
);

// GET /api/v1/fleet/:id - Get vehicle details with history
router.get('/:id', 
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
            
            // Get recent audit logs for this vehicle
            const auditLogs = await FleetAuditLog.find({ vehicle_id: vehicle._id })
                .sort({ timestamp: -1 })
                .limit(20);
            
            // Get rental history for this vehicle
            const rentals = await Rental.find({ vehicle_id: vehicle._id })
                .sort({ started_at: -1 })
                .limit(10)
                .populate('user_id', 'first_name last_name email');
            
            res.json({
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    model: vehicle.model,
                    type: vehicle.type,
                    status: vehicle.status,
                    battery_level: vehicle.battery_level,
                    range_km: vehicle.range_km,
                    price_per_minute: vehicle.price_per_minute,
                    location: {
                        lat: vehicle.location.coordinates[1],
                        lng: vehicle.location.coordinates[0]
                    },
                    last_updated: vehicle.last_updated,
                    created_at: vehicle.created_at
                },
                audit_logs: auditLogs.map(log => ({
                    id: log._id,
                    action: log.action,
                    performed_by: log.performed_by_email,
                    changes: log.changes,
                    notes: log.notes,
                    timestamp: log.timestamp
                })),
                rental_history: rentals.map(r => ({
                    id: r._id,
                    user: r.user_id ? `${r.user_id.first_name} ${r.user_id.last_name}` : 'Unknown',
                    user_email: r.user_id?.email,
                    status: r.status,
                    started_at: r.started_at,
                    ended_at: r.ended_at,
                    total_cost: r.total_cost
                }))
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching vehicle details'
            });
        }
    }
);

// GET /api/v1/fleet/:id/audit - Get audit logs for a specific vehicle
router.get('/:id/audit', 
    tokenChecker, 
    roleChecker('operator', 'admin'), 
    async (req, res) => {
        try {
            const { page = 1, limit = 20 } = req.query;
            
            const logs = await FleetAuditLog.find({ vehicle_id: req.params.id })
                .sort({ timestamp: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit));
            
            const total = await FleetAuditLog.countDocuments({ vehicle_id: req.params.id });
            
            res.json({
                logs: logs.map(log => ({
                    id: log._id,
                    action: log.action,
                    performed_by: log.performed_by_email,
                    changes: log.changes,
                    notes: log.notes,
                    timestamp: log.timestamp
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
                detail: 'Error fetching vehicle audit logs'
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
            
            // Log the action
            await FleetAuditLog.logAction({
                vehicle,
                action: 'created',
                user: req.loggedUser,
                notes: `New vehicle added: ${vehicle.model} (${vehicle.plate})`
            });
            
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

// POST /api/v1/fleet/:id/maintenance - Start or end maintenance
router.post('/:id/maintenance', 
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
            
            const { action, notes } = req.body;
            
            if (!action || !['start', 'end'].includes(action)) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: 'action must be "start" or "end"'
                });
            }
            
            // Cannot start maintenance if rented
            if (action === 'start' && vehicle.status === 'rented') {
                return res.status(409).json({
                    type: 'conflict',
                    title: 'Conflict',
                    detail: 'Cannot start maintenance on a rented vehicle'
                });
            }
            
            const oldStatus = vehicle.status;
            const newStatus = action === 'start' ? 'maintenance' : 'available';
            const logAction = action === 'start' ? 'maintenance_started' : 'maintenance_completed';
            
            vehicle.status = newStatus;
            await vehicle.save();
            
            // Log the action
            await FleetAuditLog.logAction({
                vehicle,
                action: logAction,
                user: req.loggedUser,
                changes: { status: { old: oldStatus, new: newStatus } },
                notes: notes || null
            });
            
            res.json({
                message: action === 'start' ? 'Maintenance started' : 'Maintenance completed',
                vehicle: {
                    id: vehicle._id,
                    plate: vehicle.plate,
                    status: vehicle.status
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error updating maintenance status'
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
            
            const { status, battery_level, lat, lng, range_km, price_per_minute, notes } = req.body;
            
            // Track changes for audit log
            const changes = {};
            let actionType = 'updated';
            
            if (status && status !== vehicle.status) {
                changes.status = { old: vehicle.status, new: status };
                actionType = 'status_changed';
                vehicle.status = status;
            }
            if (battery_level !== undefined && battery_level !== vehicle.battery_level) {
                changes.battery_level = { old: vehicle.battery_level, new: battery_level };
                vehicle.battery_level = battery_level;
            }
            if (range_km !== undefined && range_km !== vehicle.range_km) {
                changes.range_km = { old: vehicle.range_km, new: range_km };
                vehicle.range_km = range_km;
            }
            if (price_per_minute !== undefined && price_per_minute !== vehicle.price_per_minute) {
                changes.price_per_minute = { old: vehicle.price_per_minute, new: price_per_minute };
                vehicle.price_per_minute = price_per_minute;
            }
            
            if (lat !== undefined && lng !== undefined) {
                const oldLat = vehicle.location.coordinates[1];
                const oldLng = vehicle.location.coordinates[0];
                if (lat !== oldLat || lng !== oldLng) {
                    changes.location = { 
                        old: { lat: oldLat, lng: oldLng }, 
                        new: { lat, lng } 
                    };
                    vehicle.location = {
                        type: 'Point',
                        coordinates: [parseFloat(lng), parseFloat(lat)]
                    };
                }
            }
            
            await vehicle.save();
            
            // Only log if there were actual changes
            if (Object.keys(changes).length > 0) {
                await FleetAuditLog.logAction({
                    vehicle,
                    action: actionType,
                    user: req.loggedUser,
                    changes,
                    notes: notes || null
                });
            }
            
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
            
            // Log deletion before deleting
            await FleetAuditLog.logAction({
                vehicle,
                action: 'deleted',
                user: req.loggedUser,
                notes: req.body.notes || `Vehicle ${vehicle.plate} removed from fleet`
            });
            
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
