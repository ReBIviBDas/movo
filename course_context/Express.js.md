# Implementing WebAPIs with Express.js
**Tags:** #nodejs #express #backend #api #rest #javascript
**Source:** Lecture Slides (Marco Robol)

## 1. Introduction to Express.js
Express is a minimal and flexible Node.js web application framework. It simplifies the creation of web servers compared to using the raw Node.js `http` module.

### Setup
Initialize a project and install Express:
```bash
npm init -y
npm install express
```

### Hello World Server
Create a file (e.g., `app.js`):
```javascript
const express = require('express');
const app = express();
const port = 3000;

// Basic Route
app.get('/', (req, res) => {
    res.send('Hello World!');
});

// Start Server
app.listen(port, () => {
    console.log(`Server running on port ${port}`);
});
```

---

## 2. Routing
Routing defines how the application responds to client requests to a specific endpoint (URI + HTTP Method).

**Syntax:** `app.METHOD(PATH, HANDLER)`

```javascript
app.get('/books', (req, res) => { ... });
app.post('/books', (req, res) => { ... });
app.delete('/books/:id', (req, res) => { ... });
```

---

## 3. Handling Requests (`req`)
To build dynamic APIs, you need to extract data from the request object.

### Types of Parameters

| Type | Accessor | Usage | URL Example |
| :--- | :--- | :--- | :--- |
| **Path Params** | `req.params` | Identifiers in the URL | `/books/:id` -> `/books/5` |
| **Query Params** | `req.query` | Sorting/Filtering | `/books?sort=asc` |
| **Headers** | `req.headers` | Meta-data (Auth, Content-Type) | `Authorization: Bearer...` |
| **Body** | `req.body` | Data sent in POST/PUT | JSON payload |

### Body Parsing (Middleware)
> [!WARNING] Important
> By default, Express receives the request body as a stream. To access `req.body` as a JSON object, you **must** configure middleware.

```javascript
// Middleware to parse JSON bodies
app.use(express.json());

// Middleware to parse URL-encoded form data
app.use(express.urlencoded({ extended: true }));

// Now you can access data
app.post('/books', (req, res) => {
    console.log(req.body.title);
    console.log(req.body.author);
});
```

---

## 4. Handling Responses (`res`)
The `res` object allows you to send data back to the client.

### Common Methods
*   `res.send('text')`: Sends a text/html response.
*   `res.json({ id: 1 })`: Sends a JSON response (Standard for APIs).
*   `res.sendFile('/path/to/img.png')`: Sends a file.

### Status Codes & Chaining
Always return correct HTTP status codes.
```javascript
// Not Found
res.status(404).send('Resource not found');

// Created successfully (Chain status + json)
res.status(201).json({ message: 'Created' });
```

### Location Header
For RESTful `POST` requests (resource creation), you should return the URL of the new resource in the headers.
```javascript
app.post('/api/products', (req, res) => {
    // ... logic to create product ...
    let newId = 123;
    
    // Set Location header and return 201
    res.location("/api/products/" + newId);
    res.status(201).send();
});
```

---

## 5. Middleware & Static Files
Middleware functions are functions that have access to the request (`req`), response (`res`), and the next function (`next`) in the cycle. They run sequentially.

### Serving Static Files
You can serve HTML, images, and CSS directly without writing routes for every file.
```javascript
// Mounts the 'public' folder to the root URL
app.use(express.static('public'));

// OR Mounts to a specific path prefix
app.use('/static', express.static('public'));
```

---

## 6. API Documentation (Swagger/OpenAPI)
You can serve your `oas3.yaml` file as an interactive website using `swagger-ui-express`.

### Method 1: Serving a Static YAML File
**Install:** `npm install swagger-ui-express js-yaml`

```javascript
const swaggerUi = require('swagger-ui-express');
const fs = require('fs');
const yaml = require('js-yaml');
const path = require('path');

// Load the YAML file
const swaggerDocument = yaml.load(fs.readFileSync('./oas3.yaml', 'utf8'));

// Serve at /api-docs
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
```

### Method 2: Generating via JSDoc (Code Annotations)
Instead of a separate YAML file, you write comments in your code.
**Install:** `npm install swagger-jsdoc`

1.  **Configure Options:**
    ```javascript
    const swaggerJsdoc = require('swagger-jsdoc');
    
    const options = {
        definition: {
            openapi: '3.0.0',
            info: { title: 'My API', version: '1.0.0' },
        },
        apis: ['./routes/*.js'], // Path to files with annotations
    };
    
    const swaggerSpec = swaggerJsdoc(options);
    app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));
    ```

2.  **Annotate Routes:**
    ```javascript
    /**
     * @openapi
     * /books:
     *   get:
     *     description: Get all books
     *     responses:
     *       200:
     *         description: Success
     */
    app.get('/books', ...);
    ```

---

## 7. Testing with Postman
*   **Method:** Select GET, POST, PUT, DELETE.
*   **URL:** `http://localhost:3000/...`
*   **Body:** Select "Raw" -> "JSON" to send data to your API.
*   **Headers:** Manually add headers if needed (e.g., Auth).