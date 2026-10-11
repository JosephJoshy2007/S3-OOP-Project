# CineBook — Exhaustive KTU S3 CSE OOP (PBCST304, 2024 Scheme) Concept-by-Concept Defense Guide

**Course:** Object Oriented Programming (PBCST304) — Semester 3  
**Applicable Scheme:** KTU 2024 Scheme (CSE / CS / CA / CD / AM / CB / CN / CU / CG)  
**Project:** CineBook — Movie Ticket Booking System  
**Implementation:** 45 Modular Java Source Files (`javac CineBookApp.java` -> `java CineBookApp`)

---

## Overview Table of Contents
1. [Module 1: Introduction to Java — Individual Concepts](#module-1-introduction-to-java)
2. [Module 2: Object-Oriented Programming in Java — Individual Concepts](#module-2-object-oriented-programming-in-java)
3. [Module 3: Packages, Interfaces, Exception Handling & Design Patterns — Individual Concepts](#module-3-packages-interfaces-exception-handling--design-patterns)
4. [Module 4: Swing, Event Handling & MVC Architecture — Individual Concepts](#module-4-swing-event-handling--mvc-architecture)
5. [Project-Based Learning (PBL) Requirements Mapping](#project-based-learning-pbl-requirements-mapping)

---

# MODULE 1: Introduction to Java

### 1.1 Structure of a Simple Java Program
- **Where Used:** [`CineBookApp.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CineBookApp.java) (Lines 1–90).
- **Exact Code Snippet:**
  ```java
  public class CineBookApp {
      public static void runAutomatedCoreVerification() { ... }

      public static void main(String[] args) {
          runAutomatedCoreVerification();
          SwingUtilities.invokeLater(() -> {
              MainFrame frame = new MainFrame();
              frame.setVisible(true);
          });
      }
  }
  ```
- **Syllabus Explanation & Viva Defense:** A standard Java application requires a public class matching the source file name containing a `public static void main(String[] args)` method. In CineBook, execution begins at `main()`, which first executes an automated 6-step core verification suite before dispatching GUI initialization to the Swing Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater`.

---

### 1.2 Java Programming Environment, Compiler & Runtime (JVM)
- **Where Used:** Root workspace compilation and runtime execution.
- **Exact Command Line:**
  ```bash
  javac CineBookApp.java
  java CineBookApp
  ```
- **Syllabus Explanation & Viva Defense:** Demonstrates the JDK toolchain workflow. `javac` compiles the Java source code into platform-independent `.class` bytecode files. The Java Virtual Machine (`java.exe`) loads the bytecode, executes static verification, and runs the application via Just-In-Time (JIT) compilation without requiring external build tools like Maven or Gradle.

---

### 1.3 Primitive Data Types and Wrapper Types
- **Where Used:** [`Seat.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Seat.java), [`Show.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java), [`PriceCalculator.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java), [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java).
- **Exact Code Snippet:**
  ```java
  // Primitives in Seat.java & Show.java
  private final int rowNumber;
  private final int colNumber;
  private final double basePrice;
  private boolean isAvailable;

  // Wrapper types in collections and UI components
  JComboBox<String> genreFilter; // Generic wrapper requirement
  List<String> seatIds = new ArrayList<>();
  Integer duration = Integer.parseInt(durField.getText().trim());
  Double balance = Double.parseDouble(walletBalanceField.getText().trim());
  ```
- **Syllabus Explanation & Viva Defense:** Primitives (`int`, `double`, `boolean`, `char`) store raw binary values directly on the stack for high mathematical performance during seat index loops and ticket pricing. Wrapper classes (`Integer`, `Double`, `Boolean`) wrap primitives into objects required by the Java Collections framework and GUI controls.

---

### 1.4 Type Casting and Autoboxing / Unboxing
- **Where Used:** [`AdminPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/AdminPanel.java), [`BookingManager.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingManager.java), [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java).
- **Exact Code Snippet:**
  ```java
  // Casting in AdminPanel.java
  Movie m = (Movie) movieCombo.getSelectedItem();
  Screen sc = (Screen) screenCombo.getSelectedItem();
  String sId = (String) moviesTableModel.getValueAt(row, 0);

  // Parsing & Autoboxing
  int dur = Integer.parseInt(durField.getText().trim());
  double pr = Double.parseDouble(priceField.getText().trim());
  ```
- **Syllabus Explanation & Viva Defense:**
  - **Explicit Downcasting:** `(Movie) movieCombo.getSelectedItem()` casts the returned generic `Object` back to the concrete domain type `Movie`.
  - **Autoboxing / Unboxing:** Automatic conversion between primitives and their corresponding wrapper instances when adding integers to collections or receiving counts from `size()`.

---

### 1.5 Single-Dimensional and Multi-Dimensional Arrays
- **Where Used:**
  - **2D Arrays:** [`Show.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java) (Lines 11–35), [`SeatManager.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/SeatManager.java).
  - **1D Arrays:** [`AdminPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/AdminPanel.java), [`MoviePanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MoviePanel.java).
- **Exact Code Snippet:**
  ```java
  // Show.java: 2D Matrix of Seats
  private final Seat[][] seats;

  public Show(...) {
      this.seats = new Seat[screen.getRows()][screen.getColumns()];
      initializeSeats();
  }

  private void initializeSeats() {
      for (int r = 0; r < screen.getRows(); r++) {
          for (int c = 0; c < screen.getColumns(); c++) {
              seats[r][c] = new Seat(...);
          }
      }
  }

  // 1D Arrays for Table Columns and Dropdown Models
  String[] columns = {"Booking ID", "Customer", "Movie", "Screen", "Showtime", "Seats", "Total", "Status"};
  ```
- **Syllabus Explanation & Viva Defense:** Cinema seating layouts are naturally modeled as an $M \times N$ matrix. CineBook instantiates an independent 2D array `Seat[rows][columns]` per showtime, allowing random access $O(1)$ seat lookups via row and column coordinates while using 1D arrays for table column definitions.

---

### 1.6 Strings and Text Processing
- **Where Used:** [`Movie.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Movie.java), [`BookingHistoryPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingHistoryPanel.java), [`Booking.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Booking.java).
- **Exact Code Snippet:**
  ```java
  // Search matching in Movie.java
  public boolean matches(String keyword) {
      if (keyword == null || keyword.trim().isEmpty()) return true;
      String lower = keyword.trim().toLowerCase();
      return title.toLowerCase().contains(lower) || genre.toLowerCase().contains(lower);
  }

  // Joining and formatting in BookingHistoryPanel.java
  String seatSummary = String.join(", ", booking.getSeatIds());
  String ticketText = String.format("Booking ID: %s | Total: $%.2f", booking.getId(), booking.getTotalAmount());
  ```
- **Syllabus Explanation & Viva Defense:** Strings in Java are immutable objects. CineBook utilizes methods including `trim()`, `toLowerCase()`, `contains()`, `String.join()`, and `String.format()` to sanitize user inputs, perform case-insensitive keyword searches, and format ASCII ticket receipts.

---

### 1.7 Operators (Arithmetic, Relational, Boolean Logical, Assignment, Conditional/Ternary)
- **Where Used:** Throughout the codebase.
- **Exact Code Snippets:**
  - **Arithmetic (`+`, `-`, `*`, `/`, `%`):**
    ```java
    // CheckoutPanel.java countdown timer
    int mins = remainingSeconds / 60;
    int secs = remainingSeconds % 60;

    // Live total computation in Show.java
    Math.round(basePrice * seat.getCategory().getPriceMultiplier() * 100.0) / 100.0;
    ```
  - **Relational (`==`, `!=`, `<`, `>`, `<=`, `>=`):**
    ```java
    // Booking.java cancellation policy (at least 2 hours before show)
    return LocalDateTime.now().isBefore(show.getShowDateTime().minusHours(2));
    ```
  - **Boolean Logical (`&&`, `||`, `!`):**
    ```java
    // Seat.java availability predicate
    if (status == SeatStatus.RESERVED && reservedUntil != null && LocalDateTime.now().isAfter(reservedUntil))
    ```
  - **Conditional / Ternary Operator (`?:`):**
    ```java
    // BookingManager.java recursive seat counter
    int currentCount = (b.getBookingStatus() == BookingStatus.CONFIRMED) ? b.getSeatIds().size() : 0;
    ```
- **Syllabus Explanation & Viva Defense:** Demonstrates operator expressions across mathematical rounding, time differences, compound logic gates, and concise ternary state decisions.

---

### 1.8 Control Statements (Selection, Iteration, Jump)
- **Where Used:** [`SeatSelectionPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/SeatSelectionPanel.java), [`Show.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java), [`MovieService.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MovieService.java).
- **Exact Code Snippets:**
  - **Selection (`if`, `else if`, `else`, `switch`):**
    ```java
    // Row letter category assignment in Show.java
    if (r < 2) {
        cat = SeatCategory.STANDARD;
    } else if (r < 4) {
        cat = SeatCategory.PREMIUM;
    } else {
        cat = SeatCategory.VIP;
    }
    ```
  - **Iteration (Enhanced `for`, standard `for`, `while`):**
    ```java
    // Nested for loops in Show.java
    for (int r = 0; r < screen.getRows(); r++) {
        for (int c = 0; c < screen.getColumns(); c++) { ... }
    }
    // Enhanced for-each in MovieService.java
    for (Movie movie : manager.getMovies()) {
        if (movie.matches(query)) results.add(movie);
    }
    ```
  - **Jump Statements (`break`, `return`):**
    ```java
    // MovieService.java lookup loop
    for (Movie m : manager.getMovies()) {
        if (m.getId().equalsIgnoreCase(id)) {
            movie = m;
            break; // Jump out of search loop immediately once found
        }
    }
    ```
- **Syllabus Explanation & Viva Defense:** Covers all three control flow mechanisms: branching via selection, systematic matrix scanning via iteration, and early exits via jump statements.

---

### 1.9 Command-Line Arguments & Variable-Length Arguments (Varargs)
- **Where Used:** [`CineBookApp.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CineBookApp.java), [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java).
- **Exact Code Snippet:**
  ```java
  // Command-Line Arguments in CineBookApp.java
  public static void main(String[] args) {
      if (args.length > 0 && ("--test-only".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0]))) {
          System.out.println("Headless verification flag supplied. Exiting cleanly.");
          System.exit(0);
      }
      ...
  }

  // Varargs in Arrays.asList(...) & String.format(...)
  Booking bkg = bookingSvc.createPendingReservation(cust, s2, Arrays.asList("B1", "B2"));
  ```
- **Syllabus Explanation & Viva Defense:** `args` array captures command-line parameters allowing execution modes (e.g. headless CI automated tests vs full graphical mode). Varargs (`T...`) allows passing arbitrary arguments into `Arrays.asList` and `String.format`.

---

### 1.10 Classes, Abstract Classes, and Interfaces (Basic Syntax)
- **Where Used:**
  - Concrete Classes: [`Movie.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Movie.java), [`Seat.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Seat.java), [`Show.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java).
  - Abstract Classes: [`User.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/User.java), [`PaymentMethod.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentMethod.java).
  - Interfaces: [`Cancellable.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Cancellable.java), [`Searchable.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Searchable.java), [`PaymentGateway.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentGateway.java).
- **Syllabus Explanation & Viva Defense:** Full syntactic demonstration of classes encapsulating state and behavior, abstract classes mandating partial implementations, and interfaces specifying pure behavioral contracts.

---

# MODULE 2: Object-Oriented Programming in Java

### 2.1 Data Abstraction
- **Where Used:** [`PaymentMethod.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentMethod.java), [`User.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/User.java).
- **Exact Code Snippet:**
  ```java
  public abstract class PaymentMethod {
      private final String methodName;
      public PaymentMethod(String methodName) { this.methodName = methodName; }
      public String getMethodName() { return methodName; }
      public abstract boolean executeTransaction(double amount) throws PaymentProcessingException;
  }
  ```
- **Syllabus Explanation & Viva Defense:** Data abstraction displays only essential features while hiding internal operational complexity. Callers invoke `paymentMethod.executeTransaction(amount)` without needing to know whether it involves 16-digit card checksum verification, UPI VPA syntax parsing, or wallet balance deduction.

---

### 2.2 Encapsulation
- **Where Used:** [`Seat.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Seat.java), [`Booking.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Booking.java), [`Movie.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Movie.java).
- **Exact Code Snippet:**
  ```java
  public class Seat {
      private final String id;
      private SeatStatus status;
      private LocalDateTime reservedUntil;
      private String reservationBookingId;

      public synchronized void reserve(String bookingId, int durationMinutes) throws SeatUnavailableException {
          if (!isAvailable()) {
              throw new SeatUnavailableException("Seat " + id + " is not available for reservation.");
          }
          this.status = SeatStatus.RESERVED;
          this.reservedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
          this.reservationBookingId = bookingId;
      }
  }
  ```
- **Syllabus Explanation & Viva Defense:** Encapsulation binds together data and the methods that manipulate it while keeping both safe from outside interference. Fields are `private`; no external class can directly write `seat.status = SeatStatus.BOOKED`. Changes must pass through the `reserve()` or `confirmBooking()` methods containing defensive business validation.

---

### 2.3 Inheritance (Superclass, Subclass, and Types of Inheritance)
- **Where Used:**
  - Hierarchical Inheritance 1: `User` $\rightarrow$ `Customer`, `AdminUser`.
  - Hierarchical Inheritance 2: `PaymentMethod` $\rightarrow$ `MockCardPayment`, `MockUpiPayment`, `MockWalletPayment`.
  - Multilevel Inheritance: `Throwable` $\rightarrow$ `Exception` $\rightarrow$ `CineBookException` $\rightarrow$ `SeatUnavailableException`.
- **Exact Code Snippet:**
  ```java
  public class Customer extends User {
      private int loyaltyPoints;
      public Customer(String id, String name, String email) {
          super(id, name, email, "Customer");
      }
      public int getLoyaltyPoints() { return loyaltyPoints; }
  }
  ```
- **Syllabus Explanation & Viva Defense:** Code reuse via subtyping (*is-a* relationship). Subclasses inherit identity fields (`id`, `name`, `email`) and accessors from superclass `User` while adding role-specific attributes (`loyaltyPoints` in `Customer`, `accessLevel` in `AdminUser`).

---

### 2.4 Polymorphism (Method Overloading vs Method Overriding)
- **Where Used:**
  - **Method Overloading (Compile-Time Polymorphism):** [`Movie.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Movie.java).
  - **Method Overriding (Runtime Polymorphism):** [`Customer.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Customer.java), [`AdminUser.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/AdminUser.java), [`PaymentMethod.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentMethod.java).
- **Exact Code Snippet:**
  ```java
  // Overloading in Movie.java (Same method name, different parameter signatures)
  public boolean matches(String keyword) { ... }
  public boolean matches(String titleQuery, String genreFilter) { ... }

  // Overriding in Customer.java & AdminUser.java
  @Override
  public String getDashboardGreeting() {
      return "Welcome back, " + getName() + "! Enjoy your cinema experience.";
  }

  @Override
  public String getDashboardGreeting() {
      return "CineBook Cinema Management Console - Logged in as " + getName() + " [" + accessLevel + "]";
  }
  ```
- **Syllabus Explanation & Viva Defense:** Overloading resolves at compile-time based on argument lists. Overriding provides specific implementations in child classes of methods declared in superclasses, annotated with `@Override`.

---

### 2.5 Dynamic Method Dispatch
- **Where Used:** [`PaymentProcessor.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentProcessor.java), [`MainFrame.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MainFrame.java).
- **Exact Code Snippet:**
  ```java
  // PaymentProcessor.java receives abstract PaymentMethod reference
  public synchronized PaymentTransaction processSimulatedPayment(Booking booking, PaymentMethod paymentMethod, String mode) {
      ...
      boolean authorized = paymentMethod.executeTransaction(booking.getTotalAmount());
      ...
  }
  ```
- **Syllabus Explanation & Viva Defense:** Dynamic Method Dispatch is the mechanism by which a call to an overridden method is resolved at runtime rather than compile time. When `paymentMethod.executeTransaction(...)` is called, the JVM determines at runtime whether to execute `MockCardPayment`, `MockUpiPayment`, or `MockWalletPayment` based on the actual object instance present on the heap.

---

### 2.6 Declaring Objects, Object References, and Memory Behavior
- **Where Used:** Throughout all services and GUI controllers.
- **Exact Code Snippet:**
  ```java
  Show s1 = mgr.getShows().get(0);
  Show s2 = mgr.getShows().get(1);
  Seat seatA1_s1 = s1.findSeat("A3");
  Seat seatA1_s2 = s2.findSeat("A3");
  // Test 2 asserts: seatA1_s1 != seatA1_s2
  ```
- **Syllabus Explanation & Viva Defense:** Distinguishes variable references on the stack from object instances on the heap. Shows maintain completely separate object graphs for their seat grids; mutating `seatA1_s1` has zero side effects on `seatA1_s2`.

---

### 2.7 Constructors (Parameterized, Chained) & The `this` Keyword
- **Where Used:** [`Movie.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Movie.java), [`Show.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Show.java).
- **Exact Code Snippet:**
  ```java
  // Constructor chaining using this(...)
  public Movie(String id, String title, String genre, int durationMinutes) {
      this(id, title, genre, durationMinutes, "English", "PG-13", "Feature film presentation.");
  }

  // Resolving variable shadowing using this.field = field
  public Movie(String id, String title, String genre, int durationMinutes, String language, String cert, String desc) {
      this.id = id;
      this.title = title;
      this.genre = genre;
      this.durationMinutes = durationMinutes;
      this.language = language;
      this.certification = cert;
      this.description = desc;
  }
  ```
- **Syllabus Explanation & Viva Defense:** `this` disambiguates instance fields from local parameters and enables constructor overloading chaining (`this(...)`), reducing boilerplate initialization code.

---

### 2.8 The `super` Keyword
- **Where Used:** [`Customer.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Customer.java), [`AdminUser.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/AdminUser.java), [`MockCardPayment.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MockCardPayment.java).
- **Exact Code Snippet:**
  ```java
  public MockCardPayment(String cardNumber, String cardHolder) {
      super("Mock Credit/Debit Card"); // Invokes superclass PaymentMethod constructor
      this.cardNumber = cardNumber;
      this.cardHolder = cardHolder;
  }
  ```
- **Syllabus Explanation & Viva Defense:** `super` invokes the immediate parent class constructor, ensuring inherited private fields are initialized properly before subclass code runs.

---

### 2.9 Calling Order of Constructors
- **Where Used:** When instantiating `Customer` or `AdminUser`.
- **Syllabus Explanation & Viva Defense:** When executing `new Customer(...)`, constructor execution flows from the top of the inheritance hierarchy downward: `Object` constructor runs first, followed by `User` constructor, and finally `Customer` constructor.

---

### 2.10 Recursion
- **Where Used:** [`BookingManager.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingManager.java) (Line 92).
- **Exact Code Snippet:**
  ```java
  public int countTotalBookedSeatsRecursive(List<Booking> list, int index) {
      // BASE CASE: Terminate when index reaches or exceeds the end of the list
      if (list == null || index >= list.size()) {
          return 0;
      }
      // WORK AT CURRENT STEP
      Booking b = list.get(index);
      int currentCount = (b.getBookingStatus() == BookingStatus.CONFIRMED) ? b.getSeatIds().size() : 0;

      // RECURSIVE STEP: Add current count to the result of processing the rest of the list
      return currentCount + countTotalBookedSeatsRecursive(list, index + 1);
  }
  ```
- **Syllabus Explanation & Viva Defense:** Demonstrates genuine algorithmic recursion operating on business object collections. Has an explicit **base case** (`index >= list.size()`) returning 0 to halt recursion and a monotonic **recursive step** (`index + 1`) that pushes stack frames until the base condition is satisfied.

---

### 2.11 Static Members (Variables & Methods)
- **Where Used:** [`BookingManager.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingManager.java), [`UIConstants.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/UIConstants.java), [`CineBookApp.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CineBookApp.java).
- **Exact Code Snippet:**
  ```java
  // Static variable and static method in BookingManager
  private static volatile BookingManager instance;

  public static BookingManager getInstance() { ... }

  // Static constants in UIConstants
  public static final Color COLOR_PRIMARY = new Color(28, 64, 114);
  ```
- **Syllabus Explanation & Viva Defense:** Static members belong to the class itself rather than any specific instance. `getInstance()` provides a global access point without requiring caller instantiation; `UIConstants` variables are allocated once in PermGen/Metaspace.

---

### 2.12 Final Variables & Final with Inheritance
- **Where Used:** [`UIConstants.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/UIConstants.java), Model IDs across classes.
- **Exact Code Snippet:**
  ```java
  public final class UIConstants { ... } // Prevents inheritance
  private final String id;               // Value cannot be changed after constructor assignment
  ```
- **Syllabus Explanation & Viva Defense:**
  - `final` variables cannot be reassigned once initialized, guaranteeing immutability for domain keys.
  - `final` class (`UIConstants`) cannot be subclassed, preserving utility design semantics.

---

### 2.13 Passing Objects as Parameters and Returning Objects
- **Where Used:** [`BookingService.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingService.java), [`PaymentProcessor.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentProcessor.java).
- **Exact Code Snippet:**
  ```java
  // Objects as parameters & returning an object
  public synchronized Booking createPendingReservation(Customer customer, Show show, List<String> seatIds) throws ... {
      ...
      Booking booking = new Booking(bookingId, customer, show, seatIds, total, 5);
      return booking; // Returns domain object reference
  }
  ```
- **Syllabus Explanation & Viva Defense:** Objects are passed by reference value in Java, enabling cohesive service methods to consume rich domain instances and produce newly instantiated state objects.

---

# MODULE 3: Packages, Interfaces, Exception Handling & Design Patterns

### 3.1 Packages, CLASSPATH & Access Protection
- **Where Used:** Root compilation structure.
- **Syllabus Explanation & Viva Defense:** Demonstrates the default (unnamed) package mechanism. When compiling from the project root using `javac CineBookApp.java`, the compiler searches the current directory (default CLASSPATH `.`) to locate and compile dependent classes without requiring package declarations.

---

### 3.2 Interfaces vs Abstract Classes
- **Where Used:**
  - Interfaces: [`Cancellable.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Cancellable.java), [`Searchable.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/Searchable.java), [`PaymentGateway.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentGateway.java).
  - Abstract Classes: [`User.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/User.java), [`PaymentMethod.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentMethod.java).
- **Exact Code Snippet:**
  ```java
  // Interface: Pure contract with no mutable state
  public interface Cancellable {
      boolean isCancellable();
      void cancel(String reason) throws InvalidBookingStateException;
  }

  // Abstract Class: Holds common state and partial implementation
  public abstract class User {
      private final String id;
      private String name;
      ...
      public String getId() { return id; }
      public abstract String getDashboardGreeting();
  }
  ```
- **Syllabus Explanation & Viva Defense:**
  - **Abstract Class:** Used when classes share common identity and state (`id`, `name`) in an *is-a* relationship.
  - **Interface:** Used to define a contract of capabilities (*can-do* relationship) implemented by classes regardless of inheritance tree (`Booking implements Cancellable`).

---

### 3.3 Accessing Implementations Through Interface References
- **Where Used:** [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java), [`MoviePanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MoviePanel.java).
- **Exact Code Snippet:**
  ```java
  // Interface reference pointing to concrete adapter
  PaymentGateway adapter = new LegacyBankGatewayAdapter(legacyApi);
  PaymentTransaction txn = adapter.processPayment(currentBooking, amount, "DirectDebit");
  ```
- **Syllabus Explanation & Viva Defense:** Demonstrates loose coupling. The caller depends exclusively on the `PaymentGateway` interface; the concrete implementation can be swapped without modifying calling code.

---

### 3.4 Checked vs Unchecked Exceptions
- **Where Used:** [`CineBookException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CineBookException.java).
- **Exact Code Snippet:**
  ```java
  public class CineBookException extends Exception { ... } // Extends Exception -> CHECKED
  ```
- **Syllabus Explanation & Viva Defense:** By extending `java.lang.Exception`, `CineBookException` is a **checked exception**. The compiler enforces that callers must either handle the exception in a `try-catch` block or declare it in their method signature with `throws`.

---

### 3.5 Custom Exception Hierarchy
- **Where Used:**
  - [`CineBookException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CineBookException.java) (Base checked class)
  - [`SeatUnavailableException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/SeatUnavailableException.java)
  - [`BookingNotFoundException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingNotFoundException.java)
  - [`InvalidBookingStateException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/InvalidBookingStateException.java)
  - [`PaymentProcessingException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentProcessingException.java)
  - [`ValidationException.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/ValidationException.java)
- **Syllabus Explanation & Viva Defense:** Creates a clean domain-specific error model where different error conditions (seat conflicts, invalid states, bad input) are represented by dedicated exception types.

---

### 3.6 Exception Keywords: `try`, `catch`, `throw`, `throws`, and `finally`
- **Where Used:** [`BookingService.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingService.java), [`BookingHistoryPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingHistoryPanel.java).
- **Exact Code Snippet:**
  ```java
  // throws declaration and throw keyword in BookingService.java
  public synchronized void confirmBooking(Booking booking) throws InvalidBookingStateException, SeatUnavailableException {
      if (booking.isExpired()) {
          throw new InvalidBookingStateException("Booking has expired and cannot be confirmed.");
      }
      ...
  }

  // try-catch handling in BookingHistoryPanel.java
  try {
      bookingService.cancelConfirmedBooking(bookingId);
      JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
  } catch (BookingNotFoundException | InvalidBookingStateException ex) {
      JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
  }
  ```
- **Syllabus Explanation & Viva Defense:**
  - `throw`: Explicitly triggers an exception when a rule is broken.
  - `throws`: Declares in the method signature that the method may raise checked exceptions.
  - `try-catch`: Intercepts exceptions and prevents application crashes by showing user-friendly Swing dialogs.

---

### 3.7 Multiple Catch Clauses & Multi-Catch
- **Where Used:** [`SeatSelectionPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/SeatSelectionPanel.java), [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java).
- **Exact Code Snippet:**
  ```java
  // Multi-catch in SeatSelectionPanel.java
  try {
      Booking booking = bookingService.createPendingReservation(customer, currentShow, seatsToBook);
      parent.navigateToCheckout(booking);
  } catch (SeatUnavailableException | ValidationException ex) {
      JOptionPane.showMessageDialog(this, "Could not reserve seats: " + ex.getMessage(), "Reservation Conflict", JOptionPane.ERROR_MESSAGE);
      buildSeatGrid();
  }
  ```
- **Syllabus Explanation & Viva Defense:** Java 7+ multi-catch syntax handles multiple unrelated or sibling exception types in a single block without code duplication.

---

### 3.8 Singleton Design Pattern
- **Where Used:** [`BookingManager.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/BookingManager.java) (Lines 18–35).
- **Exact Code Snippet:**
  ```java
  public class BookingManager {
      // 1. Private volatile static instance
      private static volatile BookingManager instance;

      // 2. Private constructor prevents direct instantiation
      private BookingManager() {
          seedInitialData();
      }

      // 3. Double-Checked Locking factory method
      public static BookingManager getInstance() {
          if (instance == null) {
              synchronized (BookingManager.class) {
                  if (instance == null) {
                      instance = new BookingManager();
                  }
              }
          }
          return instance;
      }
  }
  ```
- **Syllabus Explanation & Viva Defense:**
  - **Intent:** Ensures a class has only one instance and provides a global point of access to it.
  - **Thread-Safety:** Double-checked locking prevents race conditions during initialization; the `volatile` modifier ensures memory visibility across threads and prevents instruction reordering.

---

### 3.9 Adapter Design Pattern
- **Where Used:** [`LegacyBankGatewayAdapter.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/LegacyBankGatewayAdapter.java), [`LegacyBankGatewayApi.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/LegacyBankGatewayApi.java), [`PaymentGateway.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/PaymentGateway.java).
- **Exact Code Snippet:**
  ```java
  // Target Interface
  public interface PaymentGateway {
      PaymentTransaction processPayment(Booking booking, double amount, String notes) throws PaymentProcessingException;
  }

  // Adaptee (Incompatible 3rd party class)
  public class LegacyBankGatewayApi {
      public LegacyBankResult chargeAccount(String bankAccountNumber, double amount) { ... }
  }

  // Adapter implementing Target while wrapping Adaptee
  public class LegacyBankGatewayAdapter implements PaymentGateway {
      private final LegacyBankGatewayApi legacyApi;

      public LegacyBankGatewayAdapter(LegacyBankGatewayApi legacyApi) {
          this.legacyApi = legacyApi;
      }

      @Override
      public PaymentTransaction processPayment(Booking booking, double amount, String notes) throws PaymentProcessingException {
          String simulatedAccount = "ACCT-987654321";
          LegacyBankResult result = legacyApi.chargeAccount(simulatedAccount, amount);
          if (!result.isSuccess()) {
              throw new PaymentProcessingException("Bank declined: " + result.getReference());
          }
          return new PaymentTransaction(..., result.getReference());
      }
  }
  ```
- **Syllabus Explanation & Viva Defense:** Converts the interface of an existing incompatible class (`LegacyBankGatewayApi`) into the target interface (`PaymentGateway`) expected by the client without modifying either class.

---

### 3.10 SOLID Principles in Java
- **Where Used:** Across all modules.
- **Detailed Concept Mapping:**
  1. **S — Single Responsibility Principle (SRP):**
     - `MovieService` handles only movie cataloging.
     - `ShowService` handles only show scheduling.
     - `SeatManager` handles only seat locks.
     - `PaymentProcessor` handles only transaction verification.
  2. **O — Open/Closed Principle (OCP):**
     - Open for extension, closed for modification. Adding a new payment channel (e.g. `CryptoPayment`) requires only creating a new subclass of `PaymentMethod` without modifying `PaymentProcessor` or `CheckoutPanel`.
  3. **L — Liskov Substitution Principle (LSP):**
     - Subclasses `Customer` and `AdminUser` can substitute `User` anywhere without breaking application correctness.
  4. **I — Interface Segregation Principle (ISP):**
     - Small, focused interfaces (`Cancellable`, `Searchable<T>`, `PaymentGateway`) rather than one bloated interface.
  5. **D — Dependency Inversion Principle (DIP):**
     - High-level modules (`CheckoutPanel`) depend on abstractions (`PaymentGateway`, `PaymentMethod`), not low-level concrete bank APIs.

---

# MODULE 4: Swing, Event Handling & MVC Architecture

### 4.1 Model-View-Controller (MVC) Architecture
- **Where Used:** Entire application structure.
- **Exact Mapping:**
  - **Model:** `Movie`, `Show`, `Screen`, `Seat`, `Booking`, `PaymentTransaction`, `User` (Pure state and business entities).
  - **View:** `MoviePanel`, `MovieDetailsPanel`, `SeatSelectionPanel`, `CheckoutPanel`, `BookingConfirmationPanel`, `BookingHistoryPanel`, `AdminPanel` (Swing UI presentation components).
  - **Controller / Service:** `MovieService`, `ShowService`, `BookingService`, `PaymentProcessor`, `SeatManager` (Business logic, state transitions, validation).
- **Syllabus Explanation & Viva Defense:** Cleanly decouples GUI event listeners from backend business logic. Views never directly alter domain state; they invoke service controllers, which update models and notify views.

---

### 4.2 Swing Containers: `JFrame`, `JPanel`, `JScrollPane`, `JTabbedPane`
- **Where Used:** [`MainFrame.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MainFrame.java), [`AdminPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/AdminPanel.java), [`MoviePanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/MoviePanel.java).
- **Exact Code Snippets:**
  - `JFrame`: Top-level window in `MainFrame.java` (`super("CineBook - Movie Ticket Booking System")`).
  - `JPanel`: Screen containers with borders and backgrounds.
  - `JScrollPane`: Wraps movie grids and tables to enable scrolling:
    ```java
    JScrollPane scrollPane = new JScrollPane(wrapper);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    ```
  - `JTabbedPane`: Tabbed views in `AdminPanel.java` (`tabbedPane.addTab("Manage Movies", ...)`, etc.).

---

### 4.3 Swing Components: `JLabel`, `JButton`, `JTextField`, `JTextArea`, `JComboBox`, `JTable`
- **Where Used:**
  - `JButton`: Action buttons across all panels.
  - `JLabel`: Movie titles, seat status legends, countdown timers.
  - `JTextField`: Search box, movie creation forms, card entry.
  - `JTextArea`: Multi-line movie synopsis and ASCII ticket modal.
  - `JComboBox`: Genre dropdown (`All`, `Sci-Fi`, `Action`), payment channel dropdown.
  - `JTable`: Tabular bookings and financial transaction ledgers in `BookingHistoryPanel.java` and `AdminPanel.java`.

---

### 4.4 Swing Layout Managers
- **Where Used:**
  1. **`BorderLayout`:** Used in `MainFrame.java` (North: Navigation, Center: CardLayout container) and screen headers.
  2. **`GridLayout`:** Used in `SeatSelectionPanel.java` for the 8-column theater seat grid (`new GridLayout(rows, cols + 1, 8, 8)`).
  3. **`FlowLayout`:** Used in legend panels, action toolbars, and search bars.
  4. **`BoxLayout`:** Used in `CheckoutPanel.java` and `MovieDetailsPanel.java` for vertical stacking of card sections (`BoxLayout.Y_AXIS`).
  5. **`CardLayout`:** Used in `MainFrame.java` for switching between the 7 primary application views without opening multiple windows.
- **Syllabus Explanation & Viva Defense:** Proves mastery of Swing layout managers; zero use of absolute/null positioning.

---

### 4.5 Event Handling & Delegation Event Model
- **Where Used:** All user interactions.
- **Exact Code Snippet:**
  ```java
  // Event Source: JButton
  // Event: ActionEvent
  // Event Listener: ActionListener implemented via Lambda expression
  searchBtn.addActionListener(e -> filterMovies());
  seatBtn.addActionListener(e -> toggleSeatSelection(seat, seatBtn));
  paymentMethodCombo.addActionListener(e -> updatePaymentFormFields());
  ```
- **Syllabus Explanation & Viva Defense:** Conforms to Java's Delegation Event Model: an event source (`JButton`) generates an event object (`ActionEvent`) and delivers it to registered listener objects (`ActionListener`) using modern Java lambda expressions.

---

### 4.6 Interactive GUI Event Feedback (Real-time Dynamic UI)
- **Where Used:** [`SeatSelectionPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/SeatSelectionPanel.java).
- **Exact Code Snippet:**
  ```java
  private void toggleSeatSelection(Seat seat, JButton btn) {
      if (selectedSeatIds.contains(seat.getId())) {
          selectedSeatIds.remove(seat.getId());
      } else {
          selectedSeatIds.add(seat.getId());
      }
      updateSeatButtonAppearance(btn, seat); // Changes button background to green
      updateSummary(); // Recalculates subtotal, GST tax, and total payable live
  }
  ```
- **Syllabus Explanation & Viva Defense:** Demonstrates interactive feedback: button clicks alter button colors in real time and trigger price recalculation without reloading the screen.

---

### 4.7 Swing Timers & Background Events
- **Where Used:** [`CheckoutPanel.java`](file:///c:/Users/Admin/Desktop/Joseph/OOP%20Project/CheckoutPanel.java).
- **Exact Code Snippet:**
  ```java
  countdownTimer = new javax.swing.Timer(1000, e -> {
      remainingSeconds--;
      int mins = remainingSeconds / 60;
      int secs = remainingSeconds % 60;
      timerLabel.setText(String.format("Time Remaining: %02d:%02d", mins, secs));

      if (remainingSeconds <= 0) {
          countdownTimer.stop();
          JOptionPane.showMessageDialog(this, "5-minute checkout window expired. Seats released.");
          bookingService.cancelPendingReservation(currentBooking);
          parent.navigateToSeatSelection(currentBooking.getShow());
      }
  });
  countdownTimer.start();
  ```
- **Syllabus Explanation & Viva Defense:** `javax.swing.Timer` executes on the Event Dispatch Thread (EDT) at 1,000 ms intervals, preventing thread race conditions while managing the 5-minute seat hold timeout.

---

# Project-Based Learning (PBL) Requirements Mapping

| KTU PBL Requirement | How CineBook Satisfies It |
|---|---|
| **1. Accept considerable user input for processing** | Searches movies by title/genre, interactive seat selection from 40-seat grid, payment method selection, card/UPI field entry, movie/show scheduling input forms in Admin panel. |
| **2. Maintain considerable domain data** | 6 movies, 3 screens, 7 shows, independent seat grids per show, transaction ledgers, customer profiles, and audit log entries maintained in memory. |
| **3. Process data to generate displayed output** | Tier pricing calculations ($1.0\times, 1.3\times, 1.6\times$), 18% GST addition, ASCII ticket generation, real-time recursive analytics, seat availability badges. |
| **4. Thread-Safe In-Memory Architecture** | Uses `CopyOnWriteArrayList`, `synchronized` blocks, and Double-Checked Locking in `BookingManager` to ensure zero concurrency conflicts without requiring an external database. |
