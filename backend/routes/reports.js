const express = require('express');
const router = express.Router();
const Report = require('../models/Report');
const Vehicle = require('../models/Vehicle');
const Rental = require('../models/Rental');
const tokenChecker = require('../middlewares/tokenChecker');
const roleChecker = require('../middlewares/roleChecker');
const upload = require('../middlewares/upload');

// ============================================================================
// USER ROUTES (Report Problems)
// ============================================================================

// POST /api/v1/reports - Create a new report
router.post('/',
    tokenChecker,
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

// GET /api/v1/reports/mine - Get user's own reports
router.get('/mine',
    tokenChecker,
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

// GET /api/v1/reports/:id - Get single report details
router.get('/:id',
    tokenChecker,
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
            
            // Check access: user can only see own reports, operators can see all
            const isOperator = ['operator', 'admin'].includes(req.loggedUser.role);
            const isOwner = report.user_id._id.toString() === req.loggedUser.id;
            
            if (!isOperator && !isOwner) {
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
                    operator_notes: isOperator ? report.operator_notes : undefined,
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

// ============================================================================
// OPERATOR ROUTES
// ============================================================================

// GET /api/v1/reports - List all reports (operators only)
router.get('/',
    tokenChecker,
    roleChecker('operator', 'admin'),
    async (req, res) => {
        try {
            const { status, category, priority, page = 1, limit = 20 } = req.query;
            
            const query = {};
            if (status) query.status = status;
            if (category) query.category = category;
            if (priority) query.priority = priority;
            
            const reports = await Report.find(query)
                .sort({ priority: -1, created_at: -1 })
                .skip((page - 1) * limit)
                .limit(parseInt(limit))
                .populate('user_id', 'first_name last_name email')
                .populate('vehicle_id', 'plate model');
            
            const total = await Report.countDocuments(query);
            
            // Get counts by status
            const statusCounts = await Report.aggregate([
                { $group: { _id: '$status', count: { $sum: 1 } } }
            ]);
            
            res.json({
                reports: reports.map(r => ({
                    id: r._id,
                    reference_id: r.reference_id,
                    user: `${r.user_id.first_name} ${r.user_id.last_name}`,
                    user_email: r.user_id.email,
                    vehicle: r.vehicle_id ? r.vehicle_id.plate : null,
                    category: r.category,
                    category_label: Report.getCategoryLabel(r.category),
                    status: r.status,
                    priority: r.priority,
                    created_at: r.created_at
                })),
                counts: {
                    open: statusCounts.find(s => s._id === 'open')?.count || 0,
                    in_progress: statusCounts.find(s => s._id === 'in_progress')?.count || 0,
                    resolved: statusCounts.find(s => s._id === 'resolved')?.count || 0,
                    closed: statusCounts.find(s => s._id === 'closed')?.count || 0
                },
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

// PATCH /api/v1/reports/:id - Update report (operators only)
router.patch('/:id',
    tokenChecker,
    roleChecker('operator', 'admin'),
    async (req, res) => {
        try {
            const report = await Report.findById(req.params.id);
            
            if (!report) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'Report not found'
                });
            }
            
            const { status, priority, operator_notes, assigned_to } = req.body;
            
            if (status) {
                const validStatuses = ['open', 'in_progress', 'resolved', 'closed'];
                if (!validStatuses.includes(status)) {
                    return res.status(400).json({
                        type: 'validation_error',
                        title: 'Bad Request',
                        detail: 'Invalid status'
                    });
                }
                report.status = status;
                
                // Auto-assign if taking in progress
                if (status === 'in_progress' && !report.assigned_to) {
                    report.assigned_to = req.loggedUser.id;
                }
            }
            
            if (priority) {
                const validPriorities = ['low', 'medium', 'high', 'urgent'];
                if (!validPriorities.includes(priority)) {
                    return res.status(400).json({
                        type: 'validation_error',
                        title: 'Bad Request',
                        detail: 'Invalid priority'
                    });
                }
                report.priority = priority;
            }
            
            if (operator_notes !== undefined) {
                report.operator_notes = operator_notes;
            }
            
            if (assigned_to !== undefined) {
                report.assigned_to = assigned_to || null;
            }
            
            await report.save();
            
            res.json({
                message: 'Report updated',
                report: {
                    id: report._id,
                    reference_id: report.reference_id,
                    status: report.status,
                    priority: report.priority
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error updating report'
            });
        }
    }
);

// PATCH /api/v1/reports/:id/resolve - Resolve report (operators only)
router.patch('/:id/resolve',
    tokenChecker,
    roleChecker('operator', 'admin'),
    async (req, res) => {
        try {
            const report = await Report.findById(req.params.id);
            
            if (!report) {
                return res.status(404).json({
                    type: 'not_found',
                    title: 'Not Found',
                    detail: 'Report not found'
                });
            }
            
            const { resolution } = req.body;
            
            if (!resolution) {
                return res.status(400).json({
                    type: 'validation_error',
                    title: 'Bad Request',
                    detail: 'Resolution description is required'
                });
            }
            
            report.status = 'resolved';
            report.resolution = resolution;
            report.resolved_at = new Date();
            
            if (!report.assigned_to) {
                report.assigned_to = req.loggedUser.id;
            }
            
            await report.save();
            
            res.json({
                message: 'Report resolved',
                report: {
                    id: report._id,
                    reference_id: report.reference_id,
                    status: report.status,
                    resolution: report.resolution,
                    resolved_at: report.resolved_at
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({
                type: 'server_error',
                title: 'Internal Server Error',
                detail: 'Error resolving report'
            });
        }
    }
);

module.exports = router;
