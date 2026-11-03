# CineBook — KTU S3 CSE OOP (PBCST304, 2024 Scheme) Comprehensive Syllabus Mapping

This document maps **every concept** from the **KTU B.Tech S3 Computer Science & Engineering (2024 Scheme) Object Oriented Programming (PBCST304)** syllabus directly to its concrete implementation in the CineBook codebase.

Use this guide during your project presentation, lab evaluation, and viva voce defense.

---

## Quick Reference: Syllabus Coverage Summary

```
KTU Syllabus Module 1: Java Basics & Flow Control       ---> 100% Covered
KTU Syllabus Module 2: Core OOP, Inheritance & Polymorphism ---> 100% Covered
KTU Syllabus Module 3: Interfaces, Exceptions & Patterns    ---> 100% Covered
KTU Syllabus Module 4: Swing GUI & Event-Driven Architecture ---> 100% Covered
KTU Syllabus Module 5: Database Applications using JDBC     ---> 100% Covered
PBL Project Requirements: In-Memory & SQLite Persistence     ---> 100% Covered
```

---

# MODULE 1: Introduction to Java (10 Contact Hours)

| Syllabus Topic | Specific CineBook Implementation & Code Location | Practical Demonstration & Viva Explanation |
|---|---|---|
| **Structure of a Simple Java Program** | `CineBookApp.java` (Line 1 to 90)<br>`public class CineBookApp` with `public static void main(String[] args)` | Standard Java entry point containing system bootstrapping, look-and-feel configuration, and UI invocation via `SwingUtilities.invokeLater`. |
| **Command Line Environment & Compiler** | Terminal execution using standard JDK tools: `javac CineBookApp.java` and `java CineBookApp` | No external dependencies (no Maven, Gradle, or third-party JARs). Uses standard `javac` dependency discovery. |
| **Java Virtual Machine (JVM)** | Runtime execution on JVM (verified on HotSpot JVM 17+ / 26+) | Demonstrates bytecode execution, classpath resolution, garbage collection of short-lived GUI components, and runtime memory model. |
| **Primitive Data Types & Wrapper Types** | `int`, `double`, `boolean`, `char` vs `Integer`, `Double`, `Boolean` in `PriceCalculator`, `Seat`, `Show`, `Booking`, `PaymentTransaction` | Uses primitives for performance in coordinate loops (`int r, c`) and calculation math (`double basePrice`), while using wrappers in Java Collections (`JComboBox<String>`, `List<String>`). |
| **Casting & Autoboxing** | `Integer.parseInt(durField.getText().trim())` in `AdminPanel.java`<br>`(double) amount`<br>`Character` autoboxing | Parsing text fields into numbers (`String` to `int`/`double`) and autoboxing integers into generic Collections. |
| **Arrays (Single & 2D Arrays)** | `Seat[][] seats` in `Show.java`<br>`Seat[] row` in `SeatManager.java`<br>`String[] args` in `CineBookApp.java`<br>`String[] columns` in `AdminPanel.java` | Theater seating grid is modeled as a 2-dimensional array `Seat[screen.getRows()][screen.getColumns()]`, demonstrating nested matrix traversal (`seats[r][c]`). |
| **Strings & Text Processing** | `String.format(...)`, `substring()`, `toLowerCase()`, `contains()`, `String.join(", ", seatIds)` across `Movie.java`, `Booking.java`, `CheckoutPanel.java` | Immutable text operations for ticket formatting, keyword search filtering (`matches()`), and generating prefixed identifiers (`"BKG-" + UUID`). |
| **Operators** | - **Arithmetic:** `+`, `-`, `*`, `/`, `%` in `PriceCalculator`, `countdownTimer` (`mins = remainingSeconds / 60`, `secs = remainingSeconds % 60`)<br>- **Relational:** `==`, `!=`, `<`, `>`, `<=`, `>=` in `isCancellable()`<br>- **Boolean Logical:** `&&`, `||`, `!` in `Seat.isAvailable()` and `matches()`<br>- **Ternary:** `(b.getBookingStatus() == BookingStatus.CONFIRMED) ? b.getSeatIds().size() : 0` in `BookingManager.java` | Used extensively in pricing calculations, time comparison logic, search matching, and conditional rendering. |
| **Operator Precedence** | Math and logic expressions: `subtotal * 0.18 * 100.0 / 100.0`<br>`remainingSeconds / 60` vs `% 60` | Demonstrates order of operations in currency rounding and time formatting. |
| **Control Statements — Selection** | `if`, `else if`, `else`, `switch` statements across `SeatSelectionPanel.java`, `CheckoutPanel.java`, `BookingService.java` | Determines tier multiplier (A/B = Standard, C/D = Premium, E = VIP), payment channel selection, and permission checks. |
| **Control Statements — Iteration** | - Enhanced `for (Movie m : list)`<br>- Indexed `for (int r = 0; r < rows; r++)`<br>- `while` countdown timers | Nested 2D iteration in `Show.initializeSeats()` for populating rows and columns of seats. |
| **Control Statements — Jump** | `break`, `return` in `MovieService.updateMovie()` and search loops | Terminates search upon identifying matching records; early return guards for validation. |
| **Functions / Methods** | Accessor, mutator, and business methods across all classes | Pure functions for price calculation (`calculateGrandTotal()`), state mutators (`confirmBooking()`), and predicates (`isAvailable()`). |
| **Command-Line Arguments** | `main(String[] args)` in `CineBookApp.java`<br>Flag inspection: `args[0].equalsIgnoreCase("--test-only")` | Allows command-line headless automation testing without launching the GUI (`java CineBookApp --test-only`). |
| **Variable-Length Arguments (Varargs)** | `Arrays.asList("B1", "B2")`<br>`String.format(String format, Object... args)` in `CheckoutPanel.java` | Used for formatted currency printing and ticket text rendering. |
| **Naming Conventions** | CamelCase for classes (`BookingService`), lowerCamelCase for methods/variables (`calculateSeatPrice`), UPPER_SNAKE_CASE for constants (`COLOR_PRIMARY`, `DATE_TIME_FORMATTER`) | Follows official Oracle Java Code Conventions across all 45 source files. |

