import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

// Module 6: AdminPanel (Screen 7 - Administration)
public class AdminPanel extends JPanel {
    private final MainFrame parent;
    private final BookingManager manager;
    private final MovieService movieService;
    private final ShowService showService;

    private DefaultTableModel moviesTableModel;
    private DefaultTableModel showsTableModel;
    private DefaultTableModel bookingsTableModel;
    private DefaultTableModel txnsTableModel;

    private JLabel statsLabel;

    public AdminPanel(MainFrame parent, BookingManager manager, MovieService movieService, ShowService showService) {
        this.parent = parent;
        this.manager = manager;
        this.movieService = movieService;
        this.showService = showService;

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

        JLabel title = new JLabel("CineBook Administration Console");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JButton refreshBtn = new JButton("Refresh All Data");
        refreshBtn.setFont(UIConstants.FONT_SMALL);
        refreshBtn.setBackground(UIConstants.COLOR_BG_LIGHT);
        refreshBtn.addActionListener(e -> refreshData());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);

        tabbedPane.addTab("Manage Movies", createMoviesTab());
        tabbedPane.addTab("Manage Shows", createShowsTab());
        tabbedPane.addTab("All Bookings", createBookingsTab());
        tabbedPane.addTab("Financial Transactions", createTxnsTab());
        tabbedPane.addTab("Business Metrics & Reports", createStatsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMoviesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"ID", "Title", "Genre", "Duration", "Language", "Cert", "Description"};
        moviesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(moviesTableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);

        JButton addBtn = new JButton("+ Add Movie");
        addBtn.setFont(UIConstants.FONT_BOLD);
        addBtn.setBackground(UIConstants.COLOR_PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> promptAddMovie());

