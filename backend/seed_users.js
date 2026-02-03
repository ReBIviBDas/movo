const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('./models/User'); 
require('dotenv').config();

async function seed() {
  try {
    console.log('Connecting to MongoDB...');
    if (!process.env.DB_URL) throw new Error('DB_URL not found in environment');
    await mongoose.connect(process.env.DB_URL);
    console.log('Connected.');

    // Clear existing users 
    await User.deleteMany({ email: { $in: ['operator@movo.com', 'user@movo.com'] } });
    
    // Drop all indexes to clear stale ones (like taxId_1)
    try {
      await User.collection.dropIndexes();
      console.log('Dropped old indexes.');
    } catch (e) {
      console.log('No indexes to drop or error dropping:', e.message);
    }
    
    // Re-sync indexes from schema
    await User.syncIndexes();
    console.log('Synced schema indexes.');

    console.log('Cleared old test users.');

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash('password123', salt);

    // Create Operator
    const operator = new User({
      first_name: 'Op',
      last_name: 'Erator',
      email: 'operator@movo.com',
      password: hashedPassword,
      phone: '1234567890',
      date_of_birth: new Date('1990-01-01'),
      fiscal_code: 'OPERATOR123',
      role: 'operator',
      status: 'active',
      accept_privacy: true,
      accept_terms: true
    });
    await operator.save();
    console.log('Operator created.');

    // Create User (Pending)
    const user = new User({
      first_name: 'Mario',
      last_name: 'Rossi',
      email: 'user@movo.com',
      password: hashedPassword,
      phone: '0987654321',
      date_of_birth: new Date('1995-05-05'),
      fiscal_code: 'RSSMRA95E05H501Z',
      role: 'user',
      status: 'active', // Active so they can test bookings
      accept_privacy: true,
      accept_terms: true,
      driving_enabled: true // Enable driving for testing
    });
    await user.save();
    console.log('User created.');

    process.exit(0);
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

seed();
