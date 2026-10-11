import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Module 5: MainFrame
public class MainFrame extends JFrame {
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
        nav.setBackground(UIConstants.COLOR_PRIMARY);
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
        userStatusLabel.setFont(UIConstants.FONT_REGULAR);
        userStatusLabel.setForeground(Color.WHITE);
        rightPanel.add(userStatusLabel);

        JButton switchUserBtn = new JButton("Switch User");
        switchUserBtn.setFont(UIConstants.FONT_SMALL);
        switchUserBtn.setBackground(UIConstants.COLOR_PRIMARY_DARK);
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
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UIConstants.COLOR_PRIMARY_DARK);
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
