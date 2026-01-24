
**Tags:** #authentication #jwt #security #nodejs #express #rest-api
**Source:** Lecture Slides (Marco Robol)

## 1. Core Concepts

### REST is Stateless
*   **No Sessions:** Unlike traditional web apps (Web 1.0), REST APIs do not keep session state on the server.
*   **Implication:** We cannot rely on server-side sessions/cookies to remember a user.
*   **Solution:** **Token-based Authentication**. Every single request must carry credentials (a token) to prove identity.

### Authentication vs. Authorization
*   **Authentication (Who):** Verifying the user's identity (e.g., Login).
*   **Authorization (What):** Verifying if the authenticated user has permission to access a specific resource.

### Token-Based Flow (Bearer Auth)
1.  **Client:** Sends credentials (username/password) to the server.
2.  **Server:** Validates credentials. If valid, generates a **Signed Token** (JWT) and sends it back.
3.  **Client:** Stores the token.
4.  **Client:** Sends the token in the HTTP Header for every subsequent request.
    *   *Header format:* `Authorization: Bearer <token>`
5.  **Server:** Validates the token signature. If valid, grants access.

---

## 2. JWT (JSON Web Token)
A standard (RFC 7519) for securely transmitting information as a JSON object.

*   **Structure:** `Header.Payload.Signature`
*   **Payload:** Contains user data (e.g., User ID, Email) and metadata (Expiration).
*   **Signature:** Ensures the token hasn't been tampered with. Requires a **Secret Key** known only to the server.
*   **Tool:** Debug and decode tokens at [jwt.io](https://jwt.io).

---

## 3. Implementation in Express.js

### Prerequisites
Install the required packages:
```bash
npm install jsonwebtoken dotenv
```

### Step 1: Security Configuration (`dotenv`)
**Never** hardcode secrets in source code. Use environment variables.

1.  Create a file named `.env` in your root:
    ```env
    SUPER_SECRET=my_super_secret_passphrase
    ```
2.  Load it in your application:
    *   **Option A (Code):** `import 'dotenv/config';`
    *   **Option B (Preload):** Run node with `node -r dotenv/config index.js`

### Step 2: The Authentication Route (Login)
This endpoint verifies credentials and issues the token.

**`app/authentication.js`**
```javascript
const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const Student = require('./models/student'); // Assuming you have a User model

router.post('', async function(req, res) {
    // 1. Find the user
    let user = await Student.findOne({ email: req.body.email }).exec();

    // 2. Validate user and password
    if (!user) {
        return res.json({ success: false, message: 'User not found' });
    }
    if (user.password != req.body.password) {
        return res.json({ success: false, message: 'Wrong password' });
    }

    // 3. User authenticated -> Create Token
    // Payload: What you want to access later (e.g., ID, email)
    var payload = { email: user.email, id: user._id };
    var options = { expiresIn: 86400 }; // Expires in 24 hours

    // Sign with the secret from .env
    var token = jwt.sign(payload, process.env.SUPER_SECRET, options);

    // 4. Return token to client
    res.json({
        success: true,
        message: 'Enjoy your token!',
        token: token,
        email: user.email,
        id: user._id
    });
});

module.exports = router;
```

### Step 3: The Middleware (Token Checker)
This intercepts requests to protected routes to ensure a valid token is present.

**`app/tokenChecker.js`**
```javascript
const jwt = require('jsonwebtoken');

const tokenChecker = function(req, res, next) {
    // 1. Check for token in Body, URL parameters, or Headers
    var token = req.body.token || req.query.token || req.headers['x-access-token'] || req.headers['authorization'];

    // Handle "Bearer <token>" format if sent in Authorization header
    if (token && token.startsWith('Bearer ')) {
        token = token.slice(7, token.length);
    }

    // 2. If no token, return 401 Unauthorized
    if (!token) {
        return res.status(401).json({ success: false, message: 'No token provided.' });
    }

    // 3. Verify the token
    jwt.verify(token, process.env.SUPER_SECRET, function(err, decoded) {
        if (err) {
            return res.status(403).json({ success: false, message: 'Token not valid' });
        } else {
            // 4. Token valid: Save decoded user data to request object
            req.loggedUser = decoded;
            next(); // Pass control to the actual route handler
        }
    });
};

module.exports = tokenChecker;
```

### Step 4: Protecting Routes
The order of middleware definition determines which routes are protected.

**`app.js` or `index.js`**
```javascript
const authentication = require('./app/authentication');
const tokenChecker = require('./app/tokenChecker');
const booklendings = require('./app/booklendings'); // Resource router

// 1. PUBLIC ROUTES (No Token Needed)
app.use('/api/v1/authentications', authentication); // Login route

// 2. MIDDLEWARE (Gatekeeper)
// All routes defined AFTER this line require a valid token
app.use(tokenChecker);

// 3. PROTECTED ROUTES
app.use('/api/v1/booklendings', booklendings);
```

> [!TIP] Specific Protection
> You can also apply the checker to specific routes only:
> `app.use('/api/v1/students/me', tokenChecker);`

---

## 4. Google Authentication (OAuth2)
Instead of handling passwords, you can verify tokens issued by Google.

**Library:** `google-auth-library`

```javascript
const { OAuth2Client } = require('google-auth-library');
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

async function verifyGoogleToken(token) {
    const ticket = await client.verifyIdToken({
        idToken: token,
        audience: process.env.GOOGLE_CLIENT_ID, 
    });
    
    const payload = ticket.getPayload();
    const userid = payload['sub'];
    // payload also contains email, name, picture, etc.
    return payload;
}
```

---

## 5. Summary Checklist
1.  [ ] **State:** Ensure API is stateless (No sessions).
2.  [ ] **Env:** Create `.env` and add `SUPER_SECRET`.
3.  [ ] **Login Endpoint:** Create a route that validates password and returns `jwt.sign()`.
4.  [ ] **Middleware:** Create a function that runs `jwt.verify()`.
5.  [ ] **Protect:** Apply middleware before sensitive routes.
6.  [ ] **Client:** Client must send token in headers (Best practice: `Authorization: Bearer <token>`).