import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Module 5: MainFrame (Application Navigation, Role Routing & Shell)
public class MainFrame extends JFrame {
    private final BookingManager manager;
    private final MovieService movieService;
    private final ShowService showService;
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
    private JButton btnMovies;
    private JButton btnHistory;
    private JButton btnAdmin;

    public MainFrame() {
        super("CineBook — Movie Ticket Booking System");
        this.manager = BookingManager.getInstance();
        this.movieService = new MovieService(manager);
        this.showService = new ShowService(manager);
        this.seatManager = new SeatManager();
        this.bookingService = new BookingService(manager, seatManager);
        this.paymentProcessor = new PaymentProcessor(manager, bookingService);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Wider modern window geometry to prevent any horizontal scrolling
        setSize(1280, 820);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createTopNavBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        moviePanel = new MoviePanel(this, movieService);
        movieDetailsPanel = new MovieDetailsPanel(this, showService);
        seatSelectionPanel = new SeatSelectionPanel(this, bookingService);
        checkoutPanel = new CheckoutPanel(this, paymentProcessor, bookingService);
        confirmationPanel = new BookingConfirmationPanel(this);
        historyPanel = new BookingHistoryPanel(this, manager, bookingService);
        adminPanel = new AdminPanel(this, manager, movieService, showService);

        contentPanel.add(moviePanel, "MOVIES");
        contentPanel.add(movieDetailsPanel, "MOVIE_DETAILS");
        contentPanel.add(seatSelectionPanel, "SEAT_SELECTION");
        contentPanel.add(checkoutPanel, "CHECKOUT");
        contentPanel.add(confirmationPanel, "CONFIRMATION");
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.add(adminPanel, "ADMIN");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(UIConstants.COLOR_PRIMARY);
        nav.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftPanel.setOpaque(false);

        // Logo with modern branding
        JLabel logoLabel = new JLabel("CINEBOOK");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        leftPanel.add(logoLabel);

        // Separator pipe
        JLabel sep = new JLabel("|");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sep.setForeground(new Color(100, 116, 139));
        leftPanel.add(sep);

        // Navigation buttons
        btnMovies = createNavButton("Browse Movies");
        btnMovies.addActionListener(e -> showMoviesScreen());
        leftPanel.add(btnMovies);

        btnHistory = createNavButton("My Bookings");
        btnHistory.addActionListener(e -> showHistoryScreen());
        leftPanel.add(btnHistory);

        btnAdmin = createNavButton("Admin Console");
        btnAdmin.addActionListener(e -> showAdminScreen());
        leftPanel.add(btnAdmin);

        // Initially hide until user role is evaluated
        btnAdmin.setVisible(false);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        // User badge pill
        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        userBadge.setBackground(new Color(30, 41, 59));
        userBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(2, 10, 2, 10)
        ));

        userStatusLabel = new JLabel("Not logged in");
        userStatusLabel.setFont(UIConstants.FONT_BOLD);
        userStatusLabel.setForeground(new Color(241, 245, 249));
        userBadge.add(userStatusLabel);
        rightPanel.add(userBadge);

        // Modern Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UIConstants.FONT_BOLD);
        logoutBtn.setBackground(new Color(239, 68, 68));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 38, 38), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());
        rightPanel.add(logoutBtn);

        nav.add(leftPanel, BorderLayout.WEST);
        nav.add(rightPanel, BorderLayout.EAST);
        return nav;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(new Color(30, 41, 59));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(71, 85, 105), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of CineBook?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (manager.getCurrentUser() != null) {
                manager.logAudit("User logged out: " + manager.getCurrentUser().getName());
            }
            manager.setCurrentUser(null);
            setVisible(false);

            LoginDialog loginDialog = new LoginDialog(null);
            User user = loginDialog.showLoginDialog();
            if (user != null) {
                onUserLoggedIn(user);
                setVisible(true);
            } else {
                System.exit(0);
            }
        }
    }

    /**
     * Configures the view and navigation tabs based strictly on user role:
     * - Customer: Browse Movies and My Bookings ONLY (Admin Console is completely hidden).
     * - Administrator: Admin Console ONLY (Browse Movies and Booking are completely hidden).
     */
    public void onUserLoggedIn(User user) {
        manager.setCurrentUser(user);

        if (userStatusLabel != null) {
            String roleTag = user.getRole().equalsIgnoreCase("Admin") ? " [Admin]" : " [Customer]";
            userStatusLabel.setText(user.getName() + roleTag);
        }

        boolean isAdmin = "Admin".equalsIgnoreCase(user.getRole());

        if (isAdmin) {
            // Admin should only have Admin Console (no browsing or booking)
            btnMovies.setVisible(false);
            btnHistory.setVisible(false);
            btnAdmin.setVisible(true);
            showAdminScreen();
        } else {
            // Customer should only have Browse Movies and My Bookings (hide admin console completely)
            btnMovies.setVisible(true);
            btnHistory.setVisible(true);
            btnAdmin.setVisible(false);
            showMoviesScreen();
        }

        if (historyPanel != null) historyPanel.refreshHistory();
        if (moviePanel != null) moviePanel.refreshList();
        if (adminPanel != null) adminPanel.refreshData();
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
