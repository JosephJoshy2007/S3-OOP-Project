import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Module 3: BookingConfirmationPanel (Screen 5 - Modern Success Receipt)
public class BookingConfirmationPanel extends JPanel {
    private final MainFrame parent;

    private JLabel posterLabel;
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
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initializeUI();
    }

    private void initializeUI() {
        JPanel card = new JPanel(new BorderLayout(20, 20));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(650, 480));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel successPill = UIConstants.createBadge("✓ PAYMENT VERIFIED & ADMISSION CONFIRMED", new Color(236, 253, 245), new Color(5, 150, 105));
        successPill.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Booking Confirmed Successfully!");
        title.setFont(UIConstants.FONT_HERO);
        title.setForeground(UIConstants.COLOR_TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Your official admission e-ticket has been minted and stored in the database.");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(successPill);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);

        card.add(headerPanel, BorderLayout.NORTH);

        // Body: Left Poster Thumbnail, Right Metadata List
        JPanel bodyPanel = new JPanel(new BorderLayout(18, 0));
        bodyPanel.setOpaque(false);

        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(130, 190));
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        bodyPanel.add(posterLabel, BorderLayout.WEST);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        bookingIdLabel = new JLabel("Booking ID: ");
        bookingIdLabel.setFont(UIConstants.FONT_HEADING);
        bookingIdLabel.setForeground(UIConstants.COLOR_ACCENT);

        txnIdLabel = new JLabel("Transaction ID: ");
        txnIdLabel.setFont(UIConstants.FONT_SMALL);
        txnIdLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        statusBadge = new JLabel("Status: CONFIRMED");
        statusBadge.setFont(UIConstants.FONT_BOLD);
        statusBadge.setForeground(UIConstants.COLOR_SUCCESS);

        movieLabel = new JLabel("Movie: ");
        movieLabel.setFont(UIConstants.FONT_REGULAR);
        movieLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        showtimeLabel = new JLabel("Showtime: ");
        showtimeLabel.setFont(UIConstants.FONT_REGULAR);
        showtimeLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        seatsLabel = new JLabel("Seats: ");
        seatsLabel.setFont(UIConstants.FONT_BOLD);
        seatsLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        paymentMethodLabel = new JLabel("Method: ");
        paymentMethodLabel.setFont(UIConstants.FONT_REGULAR);
        paymentMethodLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        amountLabel = new JLabel("Amount Paid: ");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLabel.setForeground(UIConstants.COLOR_SUCCESS);

        detailsPanel.add(bookingIdLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(txnIdLabel);
        detailsPanel.add(Box.createVerticalStrut(6));
        detailsPanel.add(statusBadge);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(new JSeparator());
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(movieLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(showtimeLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(seatsLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(paymentMethodLabel);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(amountLabel);

        bodyPanel.add(detailsPanel, BorderLayout.CENTER);
        card.add(bodyPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actionsPanel.setOpaque(false);

        JButton myBookingsBtn = new JButton("View in My Bookings  📄");
        myBookingsBtn.setFont(UIConstants.FONT_BOLD);
        myBookingsBtn.setBackground(UIConstants.COLOR_ACCENT);
        myBookingsBtn.setForeground(Color.WHITE);
        myBookingsBtn.setFocusPainted(false);
        myBookingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        myBookingsBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        myBookingsBtn.addActionListener(e -> parent.showHistoryScreen());

        JButton backMoviesBtn = new JButton("Browse More Movies");
        backMoviesBtn.setFont(UIConstants.FONT_REGULAR);
        backMoviesBtn.setBackground(new Color(241, 245, 249));
        backMoviesBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        backMoviesBtn.setFocusPainted(false);
        backMoviesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backMoviesBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        backMoviesBtn.addActionListener(e -> parent.showMoviesScreen());

        actionsPanel.add(myBookingsBtn);
        actionsPanel.add(backMoviesBtn);
        card.add(actionsPanel, BorderLayout.SOUTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);
    }

    public void displayConfirmation(Booking booking, PaymentTransaction txn) {
        Movie movie = booking.getShow().getMovie();
        posterLabel.setIcon(UIConstants.getMoviePoster(movie.getId(), 130, 190));

        bookingIdLabel.setText("Booking ID: " + booking.getId());
        txnIdLabel.setText("Transaction Reference: " + txn.getId() + " (" + txn.getGatewayReference() + ")");
        movieLabel.setText("Movie: " + movie.getTitle());
        showtimeLabel.setText("Screen & Showtime: " + booking.getShow().getScreen().getName() + " • " +
                booking.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));
        seatsLabel.setText("Confirmed Seats: " + String.join(", ", booking.getSeatIds()));
        paymentMethodLabel.setText("Payment Method: " + txn.getPaymentMethodName());
        amountLabel.setText(String.format("Total Amount Paid: $%.2f", txn.getAmount()));
        statusBadge.setText("Booking: " + booking.getBookingStatus().getDisplayName() +
                "  •  Payment: " + txn.getPaymentStatus().getDisplayName());
    }
}
