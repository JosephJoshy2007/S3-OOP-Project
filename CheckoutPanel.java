import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// Module 4: CheckoutPanel (Screen 4 - Modern Checkout & Payment Simulator)
public class CheckoutPanel extends JPanel {
    private final MainFrame parent;
    private final PaymentProcessor paymentProcessor;
    private final BookingService bookingService;

    private Booking currentBooking;

    private JLabel posterLabel;
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

        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Modern Top Bar with Countdown Timer Pill
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JButton cancelBtn = new JButton("← Cancel & Release Seats");
        cancelBtn.setFont(UIConstants.FONT_BOLD);
        cancelBtn.setBackground(new Color(254, 242, 242));
        cancelBtn.setForeground(new Color(220, 38, 38));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        cancelBtn.addActionListener(e -> cancelAndAbandon());

        JLabel headerTitle = new JLabel("Checkout & Simulated Payment Gateway");
        headerTitle.setFont(UIConstants.FONT_HEADING);
        headerTitle.setForeground(UIConstants.COLOR_TEXT_DARK);

        timerLabel = new JLabel("Hold Window: 05:00");
        timerLabel.setFont(UIConstants.FONT_BOLD);
        timerLabel.setForeground(new Color(220, 38, 38));
        timerLabel.setBackground(new Color(254, 242, 242));
        timerLabel.setOpaque(true);
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));

        topBar.add(cancelBtn, BorderLayout.WEST);
        topBar.add(headerTitle, BorderLayout.CENTER);
        topBar.add(timerLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center 2-Column Split Layout
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // Left Card: Order Summary with Poster
        JPanel summaryCard = new JPanel(new BorderLayout(16, 16));
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel summaryTitle = new JLabel("Booking Order Summary");
        summaryTitle.setFont(UIConstants.FONT_TITLE);
        summaryTitle.setForeground(UIConstants.COLOR_PRIMARY);
        summaryCard.add(summaryTitle, BorderLayout.NORTH);

        JPanel summaryBody = new JPanel(new BorderLayout(16, 0));
        summaryBody.setOpaque(false);

        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(110, 160));
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        summaryBody.add(posterLabel, BorderLayout.WEST);

        JPanel summaryDetails = new JPanel();
        summaryDetails.setLayout(new BoxLayout(summaryDetails, BoxLayout.Y_AXIS));
        summaryDetails.setOpaque(false);

        bookingIdLabel = new JLabel("Reference: -");
        bookingIdLabel.setFont(UIConstants.FONT_BOLD);
        bookingIdLabel.setForeground(UIConstants.COLOR_ACCENT);

        movieDetailsLabel = new JLabel("Show: -");
        movieDetailsLabel.setFont(UIConstants.FONT_REGULAR);
        movieDetailsLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        seatListLabel = new JLabel("Seats: -");
        seatListLabel.setFont(UIConstants.FONT_REGULAR);
        seatListLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        amountPayableLabel = new JLabel("Total Payable: $0.00");
        amountPayableLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountPayableLabel.setForeground(UIConstants.COLOR_SUCCESS);

        summaryDetails.add(bookingIdLabel);
        summaryDetails.add(Box.createVerticalStrut(8));
        summaryDetails.add(movieDetailsLabel);
        summaryDetails.add(Box.createVerticalStrut(8));
        summaryDetails.add(seatListLabel);
        summaryDetails.add(Box.createVerticalStrut(14));
        summaryDetails.add(new JSeparator());
        summaryDetails.add(Box.createVerticalStrut(14));
        summaryDetails.add(amountPayableLabel);

        summaryBody.add(summaryDetails, BorderLayout.CENTER);
        summaryCard.add(summaryBody, BorderLayout.CENTER);

        // Right Card: Modern Payment Selection Form
        JPanel payCard = new JPanel();
        payCard.setLayout(new BoxLayout(payCard, BoxLayout.Y_AXIS));
        payCard.setBackground(Color.WHITE);
        payCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel payTitle = new JLabel("Payment Method & Verification");
        payTitle.setFont(UIConstants.FONT_TITLE);
        payTitle.setForeground(UIConstants.COLOR_PRIMARY);
        payTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel methodLbl = new JLabel("Select Payment Channel (Strategy Pattern):");
        methodLbl.setFont(UIConstants.FONT_BOLD);
        methodLbl.setForeground(UIConstants.COLOR_TEXT_DARK);
        methodLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] methods = {
                "Credit / Debit Card (MockCardPayment)",
                "UPI Instant Pay (MockUpiPayment)",
                "Digital Wallet (MockWalletPayment)",
                "Legacy Bank DirectDebit (Adapter Pattern)"
        };
        paymentMethodCombo = new JComboBox<>(methods);
        paymentMethodCombo.setFont(UIConstants.FONT_REGULAR);
        paymentMethodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        paymentMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentMethodCombo.addActionListener(e -> updatePaymentFields());

        dynamicPaymentFieldsPanel = new JPanel();
        dynamicPaymentFieldsPanel.setLayout(new GridLayout(2, 2, 8, 8));
        dynamicPaymentFieldsPanel.setOpaque(false);
        dynamicPaymentFieldsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        dynamicPaymentFieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel simLbl = new JLabel("Simulation Mode (Demonstration Test Cases):");
        simLbl.setFont(UIConstants.FONT_BOLD);
        simLbl.setForeground(UIConstants.COLOR_TEXT_DARK);
        simLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] simModes = {
                "SUCCESS - Authorized & Completed",
                "FORCE_FAIL - Simulate Bank Declined",
                "USER_CANCEL - Abandon & Release Seats"
        };
        simulationModeCombo = new JComboBox<>(simModes);
        simulationModeCombo.setFont(UIConstants.FONT_REGULAR);
        simulationModeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        simulationModeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton payButton = new JButton("Complete Payment  →");
        payButton.setFont(UIConstants.FONT_BOLD);
        payButton.setBackground(UIConstants.COLOR_SUCCESS);
        payButton.setForeground(Color.WHITE);
        payButton.setFocusPainted(false);
        payButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        payButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        payButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(5, 150, 105), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        payButton.addActionListener(e -> executePayment());

        payCard.add(payTitle);
        payCard.add(Box.createVerticalStrut(14));
        payCard.add(methodLbl);
        payCard.add(Box.createVerticalStrut(6));
        payCard.add(paymentMethodCombo);
        payCard.add(Box.createVerticalStrut(14));
        payCard.add(dynamicPaymentFieldsPanel);
        payCard.add(Box.createVerticalStrut(14));
        payCard.add(simLbl);
        payCard.add(Box.createVerticalStrut(6));
        payCard.add(simulationModeCombo);
        payCard.add(Box.createVerticalStrut(20));
        payCard.add(payButton);

        centerGrid.add(summaryCard);
        centerGrid.add(payCard);
        add(centerGrid, BorderLayout.CENTER);

        updatePaymentFields();
    }

    private void updatePaymentFields() {
        dynamicPaymentFieldsPanel.removeAll();
        int idx = paymentMethodCombo.getSelectedIndex();

        if (idx == 0) { // Card
            dynamicPaymentFieldsPanel.add(new JLabel("Card Number:"));
            cardNumField = new JTextField("4532 8812 9012 3456");
            dynamicPaymentFieldsPanel.add(cardNumField);

            dynamicPaymentFieldsPanel.add(new JLabel("Cardholder Name:"));
            cardHolderField = new JTextField("Aarav Sharma");
            dynamicPaymentFieldsPanel.add(cardHolderField);
        } else if (idx == 1) { // UPI
            dynamicPaymentFieldsPanel.add(new JLabel("Virtual Payment Address (VPA):"));
            upiField = new JTextField("user@okaxis");
            dynamicPaymentFieldsPanel.add(upiField);
            dynamicPaymentFieldsPanel.add(new JLabel(""));
            dynamicPaymentFieldsPanel.add(new JLabel(""));
        } else if (idx == 2) { // Wallet
            dynamicPaymentFieldsPanel.add(new JLabel("Simulated Balance ($):"));
            walletBalanceField = new JTextField("150.00");
            dynamicPaymentFieldsPanel.add(walletBalanceField);
            dynamicPaymentFieldsPanel.add(new JLabel(""));
            dynamicPaymentFieldsPanel.add(new JLabel(""));
        } else { // Adapter
            JLabel adapterNote = new JLabel("<html><font color='#4F46E5'>Adapts LegacyBankGatewayApi via LegacyBankGatewayAdapter</font></html>");
            dynamicPaymentFieldsPanel.add(adapterNote);
            dynamicPaymentFieldsPanel.add(new JLabel(""));
        }

        dynamicPaymentFieldsPanel.revalidate();
        dynamicPaymentFieldsPanel.repaint();
    }

    public void setBooking(Booking booking) {
        this.currentBooking = booking;
        this.remainingSeconds = 300;

        Movie movie = booking.getShow().getMovie();
        posterLabel.setIcon(UIConstants.getMoviePoster(movie.getId(), 110, 160));

        bookingIdLabel.setText("Reference: " + booking.getId());
        movieDetailsLabel.setText("Show: " + movie.getTitle() + " (" + booking.getShow().getScreen().getName() + ")");
        seatListLabel.setText("Reserved Seats: " + String.join(", ", booking.getSeatIds()));
        amountPayableLabel.setText(String.format("Total Payable: $%.2f", booking.getTotalAmount()));

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            int mins = remainingSeconds / 60;
            int secs = remainingSeconds % 60;
            timerLabel.setText(String.format("Hold Window: %02d:%02d", mins, secs));

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
