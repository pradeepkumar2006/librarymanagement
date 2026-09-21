import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:sqlite:library.db";

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    public static void setupDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Books table
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "author TEXT NOT NULL, " +
                    "isbn TEXT UNIQUE NOT NULL, " +
                    "category TEXT, " +
                    "total_copies INTEGER NOT NULL, " +
                    "available_copies INTEGER NOT NULL, " +
                    "published_year INTEGER" +
                    ");");

            // Members table
            stmt.execute("CREATE TABLE IF NOT EXISTS members (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE, " +
                    "phone TEXT, " +
                    "status TEXT DEFAULT 'ACTIVE'" +
                    ");");

            // Transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "book_id INTEGER NOT NULL, " +
                    "member_id INTEGER NOT NULL, " +
                    "borrow_date TEXT NOT NULL, " +
                    "due_date TEXT NOT NULL, " +
                    "return_date TEXT, " +
                    "fine_amount REAL DEFAULT 0.0, " +
                    "FOREIGN KEY(book_id) REFERENCES books(id), " +
                    "FOREIGN KEY(member_id) REFERENCES members(id)" +
                    ");");

            System.out.println("[INFO] SQLite Database initialized successfully.");
        } catch (Exception e) {
            System.out.println("[ERROR] Database initialization failed: " + e.getMessage());
        }
    }
}
