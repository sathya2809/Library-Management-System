import java.util.*;
import java.text.*;

class User {
    String email, password, role;
    double securityDeposit = 1500.0;
    List<BorrowedBook> borrowedBooks = new ArrayList<>();
    List<String> fineHistory = new ArrayList<>();
    int tenureExtensions = 0;

    User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}

class Book {
    String isbn, name;
    int quantity;
    double price;
    int borrowedCount = 0;

    Book(String isbn, String name, int quantity, double price) {
        this.isbn = isbn;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
}

class BorrowedBook {
    String isbn;
    String name;
    Date borrowedDate;
    Date returnDate;
    boolean isLost = false;

    BorrowedBook(String isbn, String name, Date borrowedDate, Date returnDate) {
        this.isbn = isbn;
        this.name = name;
        this.borrowedDate = borrowedDate;
        this.returnDate = returnDate;
    }
}

public class LibraryManagementSystem {
    static Scanner sc = new Scanner(System.in);
    static Map<String, User> users = new HashMap<>();
    static Map<String, Book> books = new HashMap<>();
    static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    static User loggedInUser;

    public static void main(String[] args) {
        seedData();
        authenticate();
    }

    static void seedData() {
        users.put("admin@lib.com", new User("admin@lib.com", "admin123", "admin"));
        users.put("user@lib.com", new User("user@lib.com", "user123", "borrower"));

        books.put("ISBN001", new Book("ISBN001", "Java Basics", 5, 500));
        books.put("ISBN002", new Book("ISBN002", "Python Pro", 3, 600));
        books.put("ISBN003", new Book("ISBN003", "DBMS", 2, 400));
    }

    static void authenticate() {
        System.out.println("===== LIBRARY LOGIN =====");
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (users.containsKey(email) && users.get(email).password.equals(pass)) {
            loggedInUser = users.get(email);
            if (loggedInUser.role.equals("admin")) {
                adminMenu();
            } else {
                borrowerMenu();
            }
        } else {
            System.out.println("Invalid credentials. Try again.");
            authenticate();
        }
    }

