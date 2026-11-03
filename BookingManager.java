import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Module 6: BookingManager Singleton with SQLite JDBC Integration
public class BookingManager {
    private static volatile BookingManager instance;

    private final DatabaseManager dbManager;

    private final List<Movie> movies = new CopyOnWriteArrayList<>();
    private final List<Screen> screens = new CopyOnWriteArrayList<>();
    private final List<Show> shows = new CopyOnWriteArrayList<>();
    private final List<Booking> bookings = new CopyOnWriteArrayList<>();
    private final List<PaymentTransaction> transactions = new CopyOnWriteArrayList<>();
    private final List<Customer> customers = new CopyOnWriteArrayList<>();
    private final List<AdminUser> adminUsers = new CopyOnWriteArrayList<>();
    private final List<String> auditLogs = new CopyOnWriteArrayList<>();

    // User is NOT pre-logged in; manual login is strictly required
    private User currentUser = null;

    private BookingManager() {
        this.dbManager = DatabaseManager.getInstance();
        loadFromDatabase();
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

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public synchronized void loadFromDatabase() {
        movies.clear();
        screens.clear();
        shows.clear();
        bookings.clear();
        transactions.clear();
        customers.clear();
        adminUsers.clear();
        auditLogs.clear();

        screens.addAll(dbManager.loadAllScreens());
        movies.addAll(dbManager.loadAllMovies());
        customers.addAll(dbManager.loadAllCustomers());
        adminUsers.addAll(dbManager.loadAllAdminUsers());
        shows.addAll(dbManager.loadAllShows(movies, screens));
        bookings.addAll(dbManager.loadAllBookings(customers, shows));
        transactions.addAll(dbManager.loadAllTransactions());
        auditLogs.addAll(dbManager.loadAllAuditLogs());
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
        String timestamp = LocalDateTime.now().format(UIConstants.DATE_TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + action;
        auditLogs.add(logEntry);
        dbManager.insertAuditLog(timestamp, action);
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
}
