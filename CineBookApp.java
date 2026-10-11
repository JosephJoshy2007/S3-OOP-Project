import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CineBookApp {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");



    // SECTION 1: EXCEPTIONS
    public static class CineBookException extends Exception {
        public CineBookException(String message) { super(message); }
        public CineBookException(String message, Throwable cause) { super(message, cause); }
    }

    public static class SeatUnavailableException extends CineBookException {
        public SeatUnavailableException(String message) { super(message); }
    }

    public static class BookingNotFoundException extends CineBookException {
        public BookingNotFoundException(String message) { super(message); }
    }

    public static class InvalidBookingStateException extends CineBookException {
        public InvalidBookingStateException(String message) { super(message); }
    }

    public static class PaymentProcessingException extends CineBookException {
        public PaymentProcessingException(String message) { super(message); }
    }

    public static class ValidationException extends CineBookException {
        public ValidationException(String message) { super(message); }
    }


    // SECTION 2: ENUMS
    public enum BookingStatus {
        PENDING_PAYMENT("Pending Payment"),
        CONFIRMED("Confirmed"),
        PAYMENT_FAILED("Payment Failed"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired");

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        NOT_STARTED("Not Started"),
        PENDING("Pending"),
        SUCCESS("Success"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SeatStatus {
        AVAILABLE("Available"),
        SELECTED("Selected"),
        RESERVED("Reserved"),
        BOOKED("Booked");

        private final String displayName;

        SeatStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SeatCategory {
        STANDARD("Standard", 1.0),
        PREMIUM("Premium", 1.3),
        VIP("VIP", 1.6);

        private final String label;
        private final double priceMultiplier;

        SeatCategory(String label, double priceMultiplier) {
            this.label = label;
            this.priceMultiplier = priceMultiplier;
        }

        public String getLabel() {
            return label;
        }

        public double getPriceMultiplier() {
            return priceMultiplier;
        }
    }


    // SECTION 3: INTERFACES & DESIGN PATTERNS
    public interface Cancellable {
        boolean isCancellable();
        void cancel(String reason) throws InvalidBookingStateException;
    }

    public interface Searchable<T> {
        List<T> search(String query);
    }

    public interface PaymentGateway {
        PaymentTransaction processPayment(Booking booking, double amount, String notes)
                throws PaymentProcessingException;
    }

    public static class LegacyBankGatewayApi {
        public static class LegacyBankResult {
            private final boolean success;
            private final String reference;
            private final String errorReason;

            public LegacyBankResult(boolean success, String reference, String errorReason) {
                this.success = success;
                this.reference = reference;
                this.errorReason = errorReason;
            }
            public boolean isSuccess() { return success; }
            public String getReference() { return reference; }
            public String getErrorReason() { return errorReason; }
        }

        public LegacyBankResult dispatchDirectDebit(String accountRef, int amountInCents, String description) {
            if (accountRef == null || accountRef.trim().isEmpty()) {
                return new LegacyBankResult(false, null, "Invalid bank account reference");
            }
            String authCode = "BANK-TXN-" + System.currentTimeMillis();
            return new LegacyBankResult(true, authCode, null);
        }
    }

    public static class LegacyBankGatewayAdapter implements PaymentGateway {
        private final LegacyBankGatewayApi legacyApi;

        public LegacyBankGatewayAdapter(LegacyBankGatewayApi legacyApi) {
            this.legacyApi = legacyApi;
        }

        @Override
        public PaymentTransaction processPayment(Booking booking, double amount, String notes)
                throws PaymentProcessingException {
            int cents = (int) Math.round(amount * 100);
            String accRef = "ACC-" + booking.getCustomer().getId();
            LegacyBankGatewayApi.LegacyBankResult result = legacyApi.dispatchDirectDebit(accRef, cents, notes);

            if (!result.isSuccess()) {
                throw new PaymentProcessingException("Bank payment failed: " + result.getErrorReason());
            }

            return new PaymentTransaction(
                    "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    booking.getId(),
                    "Bank DirectDebit (Adapter)",
                    amount,
                    LocalDateTime.now(),
                    PaymentStatus.SUCCESS,
                    result.getReference()
            );
        }
    }



    // SECTION 4: PAYMENT METHOD HIERARCHY
    public static abstract class PaymentMethod {
        private final String methodName;

        public PaymentMethod(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

        public abstract boolean executeTransaction(double amount) throws PaymentProcessingException;

        @Override
        public String toString() {
            return methodName;
        }
    }

    public static class MockCardPayment extends PaymentMethod {
        private final String cardNumber;
        private final String cardHolder;

        public MockCardPayment(String cardNumber, String cardHolder) {
            super("Mock Credit/Debit Card");
            this.cardNumber = cardNumber;
            this.cardHolder = cardHolder;
        }

        @Override
        public boolean executeTransaction(double amount) throws PaymentProcessingException {
            if (cardNumber == null || cardNumber.trim().length() < 4) {
                throw new PaymentProcessingException("Card payment failed: Invalid card details provided.");
            }
            return true;
        }
    }

    public static class MockUpiPayment extends PaymentMethod {
        private final String upiId;

        public MockUpiPayment(String upiId) {
            super("Mock UPI Instant Pay");
            this.upiId = upiId;
        }

        @Override
        public boolean executeTransaction(double amount) throws PaymentProcessingException {
            if (upiId == null || !upiId.contains("@")) {
               throw new PaymentProcessingException("UPI payment failed: Invalid virtual payment address (VPA).");
            }
            return true;
        }
    }

    public static class MockWalletPayment extends PaymentMethod {
        private final String walletName;
        private final double walletBalance;

        public MockWalletPayment(String walletName, double walletBalance) {
            super("Mock Digital Wallet (" + walletName + ")");
            this.walletName = walletName;
            this.walletBalance = walletBalance;
        }

        @Override
        public boolean executeTransaction(double amount) throws PaymentProcessingException {
            if (walletBalance < amount) {
                throw new PaymentProcessingException(
                        String.format("Wallet payment failed: Insufficient balance ($%.2f available, $%.2f required).",
                                walletBalance, amount));
            }
            return true;
        }
    }



    // SECTION 5: USER & MOVIE MODELS
    public static abstract class User {
        private final String id;
        private String name;
        private String email;
        private final String role;

        public User(String id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }

        public abstract String getDashboardGreeting();

        @Override
        public String toString() { return name + " (" + role + ")"; }
    }

    public static class Customer extends User {
        private int loyaltyPoints;

        public Customer(String id, String name, String email) {
            super(id, name, email, "Customer");
            this.loyaltyPoints = 0;
          }

        public Customer(String id, String name, String email, int loyaltyPoints) {
            super(id, name, email, "Customer");
            this.loyaltyPoints = loyaltyPoints;
        }

        public int getLoyaltyPoints() { return loyaltyPoints; }
        public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }

        @Override
        public String getDashboardGreeting() { return "Welcome back, " + getName() + "! Enjoy your cinema experience."; }
    }

    public static class AdminUser extends User {
        private final String accessLevel;

        public AdminUser(String id, String name, String email, String accessLevel) {
            super(id, name, email, "Admin");
            this.accessLevel = accessLevel;
        }

        public String getAccessLevel() { return accessLevel; }

        @Override
        public String getDashboardGreeting() {
            return "CineBook Cinema Management Console - Logged in as " + getName() + " [" + accessLevel + "]";
        }
    }

    public static class Movie {
        private final String id;
        private String title;
        private String genre;
        private int durationMinutes;
        private String language;
        private String certification;
        private String description;


        public Movie(String id, String title, String genre, int durationMinutes,
                     String language, String certification, String description) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.durationMinutes = durationMinutes;
            this.language = language;
            this.certification = certification;
            this.description = description;
        }

        public Movie(String id, String title, String genre, int durationMinutes) {
            this(id, title, genre, durationMinutes, "English", "PG-13", "Feature film presentation.");
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCertification() { return certification; }
        public void setCertification(String certification) { this.certification = certification; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }


        public boolean matches(String keyword) {
            if (keyword == null || keyword.trim().isEmpty()) return true;
            String lower = keyword.trim().toLowerCase();
            return title.toLowerCase().contains(lower) || genre.toLowerCase().contains(lower);
        }

        public boolean matches(String titleQuery, String genreFilter) {
            boolean matchesTitle = (titleQuery == null || titleQuery.trim().isEmpty())
                    || title.toLowerCase().contains(titleQuery.trim().toLowerCase());
            boolean matchesGenre = (genreFilter == null || genreFilter.equalsIgnoreCase("All") || genreFilter.trim().isEmpty())
                    || genre.equalsIgnoreCase(genreFilter.trim());
            return matchesTitle && matchesGenre;
        }

        @Override
        public String toString() {
            return title + " (" + certification + ", " + durationMinutes + " mins)";
        }
    }


    // SECTION 6: THEATER, SEAT & SHOW MODELS
    public static class Screen {
        private final String id;
        private final String name;
        private final int rows;
        private final int columns;

        public Screen(String id, String name, int rows, int columns) {
            this.id = id;
            this.name = name;
            this.rows = rows;
            this.columns = columns;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getRows() { return rows; }
        public int getColumns() { return columns; }
        public int getTotalCapacity() { return rows * columns; }

        @Override
        public String toString() {
            return name + " (" + getTotalCapacity() + " seats)";
        }
    }

    public static class Seat {
        private final String id;
        private final int rowNumber;
        private final int colNumber;
        private final SeatCategory category;
        private SeatStatus status;
        private LocalDateTime reservedUntil;
        private String reservationBookingId;

        public Seat(String id, int rowNumber, int colNumber, SeatCategory category) {
            this.id = id;
            this.rowNumber = rowNumber;
            this.colNumber = colNumber;
            this.category = category;
            this.status = SeatStatus.AVAILABLE;
        }

        public String getId() { return id; }
        public int getRowNumber() { return rowNumber; }
        public int getColNumber() { return colNumber; }
        public SeatCategory getCategory() { return category; }
        public SeatStatus getStatus() { return status; }
        public void setStatus(SeatStatus status) { this.status = status; }
        public LocalDateTime getReservedUntil() { return reservedUntil; }
        public String getReservationBookingId() { return reservationBookingId; }

        public synchronized boolean isAvailable() {
            if (status == SeatStatus.RESERVED && reservedUntil != null && LocalDateTime.now().isAfter(reservedUntil)) {
                this.status = SeatStatus.AVAILABLE;
                this.reservedUntil = null;
                this.reservationBookingId = null;
                return true;
            }
            return status == SeatStatus.AVAILABLE;
        }

        public synchronized void reserve(String bookingId, int durationMinutes) throws SeatUnavailableException {
            if (!isAvailable()) {
                throw new SeatUnavailableException("Seat " + id + " is not available for reservation.");
            }
            this.status = SeatStatus.RESERVED;
            this.reservedUntil = LocalDateTime.now().plusMinutes(durationMinutes);
            this.reservationBookingId = bookingId;
        }

        public synchronized void confirmBooking(String bookingId) throws SeatUnavailableException {
            if (status == SeatStatus.BOOKED) {
                throw new SeatUnavailableException("Seat " + id + " is already booked.");
            }
            this.status = SeatStatus.BOOKED;
            this.reservationBookingId = bookingId;
            this.reservedUntil = null;
        }

        public synchronized void release(String bookingId) {
            if (this.reservationBookingId == null || this.reservationBookingId.equals(bookingId) || this.status != SeatStatus.BOOKED) {
                this.status = SeatStatus.AVAILABLE;
                this.reservedUntil = null;
                this.reservationBookingId = null;
            }
        }

        @Override
        public String toString() {
            return id + " (" + category.getLabel() + " - " + status.getDisplayName() + ")";
        }
    }

    public static class Show {
        private final String id;
        private final Movie movie;
        private final Screen screen;
        private final LocalDateTime showDateTime;
        private final double basePrice;
        private final Seat[][] seats;

        public Show(String id, Movie movie, Screen screen, LocalDateTime showDateTime, double basePrice) {
            this.id = id;
            this.movie = movie;
            this.screen = screen;
            this.showDateTime = showDateTime;
            this.basePrice = basePrice;
            this.seats = new Seat[screen.getRows()][screen.getColumns()];
            initializeSeats();
        }

        private void initializeSeats() {
            for (int r = 0; r < screen.getRows(); r++) {
                char rowLetter = (char) ('A' + r);
                SeatCategory cat;
                if (r < 2) {
                    cat = SeatCategory.STANDARD;
                } else if (r < 4) {
                    cat = SeatCategory.PREMIUM;
                } else {
                    cat = SeatCategory.VIP;
                }

                for (int c = 0; c < screen.getColumns(); c++) {
                    String seatId = "" + rowLetter + (c + 1);
                    seats[r][c] = new Seat(seatId, r + 1, c + 1, cat);
                }
            }
        }

        public String getId() { return id; }
        public Movie getMovie() { return movie; }
        public Screen getScreen() { return screen; }
        public LocalDateTime getShowDateTime() { return showDateTime; }
        public double getBasePrice() { return basePrice; }
        public Seat[][] getSeats() { return seats; }

        public Seat findSeat(String seatId) {
            if (seatId == null) return null;
            for (Seat[] row : seats) {
                for (Seat seat : row) {
                    if (seat.getId().equalsIgnoreCase(seatId.trim())) {
                        return seat;
                    }
                }
            }
            return null;
        }

        public double calculateSeatPrice(Seat seat) {
            if (seat == null) return basePrice;
            return Math.round(basePrice * seat.getCategory().getPriceMultiplier() * 100.0) / 100.0;
        }

        public int getAvailableSeatsCount() {
            int count = 0;
            for (Seat[] row : seats) {
                for (Seat seat : row) {
                    if (seat.isAvailable()) count++;
                }
            }
            return count;
        }

        public int getTotalSeatsCount() {
            return screen.getTotalCapacity();
        }

        @Override
        public String toString() {
            return movie.getTitle() + " @ " + screen.getName() + " (" + showDateTime.format(DATE_TIME_FORMATTER) + ")";
        }
    }


    // SECTION 7: BOOKING, TRANSACTION & BOOKING MANAGER (SINGLETON)
    public static class Booking implements Cancellable {
        private final String id;
        private final Customer customer;
        private final Show show;
        private final List<String> seatIds;
        private final double totalAmount;
        private final LocalDateTime createdAt;
        private final LocalDateTime expiresAt;
        private BookingStatus bookingStatus;
        private String cancellationReason;

        public Booking(String id, Customer customer, Show show, List<String> seatIds, double totalAmount, int expiryMinutes) {
            this.id = id;
            this.customer = customer;
            this.show = show;
            this.seatIds = new ArrayList<>(seatIds);
            this.totalAmount = Math.round(totalAmount * 100.0) / 100.0;
            this.createdAt = LocalDateTime.now();
            this.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
            this.bookingStatus = BookingStatus.PENDING_PAYMENT;
        }

        public String getId() { return id; }
        public Customer getCustomer() { return customer; }
        public Show getShow() { return show; }
        public List<String> getSeatIds() { return Collections.unmodifiableList(seatIds); }
        public double getTotalAmount() { return totalAmount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public BookingStatus getBookingStatus() { return bookingStatus; }
        public String getCancellationReason() { return cancellationReason; }

        public boolean isExpired() {
            return bookingStatus == BookingStatus.PENDING_PAYMENT && LocalDateTime.now().isAfter(expiresAt);
        }

        public void confirm() throws InvalidBookingStateException {
            if (bookingStatus == BookingStatus.CONFIRMED) {
                throw new InvalidBookingStateException("Booking " + id + " is already confirmed.");
            }
            if (bookingStatus == BookingStatus.CANCELLED) {
                throw new InvalidBookingStateException("Cannot confirm a cancelled booking.");
            }
            this.bookingStatus = BookingStatus.CONFIRMED;
        }

        public void markPaymentFailed() {
            if (this.bookingStatus == BookingStatus.PENDING_PAYMENT) {
                this.bookingStatus = BookingStatus.PAYMENT_FAILED;
            }
        }

        @Override
        public boolean isCancellable() {
            if (bookingStatus != BookingStatus.CONFIRMED) return false;
            // Cancellable up to 2 hours before showtime
            return LocalDateTime.now().isBefore(show.getShowDateTime().minusHours(2));
        }

        @Override
        public void cancel(String reason) throws InvalidBookingStateException {
            if (bookingStatus == BookingStatus.CANCELLED) {
                throw new InvalidBookingStateException("Booking " + id + " is already cancelled.");
            }
            this.bookingStatus = BookingStatus.CANCELLED;
            this.cancellationReason = reason;
        }

        @Override
        public String toString() {
            return "Booking #" + id + " [" + bookingStatus.getDisplayName() + "] - " +
                    show.getMovie().getTitle() + " (" + String.join(", ", seatIds) + ") $" + String.format("%.2f", totalAmount);
        }
    }

    public static class PaymentTransaction {
        private final String id;
        private final String bookingId;
        private final String paymentMethodName;
        private final double amount;
        private final LocalDateTime timestamp;
        private final PaymentStatus paymentStatus;
        private final String gatewayReference;

        public PaymentTransaction(String id, String bookingId, String paymentMethodName,
                                  double amount, LocalDateTime timestamp,
                                  PaymentStatus paymentStatus, String gatewayReference) {
            this.id = id;
            this.bookingId = bookingId;
            this.paymentMethodName = paymentMethodName;
            this.amount = Math.round(amount * 100.0) / 100.0;
            this.timestamp = timestamp;
            this.paymentStatus = paymentStatus;
            this.gatewayReference = gatewayReference;
        }

        public String getId() { return id; }
        public String getBookingId() { return bookingId; }
        public String getPaymentMethodName() { return paymentMethodName; }
        public double getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public PaymentStatus getPaymentStatus() { return paymentStatus; }
        public String getGatewayReference() { return gatewayReference; }

        @Override
        public String toString() {
            return "Txn #" + id + " (" + paymentStatus.getDisplayName() + ") $" +
                    String.format("%.2f", amount) + " via " + paymentMethodName + " [" + gatewayReference + "]";
        }
    }

    public static class BookingManager {
        private static volatile BookingManager instance;

        private final List<Movie> movies = new CopyOnWriteArrayList<>();
        private final List<Screen> screens = new CopyOnWriteArrayList<>();
        private final List<Show> shows = new CopyOnWriteArrayList<>();
        private final List<Booking> bookings = new CopyOnWriteArrayList<>();
        private final List<PaymentTransaction> transactions = new CopyOnWriteArrayList<>();
        private final List<Customer> customers = new CopyOnWriteArrayList<>();
        private final List<AdminUser> adminUsers = new CopyOnWriteArrayList<>();
        private final List<String> auditLogs = new CopyOnWriteArrayList<>();

        private User currentUser;

        private BookingManager() {
            seedInitialData();
        }

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

        public List<Movie> getMovies() { return movies; }
        public List<Screen> getScreens() { return screens; }
        public List<Show> getShows() { return shows; }
        public List<Booking> getBookings() { return bookings; }
        public List<PaymentTransaction> getTransactions() { return transactions; }
        public List<Customer> getCustomers() { return customers; }
        public List<AdminUser> getAdminUsers() { return adminUsers; }
        public List<String> getAuditLogs() { return auditLogs; }

        public User getCurrentUser() { return currentUser; }
        public void setCurrentUser(User user) { this.currentUser = user; }

        public void logAudit(String action) {
            String logEntry = "[" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + "] " + action;
            auditLogs.add(logEntry);
            System.out.println("[AUDIT] " + logEntry);
        }

        public Booking findBookingById(String bookingId) {
            if (bookingId == null) return null;
            for (Booking b : bookings) {
                if (b.getId().equalsIgnoreCase(bookingId.trim())) return b;
            }
            return null;
        }

        public PaymentTransaction findTransactionByBookingId(String bookingId) {
            if (bookingId == null) return null;
            for (PaymentTransaction pt : transactions) {
                if (pt.getBookingId().equalsIgnoreCase(bookingId.trim())) return pt;
            }
            return null;
        }

        public List<Show> getShowsForMovie(String movieId) {
            List<Show> result = new ArrayList<>();
            for (Show s : shows) {
                if (s.getMovie().getId().equalsIgnoreCase(movieId)) {
                    result.add(s);
                }
            }
            return result;
        }

        public List<Booking> getBookingsForCustomer(String customerId) {
            List<Booking> result = new ArrayList<>();
            for (Booking b : bookings) {
                if (b.getCustomer().getId().equalsIgnoreCase(customerId)) {
                    result.add(b);
                }
            }
            return result;
        }

        public int countTotalBookedSeatsRecursive(List<Booking> list, int index) {
            if (list == null || index >= list.size()) {
                return 0;
            }
            Booking b = list.get(index);
            int currentCount = (b.getBookingStatus() == BookingStatus.CONFIRMED) ? b.getSeatIds().size() : 0;
            return currentCount + countTotalBookedSeatsRecursive(list, index + 1);
        }

        public double calculateTotalRevenue() {
            double total = 0.0;
            for (PaymentTransaction t : transactions) {
                if (t.getPaymentStatus() == PaymentStatus.SUCCESS) {
                    total += t.getAmount();
                } else if (t.getPaymentStatus() == PaymentStatus.REFUNDED) {
                    total -= t.getAmount();
                }
            }
            return Math.max(0.0, Math.round(total * 100.0) / 100.0);
        }

        private void seedInitialData() {
            // Screens
            Screen sc1 = new Screen("SCR-01", "Screen 1 (IMAX Laser)", 5, 8);
            Screen sc2 = new Screen("SCR-02", "Screen 2 (Dolby Atmos)", 5, 8);
            Screen sc3 = new Screen("SCR-03", "Screen 3 (Gold Lounge)", 5, 6);
            screens.add(sc1);
            screens.add(sc2);
            screens.add(sc3);

            // Movies (6 movies to satisfy Test 1 >= 6)
            Movie m1 = new Movie("MOV-01", "Interstellar: Beyond Time", "Sci-Fi", 169, "English", "PG-13",
                    "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.");
            Movie m2 = new Movie("MOV-02", "Inception", "Sci-Fi", 148, "English", "PG-13",
                    "A thief who steals corporate secrets through dream-sharing technology is given an inverse task of planting an idea.");
            Movie m3 = new Movie("MOV-03", "The Dark Knight", "Action", 152, "English", "PG-13",
                    "Batman battles the Joker who unleashes psychological chaos across Gotham City.");
            Movie m4 = new Movie("MOV-04", "La La Land", "Romance", 128, "English", "PG-13",
                    "A jazz pianist and an aspiring actress fall in love while pursuing their passions in modern Los Angeles.");
            Movie m5 = new Movie("MOV-05", "Spirited Away", "Animation", 125, "Japanese", "PG",
                    "A young girl enters a realm governed by gods, witches, and spirits where humans are transformed into beasts.");
            Movie m6 = new Movie("MOV-06", "The Grand Budapest Hotel", "Comedy", 100, "English", "R",
                    "The adventures of Gustave H, a legendary concierge at a famous European hotel between the wars.");

            movies.add(m1);
            movies.add(m2);
            movies.add(m3);
            movies.add(m4);
            movies.add(m5);
            movies.add(m6);

            // Users
            Customer c1 = new Customer("CUST-01", "Aarav Sharma", "aarav@cinebook.com", 150);
            Customer c2 = new Customer("CUST-02", "Diya Patel", "diya@cinebook.com", 220);
            AdminUser a1 = new AdminUser("ADM-01", "System Administrator", "admin@cinebook.com", "SuperAdmin");
            customers.add(c1);
            customers.add(c2);
            adminUsers.add(a1);
            this.currentUser = c1;

            // Shows
            LocalDateTime now = LocalDateTime.now();
            Show s1 = new Show("SHW-01", m1, sc1, now.plusHours(3), 15.00);
            Show s2 = new Show("SHW-02", m1, sc1, now.plusDays(1).withHour(18).withMinute(0), 16.50);
            Show s3 = new Show("SHW-03", m2, sc2, now.plusHours(5), 14.00);
            Show s4 = new Show("SHW-04", m3, sc1, now.plusDays(1).withHour(20).withMinute(30), 18.00);
            Show s5 = new Show("SHW-05", m4, sc3, now.plusHours(2), 22.00);
            Show s6 = new Show("SHW-06", m5, sc2, now.plusDays(2).withHour(15).withMinute(0), 13.50);
            Show s7 = new Show("SHW-07", m6, sc3, now.plusHours(4), 19.00);

            // Pre-seed booked seats in s1
            try {
                Seat seat1 = s1.findSeat("A1");
                Seat seat2 = s1.findSeat("A2");
                if (seat1 != null) seat1.confirmBooking("BKG-SEED-01");
                if (seat2 != null) seat2.confirmBooking("BKG-SEED-01");

                Booking seedBooking = new Booking("BKG-SEED-01", c1, s1, Arrays.asList("A1", "A2"), 30.00, 60);
                seedBooking.confirm();
                bookings.add(seedBooking);

                PaymentTransaction seedTxn = new PaymentTransaction(
                        "TXN-SEED-01", "BKG-SEED-01", "Mock Credit/Debit Card", 30.00,
                        now.minusMinutes(10), PaymentStatus.SUCCESS, "AUTH-SEED-1234"
                );
                transactions.add(seedTxn);
            } catch (Exception ignored) {}

            shows.add(s1);
            shows.add(s2);
            shows.add(s3);
            shows.add(s4);
            shows.add(s5);
            shows.add(s6);
            shows.add(s7);

            logAudit("Initialized CineBook with 6 movies, 3 screens, 7 shows, and seed booking.");
        }
    }


    // SECTION 8: SERVICES
    public static class MovieService implements Searchable<Movie> {
        private final BookingManager manager;

        public MovieService(BookingManager manager) {
            this.manager = manager;
        }

        @Override
        public List<Movie> search(String query) {
            List<Movie> results = new ArrayList<>();
            for (Movie movie : manager.getMovies()) {
                if (movie.matches(query)) results.add(movie);
            }
            return results;
        }

        public List<Movie> search(String title, String genre) {
            List<Movie> results = new ArrayList<>();
            for (Movie movie : manager.getMovies()) {
                if (movie.matches(title, genre)) results.add(movie);
            }
            return results;
        }

        public synchronized void addMovie(Movie movie) throws ValidationException {
            if (movie == null) throw new ValidationException("Movie cannot be null.");
            if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
                throw new ValidationException("Movie title cannot be empty.");
            }
            if (movie.getDurationMinutes() <= 0) {
                throw new ValidationException("Duration must be a positive integer.");
            }
            manager.getMovies().add(movie);
            manager.logAudit("Added movie: " + movie.getTitle() + " (" + movie.getId() + ")");
        }

        public synchronized void updateMovie(String id, String title, String genre,
                                      int duration, String language, String cert, String desc)
                throws ValidationException {
            Movie movie = null;
            for (Movie m : manager.getMovies()) {
                if (m.getId().equalsIgnoreCase(id)) {
                    movie = m;
                    break;
                }
            }
            if (movie == null) throw new ValidationException("Movie with ID " + id + " not found.");
            if (title == null || title.trim().isEmpty()) throw new ValidationException("Title cannot be empty.");
            if (duration <= 0) throw new ValidationException("Duration must be positive.");

            movie.setTitle(title.trim());
            movie.setGenre(genre.trim());
            movie.setDurationMinutes(duration);
            movie.setLanguage(language.trim());
            movie.setCertification(cert.trim());
            movie.setDescription(desc.trim());
            manager.logAudit("Updated movie: " + movie.getTitle() + " (" + id + ")");
        }

        public synchronized void removeMovie(String movieId) throws ValidationException {
            for (Show show : manager.getShowsForMovie(movieId)) {
                for (Booking booking : manager.getBookings()) {
                    if (booking.getShow().getId().equals(show.getId())) {
                        throw new ValidationException("Cannot remove movie: Shows associated with this movie have active or past booking records.");
                    }
                }
            }

            manager.getShows().removeIf(s -> s.getMovie().getId().equals(movieId));
            boolean removed = manager.getMovies().removeIf(m -> m.getId().equals(movieId));
            if (!removed) {
                throw new ValidationException("Movie ID not found: " + movieId);
            }
            manager.logAudit("Safely removed movie ID: " + movieId);
        }

        public synchronized void addShow(Show show) throws ValidationException {
            if (show == null) throw new ValidationException("Show cannot be null.");
            if (show.getBasePrice() <= 0) throw new ValidationException("Base price must be greater than zero.");
            manager.getShows().add(show);
            manager.logAudit("Added show " + show.getId() + " for movie " + show.getMovie().getTitle());
        }

        public synchronized void removeShow(String showId) throws ValidationException {
            for (Booking booking : manager.getBookings()) {
                if (booking.getShow().getId().equals(showId)) {
                    throw new ValidationException("Cannot remove show: Booking records exist for Show ID " + showId);
                }
            }
            boolean removed = manager.getShows().removeIf(s -> s.getId().equals(showId));
            if (!removed) throw new ValidationException("Show ID not found: " + showId);
            manager.logAudit("Safely removed show ID: " + showId);
        }
    }


    public static class SeatManager {
        public synchronized void expireOutdatedReservations(Show show) {
            if (show == null) return;
            for (Seat[] row : show.getSeats()) {
                for (Seat seat : row) {
                    if (seat.getStatus() == SeatStatus.RESERVED &&
                            seat.getReservedUntil() != null &&
                            LocalDateTime.now().isAfter(seat.getReservedUntil())) {
                        seat.release(seat.getReservationBookingId());
                    }
                }
            }
        }


        public synchronized void releaseSeats(Show show, List<String> seatIds, String bookingId) {
            if (show == null || seatIds == null) return;
            for (String seatId : seatIds) {
                Seat seat = show.findSeat(seatId);
                if (seat != null) {
                    seat.release(bookingId);
                }
            }
        }

        public synchronized void confirmSeats(Show show, List<String> seatIds, String bookingId)
                throws SeatUnavailableException {
            for (String seatId : seatIds) {
                Seat seat = show.findSeat(seatId);
                if (seat == null) throw new SeatUnavailableException("Seat " + seatId + " not found.");
                seat.confirmBooking(bookingId);
            }
        }
    }



    public static class BookingService {
        private final BookingManager manager;
        private final SeatManager seatManager;

        public BookingService(BookingManager manager, SeatManager seatManager) {
            this.manager = manager;
            this.seatManager = seatManager;
        }


        public synchronized Booking createPendingReservation(Customer customer, Show show, List<String> seatIds)
                throws SeatUnavailableException, ValidationException {
            if (customer == null) throw new ValidationException("A valid customer must be specified.");
            if (show == null) throw new ValidationException("A valid show must be selected.");
            if (seatIds == null || seatIds.isEmpty()) {
                throw new ValidationException("At least one seat must be selected to proceed.");
            }


            seatManager.expireOutdatedReservations(show);


            double total = 0.0;
            for (String seatId : seatIds) {
                Seat seat = show.findSeat(seatId);
                if (seat == null) throw new SeatUnavailableException("Seat ID " + seatId + " does not exist.");
                if (!seat.isAvailable()) {
                    throw new SeatUnavailableException("Seat " + seatId + " is no longer available.");
                }
                total += show.calculateSeatPrice(seat);
            }


            String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            for (String seatId : seatIds) {
                Seat seat = show.findSeat(seatId);
                seat.reserve(bookingId, 5);
            }


            Booking booking = new Booking(bookingId, customer, show, seatIds, total, 5);
            manager.getBookings().add(booking);
            manager.logAudit("Created pending booking " + bookingId + " for " + customer.getName() + " (" + seatIds + ")");
            return booking;
        }


        public synchronized void cancelPendingReservation(Booking booking) {
            if (booking == null) return;
            if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT) {
                seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
                try {
                    booking.cancel("Checkout abandoned by user.");
                } catch (InvalidBookingStateException ignored) {}
                manager.logAudit("Pending reservation " + booking.getId() + " released and cancelled.");
            }
        }


        public synchronized void confirmBooking(Booking booking) throws InvalidBookingStateException, SeatUnavailableException {
            if (booking == null) throw new InvalidBookingStateException("Null booking provided.");

            if (booking.isExpired()) {
                seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
                booking.confirm();
            }

            seatManager.confirmSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
            booking.confirm();
            manager.logAudit("Confirmed booking " + booking.getId() + " for " + booking.getCustomer().getName());
        }


        public synchronized void cancelConfirmedBooking(String bookingId)
                throws BookingNotFoundException, InvalidBookingStateException {
            Booking booking = manager.findBookingById(bookingId);
            if (booking == null) {
                throw new BookingNotFoundException("Booking ID " + bookingId + " not found.");
            }

            if (!booking.isCancellable()) {
                throw new InvalidBookingStateException("Booking " + bookingId + " cannot be cancelled. Current status: " + booking.getBookingStatus());
            }

            seatManager.releaseSeats(booking.getShow(), booking.getSeatIds(), booking.getId());
            booking.cancel("Cancelled by customer request");

            PaymentTransaction refundTxn = new PaymentTransaction(
                    "TXN-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    booking.getId(),
                    "Simulated Refund",
                    booking.getTotalAmount(),
                    LocalDateTime.now(),
                    PaymentStatus.REFUNDED,
                    "REFUND-PROCESSED"
            );
            manager.getTransactions().add(refundTxn);
            manager.logAudit("Cancelled booking " + booking.getId() + ". Seats released and simulated refund recorded.");
        }
    }



    public static class PaymentProcessor {
        private final BookingManager manager;
        private final BookingService bookingService;

        public PaymentProcessor(BookingManager manager, BookingService bookingService) {
            this.manager = manager;
            this.bookingService = bookingService;
        }

        public synchronized PaymentTransaction processSimulatedPayment(
                Booking booking,
                PaymentMethod paymentMethod,
                String simulationMode
        ) throws PaymentProcessingException, InvalidBookingStateException, SeatUnavailableException {

            if (booking == null) {
                throw new PaymentProcessingException("No booking associated with payment.");
            }

            if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
                throw new InvalidBookingStateException("Booking " + booking.getId() + " is already confirmed. Duplicate payment prevented.");
            }

            if (booking.isExpired()) {
                bookingService.cancelPendingReservation(booking);
                throw new PaymentProcessingException("Checkout failed: Your 5-minute reservation window has expired. Please select seats again.");
            }

            String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            if ("USER_CANCEL".equalsIgnoreCase(simulationMode)) {
                bookingService.cancelPendingReservation(booking);
                PaymentTransaction txn = new PaymentTransaction(
                        txnId, booking.getId(), paymentMethod.getMethodName(),
                        booking.getTotalAmount(), LocalDateTime.now(),
                        PaymentStatus.CANCELLED, "USER-ABORT"
                );
                manager.getTransactions().add(txn);
                manager.logAudit("Payment cancelled by user for booking " + booking.getId());
                return txn;
            }

            if ("FORCE_FAIL".equalsIgnoreCase(simulationMode)) {
                booking.markPaymentFailed();
                bookingService.cancelPendingReservation(booking);
                PaymentTransaction txn = new PaymentTransaction(
                        txnId, booking.getId(), paymentMethod.getMethodName(),
                        booking.getTotalAmount(), LocalDateTime.now(),
                        PaymentStatus.FAILED, "ERR-DECLINED"
                );
                manager.getTransactions().add(txn);
                manager.logAudit("Payment forcefully failed for booking " + booking.getId());
                throw new PaymentProcessingException("Simulated payment was declined by the issuer.");
            }

            boolean authorized = paymentMethod.executeTransaction(booking.getTotalAmount());
            if (!authorized) {
                booking.markPaymentFailed();
                bookingService.cancelPendingReservation(booking);
                throw new PaymentProcessingException("Payment authorization failed.");
            }


            bookingService.confirmBooking(booking);

            PaymentTransaction successTxn = new PaymentTransaction(
                    txnId, booking.getId(), paymentMethod.getMethodName(),
                    booking.getTotalAmount(), LocalDateTime.now(),
                    PaymentStatus.SUCCESS, "AUTH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()
            );
            manager.getTransactions().add(successTxn);
            manager.logAudit("Payment successful (" + successTxn.getId() + ") for booking " + booking.getId());
            return successTxn;
        }
    }


    // (Style constants defined in Section 9 MainFrame)


    // SECTION 9: GUI STYLING & MAINFRAME
    public static final Color COLOR_PRIMARY = new Color(28, 64, 114);
    public static final Color COLOR_PRIMARY_DARK = new Color(18, 42, 75);
    public static final Color COLOR_BG_LIGHT = new Color(248, 249, 250);
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    public static final Color COLOR_BORDER = new Color(222, 226, 230);
    public static final Color COLOR_SEAT_AVAILABLE = new Color(230, 244, 234);
    public static final Color COLOR_SEAT_SELECTED = new Color(25, 135, 84);
    public static final Color COLOR_SEAT_RESERVED = new Color(255, 193, 7);
    public static final Color COLOR_SEAT_BOOKED = new Color(108, 117, 125);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static class MainFrame extends JFrame {
        private final BookingManager manager;
        private final MovieService movieService;
        private final SeatManager seatManager;
        private final BookingService bookingService;
        private final PaymentProcessor paymentProcessor;

        private final CardLayout cardLayout;
        private final JPanel contentPanel;

        private MoviePanel moviePanel;
        private MovieDetailsPanel movieDetailsPanel;
        private SeatSelectionPanel seatSelectionPanel;
        private CheckoutPanel checkoutPanel;
        private BookingConfirmationPanel confirmationPanel;
        private BookingHistoryPanel historyPanel;
        private AdminPanel adminPanel;

        private JLabel userStatusLabel;

        public MainFrame() {
            super("CineBook - Movie Ticket Booking System");
            this.manager = BookingManager.getInstance();
            this.movieService = new MovieService(manager);
            this.seatManager = new SeatManager();
            this.bookingService = new BookingService(manager, seatManager);
            this.paymentProcessor = new PaymentProcessor(manager, bookingService);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1100, 750);
            setMinimumSize(new Dimension(950, 650));
            setLocationRelativeTo(null);

            setLayout(new BorderLayout());

            add(createTopNavBar(), BorderLayout.NORTH);

            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);

            moviePanel = new MoviePanel(this, movieService);
            movieDetailsPanel = new MovieDetailsPanel(this, manager);
            seatSelectionPanel = new SeatSelectionPanel(this, bookingService);
            checkoutPanel = new CheckoutPanel(this, paymentProcessor, bookingService);
            confirmationPanel = new BookingConfirmationPanel(this);
            historyPanel = new BookingHistoryPanel(this, manager, bookingService);
            adminPanel = new AdminPanel(this, manager, movieService);

            contentPanel.add(moviePanel, "MOVIES");
            contentPanel.add(movieDetailsPanel, "MOVIE_DETAILS");
            contentPanel.add(seatSelectionPanel, "SEAT_SELECTION");
            contentPanel.add(checkoutPanel, "CHECKOUT");
            contentPanel.add(confirmationPanel, "CONFIRMATION");
            contentPanel.add(historyPanel, "HISTORY");
            contentPanel.add(adminPanel, "ADMIN");

            add(contentPanel, BorderLayout.CENTER);

            showMoviesScreen();
        }

        private JPanel createTopNavBar() {
            JPanel nav = new JPanel(new BorderLayout());
            nav.setBackground(COLOR_PRIMARY);
            nav.setBorder(new EmptyBorder(12, 20, 12, 20));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            leftPanel.setOpaque(false);

            JLabel logoLabel = new JLabel("CINEBOOK");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logoLabel.setForeground(Color.WHITE);
            leftPanel.add(logoLabel);

            JButton btnMovies = createNavButton("Browse Movies");
            btnMovies.addActionListener(e -> showMoviesScreen());
            leftPanel.add(btnMovies);

            JButton btnHistory = createNavButton("My Bookings");
            btnHistory.addActionListener(e -> showHistoryScreen());
            leftPanel.add(btnHistory);

            JButton btnAdmin = createNavButton("Admin Console");
            btnAdmin.addActionListener(e -> showAdminScreen());
            leftPanel.add(btnAdmin);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightPanel.setOpaque(false);

            userStatusLabel = new JLabel("User: " + manager.getCurrentUser().getName());
            userStatusLabel.setFont(FONT_REGULAR);
            userStatusLabel.setForeground(Color.WHITE);
            rightPanel.add(userStatusLabel);

            JButton switchUserBtn = new JButton("Switch User");
            switchUserBtn.setFont(FONT_SMALL);
            switchUserBtn.setBackground(COLOR_PRIMARY_DARK);
            switchUserBtn.setForeground(Color.WHITE);
            switchUserBtn.setFocusPainted(false);
            switchUserBtn.addActionListener(e -> promptSwitchUser());
            rightPanel.add(switchUserBtn);

            nav.add(leftPanel, BorderLayout.WEST);
            nav.add(rightPanel, BorderLayout.EAST);
            return nav;
        }

        private JButton createNavButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_BOLD);
            btn.setForeground(Color.WHITE);
            btn.setBackground(COLOR_PRIMARY_DARK);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(255, 255, 255, 60), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void promptSwitchUser() {
            List<User> allUsers = new ArrayList<>();
            allUsers.addAll(manager.getCustomers());
            allUsers.addAll(manager.getAdminUsers());

            String[] userNames = allUsers.stream()
                    .map(u -> u.getName() + " (" + u.getRole() + ")")
                    .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "Select active user account:",
                    "Switch User Profile",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userNames,
                    userNames[0]
            );

            if (selected != null) {
                for (User u : allUsers) {
                    if (selected.startsWith(u.getName())) {
                        manager.setCurrentUser(u);
                        userStatusLabel.setText("User: " + u.getName());
                        JOptionPane.showMessageDialog(this, u.getDashboardGreeting(), "User Switched", JOptionPane.INFORMATION_MESSAGE);
                        if (historyPanel != null) historyPanel.refreshHistory();
                        break;
                    }
                }
            }
        }

        public void showMoviesScreen() {
            moviePanel.refreshList();
            cardLayout.show(contentPanel, "MOVIES");
        }

        public void navigateToMovieDetails(Movie movie) {
            movieDetailsPanel.setMovie(movie);
            cardLayout.show(contentPanel, "MOVIE_DETAILS");
        }

        public void navigateToSeatSelection(Show show) {
            seatSelectionPanel.setShow(show);
            cardLayout.show(contentPanel, "SEAT_SELECTION");
        }

        public void navigateToCheckout(Booking booking) {
            checkoutPanel.setBooking(booking);
            cardLayout.show(contentPanel, "CHECKOUT");
        }

        public void navigateToConfirmation(Booking booking, PaymentTransaction txn) {
            confirmationPanel.displayConfirmation(booking, txn);
            cardLayout.show(contentPanel, "CONFIRMATION");
        }

        public void showHistoryScreen() {
            historyPanel.refreshHistory();
            cardLayout.show(contentPanel, "HISTORY");
        }

        public void showAdminScreen() {
            adminPanel.refreshData();
            cardLayout.show(contentPanel, "ADMIN");
        }
    }


    // SECTION 10: SCREEN 1 MOVIES & SCREEN 2 DETAILS
    public static class MoviePanel extends JPanel {
        private final MainFrame parent;
        private final MovieService movieService;

        private JTextField searchField;
        private JComboBox<String> genreFilter;
        private JPanel moviesGridPanel;
        private JLabel resultsCountLabel;

        public MoviePanel(MainFrame parent, MovieService movieService) {
            this.parent = parent;
            this.movieService = movieService;
            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(16, 24, 16, 24));

            initializeUI(this.movieService);
        }

        private void initializeUI(MovieService mvService) {
            JPanel filterCard = new JPanel(new BorderLayout(12, 12));
            filterCard.setBackground(Color.WHITE);
            filterCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(14, 18, 14, 18)
            ));

            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            titlePanel.setOpaque(false);
            JLabel sectionTitle = new JLabel("Now Showing in Cinemas");
            sectionTitle.setFont(FONT_TITLE);
            sectionTitle.setForeground(COLOR_TEXT_DARK);
            titlePanel.add(sectionTitle);

            JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            controlsPanel.setOpaque(false);

            searchField = new JTextField(16);
            searchField.setFont(FONT_REGULAR);
            searchField.setToolTipText("Search by movie title or keyword");

            genreFilter = new JComboBox<>(new String[]{"All", "Sci-Fi", "Action", "Comedy", "Thriller", "Drama", "Animation"});
            genreFilter.setFont(FONT_REGULAR);

            JButton searchBtn = new JButton("Search");
            searchBtn.setFont(FONT_BOLD);
            searchBtn.setBackground(COLOR_PRIMARY);
            searchBtn.setForeground(Color.WHITE);
            searchBtn.setFocusPainted(false);
            searchBtn.addActionListener(e -> filterMovies());

            JButton resetBtn = new JButton("Reset");
            resetBtn.setFont(FONT_REGULAR);
            resetBtn.setBackground(Color.WHITE);
            resetBtn.addActionListener(e -> {
                searchField.setText("");
                genreFilter.setSelectedIndex(0);
                filterMovies();
            });


            controlsPanel.add(new JLabel("Title:"));
            controlsPanel.add(searchField);
            controlsPanel.add(new JLabel("Genre:"));
            controlsPanel.add(genreFilter);
            controlsPanel.add(searchBtn);
            controlsPanel.add(resetBtn);


            filterCard.add(titlePanel, BorderLayout.WEST);
            filterCard.add(controlsPanel, BorderLayout.EAST);
            add(filterCard, BorderLayout.NORTH);

            moviesGridPanel = new JPanel();
            moviesGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
            moviesGridPanel.setBackground(COLOR_BG_LIGHT);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(COLOR_BG_LIGHT);
            wrapper.add(moviesGridPanel, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(wrapper);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);

            resultsCountLabel = new JLabel("Loading movies...");
            resultsCountLabel.setFont(FONT_SMALL);
            resultsCountLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
            add(resultsCountLabel, BorderLayout.SOUTH);

            refreshList();
        }

        public void refreshList() { filterMovies(); }

        private void filterMovies() {
            String query = searchField.getText().trim();
            String genre = (String) genreFilter.getSelectedItem();

            List<Movie> matched = movieService.search(query, genre);
            moviesGridPanel.removeAll();

            if (matched.isEmpty()) {
                moviesGridPanel.setLayout(new BorderLayout());
                JLabel noData = new JLabel("No movies found matching your criteria.", SwingConstants.CENTER);
                noData.setFont(FONT_HEADING);
                noData.setForeground(Color.GRAY);
                noData.setBorder(new EmptyBorder(50, 0, 50, 0));
                moviesGridPanel.add(noData, BorderLayout.CENTER);
            } else {
                moviesGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
                for (Movie m : matched) {
                    moviesGridPanel.add(createMovieCard(m));
                }
            }

            resultsCountLabel.setText("Showing " + matched.size() + " movie(s)");
            moviesGridPanel.revalidate();
            moviesGridPanel.repaint();
        }

        private JPanel createMovieCard(Movie movie) {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBackground(COLOR_CARD_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(14, 14, 14, 14)
            ));

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel title = new JLabel(movie.getTitle());
            title.setFont(FONT_HEADING);
            title.setForeground(COLOR_TEXT_DARK);

            JLabel meta = new JLabel(movie.getGenre() + " - " + movie.getDurationMinutes() + " mins - " + movie.getCertification());
            meta.setFont(FONT_SMALL);
            meta.setForeground(new Color(100, 110, 120));

            JLabel lang = new JLabel("Language: " + movie.getLanguage());
            lang.setFont(FONT_SMALL);
            lang.setForeground(Color.GRAY);

            JTextArea desc = new JTextArea(movie.getDescription());
            desc.setFont(FONT_REGULAR);
            desc.setForeground(new Color(70, 70, 70));
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setRows(3);
            desc.setBorder(new EmptyBorder(6, 0, 6, 0));

            infoPanel.add(title);
            infoPanel.add(Box.createVerticalStrut(4));
            infoPanel.add(meta);
            infoPanel.add(lang);
            infoPanel.add(Box.createVerticalStrut(6));
            infoPanel.add(desc);

            card.add(infoPanel, BorderLayout.CENTER);

            JButton bookBtn = new JButton("View Details & Shows");
            bookBtn.setFont(FONT_BOLD);
            bookBtn.setBackground(COLOR_PRIMARY);
            bookBtn.setForeground(Color.WHITE);
            bookBtn.setFocusPainted(false);
            bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bookBtn.addActionListener(e -> parent.navigateToMovieDetails(movie));

            card.add(bookBtn, BorderLayout.SOUTH);
            return card;
        }
    }


    public static class MovieDetailsPanel extends JPanel {
        private final MainFrame parent;
        private final BookingManager manager;

        private Movie currentMovie;
        private JLabel titleLabel;
        private JLabel metaLabel;
        private JTextArea synopsisArea;
        private JPanel showsContainer;

        public MovieDetailsPanel(MainFrame parent, BookingManager manager) {
            this.parent = parent;
            this.manager = manager;
            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(20, 30, 20, 30));

            initializeUI();
        }

        private void initializeUI() {
            JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            topNav.setOpaque(false);
            JButton backBtn = new JButton("<< Back to Movie Catalogue");
            backBtn.setFocusPainted(false);
            backBtn.setFont(FONT_SMALL);
            backBtn.setBackground(COLOR_BG_LIGHT);
            backBtn.addActionListener(e -> parent.showMoviesScreen());
            topNav.add(backBtn);
            add(topNav, BorderLayout.NORTH);

            JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

            JPanel detailCard = new JPanel();
            detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
            detailCard.setBackground(Color.WHITE);
            detailCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(20, 24, 20, 24)
            ));

            titleLabel = new JLabel("Movie Title");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            titleLabel.setForeground(COLOR_PRIMARY);

            metaLabel = new JLabel("Genre - Duration - Rating");
            metaLabel.setFont(FONT_REGULAR);
            metaLabel.setForeground(Color.GRAY);

            synopsisArea = new JTextArea();
            synopsisArea.setFont(FONT_REGULAR);
            synopsisArea.setLineWrap(true);
            synopsisArea.setWrapStyleWord(true);
            synopsisArea.setEditable(false);
            synopsisArea.setOpaque(false);
            synopsisArea.setBorder(new EmptyBorder(10, 0, 10, 0));

            detailCard.add(titleLabel);
            detailCard.add(Box.createVerticalStrut(6));
            detailCard.add(metaLabel);
            detailCard.add(Box.createVerticalStrut(10));
            detailCard.add(new JSeparator());
            detailCard.add(Box.createVerticalStrut(10));
            detailCard.add(synopsisArea);

            contentPanel.add(detailCard, BorderLayout.NORTH);

            JPanel showtimesCard = new JPanel(new BorderLayout(10, 10));
            showtimesCard.setBackground(Color.WHITE);
            showtimesCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(18, 24, 18, 24)
            ));

            JLabel showTitle = new JLabel("Available Showtimes for this Movie");
            showTitle.setFont(FONT_HEADING);
            showTitle.setForeground(COLOR_TEXT_DARK);
            showtimesCard.add(showTitle, BorderLayout.NORTH);

            showsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
            showsContainer.setOpaque(false);

            JScrollPane showScroll = new JScrollPane(showsContainer);
            showScroll.setBorder(null);
            showtimesCard.add(showScroll, BorderLayout.CENTER);

            contentPanel.add(showtimesCard, BorderLayout.CENTER);
            add(contentPanel, BorderLayout.CENTER);
        }

        public void setMovie(Movie movie) {
            this.currentMovie = movie;
            titleLabel.setText(movie.getTitle());
            metaLabel.setText(movie.getGenre() + " - " + movie.getDurationMinutes() + " mins - " +
                    movie.getCertification() + " - " + movie.getLanguage());
            synopsisArea.setText("Synopsis:\n" + movie.getDescription());

            showsContainer.removeAll();
            List<Show> movieShows = manager.getShowsForMovie(movie.getId());

            if (movieShows.isEmpty()) {
                JLabel noShows = new JLabel("No scheduled showtimes available for this title.");
                noShows.setFont(FONT_REGULAR);
                noShows.setForeground(Color.GRAY);
                showsContainer.add(noShows);
            } else {
                for (Show show : movieShows) {
                    showsContainer.add(createShowtimeButton(show));
                }
            }

            showsContainer.revalidate();
            showsContainer.repaint();
        }

        private JPanel createShowtimeButton(Show show) {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setPreferredSize(new Dimension(240, 110));
            card.setBackground(COLOR_BG_LIGHT);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_PRIMARY, 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));

            JLabel timeLabel = new JLabel(show.getShowDateTime().format(DATE_TIME_FORMATTER));
            timeLabel.setFont(FONT_BOLD);
            timeLabel.setForeground(COLOR_PRIMARY);

            JLabel priceLabel = new JLabel(String.format("Base Ticket: $%.2f", show.getBasePrice()));
            priceLabel.setFont(FONT_REGULAR);

            JLabel seatLabel = new JLabel(show.getAvailableSeatsCount() + " of " + show.getTotalSeatsCount() + " seats available");
            seatLabel.setFont(FONT_SMALL);
            seatLabel.setForeground(new Color(25, 135, 84));

            JPanel textPanel = new JPanel(new GridLayout(3, 1, 2, 2));
            textPanel.setOpaque(false);
            textPanel.add(timeLabel);
            textPanel.add(priceLabel);
            textPanel.add(seatLabel);
            card.add(textPanel, BorderLayout.CENTER);

            JButton selectBtn = new JButton("Select Seats");
            selectBtn.setFont(FONT_SMALL);
            selectBtn.setBackground(COLOR_PRIMARY);
            selectBtn.setForeground(Color.WHITE);
            selectBtn.setFocusPainted(false);
            selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            selectBtn.addActionListener(e -> parent.navigateToSeatSelection(show));
            card.add(selectBtn, BorderLayout.SOUTH);

            return card;
        }
    }


    // SECTION 11: SCREEN 3 SEAT SELECTION
    public static class SeatSelectionPanel extends JPanel {
        private final MainFrame parent;
        private final BookingService bookingService;

        private Show currentShow;
        private final Set<String> selectedSeatIds = new HashSet<>();
        private final Map<String, JButton> seatButtons = new HashMap<>();

        private JLabel showInfoLabel;
        private JLabel selectedSeatsLabel;
        private JLabel priceBreakdownLabel;
        private JButton checkoutButton;
        private JPanel gridPanel;

        public SeatSelectionPanel(MainFrame parent, BookingService bookingService) {
            this.parent = parent;
            this.bookingService = bookingService;
            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(16, 24, 16, 24));

            initializeUI();
        }

        private void initializeUI() {
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(Color.WHITE);
            topBar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            JButton backBtn = new JButton("<< Back to Movie Details");
            backBtn.setFont(FONT_SMALL);
            backBtn.setBackground(COLOR_BG_LIGHT);
            backBtn.setFocusPainted(false);
            backBtn.addActionListener(e -> {
                if (currentShow != null) parent.navigateToMovieDetails(currentShow.getMovie());
                else parent.showMoviesScreen();
            });

            showInfoLabel = new JLabel("Select your seats");
            showInfoLabel.setFont(FONT_HEADING);
            showInfoLabel.setForeground(COLOR_TEXT_DARK);

            topBar.add(backBtn, BorderLayout.WEST);
            topBar.add(showInfoLabel, BorderLayout.CENTER);
            add(topBar, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
            centerPanel.setBackground(COLOR_BG_LIGHT);
            centerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

            JPanel screenIndicator = new JPanel();
            screenIndicator.setBackground(new Color(200, 210, 225));
            screenIndicator.setBorder(new LineBorder(COLOR_PRIMARY, 2, true));
            screenIndicator.setPreferredSize(new Dimension(600, 36));
            JLabel screenText = new JLabel("THEATER SCREEN THIS WAY (ALL EYES FRONT)");
            screenText.setFont(FONT_BOLD);
            screenText.setForeground(COLOR_PRIMARY_DARK);
            screenIndicator.add(screenText);

            JPanel screenWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            screenWrapper.setOpaque(false);
            screenWrapper.add(screenIndicator);
            centerPanel.add(screenWrapper, BorderLayout.NORTH);

            gridPanel = new JPanel();
            gridPanel.setOpaque(false);
            JScrollPane scrollGrid = new JScrollPane(gridPanel);
            scrollGrid.setBorder(null);
            scrollGrid.setOpaque(false);
            scrollGrid.getViewport().setOpaque(false);
            centerPanel.add(scrollGrid, BorderLayout.CENTER);

            JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            legendPanel.setOpaque(false);
            legendPanel.add(createLegendItem("Available", COLOR_SEAT_AVAILABLE));
            legendPanel.add(createLegendItem("Selected", COLOR_SEAT_SELECTED));
            legendPanel.add(createLegendItem("Reserved", COLOR_SEAT_RESERVED));
            legendPanel.add(createLegendItem("Booked", COLOR_SEAT_BOOKED));
            centerPanel.add(legendPanel, BorderLayout.SOUTH);

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new BorderLayout());
            bottomBar.setBackground(Color.WHITE);
            bottomBar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(14, 18, 14, 18)
            ));

            JPanel summaryInfo = new JPanel(new GridLayout(2, 1, 4, 4));
            summaryInfo.setOpaque(false);

            selectedSeatsLabel = new JLabel("Selected Seats: None");
            selectedSeatsLabel.setFont(FONT_BOLD);
            selectedSeatsLabel.setForeground(COLOR_TEXT_DARK);

            priceBreakdownLabel = new JLabel("Subtotal: $0.00 | Total with Tax (18%): $0.00");
            priceBreakdownLabel.setFont(FONT_REGULAR);
            priceBreakdownLabel.setForeground(new Color(90, 90, 90));

            summaryInfo.add(selectedSeatsLabel);
            summaryInfo.add(priceBreakdownLabel);

            checkoutButton = new JButton("Proceed to Checkout >>");
            checkoutButton.setFont(FONT_BOLD);
            checkoutButton.setBackground(COLOR_PRIMARY);
            checkoutButton.setForeground(Color.WHITE);
            checkoutButton.setFocusPainted(false);
            checkoutButton.setEnabled(false);
            checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkoutButton.addActionListener(e -> proceedToCheckout());

            bottomBar.add(summaryInfo, BorderLayout.CENTER);
            bottomBar.add(checkoutButton, BorderLayout.EAST);
            add(bottomBar, BorderLayout.SOUTH);
        }

        private JPanel createLegendItem(String label, Color color) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setOpaque(false);
            JPanel box = new JPanel();
            box.setPreferredSize(new Dimension(18, 18));
            box.setBackground(color);
            box.setBorder(new LineBorder(Color.GRAY, 1));
            JLabel lbl = new JLabel(label);
            lbl.setFont(FONT_SMALL);
            p.add(box);
            p.add(lbl);
            return p;
        }

        public void setShow(Show show) {
            this.currentShow = show;
            this.selectedSeatIds.clear();
            this.seatButtons.clear();

            showInfoLabel.setText("  " + show.getMovie().getTitle() + " - " +
                    show.getScreen().getName() + " (" + show.getShowDateTime().format(DATE_TIME_FORMATTER) + ")");

            buildSeatGrid();
            updateSummary();
        }

        private void buildSeatGrid() {
            gridPanel.removeAll();
            if (currentShow == null) return;

            Seat[][] seats = currentShow.getSeats();
            int rows = currentShow.getScreen().getRows();
            int cols = currentShow.getScreen().getColumns();

            gridPanel.setLayout(new GridLayout(rows, cols + 1, 8, 8));

            for (int r = 0; r < rows; r++) {
                char rowChar = (char) ('A' + r);
                JLabel rowLbl = new JLabel(String.valueOf(rowChar), SwingConstants.CENTER);
                rowLbl.setFont(FONT_BOLD);
                gridPanel.add(rowLbl);

                for (int c = 0; c < cols; c++) {
                    Seat seat = seats[r][c];
                    JButton seatBtn = new JButton(seat.getId());
                    seatBtn.setFont(FONT_SMALL);
                    seatBtn.setFocusPainted(false);
                    seatBtn.setMargin(new Insets(2, 2, 2, 2));
                    seatBtn.setPreferredSize(new Dimension(50, 40));

                    updateSeatButtonAppearance(seatBtn, seat);

                    seatBtn.addActionListener(e -> toggleSeatSelection(seat, seatBtn));

                    seatButtons.put(seat.getId(), seatBtn);
                    gridPanel.add(seatBtn);
                }
            }

            gridPanel.revalidate();
            gridPanel.repaint();
        }

        private void updateSeatButtonAppearance(JButton btn, Seat seat) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                btn.setBackground(COLOR_SEAT_BOOKED);
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
                btn.setToolTipText(seat.getId() + " - Booked");
            } else if (seat.getStatus() == SeatStatus.RESERVED && !seat.isAvailable()) {
                btn.setBackground(COLOR_SEAT_RESERVED);
                btn.setForeground(Color.BLACK);
                btn.setEnabled(false);
                btn.setToolTipText(seat.getId() + " - In Checkout by another user");
            } else if (selectedSeatIds.contains(seat.getId())) {
                btn.setBackground(COLOR_SEAT_SELECTED);
                btn.setForeground(Color.WHITE);
                btn.setEnabled(true);
                btn.setToolTipText(seat.getId() + " - Selected ($" + String.format("%.2f", currentShow.calculateSeatPrice(seat)) + ")");
            } else {
                btn.setBackground(COLOR_SEAT_AVAILABLE);
                btn.setForeground(COLOR_TEXT_DARK);
                btn.setEnabled(true);
                btn.setToolTipText(seat.getId() + " (" + seat.getCategory().getLabel() + ") - $" +
                        String.format("%.2f", currentShow.calculateSeatPrice(seat)));
            }
        }

        private void toggleSeatSelection(Seat seat, JButton btn) {
            if (selectedSeatIds.contains(seat.getId())) {
                selectedSeatIds.remove(seat.getId());
            } else {
                selectedSeatIds.add(seat.getId());
            }
            updateSeatButtonAppearance(btn, seat);
            updateSummary();
        }

        private void updateSummary() {
            if (selectedSeatIds.isEmpty()) {
                selectedSeatsLabel.setText("Selected Seats: None");
                priceBreakdownLabel.setText("Subtotal: $0.00 | Total with Tax (18%): $0.00");
                checkoutButton.setEnabled(false);
                return;
            }

            double subtotal = 0.0;
            List<String> sortedList = new ArrayList<>(selectedSeatIds);
            Collections.sort(sortedList);

            for (String sId : sortedList) {
                Seat seat = currentShow.findSeat(sId);
                if (seat != null) subtotal += currentShow.calculateSeatPrice(seat);
            }

            double tax = Math.round(subtotal * 0.18 * 100.0) / 100.0;
            double grandTotal = Math.round((subtotal + tax) * 100.0) / 100.0;

            selectedSeatsLabel.setText("Selected Seats: " + String.join(", ", sortedList) + " (" + sortedList.size() + " seat(s))");
            priceBreakdownLabel.setText(String.format("Subtotal: $%.2f | Tax (18%% GST): $%.2f | Total Payable: $%.2f",
                    subtotal, tax, grandTotal));
            checkoutButton.setEnabled(true);
        }

        private void proceedToCheckout() {
            if (selectedSeatIds.isEmpty() || currentShow == null) return;

            User currentUser = BookingManager.getInstance().getCurrentUser();
            if (!(currentUser instanceof Customer)) {
                JOptionPane.showMessageDialog(this,
                        "Only Customers can book tickets. Please switch to a Customer account using the top-right button.",
                        "Customer Role Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Customer customer = (Customer) currentUser;
                List<String> seatsToBook = new ArrayList<>(selectedSeatIds);
                Booking booking = bookingService.createPendingReservation(customer, currentShow, seatsToBook);
                parent.navigateToCheckout(booking);
            } catch (SeatUnavailableException | ValidationException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not reserve seats: " + ex.getMessage(),
                        "Reservation Conflict", JOptionPane.ERROR_MESSAGE);
                buildSeatGrid();
                updateSummary();
            }
        }
    }


    // SECTION 12: SCREEN 4 CHECKOUT
    public static class CheckoutPanel extends JPanel {
        private final MainFrame parent;
        private final PaymentProcessor paymentProcessor;
        private final BookingService bookingService;

        private Booking currentBooking;

        private JLabel bookingIdLabel;
        private JLabel movieDetailsLabel;
        private JLabel seatListLabel;
        private JLabel timerLabel;
        private JLabel amountPayableLabel;

        private JComboBox<String> paymentMethodCombo;
        private JComboBox<String> simulationModeCombo;
        private JPanel dynamicPaymentFieldsPanel;
        private JTextField cardNumField;
        private JTextField cardHolderField;
        private JTextField upiField;
        private JTextField walletBalanceField;

        private javax.swing.Timer countdownTimer;
        private int remainingSeconds = 300; // 5 minutes

        public CheckoutPanel(MainFrame parent, PaymentProcessor paymentProcessor, BookingService bookingService) {
            this.parent = parent;
            this.paymentProcessor = paymentProcessor;
            this.bookingService = bookingService;

            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(16, 24, 16, 24));

            initializeUI();
        }

        private void initializeUI() {
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(Color.WHITE);
            topBar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            JButton cancelBtn = new JButton("<< Cancel & Release Seats");
            cancelBtn.setFont(FONT_SMALL);
            cancelBtn.setBackground(new Color(254, 237, 237));
            cancelBtn.setForeground(new Color(180, 40, 40));
            cancelBtn.setFocusPainted(false);
            cancelBtn.addActionListener(e -> cancelAndAbandon());

            JLabel headerTitle = new JLabel("Checkout & Simulated Payment");
            headerTitle.setFont(FONT_HEADING);
            headerTitle.setForeground(COLOR_TEXT_DARK);

            timerLabel = new JLabel("Time Remaining: 05:00");
            timerLabel.setFont(FONT_BOLD);
            timerLabel.setForeground(new Color(200, 35, 51));

            topBar.add(cancelBtn, BorderLayout.WEST);
            topBar.add(headerTitle, BorderLayout.CENTER);
            topBar.add(timerLabel, BorderLayout.EAST);
            add(topBar, BorderLayout.NORTH);

            JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
            centerGrid.setBackground(COLOR_BG_LIGHT);
            centerGrid.setBorder(new EmptyBorder(16, 0, 16, 0));

            // Left Card: Booking Order Summary
            JPanel summaryCard = new JPanel();
            summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
            summaryCard.setBackground(Color.WHITE);
            summaryCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(18, 18, 18, 18)
            ));

            JLabel summaryTitle = new JLabel("Booking Order Summary");
            summaryTitle.setFont(FONT_TITLE);
            summaryTitle.setForeground(COLOR_PRIMARY_DARK);

            bookingIdLabel = new JLabel("Booking Reference: -");
            bookingIdLabel.setFont(FONT_BOLD);

            movieDetailsLabel = new JLabel("Movie: -");
            movieDetailsLabel.setFont(FONT_REGULAR);

            seatListLabel = new JLabel("Seats: -");
            seatListLabel.setFont(FONT_REGULAR);

            amountPayableLabel = new JLabel("Total Payable: $0.00");
            amountPayableLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            amountPayableLabel.setForeground(new Color(25, 135, 84));

            summaryCard.add(summaryTitle);
            summaryCard.add(Box.createVerticalStrut(12));
            summaryCard.add(bookingIdLabel);
            summaryCard.add(Box.createVerticalStrut(8));
            summaryCard.add(movieDetailsLabel);
            summaryCard.add(Box.createVerticalStrut(8));
            summaryCard.add(seatListLabel);
            summaryCard.add(Box.createVerticalStrut(16));
            summaryCard.add(new JSeparator());
            summaryCard.add(Box.createVerticalStrut(16));
            summaryCard.add(amountPayableLabel);

            // Right Card: Payment Configuration
            JPanel payCard = new JPanel();
            payCard.setLayout(new BoxLayout(payCard, BoxLayout.Y_AXIS));
            payCard.setBackground(Color.WHITE);
            payCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(18, 18, 18, 18)
            ));

            JLabel payTitle = new JLabel("Payment Options (Simulated Gateway)");
            payTitle.setFont(FONT_TITLE);
            payTitle.setForeground(COLOR_PRIMARY_DARK);

            paymentMethodCombo = new JComboBox<>(new String[]{
                    "Mock Credit/Debit Card",
                    "Mock UPI Instant Pay",
                    "Mock Digital Wallet",
                    "Legacy Bank DirectDebit (Adapter Pattern)"
            });
            paymentMethodCombo.setFont(FONT_REGULAR);
            paymentMethodCombo.addActionListener(e -> updatePaymentFormFields());

            simulationModeCombo = new JComboBox<>(new String[]{
                    "SUCCESS (Approved)",
                    "FORCE_FAIL (Bank Decline Simulation)",
                    "USER_CANCEL (Simulated Abort)"
            });
            simulationModeCombo.setFont(FONT_REGULAR);

            dynamicPaymentFieldsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
            dynamicPaymentFieldsPanel.setBackground(Color.WHITE);
            dynamicPaymentFieldsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

            cardNumField = new JTextField("4111 2222 3333 4444");
            cardHolderField = new JTextField("Aarav Sharma");
            upiField = new JTextField("aarav@okhdfcbank");
            walletBalanceField = new JTextField("150.00");

            updatePaymentFormFields();

            JButton payButton = new JButton("Pay & Confirm Booking");
            payButton.setFont(FONT_BOLD);
            payButton.setBackground(new Color(25, 135, 84));
            payButton.setForeground(Color.WHITE);
            payButton.setFocusPainted(false);
            payButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            payButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            payButton.addActionListener(e -> executePayment());

            payCard.add(payTitle);
            payCard.add(Box.createVerticalStrut(12));
            payCard.add(new JLabel("Select Payment Channel:"));
            payCard.add(Box.createVerticalStrut(4));
            payCard.add(paymentMethodCombo);
            payCard.add(Box.createVerticalStrut(10));
            payCard.add(new JLabel("Simulation Outcome Mode (Viva Defense):"));
            payCard.add(Box.createVerticalStrut(4));
            payCard.add(simulationModeCombo);
            payCard.add(Box.createVerticalStrut(10));
            payCard.add(dynamicPaymentFieldsPanel);
            payCard.add(Box.createVerticalStrut(15));
            payCard.add(payButton);

            centerGrid.add(summaryCard);
            centerGrid.add(payCard);

            add(centerGrid, BorderLayout.CENTER);
        }

        private void updatePaymentFormFields() {
            dynamicPaymentFieldsPanel.removeAll();
            int selectedIdx = paymentMethodCombo.getSelectedIndex();

            if (selectedIdx == 0) { // Card
                dynamicPaymentFieldsPanel.add(new JLabel("Card Number:"));
                dynamicPaymentFieldsPanel.add(cardNumField);
                dynamicPaymentFieldsPanel.add(new JLabel("Name on Card:"));
                dynamicPaymentFieldsPanel.add(cardHolderField);
            } else if (selectedIdx == 1) { // UPI
                dynamicPaymentFieldsPanel.add(new JLabel("Virtual UPI ID:"));
                dynamicPaymentFieldsPanel.add(upiField);
            } else if (selectedIdx == 2) { // Wallet
                dynamicPaymentFieldsPanel.add(new JLabel("Wallet Balance:"));
                dynamicPaymentFieldsPanel.add(walletBalanceField);
            } else { // Adapter
                JLabel adapterNote = new JLabel("Adapter converts call to LegacyBankGatewayApi");
                adapterNote.setFont(FONT_SMALL);
                adapterNote.setForeground(Color.GRAY);
                dynamicPaymentFieldsPanel.add(adapterNote);
                dynamicPaymentFieldsPanel.add(new JLabel(""));
            }

            dynamicPaymentFieldsPanel.revalidate();
            dynamicPaymentFieldsPanel.repaint();
        }

        public void setBooking(Booking booking) {
            this.currentBooking = booking;
            this.remainingSeconds = 300;

            bookingIdLabel.setText("Booking ID: " + booking.getId());
            movieDetailsLabel.setText("Show: " + booking.getShow().getMovie().getTitle() +
                    " (" + booking.getShow().getShowDateTime().format(DATE_TIME_FORMATTER) + ")");
            seatListLabel.setText("Reserved Seats: " + String.join(", ", booking.getSeatIds()));
            amountPayableLabel.setText(String.format("Total Payable: $%.2f", booking.getTotalAmount()));

            if (countdownTimer != null) countdownTimer.stop();
            countdownTimer = new javax.swing.Timer(1000, e -> {
                remainingSeconds--;
                int mins = remainingSeconds / 60;
                int secs = remainingSeconds % 60;
                timerLabel.setText(String.format("Time Remaining: %02d:%02d", mins, secs));

                if (remainingSeconds <= 0) {
                    countdownTimer.stop();
                    JOptionPane.showMessageDialog(this,
                            "Your 5-minute checkout window has expired. Seats have been automatically released.",
                            "Reservation Expired", JOptionPane.WARNING_MESSAGE);
                    bookingService.cancelPendingReservation(currentBooking);
                    parent.navigateToSeatSelection(currentBooking.getShow());
                }
            });
            countdownTimer.start();
        }

        private void cancelAndAbandon() {
            if (countdownTimer != null) countdownTimer.stop();
            if (currentBooking != null) {
                bookingService.cancelPendingReservation(currentBooking);
            }
            parent.showMoviesScreen();
        }

        private void executePayment() {
            if (currentBooking == null) return;

            String simMode = "SUCCESS";
            int simIdx = simulationModeCombo.getSelectedIndex();
            if (simIdx == 1) simMode = "FORCE_FAIL";
            if (simIdx == 2) simMode = "USER_CANCEL";

            PaymentMethod method;
            int methodIdx = paymentMethodCombo.getSelectedIndex();

            if (methodIdx == 0) {
                method = new MockCardPayment(cardNumField.getText().trim(), cardHolderField.getText().trim());
            } else if (methodIdx == 1) {
                method = new MockUpiPayment(upiField.getText().trim());
            } else if (methodIdx == 2) {
                double bal = 150.0;
                try { bal = Double.parseDouble(walletBalanceField.getText().trim()); } catch (Exception ignored) {}
                method = new MockWalletPayment("Paytm/GPay Wallet", bal);
            } else {
                // Adapter Pattern Demonstration
                try {
                    if (countdownTimer != null) countdownTimer.stop();
                    LegacyBankGatewayApi legacyApi = new LegacyBankGatewayApi();
                    PaymentGateway adapter = new LegacyBankGatewayAdapter(legacyApi);
                    PaymentTransaction txn = adapter.processPayment(currentBooking, currentBooking.getTotalAmount(), "DirectDebit via Adapter");
                    bookingService.confirmBooking(currentBooking);
                    parent.navigateToConfirmation(currentBooking, txn);
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Legacy Adapter Payment Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                if (countdownTimer != null) countdownTimer.stop();
                PaymentTransaction txn = paymentProcessor.processSimulatedPayment(currentBooking, method, simMode);

                if ("USER_CANCEL".equals(simMode)) {
                    JOptionPane.showMessageDialog(this,
                            "Payment was cancelled by the user. Reservation has been released.",
                            "Payment Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    parent.showMoviesScreen();
                } else {
                    parent.navigateToConfirmation(currentBooking, txn);
                }
            } catch (PaymentProcessingException | InvalidBookingStateException | SeatUnavailableException ex) {
                JOptionPane.showMessageDialog(this,
                        "Payment Failed: " + ex.getMessage(),
                        "Transaction Declined", JOptionPane.ERROR_MESSAGE);
                parent.showMoviesScreen();
            }
        }
    }


    // SECTION 13: SCREEN 5 CONFIRMATION
    public static class BookingConfirmationPanel extends JPanel {
        private final MainFrame parent;

        private JLabel bookingIdLabel;
        private JLabel txnIdLabel;
        private JLabel movieLabel;
        private JLabel showtimeLabel;
        private JLabel seatsLabel;
        private JLabel amountLabel;
        private JLabel paymentMethodLabel;
        private JLabel statusBadge;

        public BookingConfirmationPanel(MainFrame parent) {
            this.parent = parent;
            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(30, 40, 30, 40));

            initializeUI();
        }

        private void initializeUI() {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(24, 30, 24, 30)
            ));

            JLabel title = new JLabel("[OK] Booking Confirmed Successfully!");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(new Color(25, 135, 84));

            JLabel subtitle = new JLabel("Your cinema admission ticket has been generated.");
            subtitle.setFont(FONT_REGULAR);
            subtitle.setForeground(Color.GRAY);

            bookingIdLabel = new JLabel("Booking ID: ");
            bookingIdLabel.setFont(FONT_HEADING);

            txnIdLabel = new JLabel("Transaction ID: ");
            txnIdLabel.setFont(FONT_REGULAR);

            movieLabel = new JLabel("Movie: ");
            movieLabel.setFont(FONT_REGULAR);

            showtimeLabel = new JLabel("Showtime: ");
            showtimeLabel.setFont(FONT_REGULAR);

            seatsLabel = new JLabel("Seats: ");
            seatsLabel.setFont(FONT_BOLD);

            amountLabel = new JLabel("Amount Paid: ");
            amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            amountLabel.setForeground(COLOR_PRIMARY);

            paymentMethodLabel = new JLabel("Method: ");
            paymentMethodLabel.setFont(FONT_REGULAR);

            statusBadge = new JLabel("Status: CONFIRMED");
            statusBadge.setFont(FONT_BOLD);
            statusBadge.setForeground(new Color(25, 135, 84));

            card.add(title);
            card.add(Box.createVerticalStrut(4));
            card.add(subtitle);
            card.add(Box.createVerticalStrut(14));
            card.add(new JSeparator());
            card.add(Box.createVerticalStrut(14));
            card.add(bookingIdLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(txnIdLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(statusBadge);
            card.add(Box.createVerticalStrut(10));
            card.add(new JSeparator());
            card.add(Box.createVerticalStrut(10));
            card.add(movieLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(showtimeLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(seatsLabel);
            card.add(Box.createVerticalStrut(6));
            card.add(paymentMethodLabel);
            card.add(Box.createVerticalStrut(10));
            card.add(amountLabel);
            card.add(Box.createVerticalStrut(20));

            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
            actionsPanel.setOpaque(false);

            JButton myBookingsBtn = new JButton("View in My Bookings");
            myBookingsBtn.setFont(FONT_BOLD);
            myBookingsBtn.setBackground(COLOR_PRIMARY);
            myBookingsBtn.setForeground(Color.WHITE);
            myBookingsBtn.setFocusPainted(false);
            myBookingsBtn.addActionListener(e -> parent.showHistoryScreen());

            JButton backMoviesBtn = new JButton("Back to Movies");
            backMoviesBtn.setFont(FONT_REGULAR);
            backMoviesBtn.setBackground(Color.WHITE);
            backMoviesBtn.addActionListener(e -> parent.showMoviesScreen());

            actionsPanel.add(myBookingsBtn);
            actionsPanel.add(backMoviesBtn);

            card.add(actionsPanel);

            JPanel centerWrap = new JPanel(new GridBagLayout());
            centerWrap.setOpaque(false);
            centerWrap.add(card);

            add(centerWrap, BorderLayout.CENTER);
        }

        public void displayConfirmation(Booking booking, PaymentTransaction txn) {
            bookingIdLabel.setText("Booking ID: " + booking.getId());
            txnIdLabel.setText("Transaction Reference: " + txn.getId() + " (" + txn.getGatewayReference() + ")");
            movieLabel.setText("Movie: " + booking.getShow().getMovie().getTitle());
            showtimeLabel.setText("Showtime: " + booking.getShow().getShowDateTime().format(DATE_TIME_FORMATTER));
            seatsLabel.setText("Confirmed Seats: " + String.join(", ", booking.getSeatIds()));
            paymentMethodLabel.setText("Payment Method: " + txn.getPaymentMethodName());
            amountLabel.setText(String.format("Total Amount Paid: $%.2f", txn.getAmount()));
            statusBadge.setText("Booking Status: " + booking.getBookingStatus().getDisplayName() +
                    " | Payment Status: " + txn.getPaymentStatus().getDisplayName());
        }
    }


    // SECTION 13B: SCREEN 6 MY BOOKINGS & HISTORY
    public static class BookingHistoryPanel extends JPanel {
        private final MainFrame parent;
        private final BookingManager manager;
        private final BookingService bookingService;

        private DefaultTableModel tableModel;
        private JTable bookingsTable;
        private JLabel customerStatusLabel;

        public BookingHistoryPanel(MainFrame parent, BookingManager manager, BookingService bookingService) {
            this.parent = parent;
            this.manager = manager;
            this.bookingService = bookingService;

            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(16, 24, 16, 24));

            initializeUI();
        }

        private void initializeUI() {
            JPanel topCard = new JPanel(new BorderLayout());
            topCard.setBackground(Color.WHITE);
            topCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(14, 18, 14, 18)
            ));

            customerStatusLabel = new JLabel("My Booking Records");
            customerStatusLabel.setFont(FONT_TITLE);
            customerStatusLabel.setForeground(COLOR_TEXT_DARK);

            JButton refreshBtn = new JButton("Refresh");
            refreshBtn.setFont(FONT_SMALL);
            refreshBtn.setBackground(COLOR_BG_LIGHT);
            refreshBtn.addActionListener(e -> refreshHistory());

            topCard.add(customerStatusLabel, BorderLayout.WEST);
            topCard.add(refreshBtn, BorderLayout.EAST);
            add(topCard, BorderLayout.NORTH);

            String[] columns = {"Booking ID", "Movie", "Screen", "Showtime", "Seats", "Total Amount", "Booking Status", "Cancellable?"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };

            bookingsTable = new JTable(tableModel);
            bookingsTable.setFont(FONT_REGULAR);
            bookingsTable.setRowHeight(28);
            bookingsTable.getTableHeader().setFont(FONT_BOLD);
            bookingsTable.getTableHeader().setBackground(COLOR_PRIMARY);
            bookingsTable.getTableHeader().setForeground(Color.WHITE);
            bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(bookingsTable);
            scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1));
            add(scrollPane, BorderLayout.CENTER);

            JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            bottomCard.setBackground(Color.WHITE);
            bottomCard.setBorder(new EmptyBorder(8, 0, 0, 0));

            JButton viewTicketBtn = new JButton("View Ticket Details");
            viewTicketBtn.setFont(FONT_BOLD);
            viewTicketBtn.setBackground(COLOR_PRIMARY);
            viewTicketBtn.setForeground(Color.WHITE);
            viewTicketBtn.setFocusPainted(false);
            viewTicketBtn.addActionListener(e -> viewSelectedTicket());

            JButton cancelBookingBtn = new JButton("Cancel Selected Booking");
            cancelBookingBtn.setFont(FONT_BOLD);
            cancelBookingBtn.setBackground(new Color(220, 53, 69));
            cancelBookingBtn.setForeground(Color.WHITE);
            cancelBookingBtn.setFocusPainted(false);
            cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

            bottomCard.add(viewTicketBtn);
            bottomCard.add(cancelBookingBtn);
            add(bottomCard, BorderLayout.SOUTH);
        }

        public void refreshHistory() {
            tableModel.setRowCount(0);
            User user = manager.getCurrentUser();
            customerStatusLabel.setText("Bookings for: " + user.getName() + " (" + user.getRole() + ")");

            List<Booking> userBookings = manager.getBookingsForCustomer(user.getId());

            for (Booking b : userBookings) {
                String canCancelStr = b.isCancellable() ? "YES" : "NO (Past/Closed)";
                tableModel.addRow(new Object[]{
                        b.getId(),
                        b.getShow().getMovie().getTitle(),
                        b.getShow().getScreen().getName(),
                        b.getShow().getShowDateTime().format(DATE_TIME_FORMATTER),
                        String.join(", ", b.getSeatIds()),
                        String.format("$%.2f", b.getTotalAmount()),
                        b.getBookingStatus().getDisplayName(),
                        canCancelStr
                });
            }
        }

        private void viewSelectedTicket() {
            int row = bookingsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String bookingId = (String) tableModel.getValueAt(row, 0);
            Booking booking = manager.findBookingById(bookingId);
            PaymentTransaction txn = manager.findTransactionByBookingId(bookingId);

            if (booking != null) {
                String details = String.format(
                        "=========================================\n" +
                        "             CINEBOOK E-TICKET           \n" +
                        "=========================================\n" +
                        "Booking ID:      %s\n" +
                        "Customer:        %s\n" +
                        "Movie:           %s\n" +
                        "Screen:          %s\n" +
                        "Showtime:        %s\n" +
                        "Seats:           %s\n" +
                        "Status:          %s\n" +
                        "Total Paid:      $%.2f\n" +
                        "Payment Mode:    %s\n" +
                        "Gateway Ref:     %s\n" +
                        "=========================================\n",
                        booking.getId(),
                        booking.getCustomer().getName(),
                        booking.getShow().getMovie().getTitle(),
                        booking.getShow().getScreen().getName(),
                        booking.getShow().getShowDateTime().format(DATE_TIME_FORMATTER),
                        String.join(", ", booking.getSeatIds()),
                        booking.getBookingStatus().getDisplayName(),
                        booking.getTotalAmount(),
                        (txn != null ? txn.getPaymentMethodName() : "N/A"),
                        (txn != null ? txn.getGatewayReference() : "N/A")
                );

                JTextArea area = new JTextArea(details);
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ticket Details - " + booking.getId(), JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void cancelSelectedBooking() {
            int row = bookingsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String bookingId = (String) tableModel.getValueAt(row, 0);
            Booking booking = manager.findBookingById(bookingId);

            if (booking == null) return;

            if (!booking.isCancellable()) {
                JOptionPane.showMessageDialog(this,
                        "This booking cannot be cancelled.\nPolicy: Bookings can only be cancelled at least 2 hours before showtime.",
                        "Cancellation Ineligible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel booking " + bookingId + "?\nA full simulated refund will be processed and seats will be released.",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    bookingService.cancelConfirmedBooking(bookingId);
                    JOptionPane.showMessageDialog(this,
                            "Booking " + bookingId + " has been successfully cancelled and refunded.",
                            "Cancellation Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshHistory();
                } catch (BookingNotFoundException | InvalidBookingStateException ex) {
                    JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    // SECTION 14: SCREEN 7 ADMIN CONSOLE
    public static class AdminPanel extends JPanel {
        private final MainFrame parent;
        private final BookingManager manager;
        private final MovieService movieService;

        private DefaultTableModel moviesTableModel;
        private DefaultTableModel showsTableModel;
        private DefaultTableModel bookingsTableModel;
        private DefaultTableModel txnsTableModel;

        private JLabel statsLabel;

        public AdminPanel(MainFrame parent, BookingManager manager, MovieService movieService) {
            this.parent = parent;
            this.manager = manager;
            this.movieService = movieService;

            setLayout(new BorderLayout());
            setBackground(COLOR_BG_LIGHT);
            setBorder(new EmptyBorder(16, 24, 16, 24));

            initializeUI();
        }

        private void initializeUI() {
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(Color.WHITE);
            topBar.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            JLabel title = new JLabel("CineBook Administration Console");
            title.setFont(FONT_TITLE);
            title.setForeground(COLOR_PRIMARY_DARK);

            JButton refreshBtn = new JButton("Refresh All Data");
            refreshBtn.setFont(FONT_SMALL);
            refreshBtn.setBackground(COLOR_BG_LIGHT);
            refreshBtn.addActionListener(e -> refreshData());

            topBar.add(title, BorderLayout.WEST);
            topBar.add(refreshBtn, BorderLayout.EAST);
            add(topBar, BorderLayout.NORTH);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(FONT_BOLD);

            tabbedPane.addTab("Manage Movies", createMoviesTab());
            tabbedPane.addTab("Manage Shows", createShowsTab());
            tabbedPane.addTab("All Bookings", createBookingsTab());
            tabbedPane.addTab("Financial Transactions", createTxnsTab());
            tabbedPane.addTab("Business Metrics & Reports", createStatsTab());

            add(tabbedPane, BorderLayout.CENTER);
        }

        private JPanel createMoviesTab() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[] cols = {"ID", "Title", "Genre", "Duration", "Language", "Cert", "Description"};
            moviesTableModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            JTable table = new JTable(moviesTableModel);
            table.setFont(FONT_REGULAR);
            table.setRowHeight(24);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            actions.setOpaque(false);

            JButton addBtn = new JButton("+ Add Movie");
            addBtn.setFont(FONT_BOLD);
            addBtn.setBackground(COLOR_PRIMARY);
            addBtn.setForeground(Color.WHITE);
            addBtn.addActionListener(e -> promptAddMovie());

            JButton delBtn = new JButton("Delete Movie (Safe Guard)");
            delBtn.setFont(FONT_BOLD);
            delBtn.setBackground(new Color(220, 53, 69));
            delBtn.setForeground(Color.WHITE);
            delBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a movie first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String mId = (String) moviesTableModel.getValueAt(row, 0);
                try {
                    movieService.removeMovie(mId);
                    JOptionPane.showMessageDialog(this, "Movie removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(this, "Cannot delete movie: " + ex.getMessage(), "Guard Protected", JOptionPane.WARNING_MESSAGE);
                }
            });

            actions.add(addBtn);
            actions.add(delBtn);
            panel.add(actions, BorderLayout.SOUTH);
            return panel;
        }

        private void promptAddMovie() {
            JTextField titleField = new JTextField();
            JTextField genreField = new JTextField("Action");
            JTextField durField = new JTextField("120");
            JTextField langField = new JTextField("English");
            JTextField certField = new JTextField("PG-13");
            JTextField descField = new JTextField("Film description");

            Object[] fields = {
                    "Movie Title:", titleField,
                    "Genre:", genreField,
                    "Duration (minutes):", durField,
                    "Language:", langField,
                    "Certification:", certField,
                    "Description:", descField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Add New Movie", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String title = titleField.getText().trim();
                    int dur = Integer.parseInt(durField.getText().trim());
                    String id = "MOV-" + String.format("%02d", manager.getMovies().size() + 1);
                    Movie m = new Movie(id, title, genreField.getText().trim(), dur,
                            langField.getText().trim(), certField.getText().trim(), descField.getText().trim());
                    movieService.addMovie(m);
                    JOptionPane.showMessageDialog(this, "Movie " + title + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private JPanel createShowsTab() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[] cols = {"Show ID", "Movie", "Screen", "Showtime", "Base Price", "Available Seats"};
            showsTableModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            JTable table = new JTable(showsTableModel);
            table.setFont(FONT_REGULAR);
            table.setRowHeight(24);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            actions.setOpaque(false);

            JButton addBtn = new JButton("+ Add Show");
            addBtn.setFont(FONT_BOLD);
            addBtn.setBackground(COLOR_PRIMARY);
            addBtn.setForeground(Color.WHITE);
            addBtn.addActionListener(e -> promptAddShow());

            JButton delBtn = new JButton("Cancel/Remove Show");
            delBtn.setFont(FONT_BOLD);
            delBtn.setBackground(new Color(220, 53, 69));
            delBtn.setForeground(Color.WHITE);
            delBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Select a show first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String sId = (String) showsTableModel.getValueAt(row, 0);
                try {
                    movieService.removeShow(sId);
                    JOptionPane.showMessageDialog(this, "Show removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(this, "Cannot remove show: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                }
            });

            actions.add(addBtn);
            actions.add(delBtn);
            panel.add(actions, BorderLayout.SOUTH);
            return panel;
        }

        private void promptAddShow() {
            if (manager.getMovies().isEmpty() || manager.getScreens().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Add movies and screens before scheduling shows.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JComboBox<Movie> movieCombo = new JComboBox<>(manager.getMovies().toArray(new Movie[0]));
            JComboBox<Screen> screenCombo = new JComboBox<>(manager.getScreens().toArray(new Screen[0]));
            JTextField hoursField = new JTextField("3");
            JTextField priceField = new JTextField("15.00");

            Object[] fields = {
                    "Movie:", movieCombo,
                    "Screen:", screenCombo,
                    "Hours from now:", hoursField,
                    "Base Ticket Price ($):", priceField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Schedule New Show", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    Movie m = (Movie) movieCombo.getSelectedItem();
                    Screen sc = (Screen) screenCombo.getSelectedItem();
                    int hrs = Integer.parseInt(hoursField.getText().trim());
                    double pr = Double.parseDouble(priceField.getText().trim());

                    String sId = "SHW-" + String.format("%02d", manager.getShows().size() + 1);
                    Show s = new Show(sId, m, sc, LocalDateTime.now().plusHours(hrs), pr);
                    movieService.addShow(s);
                    JOptionPane.showMessageDialog(this, "Show scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private JPanel createBookingsTab() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[] cols = {"Booking ID", "Customer", "Movie", "Screen", "Showtime", "Seats", "Total", "Status"};
            bookingsTableModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            JTable table = new JTable(bookingsTableModel);
            table.setFont(FONT_REGULAR);
            table.setRowHeight(24);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            return panel;
        }

        private JPanel createTxnsTab() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));

            String[] cols = {"Txn ID", "Booking ID", "Payment Method", "Amount", "Status", "Gateway Ref", "Timestamp"};
            txnsTableModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            JTable table = new JTable(txnsTableModel);
            table.setFont(FONT_REGULAR);
            table.setRowHeight(24);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            return panel;
        }

        private JPanel createStatsTab() {
            JPanel panel = new JPanel(new BorderLayout(16, 16));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(24, 24, 24, 24));

            statsLabel = new JLabel("Calculating analytics...");
            statsLabel.setFont(FONT_HEADING);
            statsLabel.setVerticalAlignment(SwingConstants.TOP);

            panel.add(statsLabel, BorderLayout.CENTER);
            return panel;
        }

        public void refreshData() {
            // Movies
            moviesTableModel.setRowCount(0);
            for (Movie m : manager.getMovies()) {
                moviesTableModel.addRow(new Object[]{
                        m.getId(), m.getTitle(), m.getGenre(), m.getDurationMinutes() + " mins",
                        m.getLanguage(), m.getCertification(), m.getDescription()
                });
            }

            // Shows
            showsTableModel.setRowCount(0);
            for (Show s : manager.getShows()) {
                showsTableModel.addRow(new Object[]{
                        s.getId(), s.getMovie().getTitle(), s.getScreen().getName(),
                        s.getShowDateTime().format(DATE_TIME_FORMATTER),
                        String.format("$%.2f", s.getBasePrice()),
                        s.getAvailableSeatsCount() + "/" + s.getTotalSeatsCount()
                });
            }

            // Bookings
            bookingsTableModel.setRowCount(0);
            for (Booking b : manager.getBookings()) {
                bookingsTableModel.addRow(new Object[]{
                        b.getId(), b.getCustomer().getName(), b.getShow().getMovie().getTitle(),
                        b.getShow().getScreen().getName(),
                        b.getShow().getShowDateTime().format(DATE_TIME_FORMATTER),
                        String.join(", ", b.getSeatIds()),
                        String.format("$%.2f", b.getTotalAmount()),
                        b.getBookingStatus().getDisplayName()
                });
            }

            // Transactions
            txnsTableModel.setRowCount(0);
            for (PaymentTransaction t : manager.getTransactions()) {
                txnsTableModel.addRow(new Object[]{
                        t.getId(), t.getBookingId(), t.getPaymentMethodName(),
                        String.format("$%.2f", t.getAmount()),
                        t.getPaymentStatus().getDisplayName(),
                        t.getGatewayReference(),
                        t.getTimestamp().format(DATE_TIME_FORMATTER)
                });
            }

            // Statistics (Recursion demonstration)
            int totalBookedSeats = manager.countTotalBookedSeatsRecursive(manager.getBookings(), 0);
            double revenue = manager.calculateTotalRevenue();

            String statsHtml = String.format(
                    "<html><body style='font-family: Segoe UI, sans-serif; font-size: 14px;'>" +
                    "<h2 style='color: #1C4072;'>CineBook Real-time Business Intelligence</h2>" +
                    "<table cellpadding='8' style='border-collapse: collapse; width: 600px;'>" +
                    "<tr style='background-color: #f2f5f9;'><td><b>Total Movies in Catalogue:</b></td><td>%d</td></tr>" +
                    "<tr><td><b>Active Scheduled Shows:</b></td><td>%d</td></tr>" +
                    "<tr style='background-color: #f2f5f9;'><td><b>Total Customer Bookings:</b></td><td>%d</td></tr>" +
                    "<tr><td><b>Total Confirmed Booked Seats (Recursive Calculation):</b></td><td><b style='color: #198754;'>%d seats</b></td></tr>" +
                    "<tr style='background-color: #f2f5f9;'><td><b>Total Payment Transactions:</b></td><td>%d</td></tr>" +
                    "<tr><td><b>Net Revenue Realized:</b></td><td><b style='color: #198754; font-size: 16px;'>$%.2f</b></td></tr>" +
                    "<tr style='background-color: #f2f5f9;'><td><b>Audit Trail Records:</b></td><td>%d events logged</td></tr>" +
                    "</table></body></html>",
                    manager.getMovies().size(),
                    manager.getShows().size(),
                    manager.getBookings().size(),
                    totalBookedSeats,
                    manager.getTransactions().size(),
                    revenue,
                    manager.getAuditLogs().size()
            );

            statsLabel.setText(statsHtml);
        }
    }


    // SECTION 15: AUTOMATED TEST SUITE & ENTRY POINT main
    public static void runAutomatedCoreVerification() {
        System.out.println(">>> RUNNING CINEBOOK AUTOMATED SYSTEM VERIFICATION <<<");
        BookingManager mgr = BookingManager.getInstance();
        SeatManager seatMgr = new SeatManager();
        MovieService movieSvc = new MovieService(mgr);
        BookingService bookingSvc = new BookingService(mgr, seatMgr);
        PaymentProcessor payProc = new PaymentProcessor(mgr, bookingSvc);

        int passCount = 0;
        int testCount = 0;

        testCount++;
        if (mgr.getMovies().size() >= 6) {
            passCount++;
            System.out.println("[PASS] Test 1: Sample movie catalogue loaded (" + mgr.getMovies().size() + " movies).");
        } else {
            System.err.println("[FAIL] Test 1: Movies not loaded properly.");
        }

        testCount++;
        Show s1 = mgr.getShows().get(0);
        Show s2 = mgr.getShows().get(1);
        Seat seatA1_s1 = s1.findSeat("A3");
        Seat seatA1_s2 = s2.findSeat("A3");
        if (seatA1_s1 != null && seatA1_s2 != null && seatA1_s1 != seatA1_s2) {
            passCount++;
            System.out.println("[PASS] Test 2: Shows maintain independent seat object instances.");
        } else {
            System.err.println("[FAIL] Test 2: Shows share seat instances.");
        }

        testCount++;
        try {
            Customer cust = mgr.getCustomers().get(0);
            Booking bkg = bookingSvc.createPendingReservation(cust, s2, Arrays.asList("B1", "B2"));
            PaymentMethod card = new MockCardPayment("1111222233334444", "Test User");
            PaymentTransaction txn = payProc.processSimulatedPayment(bkg, card, "SUCCESS");

            if (bkg.getBookingStatus() == BookingStatus.CONFIRMED &&
                    txn.getPaymentStatus() == PaymentStatus.SUCCESS &&
                    s2.findSeat("B1").getStatus() == SeatStatus.BOOKED) {
                passCount++;
                System.out.println("[PASS] Test 3: Temporary reservation -> Payment -> Confirmed booking completed.");
            } else {
                System.err.println("[FAIL] Test 3: Booking confirmation state incorrect.");
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3 encountered exception: " + e.getMessage());
        }

        testCount++;
        try {
            Booking bkg = mgr.getBookings().get(mgr.getBookings().size() - 1);
            bkg.confirm();
            System.err.println("[FAIL] Test 4: Double confirmation was not prevented!");
        } catch (InvalidBookingStateException expected) {
            passCount++;
            System.out.println("[PASS] Test 4: Double confirmation prevented: " + expected.getMessage());
        } catch (Exception other) {
            System.err.println("[FAIL] Test 4: Unexpected exception type: " + other.getMessage());
        }

        testCount++;
        try {
            movieSvc.removeMovie("MOV-01");
            System.err.println("[FAIL] Test 5: Deletion guard did not trigger!");
        } catch (ValidationException expected) {
            passCount++;
            System.out.println("[PASS] Test 5: Safe deletion guard prevented deleting movie with bookings: " + expected.getMessage());
        }

        testCount++;
        int recCount = mgr.countTotalBookedSeatsRecursive(mgr.getBookings(), 0);
        if (recCount > 0) {
            passCount++;
            System.out.println("[PASS] Test 6: Recursive booked seats calculation returned valid count: " + recCount);
        } else {
            System.err.println("[FAIL] Test 6: Recursive counter failed.");
        }

        System.out.println(String.format(">>> VERIFICATION SUMMARY: %d/%d TESTS PASSED <<<\n", passCount, testCount));
    }

    public static void main(String[] args) {
        runAutomatedCoreVerification();

        if (args.length > 0 && ("--test-only".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0]))) {
            System.out.println("Headless verification flag supplied. Exiting cleanly without launching GUI.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}


