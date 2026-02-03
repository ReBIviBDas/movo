/**
 * Location Service - Abstraction layer for GPS/geolocation operations
 * 
 * MOCK IMPLEMENTATION: Simulates location checks for course project
 * TO SWITCH TO REAL: Set USE_MOCK = false and implement real GPS checks
 */

// ============================================================================
// CONFIGURATION
// ============================================================================
const USE_MOCK = true; // Set to false for real GPS validation

// Trento authorized parking zones (GeoJSON polygons)
// In production, these would come from database
const AUTHORIZED_ZONES = [
    {
        name: 'Centro Storico',
        polygon: [
            [11.115, 46.064], [11.125, 46.064],
            [11.125, 46.072], [11.115, 46.072]
        ]
    },
    {
        name: 'Stazione Trento',
        polygon: [
            [11.118, 46.070], [11.128, 46.070],
            [11.128, 46.076], [11.118, 46.076]
        ]
    }
];

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Calculate distance between two points using Haversine formula
 * @returns {number} Distance in meters
 */
function haversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth's radius in meters
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
}

/**
 * Check if point is inside polygon (ray casting algorithm)
 */
function pointInPolygon(point, polygon) {
    const [x, y] = point;
    let inside = false;
    
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
        const [xi, yi] = polygon[i];
        const [xj, yj] = polygon[j];
        
        if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
            inside = !inside;
        }
    }
    
    return inside;
}

// ============================================================================
// MOCK SERVICE FUNCTIONS
// ============================================================================

const mockService = {
    /**
     * Check if user is within proximity of vehicle
     * @param {Object} userLocation { lat, lng }
     * @param {Object} vehicleLocation { lat, lng }
     * @param {number} maxDistance Maximum allowed distance in meters
     * @returns {Object} { withinRange: boolean, distance: number }
     */
    checkProximity: async (userLocation, vehicleLocation, maxDistance = 30) => {
        // MOCK: Always return within range for testing
        // In production: use real GPS coordinates
        return {
            withinRange: true,
            distance: Math.floor(Math.random() * maxDistance), // Simulated distance
            message: 'Mock: Proximity check passed'
        };
    },
    
    /**
     * Validate that vehicle is in authorized parking zone
     * @param {Object} location { lat, lng }
     * @returns {Object} { valid: boolean, zoneName?: string, nearestZone?: Object }
     */
    validateParkingZone: async (location) => {
        // MOCK: Always return valid for testing
        return {
            valid: true,
            zoneName: 'Centro Storico (Mock)',
            message: 'Mock: Parking zone validated'
        };
    },
    
    /**
     * Get nearest authorized parking zones
     * @param {Object} location { lat, lng }
     * @returns {Array} List of nearby zones with distances
     */
    getNearestZones: async (location) => {
        return AUTHORIZED_ZONES.map(zone => ({
            name: zone.name,
            distance: Math.floor(Math.random() * 500) + 100 // Mock distance
        }));
    }
};

// ============================================================================
// REAL SERVICE FUNCTIONS
// ============================================================================

const realService = {
    checkProximity: async (userLocation, vehicleLocation, maxDistance = 30) => {
        const distance = haversineDistance(
            userLocation.lat, userLocation.lng,
            vehicleLocation.lat, vehicleLocation.lng
        );
        
        return {
            withinRange: distance <= maxDistance,
            distance: Math.round(distance),
            message: distance <= maxDistance 
                ? 'You are close enough to unlock'
                : `Move ${Math.round(distance - maxDistance)}m closer to the vehicle`
        };
    },
    
    validateParkingZone: async (location) => {
        const point = [location.lng, location.lat];
        
        for (const zone of AUTHORIZED_ZONES) {
            if (pointInPolygon(point, zone.polygon)) {
                return {
                    valid: true,
                    zoneName: zone.name
                };
            }
        }
        
        return {
            valid: false,
            message: 'Vehicle is not in an authorized parking zone',
            nearestZones: await realService.getNearestZones(location)
        };
    },
    
    getNearestZones: async (location) => {
        return AUTHORIZED_ZONES.map(zone => {
            // Calculate distance to center of zone
            const centerLng = zone.polygon.reduce((sum, p) => sum + p[0], 0) / zone.polygon.length;
            const centerLat = zone.polygon.reduce((sum, p) => sum + p[1], 0) / zone.polygon.length;
            const distance = haversineDistance(location.lat, location.lng, centerLat, centerLng);
            
            return {
                name: zone.name,
                distance: Math.round(distance),
                center: { lat: centerLat, lng: centerLng }
            };
        }).sort((a, b) => a.distance - b.distance);
    }
};

// ============================================================================
// EXPORT ACTIVE SERVICE
// ============================================================================

module.exports = USE_MOCK ? mockService : realService;
