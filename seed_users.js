const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const User = require('./backend/models/User'); // Adjust path as needed
require('dotenv').config({ path: './backend/.env' });

async function seed() {
  try {
    console.log('Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected.');

    // Clear existing users to avoid conflicts (optional, or just handle errors)
    await User.deleteMany({ email: { $in: ['operator@movo.com', 'user@movo.com'] } });

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
      privacy_accepted: true,
      terms_accepted: true
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
      role: 'user', // Default
      status: 'pending', // Default
      privacy_accepted: true,
      terms_accepted: true
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
