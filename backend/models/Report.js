const mongoose = require('mongoose');
const { Schema } = mongoose;

/**
 * Report - Problem reports from users
 * Tracks issues with vehicles, app, or service
 */
const reportSchema = new Schema({
    // Auto-generated reference number (REP-YYYYMMDD-XXXX)
    reference_id: {
        type: String,
        unique: true,
        required: true
    },
    
    // User who created the report
    user_id: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        index: true
    },
    
    // Vehicle involved (optional)
    vehicle_id: {
        type: Schema.Types.ObjectId,
        ref: 'Vehicle',
        default: null
    },
    
    // Related rental (optional)
    rental_id: {
        type: Schema.Types.ObjectId,
        ref: 'Rental',
        default: null
    },
    
    // Report category
    category: {
        type: String,
        enum: [
            'vehicle_damage',      // Danni al veicolo
            'vehicle_malfunction', // Malfunzionamento
            'accident',            // Incidente
            'parking_issue',       // Problema parcheggio
            'app_issue',           // Problema app
            'payment_issue',       // Problema pagamento
            'other'                // Altro
        ],
        required: true,
        index: true
    },
    
    // Description of the problem
    description: {
        type: String,
        required: true,
        maxlength: 2000
    },
    
    // Photo attachments (file paths)
    photos: [{
        type: String
    }],
    
    // Location where problem occurred (GeoJSON Point, optional)
    location: {
        type: {
            type: String,
            enum: ['Point']
        },
        coordinates: [Number] // [longitude, latitude]
    },
    
    // Report status
    status: {
        type: String,
        enum: ['open', 'in_progress', 'resolved', 'closed'],
        default: 'open',
        index: true
    },
    
    // Priority level
    priority: {
        type: String,
        enum: ['low', 'medium', 'high', 'urgent'],
        default: 'medium'
    },
    
    // Operator who is handling the report
    assigned_to: {
        type: Schema.Types.ObjectId,
        ref: 'User',
        default: null
    },
    
    // Internal operator notes
    operator_notes: {
        type: String,
        maxlength: 2000,
        default: ''
    },
    
    // Resolution description
    resolution: {
        type: String,
        maxlength: 2000,
        default: ''
    },
    
    // When the report was resolved/closed
    resolved_at: {
        type: Date,
        default: null
    }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

// Geospatial index for location queries (sparse to allow null locations)
reportSchema.index({ location: '2dsphere' }, { sparse: true });

// Compound index for user queries
reportSchema.index({ user_id: 1, created_at: -1 });

// Compound index for operator queries
reportSchema.index({ status: 1, priority: -1, created_at: -1 });

/**
 * Generate unique reference ID
 * Format: REP-YYYYMMDD-XXXX (4 random alphanumeric chars)
 */
reportSchema.statics.generateReferenceId = async function() {
    const date = new Date();
    const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '');
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    
    let referenceId;
    let isUnique = false;
    
    while (!isUnique) {
        let suffix = '';
        for (let i = 0; i < 4; i++) {
            suffix += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        referenceId = `REP-${dateStr}-${suffix}`;
        
        // Check uniqueness
        const existing = await this.findOne({ reference_id: referenceId });
        if (!existing) {
            isUnique = true;
        }
    }
    
    return referenceId;
};

/**
 * Get category label in Italian
 */
reportSchema.statics.getCategoryLabel = function(category) {
    const labels = {
        vehicle_damage: 'Danni al veicolo',
        vehicle_malfunction: 'Malfunzionamento',
        accident: 'Incidente',
        parking_issue: 'Problema parcheggio',
        app_issue: 'Problema app',
        payment_issue: 'Problema pagamento',
        other: 'Altro'
    };
    return labels[category] || category;
};

const Report = mongoose.model('Report', reportSchema);
module.exports = Report;