---

# MODULE 2: Object-Oriented Programming in Java (8 Contact Hours)

| Syllabus Topic | Specific CineBook Implementation & Code Location | Practical Demonstration & Viva Explanation |
|---|---|---|
| **Data Abstraction** | `abstract class User`, `abstract class PaymentMethod`, `interface Cancellable`, `interface Searchable<T>` | Details of payment execution or cancellation rules are abstracted behind high-level interfaces without exposing implementation details to callers. |
| **Encapsulation** | `private final` fields in `Movie.java`, `Seat.java`, `Booking.java`, `PaymentTransaction.java` with getters and business validation gates | Data fields cannot be modified arbitrarily from outside; state transitions occur only via verified methods (`seat.reserve()`, `seat.confirmBooking()`). |
| **Inheritance (Hierarchies)** | `User` $\rightarrow$ `Customer`, `AdminUser`<br>`PaymentMethod` $\rightarrow$ `MockCardPayment`, `MockUpiPayment`, `MockWalletPayment`<br>`CineBookException` $\rightarrow$ Subclasses | Common properties (`id`, `name`, `email`) and methods (`getMethodName()`) are inherited, eliminating redundant code. |
| **Polymorphism** | - **Runtime Dynamic Dispatch:** `paymentMethod.executeTransaction(amount)` in `PaymentProcessor.java`<br>- **Compile-time Overloading:** `Movie.matches(query)` vs `Movie.matches(title, genre)` | The same call invokes different behavior depending on the runtime instance (Credit Card vs UPI vs Wallet). |
| **Declaring Objects & Object References** | `BookingManager mgr = BookingManager.getInstance();`<br>`Show show = movieShows.get(0);` | Objects instantiated on the Java heap, referenced and passed between controllers and UI views. |
| **Constructors (Default, Parameterized, Overloaded)** | - Parameterized: `Show(id, movie, screen, showDateTime, basePrice)`<br>- Overloaded: `Movie(id, title, genre, dur)` vs `Movie(id, title, genre, dur, lang, cert, desc)` | Overloaded constructors allow creating quick movie entities with sensible defaults (`"English"`, `"PG-13"`). |
| **Access Modifiers** | `public`, `protected`, `private`, package-private across all classes | Demonstrates encapsulation boundary design: sensitive fields are `private`, services are `public`. |
| **The `this` Keyword** | `this.id = id;`, `this.manager = manager;` in all model constructors<br>`this(id, title, genre, durationMinutes, "English", "PG-13", ...)` in `Movie.java` | Resolves ambiguity between method parameters and instance attributes; invokes constructor chaining. |
| **Using Objects as Parameters** | `createPendingReservation(Customer customer, Show show, List<String> seatIds)` in `BookingService.java` | Complex domain objects passed as arguments across service boundaries. |
| **Returning Objects** | `Show.findSeat(seatId)` returns `Seat`<br>`PaymentProcessor.processSimulatedPayment(...)` returns `PaymentTransaction` | Factory and lookup methods construct and return fully initialized domain entities. |
| **Recursion** | `BookingManager.countTotalBookedSeatsRecursive(List<Booking> list, int index)` | **Algorithmic Recursion Demonstration:**<br>- **Base Case:** `if (list == null || index >= list.size()) return 0;`<br>- **Recursive Step:** `count + countTotalBookedSeatsRecursive(list, index + 1);`<br>Calculates cumulative booked seats across all transactions. |
| **Static Members** | - Static methods: `BookingManager.getInstance()`, `CineBookApp.runAutomatedCoreVerification()`<br>- Static variables: `instance` in `BookingManager`, all constants in `UIConstants.java` | Shared application-wide data and helper logic that do not require object instantiation. |
| **Final Variables & Immutability** | `private final String id;` in `Movie.java`, `Seat.java`, `Booking.java`<br>`public static final Color COLOR_PRIMARY` in `UIConstants.java` | Enforces immutability for domain identity keys and design constants. |
| **The `super` Keyword** | `super(id, name, email, "Customer");` in `Customer.java`<br>`super("Mock Credit/Debit Card");` in `MockCardPayment.java`<br>`super(message);` in `SeatUnavailableException.java` | Invokes the parent class constructor and initializes inherited fields in subclasses. |
| **Method Overriding (`@Override`)** | `@Override public String toString()` in `Movie`, `Seat`, `Show`, `Booking`<br>`@Override public String getDashboardGreeting()` in `Customer` and `AdminUser` | Subclasses override base class methods to provide custom representations and behavior. |
| **Dynamic Method Dispatch** | `user.getDashboardGreeting()` in `MainFrame.java`<br>`method.executeTransaction(amount)` in `PaymentProcessor.java` | JVM determines the exact method to execute at runtime based on the actual object type on the heap. |

