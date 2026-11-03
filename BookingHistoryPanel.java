import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Module 3: BookingHistoryPanel (Screen 6 - Modern My Bookings & E-Ticket Preview)
public class BookingHistoryPanel extends JPanel {
    private final MainFrame parent;
    private final BookingManager manager;
    private final BookingService bookingService;

    private DefaultTableModel tableModel;
    private JTable bookingsTable;
    private JLabel customerStatusLabel;

    public BookingHistoryPanel(MainFrame parent, BookingManager manager, BookingService bookingService) {
        this.parent = parent;
        this.manager = manager;
        this.bookingService = bookingService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Modern Top Card
        JPanel topCard = new JPanel(new BorderLayout());
        topCard.setBackground(Color.WHITE);
        topCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        customerStatusLabel = new JLabel("My Booking Records");
        customerStatusLabel.setFont(UIConstants.FONT_TITLE);
        customerStatusLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UIConstants.FONT_BOLD);
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        refreshBtn.addActionListener(e -> refreshHistory());

        topCard.add(customerStatusLabel, BorderLayout.WEST);
        topCard.add(refreshBtn, BorderLayout.EAST);
        add(topCard, BorderLayout.NORTH);

        // Modern Tabular Ledger
        String[] columns = {"Booking ID", "Movie Title", "Auditorium", "Showtime", "Seats", "Total Paid", "Status", "Cancellation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(UIConstants.FONT_REGULAR);
        bookingsTable.setRowHeight(34);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setSelectionBackground(new Color(238, 242, 255));
        bookingsTable.setSelectionForeground(UIConstants.COLOR_TEXT_DARK);
        bookingsTable.setShowGrid(true);
        bookingsTable.setGridColor(new Color(241, 245, 249));

        // Modern Table Header
        bookingsTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        bookingsTable.getTableHeader().setBackground(UIConstants.COLOR_PRIMARY);
        bookingsTable.getTableHeader().setForeground(Color.WHITE);
        bookingsTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Center align specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bookingsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(new LineBorder(UIConstants.COLOR_BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomCard.setBackground(Color.WHITE);
        bottomCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JButton viewTicketBtn = new JButton("View E-Ticket  📄");
        viewTicketBtn.setFont(UIConstants.FONT_BOLD);
        viewTicketBtn.setBackground(UIConstants.COLOR_ACCENT);
        viewTicketBtn.setForeground(Color.WHITE);
        viewTicketBtn.setFocusPainted(false);
        viewTicketBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewTicketBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        viewTicketBtn.addActionListener(e -> viewSelectedTicket());

        JButton cancelBookingBtn = new JButton("Cancel Selected Booking");
        cancelBookingBtn.setFont(UIConstants.FONT_BOLD);
        cancelBookingBtn.setBackground(new Color(254, 242, 242));
        cancelBookingBtn.setForeground(new Color(220, 38, 38));
        cancelBookingBtn.setFocusPainted(false);
        cancelBookingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBookingBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

        bottomCard.add(viewTicketBtn);
        bottomCard.add(cancelBookingBtn);
        add(bottomCard, BorderLayout.SOUTH);
    }

    public void refreshHistory() {
        tableModel.setRowCount(0);
        User user = manager.getCurrentUser();
        if (user == null) {
            customerStatusLabel.setText("Please log in to view booking records.");
            return;
        }

        List<Booking> userBookings;
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            customerStatusLabel.setText("All Cinema Bookings (Administrator View - " + user.getName() + ")");
            userBookings = manager.getBookings();
        } else {
            customerStatusLabel.setText("Bookings for: " + user.getName() + " (" + user.getRole() + ")");
            userBookings = manager.getBookingsForCustomer(user.getId());
        }

        for (Booking b : userBookings) {
            String canCancelStr = b.isCancellable() ? "Eligible" : "Non-Cancellable";
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getShow().getMovie().getTitle(),
                    b.getShow().getScreen().getName(),
                    b.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER),
                    String.join(", ", b.getSeatIds()),
                    String.format("$%.2f", b.getTotalAmount()),
                    b.getBookingStatus().getDisplayName(),
                    canCancelStr
            });
        }
    }

    private void viewSelectedTicket() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view the e-ticket.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(row, 0);
        Booking booking = manager.findBookingById(bookingId);
        PaymentTransaction txn = manager.findTransactionByBookingId(bookingId);

        if (booking != null) {
            String details = String.format(
                    "==========================================================\n" +
                    "                  CINEBOOK CINEMA E-TICKET                \n" +
                    "==========================================================\n" +
                    "Booking Reference:    %s\n" +
                    "Customer Name:        %s\n" +
                    "Movie Presentation:   %s\n" +
                    "Screen / Hall:        %s\n" +
                    "Screening Showtime:   %s\n" +
                    "Seats Allocated:      %s\n" +
                    "Booking Status:       %s\n" +
                    "Total Amount Paid:    $%.2f (Incl. 18%% GST)\n" +
                    "Payment Method:       %s\n" +
                    "Gateway Auth Ref:     %s\n" +
                    "==========================================================\n" +
                    "   * Please present this confirmation at the theater lobby.\n" +
                    "   * Cancellations permitted up to 2 hours prior to showtime.\n",
                    booking.getId(),
                    booking.getCustomer().getName(),
                    booking.getShow().getMovie().getTitle(),
                    booking.getShow().getScreen().getName(),
                    booking.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER),
                    String.join(", ", booking.getSeatIds()),
                    booking.getBookingStatus().getDisplayName(),
                    booking.getTotalAmount(),
                    (txn != null ? txn.getPaymentMethodName() : "Simulated Checkout"),
                    (txn != null ? txn.getGatewayReference() : "OFFLINE-RECORD")
            );

            JTextArea area = new JTextArea(details);
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);
            area.setBackground(new Color(248, 250, 252));
            area.setBorder(new EmptyBorder(12, 12, 12, 12));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(520, 360));
            JOptionPane.showMessageDialog(this, scroll, "CineBook Digital Admission Ticket", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void cancelSelectedBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel booking #" + bookingId + "?\nSeats will be immediately released back to available status.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.cancelConfirmedBooking(bookingId);
                JOptionPane.showMessageDialog(this, "Booking " + bookingId + " cancelled successfully. Seats released.",
                        "Cancellation Success", JOptionPane.INFORMATION_MESSAGE);
                refreshHistory();
            } catch (BookingNotFoundException | InvalidBookingStateException ex) {
                JOptionPane.showMessageDialog(this, "Cannot cancel booking: " + ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
