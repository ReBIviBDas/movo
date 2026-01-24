
## 1. Architectural Design: Exercise (Facebook Light)
*Concept: Designing a class diagram for a simplified social network.*

### Requirements
*   **Users:** Search by surname, friend requests, status updates, "wall" (visible to friends).
*   **Groups:** Users define groups to restrict status visibility.
*   **Moderation:** Users report inappropriate content; Admins verify, delete content, or suspend users.
*   **Messaging:** Private messages between friends (read, delete, archive, reply).
*   **Logging:** All interactions must be logged.

### Evolution of the Solution
1.  **Draft 1 (Common Pitfalls):**
    *   Missing `Administrator` class.
    *   Missing `main` methods (functionality logic).
    *   Redundant data structures (e.g., `friends` list defined in multiple places).
    *   Double associations causing complexity (User <-> Message).
2.  **Draft 2 (Refined):**
    *   **Separation of Concerns:** `User`, `Status`, `Wall`, `Message`, `Group`, `Administrator`, `Log`.
    *   **Relationships:** Clear 1..* or 0..* associations.
    *   **Logic:** Methods like `search-friends`, `register`, `suspend-user` are placed in appropriate classes.

---

## 2. Testing Theory: Verification & Validation

**Testing** is the process of verifying implemented code through a series of **Test Cases**. It occurs in two phases:
1.  **Developer Testing:** Done by the programmer during creation.
2.  **QA Testing:** Done by other people (QA team/users).

### Methodologies

#### A. Static Analysis
*   Searching for anomalies **without execution**.
*   Tools analyze source code structure.
*   *Note: Covered in "Security Testing" courses.*

#### B. Dynamic Analysis
*   Searching for malfunctions by **executing the code**.
*   **Key Indicators:**
    *   **Quantity:** How many tests are run?
    *   **Coverage:** What percentage of code is executed?

### Dynamic Analysis Approaches

| Approach | Name | Focus | Key Metric |
| :--- | :--- | :--- | :--- |
| **Black Box** | Functional Analysis | Checks if requirements are met without looking at code. | Quantity of tests per requirement. |
| **White Box** | Structural Analysis | Checks internal paths and logic by looking at code. | Code Coverage %. |

> [!TIP] Mindset Shift
> In Development: "How do I implement this?"
> In Testing: "What could generate an error here?" / "What happens if the user does X?"

---

## 3. Designing Test Cases
A test case should have a high probability of finding an error, not be redundant, and be simple enough to evaluate.

### Test Case Strategy
For *every* requirement, you must identify:
1.  At least one case where the requirement is **Respected** (Success path).
2.  At least one case where the requirement is **Not Respected** (Failure path).
3.  At least one case covering **Boundary Situations** (Edge cases).

> [!IMPORTANT] Project Requirement (Document D2)
> For the course project, you must document tests using this specific table structure:

| Test Case # | Description | Test Data | Preconditions | Dependencies | Expected Result | Actual Result | Note |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Create valid account | user: valid, pass: secure | Username unused | - | Account Created (200) | | |
| 1.1 | Create duplicate user | user: existing | Username taken | After Test 1 | Error: Exists (400) | | |
| 2 | Empty username | user: "" | - | - | Error: Empty (400) | | |
| 3 | Weak password | pass: "123" | - | - | Error: Weak Pass (400) | | |

---

## 4. Practical Testing with Jest

**Jest** is a JavaScript Testing Framework focused on simplicity.

### Installation & Setup
```bash
npm install --save-dev jest
```

**Configure `package.json`:**
```json
{
  "scripts": {
    "test": "NODE_OPTIONS=--experimental-vm-modules jest"
  }
}
```
*Note: The `NODE_OPTIONS` flag is often required when testing ES Modules.*

### Basic Syntax
Create a file `sum.test.js`:
```javascript
const sum = require('./sum');

test('adds 1 + 2 to equal 3', () => {
  expect(sum(1, 2)).toBe(3);
});
```

### Common Matchers
*   `toBe(value)`: Exact equality.
*   `toEqual(object)`: Recursive equality (for objects/arrays).
*   `toBeDefined()`, `toBeNull()`.
*   `toContain(item)`: For arrays.

---

## 5. Testing Asynchronous Code & APIs

### Using `node-fetch` (External APIs)
When code runs asynchronously, Jest must wait for it to finish. Use `async/await`.

```javascript
const fetch = require("node-fetch");
const url = "http://localhost:8080/api/v1";

it('GET /books returns 200', async () => {
    expect.assertions(1); // Ensure assertion is actually reached
    const response = await fetch(url + "/books");
    expect(response.status).toEqual(200);
});
```

### Using `supertest` (Internal Express App)
Supertest allows testing Express endpoints without manually starting the server on a port.

**Installation:** `npm install --save-dev supertest`

```javascript
const request = require('supertest');
const app = require('./app'); // Your Express app instance

test('GET / returns 200', () => {
    return request(app)
        .get('/')
        .expect(200);
});
```

### Database Integration in Tests
When testing APIs that use MongoDB/Mongoose, use `beforeAll` and `afterAll` to manage connections.

```javascript
const mongoose = require('mongoose');

beforeAll(async () => {
    // Connect to a test database
    await mongoose.connect(process.env.DB_URL);
});

afterAll(async () => {
    // Close connection to prevent hanging tests
    await mongoose.connection.close();
});

test('POST /booklendings', async () => {
    // ... test logic ...
});
```

---

## 6. Advanced Testing: Mocking

**Mocking** allows you to replace real implementations (like Database calls) with fake functions to test logic in isolation.

**Example: Mocking Mongoose `find`**
```javascript
const Book = require('./models/book');

describe('GET /books', () => {
    let bookSpy;

    beforeAll(() => {
        // Intercept Book.find calls
        bookSpy = jest.spyOn(Book, 'find').mockImplementation(() => {
            return [{ id: 1010, title: 'Jest Mock Book' }];
        });
    });

    afterAll(() => {
        bookSpy.mockRestore(); // Restore original function
    });

    test('should return mocked books', async () => {
        // ... request(app).get ...
        // Assert that the response contains 'Jest Mock Book'
    });
});
```

---

## 7. Code Coverage

Code coverage measures how much of your source code is executed during tests.

**Configuration (`package.json` or `jest.config.js`):**
```json
"jest": {
    "verbose": true,
    "collectCoverage": true
}
```

**Run:** `npm test`
*Output:* Generates a table showing % of Statements, Branches, Functions, and Lines covered.