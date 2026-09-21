import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class TransactionManager {
    private static final double FINE_PER_DAY = 5.0; // Rs 5.00 per day fine

    public static void borrowBook(Scanner scanner) {
        System.out.println("\n--- Borrow a Book ---");
        int memberId = readIntegerInput(scanner, "Enter Member ID: ");
        int bookId = readIntegerInput(scanner, "Enter Book ID: ");

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check member status
            String memberSql = "SELECT status FROM members WHERE id = ?";
            try (PreparedStatement mStmt = conn.prepareStatement(memberSql)) {
                mStmt.setInt(1, memberId);
                try (ResultSet mRs = mStmt.executeQuery()) {
                    if (!mRs.next()) {
                        System.out.println("[ERROR] Member ID not found.");
                        return;
                    }
                    if (!"ACTIVE".equals(mRs.getString("status"))) {
                        System.out.println("[ERROR] Member is not active. Borrowing not allowed.");
                        return;
                    }
                }
            }

            // Check book availability
            String bookSql = "SELECT title, available_copies FROM books WHERE id = ?";
            String bookTitle = "";
            try (PreparedStatement bStmt = conn.prepareStatement(bookSql)) {
                bStmt.setInt(1, bookId);
                try (ResultSet bRs = bStmt.executeQuery()) {
                    if (!bRs.next()) {
                        System.out.println("[ERROR] Book ID not found.");
                        return;
                    }
                    bookTitle = bRs.getString("title");
                    int available = bRs.getInt("available_copies");
                    if (available <= 0) {
                        System.out.println("[ERROR] Book '" + bookTitle + "' is currently out of stock.");
                        return;
                    }
                }
            }

            // Check if member already has this book borrowed
            String checkTx = "SELECT id FROM transactions WHERE member_id = ? AND book_id = ? AND return_date IS NULL";
            try (PreparedStatement txStmt = conn.prepareStatement(checkTx)) {
                txStmt.setInt(1, memberId);
                txStmt.setInt(2, bookId);
                try (ResultSet txRs = txStmt.executeQuery()) {
                    if (txRs.next()) {
                        System.out.println("[ERROR] Member has already borrowed this book and has not returned it yet.");
                        return;
                    }
                }
            }

            // Insert transaction
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(14); // 2-week loan period

            String insertSql = "INSERT INTO transactions (book_id, member_id, borrow_date, due_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, bookId);
                insertStmt.setInt(2, memberId);
                insertStmt.setString(3, today.toString());
                insertStmt.setString(4, dueDate.toString());
                insertStmt.executeUpdate();
            }

            // Decrement copies
            String decSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?";
            try (PreparedStatement decStmt = conn.prepareStatement(decSql)) {
                decStmt.setInt(1, bookId);
                decStmt.executeUpdate();
            }

            System.out.println("[SUCCESS] Book '" + bookTitle + "' borrowed successfully!");
            System.out.println("[INFO] Due Date: " + dueDate);

        } catch (Exception e) {
            System.out.println("[ERROR] Transaction failed: " + e.getMessage());
        }
    }

    public static void returnBook(Scanner scanner) {
        System.out.println("\n--- Return a Book ---");
        int memberId = readIntegerInput(scanner, "Enter Member ID: ");
        int bookId = readIntegerInput(scanner, "Enter Book ID: ");

        String selectTx = "SELECT id, due_date FROM transactions WHERE member_id = ? AND book_id = ? AND return_date IS NULL LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement txStmt = conn.prepareStatement(selectTx)) {
            
            txStmt.setInt(1, memberId);
            txStmt.setInt(2, bookId);

            try (ResultSet rs = txStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[ERROR] No active loan transaction found for this member and book.");
                    return;
                }

                int txId = rs.getInt("id");
                String dueDateStr = rs.getString("due_date");
                
                LocalDate today = LocalDate.now();
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                double fine = 0.0;

                if (today.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                    fine = daysLate * FINE_PER_DAY;
                    System.out.printf("[WARNING] Book is returned late by %d days. Fine amount: Rs %.2f\n", daysLate, fine);
                }

                // Update transaction with return details
                String returnSql = "UPDATE transactions SET return_date = ?, fine_amount = ? WHERE id = ?";
                try (PreparedStatement returnStmt = conn.prepareStatement(returnSql)) {
                    returnStmt.setString(1, today.toString());
                    returnStmt.setDouble(2, fine);
                    returnStmt.setInt(3, txId);
                    returnStmt.executeUpdate();
                }

                // Increment copies
                String incSql = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
                try (PreparedStatement incStmt = conn.prepareStatement(incSql)) {
                    incStmt.setInt(1, bookId);
                    incStmt.executeUpdate();
                }

                System.out.println("[SUCCESS] Book returned successfully.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to return book: " + e.getMessage());
        }
    }

    public static void viewActiveLoans() {
        System.out.println("\n--- Active Borrowed Books ---");
        String sql = "SELECT t.id, b.title, m.name, t.borrow_date, t.due_date " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.id " +
                     "JOIN members m ON t.member_id = m.id " +
                     "WHERE t.return_date IS NULL";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("TxID | Book Title                | Member Name        | Borrow Dt  | Due Date");
            System.out.println("------------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-4d | %-25s | %-18s | %-10s | %-10s\n",
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("name"),
                        rs.getString("borrow_date"),
                        rs.getString("due_date"));
            }
            if (!found) {
                System.out.println("No active loans.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch loans: " + e.getMessage());
        }
    }

    public static void seedSampleData() {
        System.out.println("\nSeeding database with sample books and members...");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if books already exist
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[INFO] Books database is not empty. Seeding skipped.");
                return;
            }

            // Insert Books
            stmt.executeUpdate("INSERT INTO books (title, author, isbn, category, total_copies, available_copies, published_year) " +
                    "VALUES ('Effective Java', 'Joshua Bloch', '978-0134685991', 'Programming', 5, 5, 2018)");
            stmt.executeUpdate("INSERT INTO books (title, author, isbn, category, total_copies, available_copies, published_year) " +
                    "VALUES ('Clean Code', 'Robert C. Martin', '978-0132350884', 'Software Engineering', 3, 3, 2008)");
            stmt.executeUpdate("INSERT INTO books (title, author, isbn, category, total_copies, available_copies, published_year) " +
                    "VALUES ('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'Computer Science', 2, 2, 2009)");

            // Insert Members
            stmt.executeUpdate("INSERT INTO members (name, email, phone) VALUES ('Arun Kumar', 'arun@email.com', '9876543210')");
            stmt.executeUpdate("INSERT INTO members (name, email, phone) VALUES ('Priya Sharma', 'priya@email.com', '8765432109')");

            System.out.println("[SUCCESS] Sample data seeded successfully!");
            System.out.println("Added: 3 Books, 2 Members.");
        } catch (Exception e) {
            System.out.println("[ERROR] Seeding failed: " + e.getMessage());
        }
    }

    // Input Helpers
    private static int readIntegerInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid number format. Enter a valid number.");
            }
        }
    }
}