---

# MODULE 3: Packages, Interfaces, Exception Handling & Design Patterns (9 Contact Hours)

| Syllabus Topic | Specific CineBook Implementation & Code Location | Practical Demonstration & Viva Explanation |
|---|---|---|
| **Packages, Classpath & Access Protection** | Flat package-free modular architecture in `c:\Users\Admin\Desktop\Joseph\OOP Project` | All 45 classes reside in the root package, demonstrating how the Java compiler resolves dependencies across source files via the default classpath. |
| **Interfaces vs Abstract Classes** | `interface Cancellable`, `interface Searchable<T>`, `interface PaymentGateway` vs `abstract class User`, `abstract class PaymentMethod` | - **Abstract Classes:** Used when entities share state (`id`, `name`) and default implementations.<br>- **Interfaces:** Used to define pure contracts implemented by unrelated classes (`Booking implements Cancellable`). |
| **Interface References** | `PaymentGateway gateway = new LegacyBankGatewayAdapter(legacyApi);`<br>`Searchable<Movie> searchEngine = new MovieService(mgr);` | Loose coupling: callers interact with the interface reference rather than concrete implementation classes. |
| **Exception Handling (`try`, `catch`, `finally`)** | Try-catch blocks in `BookingService.java`, `CheckoutPanel.java`, `AdminPanel.java` | Gracefully catches expected operational failures (e.g., seat unavailable, card declined) and displays user-friendly dialogs without crashing the JVM. |
| **Multiple Catch Clauses / Multi-catch** | `catch (SeatUnavailableException \| ValidationException ex)` in `SeatSelectionPanel.java`<br>`catch (PaymentProcessingException \| InvalidBookingStateException ex)` in `CheckoutPanel.java` | Demonstrates Java multi-catch syntax for handling multiple related business exceptions uniformly. |
| **Keywords: `throw` and `throws`** | `throw new SeatUnavailableException("Seat already booked");`<br>`public synchronized void confirmBooking(...) throws InvalidBookingStateException` | Explicitly throws custom exceptions when business invariants are violated, declaring them in method signatures. |
| **Built-in Java Exceptions** | Handled `NullPointerException`, `NumberFormatException` (when parsing duration/pricing), `IllegalArgumentException` | Validates user input strings from text fields and handles format parsing errors gracefully. |
| **Custom Exception Hierarchy** | Root: `CineBookException.java`<br>Subclasses: `SeatUnavailableException`, `BookingNotFoundException`, `InvalidBookingStateException`, `PaymentProcessingException`, `ValidationException` | A structured, checked exception hierarchy representing domain-specific cinema errors. |
| **Singleton Design Pattern** | `BookingManager.java` (`private static volatile BookingManager instance;`, private constructor, double-checked locking `getInstance()`) | Ensures only one central repository manages movies, shows, and bookings across all screens throughout the application's lifecycle. |
| **Adapter Design Pattern** | Target: `PaymentGateway.java`<br>Adaptee: `LegacyBankGatewayApi.java` (incompatible `chargeAccount` method)<br>Adapter: `LegacyBankGatewayAdapter.java` | Bridges an incompatible third-party banking API to the system's `PaymentGateway` interface without modifying the legacy code. |
| **SOLID Principles in Java** | - **S (Single Responsibility):** Separate classes for `MovieService`, `ShowService`, `SeatManager`, `PaymentProcessor`<br>- **O (Open/Closed):** Add new payment methods by subclassing `PaymentMethod`<br>- **L (Liskov Substitution):** `Customer` substitutes `User` everywhere seamlessly<br>- **I (Interface Segregation):** Small, focused interfaces (`Cancellable`, `Searchable`)<br>- **D (Dependency Inversion):** Checkout depends on `PaymentMethod` abstraction | Full architectural adherence to all 5 SOLID principles as required by the KTU syllabus. |

