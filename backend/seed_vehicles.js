const mongoose = require('mongoose');
const Vehicle = require('./models/Vehicle');
require('dotenv').config();

// Trento city center coordinates
const TRENTO_CENTER = { lat: 46.0677, lng: 11.1219 };

// Mock vehicle data around Trento
const mockVehicles = [
    {
        plate: 'TN001EV',
        model: 'Fiat 500e',
        type: 'car',
        status: 'available',
        battery_level: 85,
        location: { type: 'Point', coordinates: [11.1209, 46.0687] }, // Piazza Duomo
        range_km: 180
    },
    {
        plate: 'TN002EV',
        model: 'Renault Zoe',
        type: 'car',
        status: 'available',
        battery_level: 92,
        location: { type: 'Point', coordinates: [11.1245, 46.0712] }, // Near train station
        range_km: 220
    },
    {
        plate: 'TN003EV',
        model: 'Smart EQ fortwo',
        type: 'car',
        status: 'available',
        battery_level: 78,
        location: { type: 'Point', coordinates: [11.1189, 46.0645] }, // Piazza Fiera
        range_km: 120
    },
    {
        plate: 'TN004EV',
        model: 'VW ID.3',
        type: 'car',
        status: 'charging',
        battery_level: 25,
        location: { type: 'Point', coordinates: [11.1265, 46.0655] }, // Near parking
        range_km: 50
    },
    {
        plate: 'TN005EV',
        model: 'Fiat 500e',
        type: 'car',
        status: 'available',
        battery_level: 100,
        location: { type: 'Point', coordinates: [11.1198, 46.0725] }, // Università
        range_km: 210
    },
    {
        plate: 'TN006EV',
        model: 'Tesla Model 3',
        type: 'car',
        status: 'maintenance',
        battery_level: 45,
        location: { type: 'Point', coordinates: [11.1155, 46.0618] }, // Service area
        range_km: 200
    }
];

async function seed() {
    try {
        console.log('Connecting to MongoDB...');
        await mongoose.connect(process.env.DB_URL);
        console.log('Connected.');

        // Clear existing vehicles
        await Vehicle.deleteMany({});
        console.log('Cleared existing vehicles.');

        // Insert mock vehicles
        await Vehicle.insertMany(mockVehicles);
        console.log(`Seeded ${mockVehicles.length} vehicles in Trento.`);

        // Create geospatial index
        await Vehicle.collection.createIndex({ location: '2dsphere' });
        console.log('Created 2dsphere index.');

        process.exit(0);
    } catch (err) {
        console.error('Seed error:', err);
        process.exit(1);
    }
}

seed();
