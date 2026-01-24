# OpenAPI Specification (OAS)
**Tags:** #api #rest #documentation #software-engineering #openapi #yaml
**Source:** Lecture Slides (Marco Robol)

## 1. What is OpenAPI?
OpenAPI Specification (formerly Swagger) is a standard format for describing REST APIs. It allows you to document your entire API in a single file (usually YAML or JSON), including:
*   Endpoints (Paths)
*   Operations (GET, POST, etc.)
*   Input/Output parameters
*   Authentication methods
*   Contact and License information

**Why use it?** It serves as a contract between frontend and backend, automates documentation generation, and helps in testing.

### Recommended Tools
*   **VSCode Extension:** [OpenAPI (Swagger) Editor](https://marketplace.visualstudio.com/items?itemName=42Crunch.vscode-openapi) (Recommended)
*   **Online Editors:** [Swagger Editor](https://editor.swagger.io/)
*   **Testing:** [Postman](https://www.postman.com/)

---

## 2. Basic Structure (YAML)
An OpenAPI 3.0 file consists of specific root sections.

```yaml
openapi: 3.0.0 # The version of the spec used

# General Information about the API
info:
  title: EasyLib OpenAPI 3.0
  description: API for managing book lendings.
  version: '1.0'
  license:
    name: MIT

# Base URLs for the API (Production, Staging, Local)
servers:
  - url: http://localhost:8000/api/v1
    description: Localhost
  - url: http://api.example.com/v1
    description: Production Server

# Endpoints and Operations
paths:
  # Defined later...

# Reusable definitions (Data Models)
components:
  # Defined later...
```

---

## 3. Defining Paths and Operations

Paths are the endpoints of your API (relative to the Server URL). Operations are the HTTP methods.

### Basic Path Structure
```yaml
paths:
  /users:
    get:
      summary: Returns a list of users
      description: Optional extended description using Markdown.
      responses:
        "200":
          description: Success
```

### Path Parameters (Templating)
Use curly braces `{}` to mark parameters in the URL.
*   **Correct:** `/users/{id}`
*   **Usage:** The client must replace `{id}` with a value (e.g., `/users/5`).

```yaml
paths:
  /users/{id}:
    get:
      summary: Get user by ID
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true
      responses:
        "200":
          description: User found
```

### Query Parameters
> [!WARNING] Important
> **Do NOT** put query strings in the path key (e.g., `/users?role={role}` is **WRONG**).
> Define them in the `parameters` section with `in: query`.

```yaml
paths:
  /users:
    get:
      summary: Get users filtered by role
      parameters:
        - in: query
          name: role
          schema:
            type: string
            enum: [user, admin] # Restrict values
          required: true 
```

---

## 4. Request Body & Responses

Used for `POST`, `PUT`, and `PATCH` to send data to the server.

```yaml
paths:
  /booklendings:
    post:
      summary: Borrow a book
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/Booklending" # Reference to a reusable model
      responses:
        "201":
          description: Booklending created
          headers:
            Location:
              schema:
                type: string
                description: URL of the new resource
```

---

## 5. Components (Reusable Schemas)
Instead of repeating data structures, define them in `components` and reference them using `$ref`.

```yaml
components:
  schemas:
    Booklending:
      type: object
      required:
        - student
        - book
      properties:
        student:
          type: string
          description: Link to the user
        book:
          type: integer
          description: ID of the book
    
    User:
      type: object
      properties:
        id:
          type: integer
        username:
          type: string
```

---

## 6. Design Workflow (Lab Guide)

Follow these steps to design your API from User Stories:

1.  **Setup (10 mins):** Create `oas3.yaml`. Add `info` (Title, Version) and `servers` (Localhost).
2.  **Identify Resources (15 mins):** Look at your data. Create schemas in the `components` section for your main objects (e.g., User, Book, Lending).
3.  **Define Root Paths (15 mins):** Map your resources to URLs (e.g., `/books`). Add supported methods (`GET`, `POST`). Define the `responses` (200, 201, 404).
4.  **Refine (15 mins):**
    *   Add **Sub-resources** (e.g., `/users/{userId}/books`).
    *   Add **Query Parameters** (e.g., filtering lists).

---

## 7. Versioning
Avoid breaking changes. If you must break compatibility, introduce a new version in the URL.

```yaml
servers:
  - url: http://api.example.com/v1
  - url: http://api.example.com/v2
```

---

## 8. Full Example (Copy-Paste Ready)

```yaml
openapi: 3.0.0
info:
  title: Sample Library API
  version: '1.0'
  description: API for managing users and books.
servers:
  - url: http://localhost:8000/api/v1
paths:
  /users:
    get:
      summary: List users
      parameters:
        - in: query
          name: role
          schema:
            type: string
            enum: [admin, user]
      responses:
        '200':
          description: A JSON array of users
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
  /users/{id}:
    get:
      summary: Get user by ID
      parameters:
        - in: path
          name: id
          schema:
            type: integer
          required: true
      responses:
        '200':
          description: Single User
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        username:
          type: string
        role:
          type: string
```