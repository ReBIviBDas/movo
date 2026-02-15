const express = require('express');
const router = express.Router();
const Report = require('../models/Report');
const Vehicle = require('../models/Vehicle');
const Rental = require('../models/Rental');
const tokenChecker = require('../middlewares/tokenChecker');
const upload = require('../middlewares/upload');

// All routes require authentication
router.use(tokenChecker);

// ============================================================================
// USER ISSUE ROUTES
// ============================================================================

// POST /api/v1/issues - Create a new issue report
router.post('/',
    (req, res, next) => {
        // Custom upload handler that doesn't fail if no files
        upload.array('photos', 5)(req, res, (err) => {
            if (err) {
                console.error('Upload error:', err);
                // Don't fail on upload errors, just continue without files
            }
            next();
        });
    },
    async (req, res) => {
        try {
            const { category, description, vehicle_id, rental_id, lat, lng } = req.body;
            
            console.log('Creating report with:', { category, description: description?.substring(0, 50) });
            
            // Validate required fields
            if (!category || !description) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: 'category and description are required'
                });
            }
            
            // Validate category
            const validCategories = ['vehicle_damage', 'vehicle_malfunction', 'accident', 'parking_issue', 'app_issue', 'payment_issue', 'other'];
            if (!validCategories.includes(category)) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: `Invalid category. Must be one of: ${validCategories.join(', ')}`
                });
            }
            
            // Validate vehicle if provided
            if (vehicle_id) {
                const vehicle = await Vehicle.findById(vehicle_id);
                if (!vehicle) {
                    return res.status(404).json({
                        type: 'not_found',
                        title: 'Not Found',
                        detail: 'Vehicle not found'
                    });
                }
            }
            
            // Generate unique reference ID
            const referenceId = await Report.generateReferenceId();
            console.log('Generated reference ID:', referenceId);
            
            // Build report object
            const reportData = {
                reference_id: referenceId,
                user_id: req.loggedUser.id,
                category,
                description: description.trim(),
                vehicle_id: vehicle_id || null,
                rental_id: rental_id || null,
                photos: req.files ? req.files.map(f => f.path) : [],
                priority: category === 'accident' ? 'urgent' : 
                         (category === 'vehicle_damage' ? 'high' : 'medium')
            };
            
            // Add location if provided
            if (lat && lng) {
                reportData.location = {
                    type: 'Point',
                    coordinates: [parseFloat(lng), parseFloat(lat)]
                };
            }
            
            const report = new Report(reportData);
            await report.save();
            
            console.log('Report created successfully:', report._id);
            
            res.status(201).json({
                message: 'Segnalazione inviata con successo',
                report: {
                    id: report._id,
                    reference_id: report.reference_id,
                    status: report.status,
                    created_at: report.created_at
                }
            });
        } catch (err) {
            console.error('Error creating report:', err.message);
            console.error(err.stack);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: err.message || 'Error creating report'
            });
        }
    }
);

// GET /api/v1/issues - Get user's own issues
router.get('/',
    async (req, res) => {
        try {
            const { status, page = 1, limit = 10 } = req.query;
            
            const query = { user_id: req.loggedUser.id };
            if (status) query.status = status;
            
            const reports = await Report.find(query)
                .sort({ created_at: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit))
                .populate('vehicle_id', 'plate model');
            
            const total = await Report.countDocuments(query);
            
            res.json({
                reports: reports.map(r => ({
                    id: r._id,
                    reference_id: r.reference_id,
                    category: r.category,
                    category_label: Report.getCategoryLabel(r.category),
                    description: r.description.substring(0, 100) + (r.description.length > 100 ? '...' : ''),
                    vehicle: r.vehicle_id ? `${r.vehicle_id.plate} - ${r.vehicle_id.model}` : null,
                    status: r.status,
                    priority: r.priority,
                    created_at: r.created_at,
                    resolved_at: r.resolved_at
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
                detail: 'Error fetching reports'
            });
        }
    }
);

// GET /api/v1/issues/:id - Get single issue details
router.get('/:id',
    async (req, res) => {
        try {
            const report = await Report.findById(req.params.id)
                .populate('user_id', 'first_name last_name email')
                .populate('vehicle_id', 'plate model type')
                .populate('rental_id', 'started_at ended_at total_cost')
                .populate('assigned_to', 'first_name last_name email');
            
            if (!report) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'Report not found'
                });
            }
            
            // Check access: user can only see own reports
            const isOwner = report.user_id._id.toString() === req.loggedUser.id;
            
            if (!isOwner) {
                return res.status(403).json({
                    type: 'forbidden',
                    title: 'Forbidden',
                    detail: 'You do not have access to this report'
                });
            }
            
            res.json({
                report: {
                    id: report._id,
                    reference_id: report.reference_id,
                    user: {
                        id: report.user_id._id,
                        name: `${report.user_id.first_name} ${report.user_id.last_name}`,
                        email: report.user_id.email
                    },
                    vehicle: report.vehicle_id ? {
                        id: report.vehicle_id._id,
                        plate: report.vehicle_id.plate,
                        model: report.vehicle_id.model
                    } : null,
                    rental: report.rental_id ? {
                        id: report.rental_id._id,
                        started_at: report.rental_id.started_at,
                        ended_at: report.rental_id.ended_at,
                        total_cost: report.rental_id.total_cost
                    } : null,
                    category: report.category,
                    category_label: Report.getCategoryLabel(report.category),
                    description: report.description,
                    photos: report.photos,
                    location: report.location?.coordinates ? {
                        lat: report.location.coordinates[1],
                        lng: report.location.coordinates[0]
                    } : null,
                    status: report.status,
                    priority: report.priority,
                    assigned_to: report.assigned_to ? {
                        id: report.assigned_to._id,
                        name: `${report.assigned_to.first_name} ${report.assigned_to.last_name}`
                    } : null,
                    resolution: report.resolution,
                    created_at: report.created_at,
                    updated_at: report.updated_at,
                    resolved_at: report.resolved_at
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error fetching report'
            });
        }
    }
);

module.exports = router;
