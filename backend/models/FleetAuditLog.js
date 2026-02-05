const mongoose = require('mongoose');
const { Schema } = mongoose;

/**
 * FleetAuditLog - Tracks all fleet management operations
 * Used to maintain an audit trail of vehicle changes for operators
 */
const fleetAuditLogSchema = new Schema({
    // Reference to the vehicle
    vehicle_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'Vehicle', 
        required: true,
        index: true
    },
    
    // Snapshot of vehicle plate (in case vehicle is deleted)
    vehicle_plate: {
        type: String,
        required: true
    },
    
    // Type of action performed
    action: { 
        type: String, 
        enum: [
            'created',           // Vehicle added to fleet
            'updated',           // General update
            'deleted',           // Vehicle removed from fleet
            'status_changed',    // Status field changed
            'maintenance_started', // Entered maintenance mode
            'maintenance_completed' // Exited maintenance mode
        ],
        required: true,
        index: true
    },
    
    // Operator who performed the action
    performed_by: { 
        type: Schema.Types.ObjectId, 
        ref: 'User', 
        required: true 
    },
    
    // Snapshot of operator email (for display even if user deleted)
    performed_by_email: {
        type: String,
        required: true
    },
    
    // Changes made (for updates)
    changes: {
        type: Schema.Types.Mixed,
        default: null
        // Format: { field_name: { old: value, new: value } }
    },
    
    // Optional notes/reason for the action
    notes: { 
        type: String,
        maxlength: 500
    },
    
    // Timestamp of the action
    timestamp: { 
        type: Date, 
        default: Date.now,
        index: true
    }
});

// Compound index for efficient vehicle-specific queries
fleetAuditLogSchema.index({ vehicle_id: 1, timestamp: -1 });

// Index for recent activity across all vehicles
fleetAuditLogSchema.index({ timestamp: -1 });

/**
 * Static method to log a fleet action
 */
fleetAuditLogSchema.statics.logAction = async function(params) {
    const { vehicle, action, user, changes, notes } = params;
    
    const log = new this({
        vehicle_id: vehicle._id || vehicle.id,
        vehicle_plate: vehicle.plate,
        action,
        performed_by: user._id || user.id,
        performed_by_email: user.email,
        changes: changes || null,
        notes: notes || null
    });
    
    return log.save();
};

const FleetAuditLog = mongoose.model('FleetAuditLog', fleetAuditLogSchema);
module.exports = FleetAuditLog;
