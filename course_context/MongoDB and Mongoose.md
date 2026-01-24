
**Tags:** #database #nosql #mongodb #mongoose #nodejs #backend
**Source:** Lecture Slides (Marco Robol)

## 1. What is MongoDB?
MongoDB is a **distributed, document-oriented NoSQL database**. Unlike relational databases (SQL) that use tables and rows, MongoDB stores data in **JSON-like documents** (BSON).

### Key Concepts
*   **Database:** Physical container for collections.
*   **Collection:** A group of documents (analogous to a Table in SQL). **No schema is enforced** at the database level.
*   **Document:** A set of key-value pairs (analogous to a Row in SQL). Fields can vary from document to document.

**Example Document:**
```json
{
  "_id": "60d5ec...",
  "name": "Notebook",
  "specs": { "width": 8.5, "height": 11 },
  "tags": ["school", "office"]
}
```

### Accessing MongoDB
1.  **Locally:** Install MongoDB Community Edition.
2.  **Cloud:** [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) (Managed cloud database).
    *   *Connection String:* `mongodb+srv://<user>:<password>@cluster0...`
3.  **UI Tools:**
    *   [MongoDB for VS Code](https://marketplace.visualstudio.com/items?itemName=mongodb.mongodb-vscode) (Extension).
    *   [MongoDB Compass](https://www.mongodb.com/products/compass) (GUI Application).
4.  **CLI:** `mongosh`.

---

## 2. Connecting with Node.js
There are two main ways to connect: the native driver or an ODM.

### A. Native MongoDB Driver (`mongodb`)
The low-level driver. Good for raw performance but requires more manual code.
```javascript
const { MongoClient } = require("mongodb");
const client = new MongoClient("mongodb://localhost:27017/");

async function run() {
    await client.connect();
    const db = client.db("easylib");
    const docs = await db.collection("books").find().toArray();
    console.log(docs);
}
```

### B. Mongoose (`mongoose`) - **Recommended**
An **Object Data Modeling (ODM)** library. It provides a schema-based solution to model application data, enforcing structure at the application level.

**Installation:**
```bash
npm install mongoose
```

---

## 3. Mongoose Workflow

### 1. Connecting
```javascript
const mongoose = require('mongoose');

// Connect to local or cloud instance
mongoose.connect(process.env.DB_URL)
  .then(() => console.log("Connected to DB"))
  .catch(err => console.error(err));
```

### 2. Defining a Schema
Schemas define the structure of your documents, default values, and validators.
```javascript
const { Schema } = mongoose;

const bookSchema = new Schema({
    title: String, // Simple type
    author: { type: String, required: true }, // Validation
    editor: { name: String, address: String }, // Nested object
    tags: [String], // Array of strings
    published: { type: Date, default: Date.now }, // Default value
    meta: { votes: Number, favs: Number }
});
```
> [!NOTE]
> Mongoose automatically adds a unique `_id` property (ObjectId) to every document.

### 3. Creating a Model
The model is the wrapper around the schema that allows you to interface with the database (CRUD operations).
```javascript
const Book = mongoose.model('Book', bookSchema);
// This creates a collection named 'books' (lowercase, pluralized) in MongoDB.
```

### 4. CRUD Operations

**Create:**
```javascript
// Method 1: Instance
const myBook = new Book({ title: 'Mongoose 101', author: 'Marco' });
await myBook.save();

// Method 2: Static create
await Book.create({ title: 'Mongoose 101', author: 'Marco' });
```

**Read (Querying):**
Mongoose queries accept MongoDB query syntax.
```javascript
// Find all
const allBooks = await Book.find().exec();

// Filter
const recentBooks = await Book.find({ year: { $gt: 2023 } }).exec();

// Find One
const specificBook = await Book.findOne({ title: 'Mongoose 101' });

// Find by ID
const bookById = await Book.findById('60d5ec...');
```
> [!INFO] Promises vs. Exec
> Mongoose queries return "Thenables". While `await Book.find()` works, adding `.exec()` gives you a better stack trace if an error occurs.

**Update:**
```javascript
await Book.updateOne({ title: 'Mongoose 101' }, { author: 'New Author' });
```

**Delete:**
```javascript
await Book.deleteOne({ title: 'Old Book' });
```

---

## 4. Advanced Mongoose: Relationships

### Approach 1: Subdocuments (Embedding)
Good for data that belongs strictly to the parent (e.g., comments on a book).
```javascript
const bookSchema = new Schema({
    title: String,
    // Define nested schema structure directly
    reviews: [ { user: String, comment: String } ] 
});
```

### Approach 2: References & Populate (Normalization)
Good for connecting distinct resources (e.g., Books and Authors).

**Define Schema with Reference:**
```javascript
const bookSchema = new Schema({
    title: String,
    // Store the ObjectId and reference the 'User' model name
    author: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }
});
```

**Query with Populate:**
Populate replaces the `ObjectId` with the actual document from the referenced collection (like a SQL JOIN).
```javascript
const book = await Book.findOne({ title: 'Casino Royale' }).populate('author');
console.log(book.author.name); // Access data from the User collection
```

---

## 5. Security & Configuration (`dotenv`)

**Never** hardcode connection strings (passwords) in your code. Use environment variables.

1.  **Install:** `npm install dotenv`
2.  **Create `.env` file:**
    ```env
    DB_URL=mongodb+srv://admin:password@cluster...
    ```
3.  **Usage in Code:**
    ```javascript
    // Option A: Explicit import
    require('dotenv').config();
    mongoose.connect(process.env.DB_URL);
    ```
4.  **Running (Best Practice):**
    Preload dotenv in the run script so you don't need to import it in production code.
    ```json
    // package.json
    "scripts": {
      "dev": "node -r dotenv/config index.js",
      "start": "node index.js"
    }
    ```

---

## 6. Project Structure Example
A typical Express + Mongoose structure (MVC):

*   **`app/models/book.js`**: Defines Schema and exports Model.
    ```javascript
    const mongoose = require('mongoose');
    module.exports = mongoose.model('Book', new mongoose.Schema({ title: String }));
    ```
*   **`app/books.js`** (Routes): Imports Model, handles HTTP requests.
    ```javascript
    const Book = require('./models/book');
    router.get('/', async (req, res) => {
        let books = await Book.find({});
        res.json(books);
    });
    ```
*   **`index.js`**: Connects to DB and starts server.
    ```javascript
    mongoose.connect(process.env.DB_URL).then(() => {
        app.listen(8080);
    });
    ```