        JButton delBtn = new JButton("Delete Movie (Safe Guard)");
        delBtn.setFont(UIConstants.FONT_BOLD);
        delBtn.setBackground(new Color(220, 53, 69));
        delBtn.setForeground(Color.WHITE);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a movie first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String mId = (String) moviesTableModel.getValueAt(row, 0);
            try {
                movieService.removeMovie(mId);
                JOptionPane.showMessageDialog(this, "Movie removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete movie: " + ex.getMessage(), "Guard Protected", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(addBtn);
        actions.add(delBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void promptAddMovie() {
        JTextField titleField = new JTextField();
        JTextField genreField = new JTextField("Action");
        JTextField durField = new JTextField("120");
        JTextField langField = new JTextField("English");
        JTextField certField = new JTextField("PG-13");
        JTextField descField = new JTextField("Film description");

        Object[] fields = {
                "Movie Title:", titleField,
                "Genre:", genreField,
                "Duration (minutes):", durField,
                "Language:", langField,
                "Certification:", certField,
                "Description:", descField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add New Movie", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                int dur = Integer.parseInt(durField.getText().trim());
                String id = "MOV-" + String.format("%02d", manager.getMovies().size() + 1);
                Movie m = new Movie(id, title, genreField.getText().trim(), dur,
                        langField.getText().trim(), certField.getText().trim(), descField.getText().trim());
                movieService.addMovie(m);
                JOptionPane.showMessageDialog(this, "Movie " + title + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createShowsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Show ID", "Movie", "Screen", "Showtime", "Base Price", "Available Seats"};
        showsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(showsTableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);

        JButton addBtn = new JButton("+ Add Show");
        addBtn.setFont(UIConstants.FONT_BOLD);
        addBtn.setBackground(UIConstants.COLOR_PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> promptAddShow());

        JButton delBtn = new JButton("Cancel/Remove Show");
        delBtn.setFont(UIConstants.FONT_BOLD);
        delBtn.setBackground(new Color(220, 53, 69));
        delBtn.setForeground(Color.WHITE);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a show first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String sId = (String) showsTableModel.getValueAt(row, 0);
            try {
                showService.removeShow(sId);
                JOptionPane.showMessageDialog(this, "Show removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, "Cannot remove show: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(addBtn);
        actions.add(delBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void promptAddShow() {
        if (manager.getMovies().isEmpty() || manager.getScreens().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add movies and screens before scheduling shows.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Movie> movieCombo = new JComboBox<>(manager.getMovies().toArray(new Movie[0]));
        JComboBox<Screen> screenCombo = new JComboBox<>(manager.getScreens().toArray(new Screen[0]));
        JTextField hoursField = new JTextField("3");
        JTextField priceField = new JTextField("15.00");

        Object[] fields = {
                "Movie:", movieCombo,
                "Screen:", screenCombo,
                "Hours from now:", hoursField,
                "Base Ticket Price ($):", priceField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Schedule New Show", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Movie m = (Movie) movieCombo.getSelectedItem();
                Screen sc = (Screen) screenCombo.getSelectedItem();
                int hrs = Integer.parseInt(hoursField.getText().trim());
                double pr = Double.parseDouble(priceField.getText().trim());

                String sId = "SHW-" + String.format("%02d", manager.getShows().size() + 1);
                Show s = new Show(sId, m, sc, LocalDateTime.now().plusHours(hrs), pr);
                showService.addShow(s);
                JOptionPane.showMessageDialog(this, "Show scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Booking ID", "Customer", "Movie", "Screen", "Showtime", "Seats", "Total", "Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(bookingsTableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTxnsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Txn ID", "Booking ID", "Payment Method", "Amount", "Status", "Gateway Ref", "Timestamp"};
        txnsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(txnsTableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        statsLabel = new JLabel("Calculating analytics...");
        statsLabel.setFont(UIConstants.FONT_HEADING);
        statsLabel.setVerticalAlignment(SwingConstants.TOP);

        panel.add(statsLabel, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        // Movies
        moviesTableModel.setRowCount(0);
        for (Movie m : manager.getMovies()) {
            moviesTableModel.addRow(new Object[]{
                    m.getId(), m.getTitle(), m.getGenre(), m.getDurationMinutes() + " mins",
                    m.getLanguage(), m.getCertification(), m.getDescription()
            });
        }

        // Shows
        showsTableModel.setRowCount(0);
        for (Show s : manager.getShows()) {
            showsTableModel.addRow(new Object[]{
                    s.getId(), s.getMovie().getTitle(), s.getScreen().getName(),
                    s.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER),
                    String.format("$%.2f", s.getBasePrice()),
                    s.getAvailableSeatsCount() + "/" + s.getTotalSeatsCount()
            });
        }

        // Bookings
        bookingsTableModel.setRowCount(0);
        for (Booking b : manager.getBookings()) {
            bookingsTableModel.addRow(new Object[]{
                    b.getId(), b.getCustomer().getName(), b.getShow().getMovie().getTitle(),
                    b.getShow().getScreen().getName(),
                    b.getShow().getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER),
                    String.join(", ", b.getSeatIds()),
                    String.format("$%.2f", b.getTotalAmount()),
                    b.getBookingStatus().getDisplayName()
            });
        }

        // Transactions
        txnsTableModel.setRowCount(0);
        for (PaymentTransaction t : manager.getTransactions()) {
            txnsTableModel.addRow(new Object[]{
                    t.getId(), t.getBookingId(), t.getPaymentMethodName(),
                    String.format("$%.2f", t.getAmount()),
                    t.getPaymentStatus().getDisplayName(),
                    t.getGatewayReference(),
                    t.getTimestamp().format(UIConstants.DATE_TIME_FORMATTER)
            });
        }

        // Statistics (Recursion demonstration)
        int totalBookedSeats = manager.countTotalBookedSeatsRecursive(manager.getBookings(), 0);
        double revenue = manager.calculateTotalRevenue();

        String statsHtml = String.format(
                "<html><body style='font-family: Segoe UI, sans-serif; font-size: 14px;'>" +
                "<h2 style='color: #1C4072;'>CineBook Real-time Business Intelligence</h2>" +
                "<table cellpadding='8' style='border-collapse: collapse; width: 600px;'>" +
                "<tr style='background-color: #f2f5f9;'><td><b>Total Movies in Catalogue:</b></td><td>%d</td></tr>" +
                "<tr><td><b>Active Scheduled Shows:</b></td><td>%d</td></tr>" +
                "<tr style='background-color: #f2f5f9;'><td><b>Total Customer Bookings:</b></td><td>%d</td></tr>" +
                "<tr><td><b>Total Confirmed Booked Seats (Recursive Calculation):</b></td><td><b style='color: #198754;'>%d seats</b></td></tr>" +
                "<tr style='background-color: #f2f5f9;'><td><b>Total Payment Transactions:</b></td><td>%d</td></tr>" +
                "<tr><td><b>Net Revenue Realized:</b></td><td><b style='color: #198754; font-size: 16px;'>$%.2f</b></td></tr>" +
                "<tr style='background-color: #f2f5f9;'><td><b>Audit Trail Records:</b></td><td>%d events logged</td></tr>" +
                "</table></body></html>",
                manager.getMovies().size(),
                manager.getShows().size(),
                manager.getBookings().size(),
                totalBookedSeats,
                manager.getTransactions().size(),
                revenue,
                manager.getAuditLogs().size()
        );

        statsLabel.setText(statsHtml);
    }
}
