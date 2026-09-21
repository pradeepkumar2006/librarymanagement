# 📚 Library Management System

A robust console-based Library Management System written in **Java** with an **SQLite** database backend.

---

## 🚀 Features

- **📖 Book Management**:
  - Add new books with ISBN, title, author, category, published year, and copy counts.
  - View all books and search by title, author, or category.
  - Update book details and delete books.
- **👥 Member Management**:
  - Register new members with validation (name, unique email, phone).
  - Search, update, and manage member statuses (ACTIVE/INACTIVE).
  - Delete member records safely.
- **🔄 Transaction Management**:
  - Issue/borrow books with automatic stock updates and member status checks.
  - Return books with automatic fine calculation for overdue returns ($1/day).
  - View active book loans and pending overdue fines.
- **⚡ Database Initialization & Seeding**:
  - Automatic SQLite table creation (`books`, `members`, `transactions`).
  - One-click sample data seeding for instant demonstration.

---

## 🛠️ Tech Stack & Requirements

- **Language**: Java (JDK 17 or higher recommended, JDK 11+ supported)
- **Database**: SQLite
- **Driver**: SQLite JDBC (`sqlite-jdbc-3.36.0.3.jar`)

---

## 🏃 How to Run

### Windows (Quick Start)
Simply double-click or run the provided batch script in the terminal:
```cmd
run.bat
```
*(The script will automatically compile all `.java` files, download the JDBC driver if missing, and launch the application.)*

### Manual Execution
1. Compile the Java files:
   ```cmd
   javac *.java
   ```
2. Run the application with the SQLite JDBC driver in the classpath:
   ```cmd
   java -cp ".;sqlite-jdbc.jar" LibrarySystem
   ```

---

## 📂 Project Structure

```
LibraryManagement/
├── BookManager.java        # Book catalog and CRUD logic
├── MemberManager.java      # Member registration and profile logic
├── TransactionManager.java # Borrowing, returning, and fine calculation
├── DatabaseConfig.java     # SQLite connection and schema setup
├── LibrarySystem.java      # Main entry point and CLI menu
├── run.bat                 # One-click execution script
├── library.db              # SQLite database file
├── sqlite-jdbc.jar         # SQLite JDBC driver
├── .gitignore              # Ignored files list
└── README.md               # Project documentation
```

---

## 👤 Author
- **Pradeep Kumar** ([@pradeepkumar2006](https://github.com/pradeepkumar2006))
