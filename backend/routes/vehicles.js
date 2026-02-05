const express = require('express');
const router = express.Router();
const Vehicle = require('../models/Vehicle');

// ============================================================================
// PUBLIC ROUTES (No authentication required)
// ============================================================================

// GET /api/v1/vehicles/public - Public list of available vehicles (no auth)
router.get('/public', async (req, res) => {
    try {
        const { lat, lng, radius } = req.query;
        
        // Only show available vehicles
        const query = { status: 'available' };
        
        let vehicles;
        
        // If location provided, use geospatial query
        if (lat && lng) {
            const radiusKm = parseFloat(radius) || 5;
            const radiusMeters = radiusKm * 1000;
            
            vehicles = await Vehicle.find({
                ...query,
                location: {
                    $near: {
                        $geometry: {
                            type: 'Point',
                            coordinates: [parseFloat(lng), parseFloat(lat)]
                        },
                        $maxDistance: radiusMeters
                    }
                }
            }).limit(50);
        } else {
            vehicles = await Vehicle.find(query)
                .sort({ last_updated: -1 })
                .limit(50);
        }
        
        res.json({
            count: vehicles.length,
            vehicles: vehicles.map(v => ({
                id: v._id,
                model: v.model,
                type: v.type,
                status: v.status,
                battery_level: v.battery_level,
                range_km: v.range_km,
                price_per_minute: v.price_per_minute,
                location: {
                    lat: v.location.coordinates[1],
                    lng: v.location.coordinates[0]
                }
                // Note: No plate for public view (privacy)
            }))
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Error fetching vehicles'
        });
    }
});

// ============================================================================
// AUTHENTICATED ROUTES (Available to logged-in users)
// ============================================================================

// GET /api/v1/vehicles - List available vehicles (authenticated)
router.get('/', async (req, res) => {
    try {
        const { type, min_battery, lat, lng, radius } = req.query;
        
        // Build query - only show available vehicles
        const query = { status: 'available' };
        
        // Filter by type
        if (type) {
            query.type = type;
        }
        
        // Filter by minimum battery
        if (min_battery) {
            query.battery_level = { $gte: parseInt(min_battery) };
        }
        
        let vehicles;
        
        // If location provided, use geospatial query
        if (lat && lng) {
            const radiusKm = parseFloat(radius) || 5; // Default 5km radius
            const radiusMeters = radiusKm * 1000;
            
            vehicles = await Vehicle.find({
                ...query,
                location: {
                    $near: {
                        $geometry: {
                            type: 'Point',
                            coordinates: [parseFloat(lng), parseFloat(lat)]
                        },
                        $maxDistance: radiusMeters
                    }
                }
            }).limit(50);
        } else {
            vehicles = await Vehicle.find(query)
                .sort({ last_updated: -1 })
                .limit(50);
        }
        
        res.json({
            count: vehicles.length,
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
                image_url: v.image_url
            }))
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Error fetching vehicles'
        });
    }
});

// GET /api/v1/vehicles/:id - Get single vehicle details
router.get('/:id', async (req, res) => {
    try {
        const vehicle = await Vehicle.findById(req.params.id);
        
        if (!vehicle) {
            return res.status(404).json({
                type: 'not_found',
                title: 'Not Found',
                detail: 'Vehicle not found'
            });
        }
        
        res.json({
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
            image_url: vehicle.image_url,
            last_updated: vehicle.last_updated
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({
            type: 'server_error',
            title: 'Internal Server Error',
            detail: 'Error fetching vehicle'
        });
    }
});

module.exports = router;
