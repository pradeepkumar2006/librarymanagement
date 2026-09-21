import java.util.Scanner;

public class LibrarySystem {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("          LIBRARY MANAGEMENT SYSTEM              ");
        System.out.println("=================================================");

        // Setup database tables
        DatabaseConfig.setupDatabase();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntegerInput("Enter choice (1-7): ");
            switch (choice) {
                case 1 -> BookManager.bookMenu(scanner);
                case 2 -> MemberManager.memberMenu(scanner);
                case 3 -> TransactionManager.borrowBook(scanner);
                case 4 -> TransactionManager.returnBook(scanner);
                case 5 -> TransactionManager.viewActiveLoans();
                case 6 -> TransactionManager.seedSampleData();
                case 7 -> {
                    System.out.println("\nThank you for using the Library Management System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("[ERROR] Invalid choice. Enter a number between 1 and 7.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Borrow a Book");
        System.out.println("4. Return a Book");
        System.out.println("5. View Active Loans & Fines");
        System.out.println("6. Seed Sample Data");
        System.out.println("7. Exit");
        System.out.println("-----------------");
    }

    private static int readIntegerInput(String prompt) {
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
