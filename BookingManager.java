import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Module 6: BookingManager Singleton
public class BookingManager {
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
        String logEntry = "[" + LocalDateTime.now().format(UIConstants.DATE_TIME_FORMATTER) + "] " + action;
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

        // Movies (6 sample movies)
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