    static void adminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add Book\n2. Modify Book\n3. Delete Book\n4. Add User\n5. View Books\n6. Search Book\n7. Manage Fine Limit\n8. View Reports\n9. Logout");
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1 -> addBook();
                case 2 -> modifyBook();
                case 3 -> deleteBook();
                case 4 -> addUser();
                case 5 -> viewBooks();
                case 6 -> searchBook();
                case 7 -> System.out.println("Fine limit managed manually.");
                case 8 -> viewReports();
                case 9 -> { System.out.println("Logging out..."); authenticate(); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static void borrowerMenu() {
        while (true) {
            System.out.println("\n--- BORROWER MENU ---");
            System.out.println("1. View Books\n2. Borrow Book\n3. View Fines\n4. Extend Tenure\n5. Mark Book Lost\n6. Lost Membership Card\n7. Logout");
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1 -> viewBooks();
                case 2 -> borrowBook();
                case 3 -> viewFines();
                case 4 -> extendTenure();
                case 5 -> markBookLost();
                case 6 -> lostCard();
                case 7 -> { System.out.println("Logging out..."); authenticate(); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // -------- Admin Methods --------

    static void addBook() {
        System.out.print("Enter ISBN: ");
        String isbn = sc.nextLine();
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Quantity: ");
        int qty = sc.nextInt();
        System.out.print("Price: ");
        double price = sc.nextDouble(); sc.nextLine();
        books.put(isbn, new Book(isbn, name, qty, price));
        System.out.println("Book added.");
    }

    static void modifyBook() {
        System.out.print("Enter ISBN to modify: ");
        String isbn = sc.nextLine();
        if (books.containsKey(isbn)) {
            System.out.print("New Quantity: ");
            books.get(isbn).quantity = sc.nextInt(); sc.nextLine();
            System.out.println("Updated.");
        } else {
            System.out.println("Book not found.");
        }
    }

    static void deleteBook() {
        System.out.print("Enter ISBN to delete: ");
        String isbn = sc.nextLine();
        books.remove(isbn);
        System.out.println("Book deleted.");
    }

    static void addUser() {
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();
        System.out.print("Role (admin/borrower): ");
        String role = sc.nextLine();
        users.put(email, new User(email, pass, role));
        System.out.println("User added.");
    }

    static void viewBooks() {
        books.values().stream().sorted(Comparator.comparing(b -> b.name)).forEach(b ->
            System.out.println(b.name + " | ISBN: " + b.isbn + " | Qty: " + b.quantity)
        );
    }

    static void searchBook() {
        System.out.print("Search by (name/isbn): ");
        String type = sc.nextLine();
        System.out.print("Enter query: ");
        String query = sc.nextLine();

        books.values().stream().filter(b ->
            type.equals("name") ? b.name.equalsIgnoreCase(query) : b.isbn.equalsIgnoreCase(query)
        ).forEach(b -> System.out.println("Found: " + b.name + " | ISBN: " + b.isbn));
    }

    static void viewReports() {
        System.out.println("Books with low quantity:");
        books.values().stream().filter(b -> b.quantity < 2).forEach(b -> System.out.println(b.name));

        System.out.println("\nBooks not borrowed yet:");
        books.values().stream().filter(b -> b.borrowedCount == 0).forEach(b -> System.out.println(b.name));

        System.out.println("\nBooks heavily borrowed:");
        books.values().stream().filter(b -> b.borrowedCount > 5).forEach(b -> System.out.println(b.name));
    }

    // -------- Borrower Methods --------

    static void borrowBook() {
        if (loggedInUser.borrowedBooks.size() >= 3) {
            System.out.println("Max 3 books allowed.");
            return;
        }

        System.out.print("Enter ISBN: ");
        String isbn = sc.nextLine();

        if (!books.containsKey(isbn)) {
            System.out.println("Book not found.");
            return;
        }

        Book book = books.get(isbn);
        if (book.quantity <= 0) {
            System.out.println("Not in stock.");
            return;
        }

        if (loggedInUser.borrowedBooks.stream().anyMatch(bb -> bb.isbn.equals(isbn))) {
            System.out.println("Book already borrowed.");
            return;
        }

        if (loggedInUser.securityDeposit < 500) {
            System.out.println("Insufficient deposit.");
            return;
        }

        try {
            System.out.print("Enter expected return date (DD/MM/YYYY): ");
            Date returnDate = sdf.parse(sc.nextLine());
            BorrowedBook bb = new BorrowedBook(isbn, book.name, new Date(), returnDate);
            loggedInUser.borrowedBooks.add(bb);
            book.quantity--;
            book.borrowedCount++;
            System.out.println("Book borrowed.");
        } catch (Exception e) {
            System.out.println("Invalid date.");
        }
    }

    static void viewFines() {
        if (loggedInUser.fineHistory.isEmpty()) {
            System.out.println("No fines.");
        } else {
            loggedInUser.fineHistory.forEach(System.out::println);
        }
    }

    static void extendTenure() {
        if (loggedInUser.tenureExtensions >= 2) {
            System.out.println("Max extensions reached.");
            return;
        }
        System.out.print("Enter ISBN to extend: ");
        String isbn = sc.nextLine();
        for (BorrowedBook bb : loggedInUser.borrowedBooks) {
            if (bb.isbn.equals(isbn)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(bb.returnDate);
                cal.add(Calendar.DATE, 15);
                bb.returnDate = cal.getTime();
                loggedInUser.tenureExtensions++;
                System.out.println("Extended by 15 days.");
                return;
            }
        }
        System.out.println("Book not found.");
    }

    static void markBookLost() {
        System.out.print("Enter ISBN to mark as lost: ");
        String isbn = sc.nextLine();
        for (BorrowedBook bb : loggedInUser.borrowedBooks) {
            if (bb.isbn.equals(isbn)) {
                double fine = books.get(isbn).price * 0.5;
                loggedInUser.securityDeposit -= fine;
                loggedInUser.fineHistory.add("Book lost: " + bb.name + ", Fine: ₹" + fine);
                loggedInUser.borrowedBooks.remove(bb);
                System.out.println("Book marked lost. Fine deducted.");
                return;
            }
        }
        System.out.println("Book not found.");
    }

    static void lostCard() {
        loggedInUser.securityDeposit -= 10;
        loggedInUser.fineHistory.add("Membership card lost. ₹10 deducted.");
        System.out.println("Fine for card loss deducted.");
    }
}
