import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Module 3: BookingConfirmationPanel (Screen 5)
public class BookingConfirmationPanel extends JPanel {
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
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initializeUI();
    }

    private void initializeUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(24, 30, 24, 30)
        ));

        JLabel title = new JLabel("[OK] Booking Confirmed Successfully!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 135, 84));

        JLabel subtitle = new JLabel("Your cinema admission ticket has been generated.");
        subtitle.setFont(UIConstants.FONT_REGULAR);
        subtitle.setForeground(Color.GRAY);

        bookingIdLabel = new JLabel("Booking ID: ");
        bookingIdLabel.setFont(UIConstants.FONT_HEADING);

        txnIdLabel = new JLabel("Transaction ID: ");
        txnIdLabel.setFont(UIConstants.FONT_REGULAR);

        movieLabel = new JLabel("Movie: ");
        movieLabel.setFont(UIConstants.FONT_REGULAR);

        showtimeLabel = new JLabel("Showtime: ");
        showtimeLabel.setFont(UIConstants.FONT_REGULAR);

        seatsLabel = new JLabel("Seats: ");
        seatsLabel.setFont(UIConstants.FONT_BOLD);

        amountLabel = new JLabel("Amount Paid: ");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amountLabel.setForeground(UIConstants.COLOR_PRIMARY);

        paymentMethodLabel = new JLabel("Method: ");
        paymentMethodLabel.setFont(UIConstants.FONT_REGULAR);

        statusBadge = new JLabel("Status: CONFIRMED");
        statusBadge.setFont(UIConstants.FONT_BOLD);
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
        myBookingsBtn.setFont(UIConstants.FONT_BOLD);
        myBookingsBtn.setBackground(UIConstants.COLOR_PRIMARY);
        myBookingsBtn.setForeground(Color.WHITE);
        myBookingsBtn.setFocusPainted(false);
        myBookingsBtn.addActionListener(e -> parent.showHistoryScreen());

        JButton backMoviesBtn = new JButton("Back to Movies");
        backMoviesBtn.setFont(UIConstants.FONT_REGULAR);
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
        showtimeLabel.setText("Showtime: " + booking.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));
        seatsLabel.setText("Confirmed Seats: " + String.join(", ", booking.getSeatIds()));
        paymentMethodLabel.setText("Payment Method: " + txn.getPaymentMethodName());
        amountLabel.setText(String.format("Total Amount Paid: $%.2f", txn.getAmount()));
        statusBadge.setText("Booking Status: " + booking.getBookingStatus().getDisplayName() +
                " | Payment Status: " + txn.getPaymentStatus().getDisplayName());
    }
}