---

# MODULE 4: Swing, Event Handling & MVC Architecture (10 Contact Hours)

| Syllabus Topic | Specific CineBook Implementation & Code Location | Practical Demonstration & Viva Explanation |
|---|---|---|
| **Model-View-Controller (MVC) in Swing** | - **Model:** `Movie`, `Show`, `Seat`, `Booking`, `PaymentTransaction`<br>- **View:** `MoviePanel`, `MovieDetailsPanel`, `SeatSelectionPanel`, `CheckoutPanel`, `AdminPanel`<br>- **Controller/Service:** `MovieService`, `ShowService`, `BookingService`, `PaymentProcessor` | Clear separation between business state, business logic, and Swing presentation panels. |
| **Swing Containers & Components** | `JFrame` (`MainFrame`), `JPanel`, `JScrollPane`, `JTabbedPane` (in `AdminPanel`), `JSeparator` | Hierarchical container composition with scroll panes for handling dynamic lists and large seating layouts. |
| **Swing Controls** | `JButton`, `JLabel`, `JTextField`, `JTextArea`, `JComboBox`, `JTable` (in `BookingHistoryPanel` and `AdminPanel`) | Rich user controls for filtering movies, typing card numbers, displaying status badges, and rendering tabular ledgers. |
| **Layout Managers** | - `BorderLayout`: Main application frame and card headers<br>- `GridLayout`: 8-column theater seat grid, 3-column movie cards, table grids<br>- `FlowLayout`: Toolbars, legends, and navigation buttons<br>- `BoxLayout`: Vertical card stacks in `MovieDetailsPanel` and `CheckoutPanel`<br>- `CardLayout`: Screen switching in `MainFrame` | Dynamic layout composition with zero absolute positioning (`null` layouts avoided). |
| **Screen Switching via `CardLayout`** | `cardLayout = new CardLayout(); contentPanel = new JPanel(cardLayout);` in `MainFrame.java` | Seamless transitions between all 7 screens within a single window frame without opening messy pop-ups. |
| **Event Handling & Delegation Model** | `JButton.addActionListener(e -> ...)`<br>`JComboBox.addActionListener(e -> ...)` | Uses Swing Delegation Event Model with Java Lambdas as clean `ActionListener` implementations. |
| **Interactive Event Feedback** | Dynamic seat button color updates on click (`COLOR_SEAT_SELECTED`, `COLOR_SEAT_AVAILABLE`) in `SeatSelectionPanel.java` | Interactive UI state response: clicking toggles seat selection and updates subtotal and tax labels in real time. |
| **Timers & Background Events** | `javax.swing.Timer countdownTimer` in `CheckoutPanel.java` | 5-minute checkout hold timer firing every 1,000 ms to update the ticking clock and auto-release seats upon expiration. |
| **In-Memory Ledger (PBL Syllabus Requirement)** | Thread-safe concurrent storage (`CopyOnWriteArrayList`, `ConcurrentHashMap`) in `BookingManager.java` | Fulfills the project requirement to process and maintain transactions, bookings, and customer records in memory without requiring external database drivers. |

