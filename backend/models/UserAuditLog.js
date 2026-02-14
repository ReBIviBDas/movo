const mongoose = require('mongoose');
const { Schema } = mongoose;

const userAuditLogSchema = new Schema({
    user_id: { 
        type: Schema.Types.ObjectId, 
        ref: 'User', 
        required: true,
        index: true
    },

    user_email: {
        type: String,
        required: true
    },

    action: { 
        type: String, 
        enum: [
            'approved',
            'rejected',
            'suspended',
            'reactivated',
            'blocked',
            'documents_uploaded',
            'status_changed'
        ],
        required: true,
        index: true
    },

    performed_by: { 
        type: Schema.Types.ObjectId, 
        ref: 'User', 
        required: true 
    },

    performed_by_email: {
        type: String,
        required: true
    },

    changes: {
        type: Schema.Types.Mixed,
        default: null
    },

    reason: { 
        type: String,
        maxlength: 500
    },

    timestamp: { 
        type: Date, 
        default: Date.now,
        index: true
    }
});

userAuditLogSchema.index({ user_id: 1, timestamp: -1 });
userAuditLogSchema.index({ timestamp: -1 });

userAuditLogSchema.statics.logAction = async function(params) {
    const { targetUser, action, operator, changes, reason } = params;
    
    const log = new this({
        user_id: targetUser._id || targetUser.id,
        user_email: targetUser.email,
        action,
        performed_by: operator._id || operator.id,
        performed_by_email: operator.email,
        changes: changes || null,
        reason: reason || null
    });
    
    return log.save();
};

const UserAuditLog = mongoose.model('UserAuditLog', userAuditLogSchema);
module.exports = UserAuditLog;
