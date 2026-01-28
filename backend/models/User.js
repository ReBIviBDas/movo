const mongoose = require('mongoose');
const { Schema } = mongoose;

const userSchema = new Schema({
    // Personal Data (RF2) - Following OpenAPI spec naming conventions
    first_name: { type: String, required: true },
    last_name: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    phone: { type: String, required: true },
    date_of_birth: { type: Date, required: true },
    fiscal_code: { type: String, required: true, unique: true }, // Codice Fiscale
    address: { type: String },

    // Language preference
    language: { 
        type: String, 
        enum: ['it', 'en', 'de'], 
        default: 'it' 
    },

    // Role Management (User vs Operator)
    role: { 
        type: String, 
        enum: ['user', 'operator', 'admin'], 
        default: 'user' 
    },

    // Account Status (as per OpenAPI spec)
    status: {
        type: String,
        enum: ['active', 'pending_approval', 'suspended', 'blocked'],
        default: 'active'
    },

    // Terms acceptance
    accept_terms: { type: Boolean, default: false },
    accept_privacy: { type: Boolean, default: false },
    accept_cookies: { type: Boolean, default: false },

    // Driver-specific fields
    driving_enabled: { type: Boolean, default: false },
    driving_license_verified: { type: Boolean, default: false },
    driving_license: { type: String }, // Link to file
    driving_license_expiry: { type: Date },
    identity_document: { type: String }, // Link to file  
    identity_document_expiry: { type: Date },

    // OAuth
    google_id: { type: String, sparse: true }
}, {
    timestamps: { createdAt: 'created_at', updatedAt: 'updated_at' }
});

const User = mongoose.model('User', userSchema);
module.exports = User;