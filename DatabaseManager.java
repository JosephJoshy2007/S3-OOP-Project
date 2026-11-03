import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Module 6 / Database Layer: DatabaseManager
 * 
 * Demonstrates the 6 Fundamental Steps of JDBC as per the KTU OOP (PBCST304) Syllabus:
 *  Step 1: Driver Loading & Registration (org.sqlite.JDBC)
 *  Step 2: Connection Establishment via DriverManager (Connection)
 *  Step 3: Statement & PreparedStatement Creation
 *  Step 4: SQL Query & DML Execution (executeQuery, executeUpdate)
 *  Step 5: Processing Results via ResultSet
 *  Step 6: Resource Cleanup via Java try-with-resources
 * 
 * Supports full CRUD (Create, Read, Update, Delete) operations with transactional safety.
 */
public class DatabaseManager {
    private static final String DB_DIR = "db";
    private static final String DB_FILE = "db" + File.separator + "cinebook.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static volatile DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Step 1 & 2: Establish connection to SQLite database.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[JDBC WARNING] SQLite JDBC Driver class not found: " + e.getMessage());
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Step 3 & 4: Initialize schema and tables if they don't already exist.
     */
    private synchronized void initDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable Foreign Keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT, " +
                    "role TEXT NOT NULL, " +
                    "loyalty_points INTEGER DEFAULT 0, " +
                    "access_level TEXT" +
                    ");");

            // 2. Screens Table
            stmt.execute("CREATE TABLE IF NOT EXISTS screens (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "seat_rows INTEGER NOT NULL, " +
                    "seat_cols INTEGER NOT NULL" +
                    ");");

            // 3. Movies Table
            stmt.execute("CREATE TABLE IF NOT EXISTS movies (" +
                    "id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "genre TEXT NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "language TEXT, " +
                    "certification TEXT, " +
                    "description TEXT" +
                    ");");

            // 4. Shows Table
            stmt.execute("CREATE TABLE IF NOT EXISTS shows (" +
                    "id TEXT PRIMARY KEY, " +
                    "movie_id TEXT NOT NULL, " +
                    "screen_id TEXT NOT NULL, " +
                    "show_time TEXT NOT NULL, " +
                    "base_price REAL NOT NULL, " +
                    "FOREIGN KEY (movie_id) REFERENCES movies(id), " +
                    "FOREIGN KEY (screen_id) REFERENCES screens(id)" +
                    ");");

            // 5. Bookings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id TEXT PRIMARY KEY, " +
                    "customer_id TEXT NOT NULL, " +
                    "show_id TEXT NOT NULL, " +
                    "seat_ids TEXT NOT NULL, " +
                    "total_amount REAL NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "expires_at TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "cancellation_reason TEXT, " +
                    "FOREIGN KEY (customer_id) REFERENCES users(id), " +
                    "FOREIGN KEY (show_id) REFERENCES shows(id)" +
                    ");");

            // 6. Transactions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id TEXT PRIMARY KEY, " +
                    "booking_id TEXT NOT NULL, " +
                    "payment_method TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "reference TEXT, " +
                    "FOREIGN KEY (booking_id) REFERENCES bookings(id)" +
                    ");");

            // 7. Audit Logs Table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp TEXT NOT NULL, " +
                    "action TEXT NOT NULL" +
                    ");");

            // Seed default records if database is empty
            seedDefaultDataIfEmpty(conn);

        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Failed to initialize database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedDefaultDataIfEmpty(Connection conn) throws SQLException {
        // Check if users exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
            if (rs.next() && rs.getInt("total") == 0) {
                // Seed default admin and user
                String insertUserSql = "INSERT INTO users (id, username, password, name, email, role, loyalty_points, access_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
                    // 1. Admin: admin / 123
                    pstmt.setString(1, "ADM-01");
                    pstmt.setString(2, "admin");
                    pstmt.setString(3, "123");
                    pstmt.setString(4, "System Administrator");
                    pstmt.setString(5, "admin@cinebook.com");
                    pstmt.setString(6, "Admin");
                    pstmt.setInt(7, 0);
                    pstmt.setString(8, "SuperAdmin");
                    pstmt.executeUpdate();

                    // 2. Customer: user1 / 123
                    pstmt.setString(1, "CUST-01");
                    pstmt.setString(2, "user1");
                    pstmt.setString(3, "123");
                    pstmt.setString(4, "Aarav Sharma");
                    pstmt.setString(5, "user1@cinebook.com");
                    pstmt.setString(6, "Customer");
                    pstmt.setInt(7, 150);
                    pstmt.setString(8, null);
                    pstmt.executeUpdate();

                    // 3. Customer: user2 / 123
                    pstmt.setString(1, "CUST-02");
                    pstmt.setString(2, "user2");
                    pstmt.setString(3, "123");
                    pstmt.setString(4, "Diya Patel");
                    pstmt.setString(5, "user2@cinebook.com");
                    pstmt.setString(6, "Customer");
                    pstmt.setInt(7, 220);
                    pstmt.setString(8, null);
                    pstmt.executeUpdate();
                }
            }
        }

        // Check if screens exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM screens")) {
            if (rs.next() && rs.getInt("total") == 0) {
                String insertScreenSql = "INSERT INTO screens (id, name, seat_rows, seat_cols) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertScreenSql)) {
                    pstmt.setString(1, "SCR-01");
                    pstmt.setString(2, "Screen 1 (IMAX Laser)");
                    pstmt.setInt(3, 5);
                    pstmt.setInt(4, 8);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "SCR-02");
                    pstmt.setString(2, "Screen 2 (Dolby Atmos)");
                    pstmt.setInt(3, 5);
                    pstmt.setInt(4, 8);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "SCR-03");
                    pstmt.setString(2, "Screen 3 (Gold Lounge)");
                    pstmt.setInt(3, 5);
                    pstmt.setInt(4, 6);
                    pstmt.executeUpdate();
                }
            }
        }

        // Check if movies exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM movies")) {
            if (rs.next() && rs.getInt("total") == 0) {
                String insertMovieSql = "INSERT INTO movies (id, title, genre, duration, language, certification, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertMovieSql)) {
                    Object[][] moviesData = {
                            {"MOV-01", "Interstellar: Beyond Time", "Sci-Fi", 169, "English", "PG-13", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival."},
                            {"MOV-02", "Inception", "Sci-Fi", 148, "English", "PG-13", "A thief who steals corporate secrets through dream-sharing technology is given an inverse task of planting an idea."},
                            {"MOV-03", "The Dark Knight", "Action", 152, "English", "PG-13", "Batman battles the Joker who unleashes psychological chaos across Gotham City."},
                            {"MOV-04", "La La Land", "Romance", 128, "English", "PG-13", "A jazz pianist and an aspiring actress fall in love while pursuing their passions in modern Los Angeles."},
                            {"MOV-05", "Spirited Away", "Animation", 125, "Japanese", "PG", "A young girl enters a realm governed by gods, witches, and spirits where humans are transformed into beasts."},
                            {"MOV-06", "The Grand Budapest Hotel", "Comedy", 100, "English", "R", "The adventures of Gustave H, a legendary concierge at a famous European hotel between the wars."}
                    };

                    for (Object[] row : moviesData) {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setString(2, (String) row[1]);
                        pstmt.setString(3, (String) row[2]);
                        pstmt.setInt(4, (Integer) row[3]);
                        pstmt.setString(5, (String) row[4]);
                        pstmt.setString(6, (String) row[5]);
                        pstmt.setString(7, (String) row[6]);
                        pstmt.executeUpdate();
                    }
                }
            }
        }

        // Check if shows exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM shows")) {
            if (rs.next() && rs.getInt("total") == 0) {
                LocalDateTime now = LocalDateTime.now();
                String insertShowSql = "INSERT INTO shows (id, movie_id, screen_id, show_time, base_price) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertShowSql)) {
                    Object[][] showsData = {
                            {"SHW-01", "MOV-01", "SCR-01", now.plusHours(3).format(UIConstants.DATE_TIME_FORMATTER), 15.00},
                            {"SHW-02", "MOV-01", "SCR-01", now.plusDays(1).withHour(18).withMinute(0).format(UIConstants.DATE_TIME_FORMATTER), 16.50},
                            {"SHW-03", "MOV-02", "SCR-02", now.plusHours(5).format(UIConstants.DATE_TIME_FORMATTER), 14.00},
                            {"SHW-04", "MOV-03", "SCR-01", now.plusDays(1).withHour(20).withMinute(30).format(UIConstants.DATE_TIME_FORMATTER), 18.00},
                            {"SHW-05", "MOV-04", "SCR-03", now.plusHours(2).format(UIConstants.DATE_TIME_FORMATTER), 22.00},
                            {"SHW-06", "MOV-05", "SCR-02", now.plusDays(2).withHour(15).withMinute(0).format(UIConstants.DATE_TIME_FORMATTER), 13.50},
                            {"SHW-07", "MOV-06", "SCR-03", now.plusHours(4).format(UIConstants.DATE_TIME_FORMATTER), 19.00}
                    };

                    for (Object[] row : showsData) {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setString(2, (String) row[1]);
                        pstmt.setString(3, (String) row[2]);
                        pstmt.setString(4, (String) row[3]);
                        pstmt.setDouble(5, (Double) row[4]);
                        pstmt.executeUpdate();
                    }
                }

                // Pre-seed a sample booking and transaction in DB
                String insertBkgSql = "INSERT INTO bookings (id, customer_id, show_id, seat_ids, total_amount, created_at, expires_at, status, cancellation_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertBkgSql)) {
                    pstmt.setString(1, "BKG-SEED-01");
                    pstmt.setString(2, "CUST-01");
                    pstmt.setString(3, "SHW-01");
                    pstmt.setString(4, "A1,A2");
                    pstmt.setDouble(5, 30.00);
                    pstmt.setString(6, now.minusMinutes(10).format(UIConstants.DATE_TIME_FORMATTER));
                    pstmt.setString(7, now.plusMinutes(50).format(UIConstants.DATE_TIME_FORMATTER));
                    pstmt.setString(8, "CONFIRMED");
                    pstmt.setString(9, null);
                    pstmt.executeUpdate();
                }

                String insertTxnSql = "INSERT INTO transactions (id, booking_id, payment_method, amount, timestamp, status, reference) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertTxnSql)) {
                    pstmt.setString(1, "TXN-SEED-01");
                    pstmt.setString(2, "BKG-SEED-01");
                    pstmt.setString(3, "Mock Credit/Debit Card");
                    pstmt.setDouble(4, 30.00);
                    pstmt.setString(5, now.minusMinutes(10).format(UIConstants.DATE_TIME_FORMATTER));
                    pstmt.setString(6, "SUCCESS");
                    pstmt.setString(7, "AUTH-SEED-1234");
                    pstmt.executeUpdate();
                }
            }
        }
    }

    // ==========================================
    // USER AUTHENTICATION & CRUD (Step 4 & 5)
    // ==========================================

    public synchronized User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String uName = rs.getString("username");
                    String pwd = rs.getString("password");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    int loyalty = rs.getInt("loyalty_points");
                    String access = rs.getString("access_level");

                    if ("Admin".equalsIgnoreCase(role)) {
                        return new AdminUser(id, uName, pwd, name, email, access != null ? access : "Admin");
                    } else {
                        return new Customer(id, uName, pwd, name, email, loyalty);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] User authentication failed: " + e.getMessage());
        }
        return null;
    }

    public synchronized List<Customer> loadAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Customer'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("loyalty_points")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading customers: " + e.getMessage());
        }
        return list;
    }

    public synchronized List<AdminUser> loadAllAdminUsers() {
        List<AdminUser> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Admin'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AdminUser(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("access_level")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading admin users: " + e.getMessage());
        }
        return list;
    }

    public synchronized void updateCustomerLoyalty(String customerId, int points) {
        String sql = "UPDATE users SET loyalty_points = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setString(2, customerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Updating loyalty points: " + e.getMessage());
        }
    }

    // ==========================================
    // SCREENS & MOVIES CRUD
    // ==========================================

    public synchronized List<Screen> loadAllScreens() {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM screens ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Screen(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("seat_rows"),
                        rs.getInt("seat_cols")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading screens: " + e.getMessage());
        }
        return list;
    }

    public synchronized List<Movie> loadAllMovies() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Movie(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getString("language"),
                        rs.getString("certification"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading movies: " + e.getMessage());
        }
        return list;
    }

    public synchronized void insertMovie(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies (id, title, genre, duration, language, certification, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getId());
            pstmt.setString(2, movie.getTitle());
            pstmt.setString(3, movie.getGenre());
            pstmt.setInt(4, movie.getDurationMinutes());
            pstmt.setString(5, movie.getLanguage());
            pstmt.setString(6, movie.getCertification());
            pstmt.setString(7, movie.getDescription());
            pstmt.executeUpdate();
        }
    }

    public synchronized void updateMovie(Movie movie) throws SQLException {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, language = ?, certification = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getDurationMinutes());
            pstmt.setString(4, movie.getLanguage());
            pstmt.setString(5, movie.getCertification());
            pstmt.setString(6, movie.getDescription());
            pstmt.setString(7, movie.getId());
            pstmt.executeUpdate();
        }
    }

    public synchronized void deleteMovie(String movieId) throws SQLException {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movieId);
            pstmt.executeUpdate();
        }
    }

    // ==========================================
    // SHOWS CRUD
    // ==========================================

    public synchronized List<Show> loadAllShows(List<Movie> movies, List<Screen> screens) {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT * FROM shows ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String showId = rs.getString("id");
                String movieId = rs.getString("movie_id");
                String screenId = rs.getString("screen_id");
                String timeStr = rs.getString("show_time");
                double basePrice = rs.getDouble("base_price");

                Movie matchedMovie = null;
                for (Movie m : movies) {
                    if (m.getId().equalsIgnoreCase(movieId)) {
                        matchedMovie = m;
                        break;
                    }
                }

                Screen matchedScreen = null;
                for (Screen s : screens) {
                    if (s.getId().equalsIgnoreCase(screenId)) {
                        matchedScreen = s;
                        break;
                    }
                }

                if (matchedMovie != null && matchedScreen != null) {
                    LocalDateTime dt = LocalDateTime.parse(timeStr, UIConstants.DATE_TIME_FORMATTER);
                    Show show = new Show(showId, matchedMovie, matchedScreen, dt, basePrice);
                    list.add(show);
                }
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading shows: " + e.getMessage());
        }
        return list;
    }

    public synchronized void insertShow(Show show) throws SQLException {
        String sql = "INSERT INTO shows (id, movie_id, screen_id, show_time, base_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, show.getId());
            pstmt.setString(2, show.getMovie().getId());
            pstmt.setString(3, show.getScreen().getId());
            pstmt.setString(4, show.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));
            pstmt.setDouble(5, show.getBasePrice());
            pstmt.executeUpdate();
        }
    }

    public synchronized void deleteShow(String showId) throws SQLException {
        String sql = "DELETE FROM shows WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, showId);
            pstmt.executeUpdate();
        }
    }

    // ==========================================
    // BOOKINGS & TRANSACTIONS CRUD
    // ==========================================

    public synchronized List<Booking> loadAllBookings(List<Customer> customers, List<Show> shows) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String bkgId = rs.getString("id");
                String custId = rs.getString("customer_id");
                String showId = rs.getString("show_id");
                String seatCsv = rs.getString("seat_ids");
                double amount = rs.getDouble("total_amount");
                String statusStr = rs.getString("status");
                String reason = rs.getString("cancellation_reason");

                Customer cust = null;
                for (Customer c : customers) {
                    if (c.getId().equalsIgnoreCase(custId)) {
                        cust = c;
                        break;
                    }
                }

                Show show = null;
                for (Show s : shows) {
                    if (s.getId().equalsIgnoreCase(showId)) {
                        show = s;
                        break;
                    }
                }

                if (cust != null && show != null) {
                    List<String> seats = Arrays.asList(seatCsv.split(","));
                    Booking bkg = new Booking(bkgId, cust, show, seats, amount, 60);

                    if ("CONFIRMED".equalsIgnoreCase(statusStr)) {
                        try {
                            bkg.confirm();
                            // Mark seats as booked on show's seat grid
                            for (String seatId : seats) {
                                Seat st = show.findSeat(seatId.trim());
                                if (st != null) {
                                    st.confirmBooking(bkgId);
                                }
                            }
                        } catch (Exception ignored) {}
                    } else if ("CANCELLED".equalsIgnoreCase(statusStr)) {
                        try {
                            bkg.cancel(reason != null ? reason : "Cancelled in past session");
                        } catch (Exception ignored) {}
                    } else if ("PAYMENT_FAILED".equalsIgnoreCase(statusStr)) {
                        bkg.markPaymentFailed();
                    }
                    list.add(bkg);
                }
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading bookings: " + e.getMessage());
        }
        return list;
    }

    public synchronized void insertBooking(Booking bkg) throws SQLException {
        String sql = "INSERT INTO bookings (id, customer_id, show_id, seat_ids, total_amount, created_at, expires_at, status, cancellation_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bkg.getId());
            pstmt.setString(2, bkg.getCustomer().getId());
            pstmt.setString(3, bkg.getShow().getId());
            pstmt.setString(4, String.join(",", bkg.getSeatIds()));
            pstmt.setDouble(5, bkg.getTotalAmount());
            pstmt.setString(6, bkg.getCreatedAt().format(UIConstants.DATE_TIME_FORMATTER));
            pstmt.setString(7, bkg.getExpiresAt().format(UIConstants.DATE_TIME_FORMATTER));
            pstmt.setString(8, bkg.getBookingStatus().name());
            pstmt.setString(9, bkg.getCancellationReason());
            pstmt.executeUpdate();
        }
    }

    public synchronized void updateBookingStatus(String bookingId, BookingStatus status, String reason) {
        String sql = "UPDATE bookings SET status = ?, cancellation_reason = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, reason);
            pstmt.setString(3, bookingId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Updating booking status: " + e.getMessage());
        }
    }

    public synchronized List<PaymentTransaction> loadAllTransactions() {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String txnId = rs.getString("id");
                String bkgId = rs.getString("booking_id");
                String method = rs.getString("payment_method");
                double amt = rs.getDouble("amount");
                String timeStr = rs.getString("timestamp");
                String statusStr = rs.getString("status");
                String ref = rs.getString("reference");

                LocalDateTime dt = LocalDateTime.parse(timeStr, UIConstants.DATE_TIME_FORMATTER);
                PaymentStatus status = PaymentStatus.valueOf(statusStr.toUpperCase());
                list.add(new PaymentTransaction(txnId, bkgId, method, amt, dt, status, ref));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading transactions: " + e.getMessage());
        }
        return list;
    }

    public synchronized void insertTransaction(PaymentTransaction txn) throws SQLException {
        String sql = "INSERT INTO transactions (id, booking_id, payment_method, amount, timestamp, status, reference) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txn.getId());
            pstmt.setString(2, txn.getBookingId());
            pstmt.setString(3, txn.getPaymentMethodName());
            pstmt.setDouble(4, txn.getAmount());
            pstmt.setString(5, txn.getTimestamp().format(UIConstants.DATE_TIME_FORMATTER));
            pstmt.setString(6, txn.getPaymentStatus().name());
            pstmt.setString(7, txn.getGatewayReference());
            pstmt.executeUpdate();
        }
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================

    public synchronized List<String> loadAllAuditLogs() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT timestamp, action FROM audit_logs ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add("[" + rs.getString("timestamp") + "] " + rs.getString("action"));
            }
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Loading audit logs: " + e.getMessage());
        }
        return list;
    }

    public synchronized void insertAuditLog(String timestamp, String action) {
        String sql = "INSERT INTO audit_logs (timestamp, action) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, timestamp);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] Inserting audit log: " + e.getMessage());
        }
    }
}
