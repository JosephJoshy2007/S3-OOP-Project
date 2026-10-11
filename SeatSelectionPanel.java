import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

// Module 2: SeatSelectionPanel (Screen 3)
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

        JButton backBtn = new JButton("<< Back to Movie Details");
        backBtn.setFont(UIConstants.FONT_SMALL);
        backBtn.setBackground(UIConstants.COLOR_BG_LIGHT);
        backBtn.setFocusPainted(false);
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

        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setBackground(UIConstants.COLOR_BG_LIGHT);
        centerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JPanel screenIndicator = new JPanel();
        screenIndicator.setBackground(new Color(200, 210, 225));
        screenIndicator.setBorder(new LineBorder(UIConstants.COLOR_PRIMARY, 2, true));
        screenIndicator.setPreferredSize(new Dimension(600, 36));
        JLabel screenText = new JLabel("THEATER SCREEN THIS WAY (ALL EYES FRONT)");
        screenText.setFont(UIConstants.FONT_BOLD);
        screenText.setForeground(UIConstants.COLOR_PRIMARY_DARK);
        screenIndicator.add(screenText);

        JPanel screenWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        screenWrapper.setOpaque(false);
        screenWrapper.add(screenIndicator);
        centerPanel.add(screenWrapper, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        JScrollPane scrollGrid = new JScrollPane(gridPanel);
        scrollGrid.setBorder(null);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        centerPanel.add(scrollGrid, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Available", UIConstants.COLOR_SEAT_AVAILABLE));
        legendPanel.add(createLegendItem("Selected", UIConstants.COLOR_SEAT_SELECTED));
        legendPanel.add(createLegendItem("Reserved", UIConstants.COLOR_SEAT_RESERVED));
        legendPanel.add(createLegendItem("Booked", UIConstants.COLOR_SEAT_BOOKED));
        centerPanel.add(legendPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel summaryInfo = new JPanel(new GridLayout(2, 1, 4, 4));
        summaryInfo.setOpaque(false);

        selectedSeatsLabel = new JLabel("Selected Seats: None");
        selectedSeatsLabel.setFont(UIConstants.FONT_BOLD);
        selectedSeatsLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        priceBreakdownLabel = new JLabel("Subtotal: $0.00 | Total with Tax (18%): $0.00");
        priceBreakdownLabel.setFont(UIConstants.FONT_REGULAR);
        priceBreakdownLabel.setForeground(new Color(90, 90, 90));

        summaryInfo.add(selectedSeatsLabel);
        summaryInfo.add(priceBreakdownLabel);

        checkoutButton = new JButton("Proceed to Checkout >>");
        checkoutButton.setFont(UIConstants.FONT_BOLD);
        checkoutButton.setBackground(UIConstants.COLOR_PRIMARY);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setEnabled(false);
        checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkoutButton.addActionListener(e -> proceedToCheckout());

        bottomBar.add(summaryInfo, BorderLayout.CENTER);
        bottomBar.add(checkoutButton, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(18, 18));
        box.setBackground(color);
        box.setBorder(new LineBorder(Color.GRAY, 1));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        p.add(box);
        p.add(lbl);
        return p;
    }

    public void setShow(Show show) {
        this.currentShow = show;
        this.selectedSeatIds.clear();
        this.seatButtons.clear();

        showInfoLabel.setText("  " + show.getMovie().getTitle() + " - " +
                show.getScreen().getName() + " (" + show.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER) + ")");

        buildSeatGrid();
        updateSummary();
    }

    private void buildSeatGrid() {
        gridPanel.removeAll();
        if (currentShow == null) return;

        Seat[][] seats = currentShow.getSeats();
        int rows = currentShow.getScreen().getRows();
        int cols = currentShow.getScreen().getColumns();

        gridPanel.setLayout(new GridLayout(rows, cols + 1, 8, 8));

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);
            JLabel rowLbl = new JLabel(String.valueOf(rowChar), SwingConstants.CENTER);
            rowLbl.setFont(UIConstants.FONT_BOLD);
            gridPanel.add(rowLbl);

            for (int c = 0; c < cols; c++) {
                Seat seat = seats[r][c];
                JButton seatBtn = new JButton(seat.getId());
                seatBtn.setFont(UIConstants.FONT_SMALL);
                seatBtn.setFocusPainted(false);
                seatBtn.setMargin(new Insets(2, 2, 2, 2));
                seatBtn.setPreferredSize(new Dimension(50, 40));

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
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
            btn.setToolTipText(seat.getId() + " - Booked");
        } else if (seat.getStatus() == SeatStatus.RESERVED && !seat.isAvailable()) {
            btn.setBackground(UIConstants.COLOR_SEAT_RESERVED);
            btn.setForeground(Color.BLACK);
            btn.setEnabled(false);
            btn.setToolTipText(seat.getId() + " - In Checkout by another user");
        } else if (selectedSeatIds.contains(seat.getId())) {
            btn.setBackground(UIConstants.COLOR_SEAT_SELECTED);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(true);
            btn.setToolTipText(seat.getId() + " - Selected ($" + String.format("%.2f", currentShow.calculateSeatPrice(seat)) + ")");
        } else {
            btn.setBackground(UIConstants.COLOR_SEAT_AVAILABLE);
            btn.setForeground(UIConstants.COLOR_TEXT_DARK);
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
            priceBreakdownLabel.setText("Subtotal: $0.00 | Total with Tax (18%): $0.00");
            checkoutButton.setEnabled(false);
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
        priceBreakdownLabel.setText(String.format("Subtotal: $%.2f | Tax (18%% GST): $%.2f | Total Payable: $%.2f",
                subtotal, tax, grandTotal));
        checkoutButton.setEnabled(true);
    }

    private void proceedToCheckout() {
        if (selectedSeatIds.isEmpty() || currentShow == null) return;

        User currentUser = BookingManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(this,
                    "Only Customers can book tickets. Please switch to a Customer account using the top-right button.",
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
