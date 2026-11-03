import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

// Module 2: SeatSelectionPanel (Screen 3 - Modern Interactive Auditorium Grid)
public class SeatSelectionPanel extends JPanel {
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
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Modern Top Bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JButton backBtn = new JButton("← Back to Details");
        backBtn.setFont(UIConstants.FONT_BOLD);
        backBtn.setBackground(new Color(241, 245, 249));
        backBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        backBtn.addActionListener(e -> {
            if (currentShow != null) parent.navigateToMovieDetails(currentShow.getMovie());
            else parent.showMoviesScreen();
        });

        showInfoLabel = new JLabel("Select your seats");
        showInfoLabel.setFont(UIConstants.FONT_HEADING);
        showInfoLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(showInfoLabel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // Center: Cinema Screen + Seating Grid + Legend Card
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Modern Sleek Cinema Screen Header
        JPanel screenIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle curved beam gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229, 60), 0, getHeight(), new Color(241, 245, 249, 10));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(UIConstants.COLOR_ACCENT);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(20, 6, getWidth() - 20, 6);
            }
        };
        screenIndicator.setOpaque(false);
        screenIndicator.setPreferredSize(new Dimension(650, 40));

        JLabel screenText = new JLabel("CINEMA SCREEN (ALL EYES THIS DIRECTION)");
        screenText.setFont(UIConstants.FONT_BADGE);
        screenText.setForeground(UIConstants.COLOR_ACCENT);
        screenIndicator.add(screenText);

        JPanel screenWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        screenWrapper.setOpaque(false);
        screenWrapper.add(screenIndicator);
        centerPanel.add(screenWrapper, BorderLayout.NORTH);

        // Grid Container
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);

        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel);

        JScrollPane scrollGrid = new JScrollPane(gridWrapper);
        scrollGrid.setBorder(null);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        centerPanel.add(scrollGrid, BorderLayout.CENTER);

        // Modern Status Badges Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 6));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", UIConstants.COLOR_SEAT_AVAILABLE, UIConstants.COLOR_SEAT_AVAILABLE_BORDER));
        legendPanel.add(createLegendItem("Selected", UIConstants.COLOR_SEAT_SELECTED, new Color(5, 150, 105)));
        legendPanel.add(createLegendItem("In Checkout", UIConstants.COLOR_SEAT_RESERVED, new Color(250, 204, 21)));
        legendPanel.add(createLegendItem("Booked", UIConstants.COLOR_SEAT_BOOKED, new Color(148, 163, 184)));
        centerPanel.add(legendPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Modern Floating Bottom Bar Summary
        JPanel bottomBar = new JPanel(new BorderLayout(16, 0));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel summaryInfo = new JPanel();
        summaryInfo.setLayout(new BoxLayout(summaryInfo, BoxLayout.Y_AXIS));
        summaryInfo.setOpaque(false);

        selectedSeatsLabel = new JLabel("Selected Seats: None");
        selectedSeatsLabel.setFont(UIConstants.FONT_HEADING);
        selectedSeatsLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        priceBreakdownLabel = new JLabel("Subtotal: $0.00  |  Tax (18% GST): $0.00  |  Total Payable: $0.00");
        priceBreakdownLabel.setFont(UIConstants.FONT_REGULAR);
        priceBreakdownLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        summaryInfo.add(selectedSeatsLabel);
        summaryInfo.add(Box.createVerticalStrut(3));
        summaryInfo.add(priceBreakdownLabel);

        checkoutButton = new JButton("Proceed to Checkout  →");
        checkoutButton.setFont(UIConstants.FONT_BOLD);
        checkoutButton.setBackground(UIConstants.COLOR_ACCENT);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setEnabled(false);
        checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkoutButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        checkoutButton.addActionListener(e -> proceedToCheckout());

        bottomBar.add(summaryInfo, BorderLayout.CENTER);
        bottomBar.add(checkoutButton, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createLegendItem(String label, Color color, Color border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(20, 20));
        box.setBackground(color);
        box.setBorder(new LineBorder(border, 1, true));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.COLOR_TEXT_DARK);
        p.add(box);
        p.add(lbl);
        return p;
    }

    public void setShow(Show show) {
        this.currentShow = show;
        this.selectedSeatIds.clear();
        this.seatButtons.clear();

        showInfoLabel.setText(show.getMovie().getTitle() + "  •  " +
                show.getScreen().getName() + "  •  " + show.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));

        buildSeatGrid();
        updateSummary();
    }

    private void buildSeatGrid() {
        gridPanel.removeAll();
        if (currentShow == null) return;

        Seat[][] seats = currentShow.getSeats();
        int rows = currentShow.getScreen().getRows();
        int cols = currentShow.getScreen().getColumns();

        gridPanel.setLayout(new GridLayout(rows, cols + 1, 10, 10));

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            JLabel rowLbl = new JLabel(String.valueOf(rowChar), SwingConstants.CENTER);
            rowLbl.setFont(UIConstants.FONT_BOLD);
            rowLbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
            rowLbl.setPreferredSize(new Dimension(26, 42));
            gridPanel.add(rowLbl);

            for (int c = 0; c < cols; c++) {
                Seat seat = seats[r][c];
                JButton seatBtn = new JButton(seat.getId());
                seatBtn.setFont(UIConstants.FONT_BOLD);
                seatBtn.setFocusPainted(false);
                seatBtn.setMargin(new Insets(2, 2, 2, 2));
                seatBtn.setPreferredSize(new Dimension(54, 42));
                seatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
            btn.setBackground(UIConstants.COLOR_SEAT_BOOKED);
            btn.setForeground(new Color(148, 163, 184));
            btn.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
            btn.setEnabled(false);
            btn.setToolTipText(seat.getId() + " - Already Booked");
        } else if (seat.getStatus() == SeatStatus.RESERVED && !seat.isAvailable()) {
            btn.setBackground(UIConstants.COLOR_SEAT_RESERVED);
            btn.setForeground(new Color(133, 77, 14));
            btn.setBorder(new LineBorder(new Color(250, 204, 21), 1, true));
            btn.setEnabled(false);
            btn.setToolTipText(seat.getId() + " - In Checkout Hold by Another Customer");
        } else if (selectedSeatIds.contains(seat.getId())) {
            btn.setBackground(UIConstants.COLOR_SEAT_SELECTED);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new LineBorder(new Color(5, 150, 105), 2, true));
            btn.setEnabled(true);
            btn.setToolTipText(seat.getId() + " - Selected ($" + String.format("%.2f", currentShow.calculateSeatPrice(seat)) + ")");
        } else {
            btn.setBackground(UIConstants.COLOR_SEAT_AVAILABLE);
            btn.setForeground(new Color(22, 101, 52));
            btn.setBorder(new LineBorder(UIConstants.COLOR_SEAT_AVAILABLE_BORDER, 1, true));
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
            priceBreakdownLabel.setText("Subtotal: $0.00  |  Tax (18% GST): $0.00  |  Total Payable: $0.00");
            checkoutButton.setEnabled(false);
            checkoutButton.setBackground(new Color(203, 213, 225));
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
        priceBreakdownLabel.setText(String.format("Subtotal: $%.2f  |  Tax (18%% GST): $%.2f  |  Total Payable: $%.2f",
                subtotal, tax, grandTotal));
        checkoutButton.setEnabled(true);
        checkoutButton.setBackground(UIConstants.COLOR_ACCENT);
    }

    private void proceedToCheckout() {
        if (selectedSeatIds.isEmpty() || currentShow == null) return;

        User currentUser = BookingManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(this,
                    "Only Customers can book tickets. Please log in with a Customer account (e.g. username 'user').",
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
