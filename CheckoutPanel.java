import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Module 4: CheckoutPanel (Screen 4)
public class CheckoutPanel extends JPanel {
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
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        initializeUI();
    }

    private void initializeUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JButton cancelBtn = new JButton("<< Cancel & Release Seats");
        cancelBtn.setFont(UIConstants.FONT_SMALL);
        cancelBtn.setBackground(new Color(254, 237, 237));
        cancelBtn.setForeground(new Color(180, 40, 40));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> cancelAndAbandon());

        JLabel headerTitle = new JLabel("Checkout & Simulated Payment");
        headerTitle.setFont(UIConstants.FONT_HEADING);
        headerTitle.setForeground(UIConstants.COLOR_TEXT_DARK);

        timerLabel = new JLabel("Time Remaining: 05:00");
        timerLabel.setFont(UIConstants.FONT_BOLD);
        timerLabel.setForeground(new Color(200, 35, 51));

        topBar.add(cancelBtn, BorderLayout.WEST);
        topBar.add(headerTitle, BorderLayout.CENTER);
        topBar.add(timerLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setBackground(UIConstants.COLOR_BG_LIGHT);
        centerGrid.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Left Card: Booking Order Summary
        JPanel summaryCard = new JPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel summaryTitle = new JLabel("Booking Order Summary");
        summaryTitle.setFont(UIConstants.FONT_TITLE);
        summaryTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        bookingIdLabel = new JLabel("Booking Reference: -");
        bookingIdLabel.setFont(UIConstants.FONT_BOLD);

        movieDetailsLabel = new JLabel("Movie: -");
        movieDetailsLabel.setFont(UIConstants.FONT_REGULAR);

        seatListLabel = new JLabel("Seats: -");
        seatListLabel.setFont(UIConstants.FONT_REGULAR);

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
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel payTitle = new JLabel("Payment Options (Simulated Gateway)");
        payTitle.setFont(UIConstants.FONT_TITLE);
        payTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        paymentMethodCombo = new JComboBox<>(new String[]{
                "Mock Credit/Debit Card",
                "Mock UPI Instant Pay",
                "Mock Digital Wallet",
                "Legacy Bank DirectDebit (Adapter Pattern)"
        });
        paymentMethodCombo.setFont(UIConstants.FONT_REGULAR);
        paymentMethodCombo.addActionListener(e -> updatePaymentFormFields());

        simulationModeCombo = new JComboBox<>(new String[]{
                "SUCCESS (Approved)",
                "FORCE_FAIL (Bank Decline Simulation)",
                "USER_CANCEL (Simulated Abort)"
        });
        simulationModeCombo.setFont(UIConstants.FONT_REGULAR);

        dynamicPaymentFieldsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        dynamicPaymentFieldsPanel.setBackground(Color.WHITE);
        dynamicPaymentFieldsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        cardNumField = new JTextField("4111 2222 3333 4444");
        cardHolderField = new JTextField("Aarav Sharma");
        upiField = new JTextField("aarav@okhdfcbank");
        walletBalanceField = new JTextField("150.00");

        updatePaymentFormFields();

        JButton payButton = new JButton("Pay & Confirm Booking");
        payButton.setFont(UIConstants.FONT_BOLD);
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
            adapterNote.setFont(UIConstants.FONT_SMALL);
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
                " (" + booking.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER) + ")");
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
