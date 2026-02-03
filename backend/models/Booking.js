const mongoose = require('mongoose');
const { Schema } = mongoose;

const bookingSchema = new Schema({
    // User who made the booking
    user_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'User',
        required: true 
    },
    
    // Vehicle being booked
    vehicle_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'Vehicle',
        required: true 
    },
    
    // Booking status
    status: {
        type: String,
        enum: ['active', 'completed', 'expired', 'cancelled'],
        default: 'active'
    },
    
    // Expiration time (15 or 30 minutes from creation)
    expires_at: {
        type: Date,
        required: true
    },
    
    // Duration in minutes (15 for regular, 30 for subscribers)
    duration_minutes: {
        type: Number,
        default: 15
    },
    
    // Vehicle snapshot at booking time
    vehicle_snapshot: {
        plate: String,
        model: String,
        location: {
            lat: Number,
            lng: Number
        }
    }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

// Index for finding active bookings
bookingSchema.index({ user_id: 1, status: 1 });
bookingSchema.index({ vehicle_id: 1, status: 1 });
bookingSchema.index({ expires_at: 1 }); // For expiration job

const Booking = mongoose.model('Booking', bookingSchema);
module.exports = Booking;
