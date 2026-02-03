/**
 * Payment Service - Abstraction layer for payment operations
 * 
 * MOCK IMPLEMENTATION: Simulates payment processing for course project
 * TO SWITCH TO STRIPE: Replace functions with Stripe SDK calls
 * 
 * Example Stripe replacement:
 *   const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
 *   validateCard: (cardData) => stripe.paymentMethods.create({...})
 */

// ============================================================================
// CONFIGURATION
// ============================================================================
const USE_MOCK = true; // Set to false when integrating real Stripe

// ============================================================================
// MOCK IMPLEMENTATIONS
// ============================================================================

/**
 * Luhn algorithm for card number validation
 */
function luhnCheck(cardNumber) {
    const digits = cardNumber.replace(/\D/g, '');
    let sum = 0;
    let isEven = false;
    
    for (let i = digits.length - 1; i >= 0; i--) {
        let digit = parseInt(digits[i], 10);
        
        if (isEven) {
            digit *= 2;
            if (digit > 9) digit -= 9;
        }
        
        sum += digit;
        isEven = !isEven;
    }
    
    return sum % 10 === 0;
}

/**
 * Detect card type from number
 */
function detectCardType(cardNumber) {
    const patterns = {
        visa: /^4/,
        mastercard: /^5[1-5]/,
        amex: /^3[47]/
    };
    
    for (const [type, pattern] of Object.entries(patterns)) {
        if (pattern.test(cardNumber)) return type;
    }
    return 'unknown';
}

/**
 * Generate mock token (simulates Stripe tokenization)
 */
function generateMockToken() {
    return 'tok_mock_' + Math.random().toString(36).substring(2, 15);
}

// ============================================================================
// MOCK SERVICE FUNCTIONS
// ============================================================================

const mockService = {
    /**
     * Validate card data
     * @returns {Object} { valid: boolean, error?: string, cardType?: string }
     */
    validateCard: async (cardData) => {
        const { card_number, expiry_month, expiry_year, cvv } = cardData;
        
        // Basic validation
        if (!card_number || !expiry_month || !expiry_year || !cvv) {
            return { valid: false, error: 'Missing required fields' };
        }
        
        // Luhn check
        if (!luhnCheck(card_number)) {
            return { valid: false, error: 'Invalid card number' };
        }
        
        // Expiry check
        const now = new Date();
        const expiry = new Date(expiry_year, expiry_month - 1);
        if (expiry < now) {
            return { valid: false, error: 'Card has expired' };
        }
        
        // CVV check
        if (!/^\d{3,4}$/.test(cvv)) {
            return { valid: false, error: 'Invalid CVV' };
        }
        
        return { 
            valid: true, 
            cardType: detectCardType(card_number),
            lastFour: card_number.slice(-4)
        };
    },
    
    /**
     * Create payment token (simulates Stripe tokenization)
     * @returns {Object} { success: boolean, token?: string, error?: string }
     */
    createToken: async (cardData) => {
        const validation = await mockService.validateCard(cardData);
        if (!validation.valid) {
            return { success: false, error: validation.error };
        }
        
        return {
            success: true,
            token: generateMockToken(),
            cardType: validation.cardType,
            lastFour: validation.lastFour
        };
    },
    
    /**
     * Charge a payment method
     * @returns {Object} { success: boolean, chargeId?: string, error?: string }
     */
    chargeCard: async (token, amount, description) => {
        // Mock: Always succeeds unless amount is negative
        if (amount <= 0) {
            return { success: false, error: 'Invalid amount' };
        }
        
        // Simulate processing delay
        await new Promise(resolve => setTimeout(resolve, 500));
        
        return {
            success: true,
            chargeId: 'ch_mock_' + Date.now(),
            amount,
            description
        };
    },
    
    /**
     * Refund a charge
     * @returns {Object} { success: boolean, refundId?: string }
     */
    refundCharge: async (chargeId, amount) => {
        return {
            success: true,
            refundId: 'rf_mock_' + Date.now(),
            amount
        };
    }
};

// ============================================================================
// STRIPE SERVICE FUNCTIONS (Placeholder for real implementation)
// ============================================================================

const stripeService = {
    validateCard: async (cardData) => {
        // TODO: Replace with Stripe PaymentMethod creation
        // const paymentMethod = await stripe.paymentMethods.create({
        //     type: 'card',
        //     card: { number, exp_month, exp_year, cvc }
        // });
        throw new Error('Stripe not configured. Set STRIPE_SECRET_KEY in .env');
    },
    
    createToken: async (cardData) => {
        // TODO: Replace with Stripe SetupIntent
        throw new Error('Stripe not configured');
    },
    
    chargeCard: async (token, amount, description) => {
        // TODO: Replace with Stripe PaymentIntent
        // const paymentIntent = await stripe.paymentIntents.create({
        //     amount: Math.round(amount * 100), // Stripe uses cents
        //     currency: 'eur',
        //     payment_method: token,
        //     confirm: true
        // });
        throw new Error('Stripe not configured');
    },
    
    refundCharge: async (chargeId, amount) => {
        // TODO: Replace with Stripe refund
        throw new Error('Stripe not configured');
    }
};

// ============================================================================
// EXPORT ACTIVE SERVICE
// ============================================================================

module.exports = USE_MOCK ? mockService : stripeService;
