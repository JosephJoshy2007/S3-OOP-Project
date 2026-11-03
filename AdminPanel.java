import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

// Module 6: AdminPanel (Screen 7 - Modern Administrator Console)
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

        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Modern Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("CineBook Cinema Management & Operations");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.COLOR_TEXT_DARK);

        JLabel subtitle = new JLabel("Manage theatrical catalogue, auditorium scheduling, bookings ledger, and business metrics");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        JButton refreshBtn = new JButton("Sync / Refresh Database");
        refreshBtn.setFont(UIConstants.FONT_BOLD);
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        refreshBtn.addActionListener(e -> {
            manager.loadFromDatabase();
            refreshData();
        });

        topBar.add(titleBlock, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIConstants.FONT_BOLD);

        tabbedPane.addTab("Movie Catalogue", createMoviesTab());
        tabbedPane.addTab("Showtime Schedules", createShowsTab());
        tabbedPane.addTab("All Bookings", createBookingsTab());
        tabbedPane.addTab("Financial Ledger", createTxnsTab());
        tabbedPane.addTab("Business Analytics", createStatsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMoviesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"ID", "Title", "Genre", "Duration (mins)", "Language", "Cert", "Description"};
        moviesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = createStyledTable(moviesTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton addBtn = new JButton("+ Add Movie");
        addBtn.setFont(UIConstants.FONT_BOLD);
        addBtn.setBackground(UIConstants.COLOR_ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        addBtn.addActionListener(e -> promptAddMovie());

        JButton delBtn = new JButton("Delete Selected Movie");
        delBtn.setFont(UIConstants.FONT_BOLD);
        delBtn.setBackground(new Color(254, 242, 242));
        delBtn.setForeground(new Color(220, 38, 38));
        delBtn.setFocusPainted(false);
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a movie to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String mId = (String) moviesTableModel.getValueAt(row, 0);
            try {
                movieService.removeMovie(mId);
                JOptionPane.showMessageDialog(this, "Movie removed successfully from SQLite.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete movie: " + ex.getMessage(), "Referential Guard Triggered", JOptionPane.WARNING_MESSAGE);
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
        JTextArea descArea = new JTextArea(3, 20);

        JPanel form = new JPanel(new GridLayout(6, 2, 6, 8));
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Genre:"));
        form.add(genreField);
        form.add(new JLabel("Duration (mins):"));
        form.add(durField);
        form.add(new JLabel("Language:"));
        form.add(langField);
        form.add(new JLabel("Certification:"));
        form.add(certField);
        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Movie Title", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = "MOV-" + String.format("%02d", manager.getMovies().size() + 1);
                int dur = Integer.parseInt(durField.getText().trim());
                Movie m = new Movie(id, titleField.getText().trim(), genreField.getText().trim(),
                        dur, langField.getText().trim(), certField.getText().trim(), descArea.getText().trim());
                movieService.addMovie(m);
                JOptionPane.showMessageDialog(this, "Movie successfully added and saved to database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding movie: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createShowsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Show ID", "Movie", "Auditorium Screen", "Screening Time", "Base Price", "Seat Occupancy"};
        showsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = createStyledTable(showsTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton addBtn = new JButton("+ Schedule Show");
        addBtn.setFont(UIConstants.FONT_BOLD);
        addBtn.setBackground(UIConstants.COLOR_ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        addBtn.addActionListener(e -> promptAddShow());

        JButton delBtn = new JButton("Remove Selected Show");
        delBtn.setFont(UIConstants.FONT_BOLD);
        delBtn.setBackground(new Color(254, 242, 242));
        delBtn.setForeground(new Color(220, 38, 38));
        delBtn.setFocusPainted(false);
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a show first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String sId = (String) showsTableModel.getValueAt(row, 0);
            try {
                showService.removeShow(sId);
                JOptionPane.showMessageDialog(this, "Show deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete show: " + ex.getMessage(), "Active Bookings Exist", JOptionPane.WARNING_MESSAGE);
            }
        });

        actions.add(addBtn);
        actions.add(delBtn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void promptAddShow() {
        if (manager.getMovies().isEmpty() || manager.getScreens().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ensure movies and screens exist before scheduling a show.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Movie> movieCombo = new JComboBox<>(manager.getMovies().toArray(new Movie[0]));
        JComboBox<Screen> screenCombo = new JComboBox<>(manager.getScreens().toArray(new Screen[0]));
        JTextField hoursOffsetField = new JTextField("4");
        JTextField priceField = new JTextField("16.00");

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 8));
        form.add(new JLabel("Select Movie:"));
        form.add(movieCombo);
        form.add(new JLabel("Select Auditorium:"));
        form.add(screenCombo);
        form.add(new JLabel("Hours from now:"));
        form.add(hoursOffsetField);
        form.add(new JLabel("Base Price ($):"));
        form.add(priceField);

        int result = JOptionPane.showConfirmDialog(this, form, "Schedule New Showtime", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Movie m = (Movie) movieCombo.getSelectedItem();
                Screen sc = (Screen) screenCombo.getSelectedItem();
                int hours = Integer.parseInt(hoursOffsetField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                String sId = "SHW-" + String.format("%02d", manager.getShows().size() + 1);
                Show newShow = new Show(sId, m, sc, LocalDateTime.now().plusHours(hours), price);
                showService.addShow(newShow);
                JOptionPane.showMessageDialog(this, "Show scheduled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error scheduling show: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Booking ID", "Customer Name", "Movie", "Auditorium", "Showtime", "Seats", "Total Paid", "Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = createStyledTable(bookingsTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTxnsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Transaction ID", "Booking Ref", "Payment Channel", "Amount Paid", "Status", "Gateway Ref", "Timestamp"};
        txnsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = createStyledTable(txnsTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        statsLabel = new JLabel();
        statsLabel.setFont(UIConstants.FONT_REGULAR);
        panel.add(new JScrollPane(statsLabel), BorderLayout.CENTER);
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(UIConstants.COLOR_TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        return table;
    }

    public void refreshData() {
        // Movies
        moviesTableModel.setRowCount(0);
        for (Movie m : manager.getMovies()) {
            moviesTableModel.addRow(new Object[]{
                    m.getId(), m.getTitle(), m.getGenre(), m.getDurationMinutes(),
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
                    s.getAvailableSeatsCount() + " / " + s.getTotalSeatsCount()
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

        // Business Intelligence Metrics
        int totalBookedSeats = manager.countTotalBookedSeatsRecursive(manager.getBookings(), 0);
        double revenue = manager.calculateTotalRevenue();

        String statsHtml = String.format(
                "<html><body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #0F172A;'>" +
                "<h2 style='color: #0F172A; margin-bottom: 4px;'>CineBook Real-time Business Intelligence</h2>" +
                "<p style='color: #64748B; margin-top: 0px;'>Direct SQLite JDBC Telemetry and Algorithmic Analytics</p>" +
                "<br/>" +
                "<table cellpadding='10' style='border-collapse: collapse; width: 680px;'>" +
                "<tr style='background-color: #F8FAFC;'><td><b>Theatrical Movies in Catalogue:</b></td><td><b style='color: #4F46E5;'>%d titles</b></td></tr>" +
                "<tr><td><b>Active Screening Schedules:</b></td><td><b>%d sessions</b></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td><b>Total Customer Booking Records:</b></td><td><b>%d bookings</b></td></tr>" +
                "<tr><td><b>Total Confirmed Booked Seats (Recursive):</b></td><td><b style='color: #10B981; font-size: 15px;'>%d seats</b></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td><b>Total Payment Transactions Logged:</b></td><td><b>%d records</b></td></tr>" +
                "<tr><td><b>Net Revenue Realized:</b></td><td><b style='color: #10B981; font-size: 17px;'>$%.2f</b></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td><b>Audit Trail Security Events:</b></td><td>%d events recorded</td></tr>" +
                "</table>" +
                "</body></html>",
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
