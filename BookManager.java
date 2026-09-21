import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class BookManager {

    public static void bookMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- BOOK MANAGEMENT ---");
            System.out.println("1. Add a Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Books");
            System.out.println("4. Update a Book");
            System.out.println("5. Delete a Book");
            System.out.println("6. Back to Main Menu");

            int choice = readIntegerInput(scanner, "Enter choice: ");
            if (choice == 6) break;

            switch (choice) {
                case 1 -> addBook(scanner);
                case 2 -> viewAllBooks();
                case 3 -> searchBooks(scanner);
                case 4 -> updateBook(scanner);
                case 5 -> deleteBook(scanner);
                default -> System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void addBook(Scanner scanner) {
        System.out.println("\n--- Add a New Book ---");
        String title = readStringInput(scanner, "Enter Title: ");
        String author = readStringInput(scanner, "Enter Author: ");
        String isbn = readStringInput(scanner, "Enter ISBN: ");
        String category = readStringInput(scanner, "Enter Category: ");
        int totalCopies = readIntegerInput(scanner, "Enter Total Copies: ");
        int year = readIntegerInput(scanner, "Enter Published Year: ");

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            System.out.println("[ERROR] Title, Author, and ISBN are required fields.");
            return;
        }

        String sql = "INSERT INTO books (title, author, isbn, category, total_copies, available_copies, published_year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.setString(4, category);
            pstmt.setInt(5, totalCopies);
            pstmt.setInt(6, totalCopies);
            pstmt.setInt(7, year);

            pstmt.executeUpdate();
            System.out.println("[SUCCESS] Book registered successfully!");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to add book: " + e.getMessage());
        }
    }

    public static void viewAllBooks() {
        System.out.println("\n--- All Books ---");
        String sql = "SELECT * FROM books";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("ID  | Title                     | Author              | ISBN          | Stock  | Year");
            System.out.println("--------------------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-3d | %-25s | %-19s | %-13s | %d/%-4d | %d\n",
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available_copies"),
                        rs.getInt("total_copies"),
                        rs.getInt("published_year"));
            }
            if (!found) {
                System.out.println("No books in database.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to list books: " + e.getMessage());
        }
    }

    private static void searchBooks(Scanner scanner) {
        System.out.println("\n--- Search Books ---");
        String query = readStringInput(scanner, "Enter title, author, or category keyword: ");
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("ID  | Title                     | Author              | ISBN          | Stock  | Year");
                System.out.println("--------------------------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-3d | %-25s | %-19s | %-13s | %d/%-4d | %d\n",
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getInt("available_copies"),
                            rs.getInt("total_copies"),
                            rs.getInt("published_year"));
                }
                if (!found) {
                    System.out.println("No matching books found.");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to search: " + e.getMessage());
        }
    }

    private static void updateBook(Scanner scanner) {
        System.out.println("\n--- Update a Book ---");
        int id = readIntegerInput(scanner, "Enter Book ID to update: ");

        String selectSql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setInt(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[ERROR] Book ID not found.");
                    return;
                }

                String currentTitle = rs.getString("title");
                String currentAuthor = rs.getString("author");
                String currentIsbn = rs.getString("isbn");
                String currentCategory = rs.getString("category");
                int currentTotal = rs.getInt("total_copies");
                int currentAvailable = rs.getInt("available_copies");
                int currentYear = rs.getInt("published_year");

                System.out.println("Current Title: " + currentTitle);
                String title = readStringInput(scanner, "New Title (Press Enter to keep current): ");
                if (title.isEmpty()) title = currentTitle;

                System.out.println("Current Author: " + currentAuthor);
                String author = readStringInput(scanner, "New Author (Press Enter to keep current): ");
                if (author.isEmpty()) author = currentAuthor;

                System.out.println("Current ISBN: " + currentIsbn);
                String isbn = readStringInput(scanner, "New ISBN (Press Enter to keep current): ");
                if (isbn.isEmpty()) isbn = currentIsbn;

                System.out.println("Current Category: " + currentCategory);
                String category = readStringInput(scanner, "New Category (Press Enter to keep current): ");
                if (category.isEmpty()) category = currentCategory;

                System.out.println("Current Total Copies: " + currentTotal);
                String totalInput = readStringInput(scanner, "New Total Copies (Press Enter to keep current): ");
                int totalCopies = currentTotal;
                int availableCopies = currentAvailable;
                if (!totalInput.isEmpty()) {
                    totalCopies = Integer.parseInt(totalInput);
                    int diff = totalCopies - currentTotal;
                    availableCopies = Math.max(0, currentAvailable + diff);
                }

                System.out.println("Current Year: " + currentYear);
                String yearInput = readStringInput(scanner, "New Published Year (Press Enter to keep current): ");
                int year = currentYear;
                if (!yearInput.isEmpty()) {
                    year = Integer.parseInt(yearInput);
                }

                String updateSql = "UPDATE books SET title = ?, author = ?, isbn = ?, category = ?, total_copies = ?, available_copies = ?, published_year = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, title);
                    updateStmt.setString(2, author);
                    updateStmt.setString(3, isbn);
                    updateStmt.setString(4, category);
                    updateStmt.setInt(5, totalCopies);
                    updateStmt.setInt(6, availableCopies);
                    updateStmt.setInt(7, year);
                    updateStmt.setInt(8, id);

                    updateStmt.executeUpdate();
                    System.out.println("[SUCCESS] Book updated successfully!");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update book: " + e.getMessage());
        }
    }

    private static void deleteBook(Scanner scanner) {
        System.out.println("\n--- Delete a Book ---");
        int id = readIntegerInput(scanner, "Enter Book ID to delete: ");

        String confirm = readStringInput(scanner, "Are you sure? (yes/no): ");
        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println("[INFO] Delete cancelled.");
            return;
        }

        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("[SUCCESS] Book deleted successfully!");
            } else {
                System.out.println("[ERROR] Book ID not found.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to delete book (It might be currently borrowed): " + e.getMessage());
        }
    }

    // Input Helpers
    private static String readStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

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
