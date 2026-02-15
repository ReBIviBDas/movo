/**
 * Payment Service
 * Mock implementation for development
 */

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
 * Generate mock token
 */
function generateMockToken() {
    return 'tok_mock_' + Math.random().toString(36).substring(2, 15);
}

/**
 * Validate card data
 * @returns {Object} { valid: boolean, error?: string, cardType?: string }
 */
const validateCard = async (cardData) => {
    const { card_number, expiry_month, expiry_year, cvv } = cardData;
    
    if (!card_number || !expiry_month || !expiry_year || !cvv) {
        return { valid: false, error: 'Missing required fields' };
    }
    
    if (!luhnCheck(card_number)) {
        return { valid: false, error: 'Invalid card number' };
    }
    
    const now = new Date();
    const expiry = new Date(expiry_year, expiry_month - 1);
    if (expiry < now) {
        return { valid: false, error: 'Card has expired' };
    }
    
    if (!/^\d{3,4}$/.test(cvv)) {
        return { valid: false, error: 'Invalid CVV' };
    }
    
    return { 
        valid: true, 
        cardType: detectCardType(card_number),
        lastFour: card_number.slice(-4)
    };
};

/**
 * Create payment token
 * @returns {Object} { success: boolean, token?: string, error?: string }
 */
const createToken = async (cardData) => {
    const validation = await validateCard(cardData);
    if (!validation.valid) {
        return { success: false, error: validation.error };
    }
    
    return {
        success: true,
        token: generateMockToken(),
        cardType: validation.cardType,
        lastFour: validation.lastFour
    };
};

/**
 * Charge a payment method
 * @returns {Object} { success: boolean, chargeId?: string, error?: string }
 */
const chargeCard = async (token, amount, description) => {
    if (amount <= 0) {
        return { success: false, error: 'Invalid amount' };
    }
    
    await new Promise(resolve => setTimeout(resolve, 500));
    
    return {
        success: true,
        chargeId: 'ch_mock_' + Date.now(),
        amount,
        description
    };
};

/**
 * Refund a charge
 * @returns {Object} { success: boolean, refundId?: string }
 */
const refundCharge = async (chargeId, amount) => {
    return {
        success: true,
        refundId: 'rf_mock_' + Date.now(),
        amount
    };
};

module.exports = {
    validateCard,
    createToken,
    chargeCard,
    refundCharge
};