---

# MODULE 5: Developing Database Applications using JDBC

| Syllabus Topic | Specific CineBook Implementation & Code Location | Practical Demonstration & Viva Explanation |
|---|---|---|
| **JDBC Overview & Driver Architecture** | `DatabaseManager.java` (Line 1 to 700)<br>SQLite Type 4 Pure Java Native JDBC Driver (`lib/sqlite-jdbc-3.47.2.0.jar`) | Direct database connectivity using JDBC API (`java.sql.*`). Connects to local `./db/cinebook.db`. |
| **6 Fundamental Steps of JDBC** | 1. Load Driver: `Class.forName("org.sqlite.JDBC")`<br>2. Establish Connection: `DriverManager.getConnection(DB_URL)`<br>3. Create Statement: `conn.prepareStatement(sql)`<br>4. Execute Query/DML: `pstmt.executeQuery()` / `executeUpdate()`<br>5. Process ResultSet: `while(rs.next())`<br>6. Close Resources: Java try-with-resources | Step-by-step implementation across all CRUD functions in `DatabaseManager.java`. |
| **Connection Establishment** | `getConnection()` in `DatabaseManager.java` returning `java.sql.Connection` | Manages relational connection to `jdbc:sqlite:db/cinebook.db` with PRAGMA foreign keys enabled. |
| **Creating & Executing SQL Queries** | DDL: `CREATE TABLE IF NOT EXISTS` for `users`, `movies`, `screens`, `shows`, `bookings`, `transactions`, `audit_logs`<br>DML: `SELECT`, `INSERT`, `UPDATE`, `DELETE` | Executes standard SQL statements for relational persistence. |
| **Working with ResultSet** | `ResultSet rs = pstmt.executeQuery()` in `authenticate()`, `loadAllMovies()`, etc. | Navigates through cursor rows using `rs.next()`, extracting column attributes via `getString()`, `getInt()`, and `getDouble()`. |
| **Parameterized Queries (`PreparedStatement`)** | `PreparedStatement pstmt = conn.prepareStatement(sql)` with placeholder binding (`pstmt.setString(1, ...);`) | Prevents SQL injection attacks and improves execution efficiency for dynamic queries. |
| **Performing CRUD Operations with JDBC** | - **Create (INSERT):** `insertMovie()`, `insertShow()`, `insertBooking()`, `insertTransaction()`<br>- **Read (SELECT):** `authenticate()`, `loadAllMovies()`, `loadAllShows()`<br>- **Update (UPDATE):** `updateMovie()`, `updateBookingStatus()`, `updateCustomerLoyalty()`<br>- **Delete (DELETE):** `deleteMovie()`, `deleteShow()` | Complete relational lifecycle management matching business entity updates. |
| **Authentication Flow & Session Control** | `LoginDialog.java` and `MainFrame.handleLogout()` | Queries SQLite `users` table to authenticate `admin` and `user` (password `123`), requiring manual login on each program startup. |

---

# 6-Member Team Viva Cheat Sheet

When the examiner asks each member to explain their contribution, use this quick reference:

### Member 1: Movie Management
- **Files Owned:** `Movie.java`, `MovieService.java`, `MoviePanel.java`
- **Key Concepts to Defend:**
  - Encapsulation and Data Hiding (private fields, getter methods).
  - Method Overloading: `Movie.matches(query)` vs `Movie.matches(title, genre)`.
  - Generic Interface Implementation: `MovieService implements Searchable<Movie>`.
  - Swing Card Layout & Flow Layout in `MoviePanel`.

