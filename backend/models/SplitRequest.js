const mongoose = require('mongoose');
const { Schema } = mongoose;

const participantSchema = new Schema({
    user_id: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    percentage: { type: Number, required: true, min: 1, max: 99 },
    amount_cents: { type: Number, default: 0 },
    status: {
        type: String,
        enum: ['pending', 'accepted', 'rejected'],
        default: 'pending'
    }
}, { _id: false });

const splitRequestSchema = new Schema({
    rental_id: { type: Schema.Types.ObjectId, ref: 'Rental', required: true },
    requester_id: { type: Schema.Types.ObjectId, ref: 'User', required: true },
    status: {
        type: String,
        enum: ['pending', 'accepted', 'rejected', 'expired'],
        default: 'pending'
    },
    participants: {
        type: [participantSchema],
        validate: [arr => arr.length >= 1 && arr.length <= 4, 'Participants must be between 1 and 4']
    },
    expires_at: { type: Date, required: true }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

splitRequestSchema.index({ rental_id: 1 });
splitRequestSchema.index({ requester_id: 1 });
splitRequestSchema.index({ 'participants.user_id': 1 });

module.exports = mongoose.model('SplitRequest', splitRequestSchema);
