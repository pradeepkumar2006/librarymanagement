import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class MemberManager {

    public static void memberMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- MEMBER MANAGEMENT ---");
            System.out.println("1. Register a Member");
            System.out.println("2. View All Members");
            System.out.println("3. Search Members");
            System.out.println("4. Update Member Details");
            System.out.println("5. Delete a Member");
            System.out.println("6. Back to Main Menu");

            int choice = readIntegerInput(scanner, "Enter choice: ");
            if (choice == 6) break;

            switch (choice) {
                case 1 -> registerMember(scanner);
                case 2 -> viewAllMembers();
                case 3 -> searchMembers(scanner);
                case 4 -> updateMember(scanner);
                case 5 -> deleteMember(scanner);
                default -> System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void registerMember(Scanner scanner) {
        System.out.println("\n--- Register a Member ---");
        String name = readStringInput(scanner, "Enter Member Name: ");
        String email = readStringInput(scanner, "Enter Email: ");
        String phone = readStringInput(scanner, "Enter Phone Number: ");

        if (name.isEmpty()) {
            System.out.println("[ERROR] Member name cannot be empty.");
            return;
        }

        String sql = "INSERT INTO members (name, email, phone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);

            pstmt.executeUpdate();
            System.out.println("[SUCCESS] Member registered successfully!");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to register member: " + e.getMessage());
        }
    }

    public static void viewAllMembers() {
        System.out.println("\n--- All Members ---");
        String sql = "SELECT * FROM members";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("ID  | Name                 | Email                    | Phone        | Status");
            System.out.println("-----------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-3d | %-20s | %-24s | %-12s | %s\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status"));
            }
            if (!found) {
                System.out.println("No members registered.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to list members: " + e.getMessage());
        }
    }

    private static void searchMembers(Scanner scanner) {
        System.out.println("\n--- Search Members ---");
        String query = readStringInput(scanner, "Enter member name or email keyword: ");
        String sql = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("ID  | Name                 | Email                    | Phone        | Status");
                System.out.println("-----------------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-3d | %-20s | %-24s | %-12s | %s\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("status"));
                }
                if (!found) {
                    System.out.println("No matching members found.");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to search: " + e.getMessage());
        }
    }

    private static void updateMember(Scanner scanner) {
        System.out.println("\n--- Update Member ---");
        int id = readIntegerInput(scanner, "Enter Member ID to update: ");

        String selectSql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setInt(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[ERROR] Member ID not found.");
                    return;
                }

                String currentName = rs.getString("name");
                String currentEmail = rs.getString("email");
                String currentPhone = rs.getString("phone");
                String currentStatus = rs.getString("status");

                System.out.println("Current Name: " + currentName);
                String name = readStringInput(scanner, "New Name (Press Enter to keep current): ");
                if (name.isEmpty()) name = currentName;

                System.out.println("Current Email: " + currentEmail);
                String email = readStringInput(scanner, "New Email (Press Enter to keep current): ");
                if (email.isEmpty()) email = currentEmail;

                System.out.println("Current Phone: " + currentPhone);
                String phone = readStringInput(scanner, "New Phone (Press Enter to keep current): ");
                if (phone.isEmpty()) phone = currentPhone;

                System.out.println("Current Status: " + currentStatus);
                String status = readStringInput(scanner, "New Status (ACTIVE/SUSPENDED) (Press Enter to keep current): ");
                if (status.isEmpty()) status = currentStatus;

                String updateSql = "UPDATE members SET name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, name);
                    updateStmt.setString(2, email);
                    updateStmt.setString(3, phone);
                    updateStmt.setString(4, status.toUpperCase());
                    updateStmt.setInt(5, id);

                    updateStmt.executeUpdate();
                    System.out.println("[SUCCESS] Member details updated successfully!");
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update member: " + e.getMessage());
        }
    }

    private static void deleteMember(Scanner scanner) {
        System.out.println("\n--- Delete a Member ---");
        int id = readIntegerInput(scanner, "Enter Member ID to delete: ");

        String confirm = readStringInput(scanner, "Are you sure? (yes/no): ");
        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println("[INFO] Delete cancelled.");
            return;
        }

        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("[SUCCESS] Member deleted successfully!");
            } else {
                System.out.println("[ERROR] Member ID not found.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to delete member (They may have active transactions): " + e.getMessage());
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