### Member 2: Show & Schedule Management
- **Files Owned:** `Show.java`, `Screen.java`, `ShowService.java`, `MovieDetailsPanel.java`
- **Key Concepts to Defend:**
  - 2D Array Representation: `Seat[][] seats` initialized in `Show.initializeSeats()`.
  - Showtime Instance Isolation: Each `Show` has its own independent `Seat[][]` grid in memory.
  - Composition: `Show` has-a `Movie` and has-a `Screen`.
  - Date & Time Formatting with `java.time.LocalDateTime`.

### Member 3: Seat Selection & Pricing
- **Files Owned:** `Seat.java`, `SeatStatus.java`, `SeatCategory.java`, `SeatManager.java`, `SeatSelectionPanel.java`
- **Key Concepts to Defend:**
  - Enums with fields and methods: `SeatStatus` and `SeatCategory` with price multipliers (`1.0x`, `1.3x`, `1.6x`).
  - Thread Synchronization: `synchronized boolean isAvailable()` and `synchronized void reserve()`.
  - Swing Interactive Event Handling: Dynamic seat toggle buttons and real-time total calculation.
  - Multi-catch exception handling: `catch (SeatUnavailableException | ValidationException ex)`.

### Member 4: Booking & Cancellation
- **Files Owned:** `Booking.java`, `BookingStatus.java`, `Cancellable.java`, `BookingService.java`, `BookingHistoryPanel.java`, `BookingConfirmationPanel.java`
- **Key Concepts to Defend:**
  - Interface Implementation: `Booking implements Cancellable`.
  - State Machine Lifecycle: `PENDING_PAYMENT` $\rightarrow$ `CONFIRMED` $\rightarrow$ `CANCELLED`.
  - Decoupling of `BookingStatus` from `PaymentStatus`.
  - Tabular Data Presentation: `JTable` with custom `DefaultTableModel` and ASCII E-Ticket modal dialog.

### Member 5: Payment & Transaction Management
- **Files Owned:** `PaymentStatus.java`, `PaymentMethod.java`, `MockCardPayment.java`, `MockUpiPayment.java`, `MockWalletPayment.java`, `PaymentGateway.java`, `LegacyBankGatewayApi.java`, `LegacyBankResult.java`, `LegacyBankGatewayAdapter.java`, `PaymentTransaction.java`, `PaymentProcessor.java`, `CheckoutPanel.java`
- **Key Concepts to Defend:**
  - Strategy Design Pattern: `PaymentMethod` abstract base class with polymorphic `executeTransaction(amount)` in Card, UPI, and Wallet subclasses.
  - Adapter Design Pattern: `LegacyBankGatewayAdapter` adapting `LegacyBankGatewayApi` to `PaymentGateway`.
  - Dynamic Method Dispatch: Calling `paymentMethod.executeTransaction()` at runtime without switch statements.
  - `javax.swing.Timer` for the 5-minute checkout hold countdown.

### Member 6: Main UI, Administration, Database & Architecture
- **Files Owned:** `CineBookApp.java`, `MainFrame.java`, `LoginDialog.java`, `DatabaseManager.java`, `UIConstants.java`, `BookingManager.java`, `AdminPanel.java`, `User.java`, `Customer.java`, `AdminUser.java`, `Searchable.java`, `CineBookExceptions.java`
- **Key Concepts to Defend:**
  - Singleton Design Pattern with Double-Checked Locking in `BookingManager.getInstance()` and `DatabaseManager.getInstance()`.
  - JDBC Architecture & 6 Steps: Connection establishment, `PreparedStatement`, `ResultSet`, and relational CRUD operations.
  - Interactive Modal Authentication: `LoginDialog` authenticating against SQLite credentials with no pre-authenticated sessions.
  - Inheritance and Polymorphic greetings: `abstract class User` extended by `Customer` and `AdminUser`.
  - Algorithmic Recursion: `countTotalBookedSeatsRecursive(List<Booking>, int index)`.
  - Custom Checked Exception Hierarchy: `CineBookException` base class.
  - Safe Deletion Guards: Blocking deletion of movies that have active bookings to preserve referential integrity.
