const mongoose = require('mongoose');
const { Schema } = mongoose;

const rentalSchema = new Schema({
    // User renting the vehicle
    user_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'User',
        required: true 
    },
    
    // Vehicle being rented
    vehicle_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'Vehicle',
        required: true 
    },
    
    // Associated booking (optional, if rental started from booking)
    booking_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'Booking'
    },
    
    // Rental status
    status: {
        type: String,
        enum: ['active', 'completed', 'cancelled'],
        default: 'active'
    },
    
    // Time tracking
    started_at: {
        type: Date,
        required: true,
        default: Date.now
    },
    ended_at: {
        type: Date
    },
    
    // Location tracking
    start_location: {
        lat: Number,
        lng: Number
    },
    end_location: {
        lat: Number,
        lng: Number
    },
    
    // Distance (in km, optional for future use)
    distance_km: {
        type: Number,
        default: 0
    },
    
    // Cost calculation
    duration_minutes: {
        type: Number
    },
    price_per_minute: {
        type: Number
    },
    subtotal: {
        type: Number
    },
    discount: {
        type: Number,
        default: 0
    },
    total_cost: {
        type: Number
    },
    
    // Payment info
    payment_method_id: {
        type: Schema.Types.ObjectId,
        ref: 'PaymentMethod'
    },
    payment_status: {
        type: String,
        enum: ['pending', 'charged', 'failed', 'refunded'],
        default: 'pending'
    },
    charge_id: {
        type: String
    },
    
    // Vehicle snapshot at rental start
    vehicle_snapshot: {
        type: {
            plate: String,
            model: String,
            type: String,
            battery_level: Number
        },
        _id: false
    }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

// Index for finding active rentals
rentalSchema.index({ user_id: 1, status: 1 });
rentalSchema.index({ vehicle_id: 1, status: 1 });

const Rental = mongoose.model('Rental', rentalSchema);
module.exports = Rental;
