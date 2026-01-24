# JavaScript and Node.js Fundamentals
**Tags:** #javascript #nodejs #web-development #software-engineering
**Source:** Lecture Slides (Marco Robol)

## 1. Web 2.0 & Architecture
*   **Web 1.0:** Static HTML, interaction via forms, page reloads.
*   **Web 2.0:** Dynamic, JavaScript-based, Asynchronous HTTP requests (AJAX), DOM manipulation.
*   **Current Architecture:**
    *   **Backend:** Web Services (e.g., Node.js + Express) providing APIs.
    *   **Frontend:** Single Page Applications (SPA) consuming APIs (e.g., Vue.js).
    *   **Communication:** JSON payloads over HTTP.

---

## 2. JavaScript Basics
JavaScript is a lightweight, interpreted, JIT-compiled, multi-paradigm language.

### Types & Variables
*   **Dynamic Typing:** Variables do not have fixed types.
*   **Scopes:**
    *   `var`: Function-scoped, hoisted.
    *   `let`: Block-scoped.
    *   `const`: Block-scoped, immutable reference.
*   **Primitive Types:** Number, String, Boolean, Null, Undefined.
*   **Composite Types:** Objects, Arrays.

> [!WARNING] Hoisting
> Variable declarations (`var`) and function declarations are moved to the top of their scope during compilation.
> ```javascript
> console.log(x); // undefined (not error!)
> var x = 5;
> ```

### Functions
JS treats functions as "first-class citizens."

```javascript
// 1. Function Declaration (Hoisted)
function add(a, b) { return a + b; }

// 2. Function Expression
const mult = function(a, b) { return a * b; };

// 3. Arrow Function (ES6) - No own 'this' context
const mod = (a, b) => a % b;
const square = a => a * a; // Implicit return for single line
```

### Objects & Classes
Evolution of Object-Oriented Programming in JS:

1.  **Object Literal:**
    ```javascript
    const car = {
        model: 'Fiat',
        desc: function() { return this.model; }
    };
    ```
2.  **Constructor Function (Pre-ES6):**
    ```javascript
    function Car(model) {
        this.model = model;
    }
    const myCar = new Car('Fiat');
    ```
3.  **Prototypes:** Mechanism for inheritance before classes.
4.  **ES6 Classes:** Syntactic sugar over prototypes.
    ```javascript
    class Car {
        constructor(model) { this.model = model; }
        drive() { console.log('Vroom'); }
    }
    
    class SUV extends Car {
        drive() { console.log('Big Vroom'); }
    }
    ```

### JSDoc
Standard way to document types in JS comments (improves IDE support).
```javascript
/**
 * @param {string} title
 * @returns {Book}
 */
function createBook(title) { ... }
```

---

## 3. Node.js & Modules

**Node.js** is a server-side platform built on Chrome's V8 Engine. It is **event-driven, single-threaded, and non-blocking**.

### Module Systems
Node supports two module systems. You must know both.

| Feature | CommonJS (CJS) | ES Modules (ESM) |
| :--- | :--- | :--- |
| **Standard** | Node.js Legacy/Default | Modern Web Standard |
| **Import** | `const fs = require('fs');` | `import fs from 'fs';` |
| **Export** | `module.exports = fn;` | `export default fn;` |
| **Loading** | Synchronous | Asynchronous |

### Package Management (NPM)
*   `package.json`: Metadata, scripts, and dependencies.
*   `node_modules`: Folder containing downloaded libraries (never commit this to Git).
*   **Commands:**
    *   `npm init`: Create package.json.
    *   `npm install <pkg>`: Install and save to dependencies.
    *   `npm install`: Install all dependencies listed in package.json.

---

## 4. Asynchronous Programming

Node.js relies on asynchronous code to handle I/O without blocking the single thread.

### 1. Callbacks
Functions passed as arguments to be executed after a task finishes.
```javascript
// Non-blocking
fs.readFile('file.txt', (err, data) => {
    if (err) throw err;
    console.log(data);
});
console.log("This runs BEFORE file is read");
```

### 2. Promises
Objects representing the eventual completion (or failure) of an async operation.
*   **States:** Pending -> Resolved (Success) OR Rejected (Error).
*   **Chaining:** Avoids callback hell.

```javascript
myAsyncFunction()
    .then(data => console.log(data))
    .catch(err => console.error(err))
    .finally(() => console.log("Done"));
```

### 3. Async / Await (ES2017)
Syntactic sugar for Promises that makes async code look synchronous.
*   `async` function returns a Promise.
*   `await` pauses execution until the Promise resolves.

```javascript
async function main() {
    try {
        const result = await myAsyncFunction();
        console.log(result);
    } catch (err) {
        console.error(err);
    }
}
```

---

## 5. The Event Loop (Deep Dive)

The Event Loop is what allows Node.js to perform non-blocking I/O despite being single-threaded. It offloads operations to the system kernel.

> [!INFO] Execution Order Priority
> 1.  **Main Stack** (Synchronous code)
> 2.  **process.nextTick()** (Highest priority microtask)
> 3.  **Microtasks** (Promises/`then`)
> 4.  **Macrotasks** (Timers, I/O, `setImmediate`)

### Queues Hierarchy

1.  **Process.nextTick Queue:**
    *   Processed immediately after the current operation completes, *before* the event loop continues.
    *   *Danger:* Recursive `nextTick` calls will block the event loop (starve I/O).

2.  **Microtask Queue (Promises):**
    *   Processed after `nextTick` but before Macrotasks.

3.  **Macrotask Queue:**
    *   `setTimeout` / `setInterval`
    *   `setImmediate`
    *   I/O Callbacks

### Comparison Example
```javascript
console.log('1. Main');

setTimeout(() => console.log('5. setTimeout'), 0);
setImmediate(() => console.log('6. setImmediate'));

Promise.resolve().then(() => console.log('4. Promise'));

process.nextTick(() => console.log('2. nextTick'));

console.log('3. End Main');

/* Output Order:
1. Main
2. End Main
3. nextTick
4. Promise
5. setTimeout (order vs immediate varies by env/I-O context)
6. setImmediate
*/
```

### Generators
Functions that can be paused and resumed. Used internally by some async libraries.
```javascript
function* generator(i) {
    yield i;
    yield i + 10;
}
const gen = generator(10);
console.log(gen.next().value); // 10
console.log(gen.next().value); // 20
```

---

## 6. Quick Setup Guide

1.  **Install Node:** Download from [nodejs.org](https://nodejs.org).
2.  **Initialize Project:** `npm init -y` inside a folder.
3.  **Install Libraries:** `npm install express`.
4.  **Run Script:** `node app.js`.
5.  **Use VS Code:** Recommended editor with `jsconfig.json` for better type checking.