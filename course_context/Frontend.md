## 1. Fundamentals: HTML, CSS, & JS

### HTML (Structure)
The standard markup language for Web pages.
```html
<!DOCTYPE html>
<html>
<head>
    <title>EasyLib</title>
</head>
<body>
    <h1>EasyLib</h1>
    <ul id="books">
        <li><a href="./api/v1/books/1">Book 1</a></li>
    </ul>
</body>
</html>
```

### CSS (Style)
The language used to style the HTML document.
*   **Selectors:** Element (`body`), Class (`.borded`), ID (`#myId`), Pseudo-classes (`a:hover`).
*   **Flexbox:** A modern layout module for responsive design.
    ```css
    .container {
        display: flex;
        flex-wrap: wrap; /* Items wrap to next line if needed */
    }
    ```
*   **Media Queries:** Apply styles based on viewport size (Responsive Design).
    ```css
    @media only screen and (max-width: 600px) {
        body { background-color: lightblue; }
    }
    ```

### JavaScript (Interactivity)
*   **DOM Manipulation:** Access and modify HTML elements.
    ```javascript
    document.getElementById("demo").innerHTML = "Hello";
    ```
*   **AJAX & Fetch API:** Communicate with servers asynchronously.
    ```javascript
    fetch('../api/v1/books')
        .then(resp => resp.json())
        .then(data => console.log(data));
    ```

> [!WARNING] CORS Policy
> Browsers block requests to a different domain/port by default (e.g., frontend on port 8080 accessing backend on port 3000). You must enable CORS on the backend.
> `npm install cors` -> `app.use(cors())`

---

## 2. CSS Frameworks

### Tailwind CSS
A utility-first CSS framework. Instead of writing custom CSS classes, you use pre-defined utility classes directly in your HTML.
*   *Example:* `class="text-3xl font-bold hover:text-red-500"`

### DaisyUI
A component library for Tailwind CSS. It provides higher-level components like Buttons, Navbars, and Modals.
*   *Example:* `class="btn btn-primary"`

---

## 3. Vue.js Framework

Vue is a JavaScript framework for building user interfaces based on **declarative rendering** and **components**.

### Core Concepts
1.  **Declarative Rendering:** Describe *what* the UI should look like based on state, not *how* to change it.
2.  **Reactivity:** Vue automatically tracks JavaScript state changes and updates the DOM.

### Basic Syntax (Composition API)
```html
<script setup>
import { ref } from 'vue'

const count = ref(0) // Reactive state
const message = ref('Hello Vue!')

function increment() {
  count.value++
}
</script>

<template>
  <h1>{{ message }}</h1>
  <button @click="increment">Count is: {{ count }}</button>
</template>
```

### Directives
*   **`v-bind` (or `:`):** Bind an attribute to a variable.
    *   `<div :class="myClass"></div>`
*   **`v-on` (or `@`):** Listen for events.
    *   `<button @click="doSomething"></button>`
*   **`v-model`:** Two-way data binding (perfect for forms).
    *   `<input v-model="text">`
*   **`v-if` / `v-else`:** Conditional rendering.
*   **`v-for`:** Loop rendering.
    *   `<li v-for="item in list" :key="item.id">{{ item.text }}</li>`

### Components
Vue apps are trees of nested components.
*   **Props:** Pass data **down** from parent to child.
    *   `defineProps({ msg: String })`
*   **Emits:** Send events **up** from child to parent.
    *   `const emit = defineEmits(['response'])` -> `emit('response', data)`

---

## 4. Building a Vue Application (EasyLib)

### Setup with Vite
```bash
npm init vue@latest
cd my-project
npm install
npm run dev
```

### Application Structure
*   **`App.vue`**: The root component.
*   **`router/index.js`**: Handles client-side navigation (Routes to Views).
*   **`views/`**: Page-level components (e.g., `HomeView.vue`, `BooksView.vue`).
*   **`components/`**: Reusable UI parts (e.g., `BookTable.vue`, `Login.vue`).
*   **`states/`**: Shared state management (simple store pattern).

### Global State Management (Simple Store)
Instead of complex libraries like Pinia (for now), you can use Vue's `reactive()` to create a shared state file.

**`src/states/loggedUser.js`**
```javascript
import { reactive } from 'vue'

export const loggedUser = reactive({
    token: undefined,
    email: undefined
})

export function setLoggedUser(data) {
    loggedUser.token = data.token;
    loggedUser.email = data.email;
}
```

### Fetching Data in Components
Use Lifecycle Hooks like `onMounted` to fetch data when a component loads.

```javascript
import { onMounted, ref } from 'vue';

const books = ref([]);

onMounted(async () => {
    const response = await fetch('http://localhost:3000/api/v1/books');
    books.value = await response.json();
});
```

---

## 5. Deployment & Integration

### Building for Production
```bash
npm run build
```
This compiles your Vue app into static files (HTML, CSS, JS) in the `dist/` folder.

### Serving from Backend (Node.js)
You can serve your frontend directly from your Express backend.

**`app.js`**
```javascript
const path = require('path');

// Serve static files from the Vue build folder
app.use(express.static(path.join(__dirname, '../EasyLibVue/dist')));

// Catch-all route to handle History Mode routing in Vue
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../EasyLibVue/dist/index.html'));
});
```

---

## 6. Storage: Cookies vs. LocalStorage

| Feature | LocalStorage / SessionStorage | Cookies |
| :--- | :--- | :--- |
| **Capacity** | ~5MB | ~4KB |
| **Transmission** | **Not sent** with HTTP requests | **Sent** with every HTTP request |
| **Usage** | Storing UI state, JWT tokens (sometimes) | Auth tokens, Server-side sessions |
| **Persistence** | Local: Forever, Session: Until tab close | Defined by expiration date |

**Usage Example:**
```javascript
// Save
localStorage.setItem('token', 'eyJhbGci...');

// Read
const token = localStorage.getItem('token');
```