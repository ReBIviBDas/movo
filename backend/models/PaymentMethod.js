const mongoose = require('mongoose');
const { Schema } = mongoose;

const paymentMethodSchema = new Schema({
    // Owner
    user_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'User',
        required: true 
    },
    
    // Card info (no sensitive data stored)
    card_type: {
        type: String,
        enum: ['visa', 'mastercard', 'amex', 'unknown'],
        required: true
    },
    last_four: {
        type: String,
        required: true,
        match: /^\d{4}$/
    },
    expiry_month: {
        type: Number,
        required: true,
        min: 1,
        max: 12
    },
    expiry_year: {
        type: Number,
        required: true
    },
    
    // Payment token (from Stripe or mock)
    token: {
        type: String,
        required: true
    },
    
    // Default method for this user
    is_default: {
        type: Boolean,
        default: false
    },
    
    // Nickname (optional)
    nickname: {
        type: String,
        maxlength: 50
    }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

// Ensure only one default per user
paymentMethodSchema.pre('save', async function(next) {
    if (this.is_default) {
        await this.constructor.updateMany(
            { user_id: this.user_id, _id: { $ne: this._id } },
            { is_default: false }
        );
    }
    next();
});

const PaymentMethod = mongoose.model('PaymentMethod', paymentMethodSchema);
module.exports = PaymentMethod;
