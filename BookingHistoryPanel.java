import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Module 3: BookingHistoryPanel (Screen 6)
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

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        initializeUI();
    }

    private void initializeUI() {
        JPanel topCard = new JPanel(new BorderLayout());
        topCard.setBackground(Color.WHITE);
        topCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        customerStatusLabel = new JLabel("My Booking Records");
        customerStatusLabel.setFont(UIConstants.FONT_TITLE);
        customerStatusLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UIConstants.FONT_SMALL);
        refreshBtn.setBackground(UIConstants.COLOR_BG_LIGHT);
        refreshBtn.addActionListener(e -> refreshHistory());

        topCard.add(customerStatusLabel, BorderLayout.WEST);
        topCard.add(refreshBtn, BorderLayout.EAST);
        add(topCard, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Movie", "Screen", "Showtime", "Seats", "Total Amount", "Booking Status", "Cancellable?"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(UIConstants.FONT_REGULAR);
        bookingsTable.setRowHeight(28);
        bookingsTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        bookingsTable.getTableHeader().setBackground(UIConstants.COLOR_PRIMARY);
        bookingsTable.getTableHeader().setForeground(Color.WHITE);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(new LineBorder(UIConstants.COLOR_BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomCard.setBackground(Color.WHITE);
        bottomCard.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton viewTicketBtn = new JButton("View Ticket Details");
        viewTicketBtn.setFont(UIConstants.FONT_BOLD);
        viewTicketBtn.setBackground(UIConstants.COLOR_PRIMARY);
        viewTicketBtn.setForeground(Color.WHITE);
        viewTicketBtn.setFocusPainted(false);
        viewTicketBtn.addActionListener(e -> viewSelectedTicket());

        JButton cancelBookingBtn = new JButton("Cancel Selected Booking");
        cancelBookingBtn.setFont(UIConstants.FONT_BOLD);
        cancelBookingBtn.setBackground(new Color(220, 53, 69));
        cancelBookingBtn.setForeground(Color.WHITE);
        cancelBookingBtn.setFocusPainted(false);
        cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

        bottomCard.add(viewTicketBtn);
        bottomCard.add(cancelBookingBtn);
        add(bottomCard, BorderLayout.SOUTH);
    }

    public void refreshHistory() {
        tableModel.setRowCount(0);
        User user = manager.getCurrentUser();
        customerStatusLabel.setText("Bookings for: " + user.getName() + " (" + user.getRole() + ")");

        List<Booking> userBookings = manager.getBookingsForCustomer(user.getId());

        for (Booking b : userBookings) {
            String canCancelStr = b.isCancellable() ? "YES" : "NO (Past/Closed)";
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
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(row, 0);
        Booking booking = manager.findBookingById(bookingId);
        PaymentTransaction txn = manager.findTransactionByBookingId(bookingId);

        if (booking != null) {
            String details = String.format(
                    "=========================================\n" +
                    "             CINEBOOK E-TICKET           \n" +
                    "=========================================\n" +
                    "Booking ID:      %s\n" +
                    "Customer:        %s\n" +
                    "Movie:           %s\n" +
                    "Screen:          %s\n" +
                    "Showtime:        %s\n" +
                    "Seats:           %s\n" +
                    "Status:          %s\n" +
                    "Total Paid:      $%.2f\n" +
                    "Payment Mode:    %s\n" +
                    "Gateway Ref:     %s\n" +
                    "=========================================\n",
                    booking.getId(),
                    booking.getCustomer().getName(),
                    booking.getShow().getMovie().getTitle(),
                    booking.getShow().getScreen().getName(),
                    booking.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER),
                    String.join(", ", booking.getSeatIds()),
                    booking.getBookingStatus().getDisplayName(),
                    booking.getTotalAmount(),
                    (txn != null ? txn.getPaymentMethodName() : "N/A"),
                    (txn != null ? txn.getGatewayReference() : "N/A")
            );

            JTextArea area = new JTextArea(details);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ticket Details - " + booking.getId(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cancelSelectedBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingId = (String) tableModel.getValueAt(row, 0);
        Booking booking = manager.findBookingById(bookingId);

        if (booking == null) return;

        if (!booking.isCancellable()) {
            JOptionPane.showMessageDialog(this,
                    "This booking cannot be cancelled.\nPolicy: Bookings can only be cancelled at least 2 hours before showtime.",
                    "Cancellation Ineligible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking " + bookingId + "?\nA full simulated refund will be processed and seats will be released.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.cancelConfirmedBooking(bookingId);
                JOptionPane.showMessageDialog(this,
                        "Booking " + bookingId + " has been successfully cancelled and refunded.",
                        "Cancellation Success", JOptionPane.INFORMATION_MESSAGE);
                refreshHistory();
            } catch (BookingNotFoundException | InvalidBookingStateException ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
