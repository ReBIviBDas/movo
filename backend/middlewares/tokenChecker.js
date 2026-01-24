const jwt = require('jsonwebtoken');

const tokenChecker = (req, res, next) => {
    // 1. Check if the token is in the header, body, or query
    let token = req.headers['authorization'] || req.body.token || req.query.token;

    // 2. If no token, kick them out
    if (!token) {
        return res.status(401).json({ success: false, message: 'No token provided.' });
    }

    // 3. Clean up the token (Remove "Bearer " prefix if present)
    if (token.startsWith('Bearer ')) {
        token = token.slice(7, token.length);
    }

    // 4. Verify the signature
    jwt.verify(token, process.env.SUPER_SECRET, (err, decoded) => {
        if (err) {
            return res.status(403).json({ success: false, message: 'Token is invalid or expired.' });
        } else {
            // 5. Success! Attach the user info to the request so we can use it later
            req.loggedUser = decoded;
            next(); // Pass control to the next function
        }
    });
};

module.exports = tokenChecker;