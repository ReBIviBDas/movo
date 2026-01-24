const mongoose = require('mongoose');
const { Schema } = mongoose;

const userSchema = new Schema({
    // Personal Data (RF2)
    name: { type: String, required: true },
    surname: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    phone: { type: String, required: true },
    dateOfBirth: { type: Date, required: true },
    taxId: { type: String, required: true, unique: true }, // Codice Fiscale

    // Role Management (User vs Operator)
    role: { 
        type: String, 
        enum: ['user', 'operator', 'admin'], 
        default: 'user' 
    },

    // Documents (We will handle upload later, just placeholders for now)
    driverLicense: { type: String }, // Link to file
    isDriverApproved: { type: Boolean, default: false }
});

const User = mongoose.model('User', userSchema);
module.exports = User;