const mongoose = require('mongoose');
const { Schema } = mongoose;

const vehicleSchema = new Schema({
    // Vehicle identification
    plate: { 
        type: String, 
        required: true, 
        unique: true,
        uppercase: true 
    },
    model: { 
        type: String, 
        required: true 
    },
    type: {
        type: String,
        enum: ['car', 'scooter', 'bike'],
        default: 'car'
    },
    
    // Status
    status: {
        type: String,
        enum: ['available', 'rented', 'maintenance', 'charging'],
        default: 'available'
    },
    
    // Battery level (0-100)
    battery_level: {
        type: Number,
        min: 0,
        max: 100,
        default: 100
    },
    
    // Location (GeoJSON Point)
    location: {
        type: {
            type: String,
            enum: ['Point'],
            default: 'Point'
        },
        coordinates: {
            type: [Number], // [longitude, latitude]
            required: true
        }
    },
    
    // Additional info
    range_km: { type: Number, default: 200 }, // Estimated range
    price_per_minute: { type: Number, default: 0.35 }, // EUR per minute
    image_url: { type: String },
    
    // Current rental (if rented)
    current_rental_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'Rental',
        default: null
    }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'last_updated' }
});

// Create 2dsphere index for geospatial queries
vehicleSchema.index({ location: '2dsphere' });

const Vehicle = mongoose.model('Vehicle', vehicleSchema);
module.exports = Vehicle;
