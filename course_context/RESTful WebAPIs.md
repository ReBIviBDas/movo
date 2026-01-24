
# Intro to Web 2.0 and RESTful WebAPIs
**Tags:** #web-development #rest-api #architecture #software-engineering
**Source:** Lecture Slides (Marco Robol)

## 1. Web Evolution & Architecture

### Web 1.0 vs. Web 2.0
*   **Web 1.0 (Static):** Static HTML with hyperlinks. Interaction limited to HTML forms with full page reloads.
*   **Web 2.0 (Dynamic):** JavaScript-based applications.
    *   **AJAX (Asynchronous JavaScript and XML):** Decouples data retrieval from presentation.
    *   **Key Behavior:** Sends HTTP requests in the background, updating the DOM dynamically without reloading the page.

### Modern Architectures
1.  **Standard Web App:** Browser -> Load Balancer -> Web Server -> Database (often cached via CDN).
2.  **Serverless Architecture:** Backend built on cloud functions (FaaS like AWS Lambda/Azure Functions). Event-driven (e.g., triggering a function to send an email).
3.  **Lab Architecture (Course Requirement):**
    *   **Backend:** RESTful Web Service (Node.js/Express) deployed on PaaS (e.g., Render).
    *   **Frontend:** Single-Page Application (SPA) using Vue.js.
    *   **Delivery:** Static content served via CDN.
    *   *Note:* No Server-Side Rendering (SSR) for this lab.

---

## 2. RESTful WebAPIs Concepts

**API (Application Programming Interface):** Defines how applications communicate.
**REST (Representational State Transfer):** An architectural style using standard HTTP methods to manipulate resources in a stateless manner.

### Core Components
1.  **Resources:** Objects identified by a **URI** (Uniform Resource Identifier).
    *   *Example:* `/api/products`
2.  **Representation:** How the resource is returned (JSON, XML).
3.  **Operations:** CRUD actions mapped to HTTP Verbs.

### HTTP Methods & CRUD Mapping

| Operation | HTTP Verb | URI Pattern | Request Body | Response Body | Success Code |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Search/Read** | `GET` | `/products` | Empty | List `[Product+]` | `200 OK` |
| **Create** | `POST` | `/products` | New Product | Created Product | `201 Created` |
| **Read One** | `GET` | `/products/:id` | Empty | Single Product | `200 OK` |
| **Update** | `PUT` | `/products/:id` | **Complete** Product | Updated Product | `200 OK` |
| **Modify** | `PATCH` | `/products/:id` | **Partial** Fields | Updated Product | `200 OK` |
| **Delete** | `DELETE` | `/products/:id` | Empty | Empty | `204 No Content`|

> [!INFO] Properties of GET
> **Safe:** Does not modify data.
> **Idempotent:** Can be repeated multiple times without changing the result/state beyond the initial application.

### PUT vs. PATCH
*   **PUT:** Replaces the *entire* resource. Missing fields in the body might be set to `NULL`.
*   **PATCH:** Updates *only* the specified fields.

---

## 3. Response Structure & Navigation

### Status Codes
Always return semantically correct HTTP status codes.
*   `2xx`: Success (200 OK, 201 Created, 204 No Content).
*   `4xx`: Client Error (400 Bad Request, 404 Not Found).
*   `5xx`: Server Error.

### HATEOAS / Linking
Instead of just returning raw database IDs, RESTful APIs should provide links to traverse relationships (Hypermedia).

**Example: Getting a Product**
```json
// GET /products/123
{
  "self": "/products/123",
  "name": "iPhone",
  "producer": "/company/456"  // Link to the company resource
}
```

**Example: Getting a Company**
```json
// GET /company/456
{
  "self": "/company/456",
  "name": "Apple",
  "products": "/products?producer=/company/456" // Link to filter products
}
```

---

## 4. Designing Your API
**Process for the Lab:**

1.  **Start from User Stories:** What does the user need to do?
2.  **Identify Resources:** Define the data structure (e.g., Books, Users, Lendings).
3.  **Define Hierarchy:** Main resources vs. Sub-resources.
4.  **Map Constraints:** Which methods are allowed? What parameters are required?
5.  **Define Status Codes:** What constitutes success vs. failure?

---

## 5. Tools & Resources

**Testing:**
*   [Postman](https://www.postman.com/): Essential for testing API endpoints manually.

**Public APIs for Practice:**
*   [JSONPlaceholder](https://jsonplaceholder.typicode.com/): Fake API for prototyping.
*   [Restful-api.dev](https://restful-api.dev): Test objects.
*   [PokeAPI](https://pokeapi.co/): Pokemon data.

**Documentation Examples:**
*   [EasyLib APIary](https://easylib.docs.apiary.io/#) (Reference project).
*   [SwaggerHub](https://app.swaggerhub.com/apis/SunSync/SunSync/2.0.0).