import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

// Module 2: MovieDetailsPanel (Showtimes Selection for Movie)
public class MovieDetailsPanel extends JPanel {
    private final MainFrame parent;
    private final ShowService showService;

    private Movie currentMovie;
    private JLabel titleLabel;
    private JLabel metaLabel;
    private JTextArea synopsisArea;
    private JPanel showsContainer;

    public MovieDetailsPanel(MainFrame parent, ShowService showService) {
        this.parent = parent;
        this.showService = showService;
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        initializeUI();
    }

    private void initializeUI() {
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setOpaque(false);
        JButton backBtn = new JButton("<< Back to Movie Catalogue");
        backBtn.setFocusPainted(false);
        backBtn.setFont(UIConstants.FONT_SMALL);
        backBtn.setBackground(UIConstants.COLOR_BG_LIGHT);
        backBtn.addActionListener(e -> parent.showMoviesScreen());
        topNav.add(backBtn);
        add(topNav, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel detailCard = new JPanel();
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        titleLabel = new JLabel("Movie Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);

        metaLabel = new JLabel("Genre - Duration - Rating");
        metaLabel.setFont(UIConstants.FONT_REGULAR);
        metaLabel.setForeground(Color.GRAY);

        synopsisArea = new JTextArea();
        synopsisArea.setFont(UIConstants.FONT_REGULAR);
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setEditable(false);
        synopsisArea.setOpaque(false);
        synopsisArea.setBorder(new EmptyBorder(10, 0, 10, 0));

        detailCard.add(titleLabel);
        detailCard.add(Box.createVerticalStrut(6));
        detailCard.add(metaLabel);
        detailCard.add(Box.createVerticalStrut(10));
        detailCard.add(new JSeparator());
        detailCard.add(Box.createVerticalStrut(10));
        detailCard.add(synopsisArea);

        contentPanel.add(detailCard, BorderLayout.NORTH);

        JPanel showtimesCard = new JPanel(new BorderLayout(10, 10));
        showtimesCard.setBackground(Color.WHITE);
        showtimesCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(18, 24, 18, 24)
        ));

        JLabel showTitle = new JLabel("Available Showtimes for this Movie");
        showTitle.setFont(UIConstants.FONT_HEADING);
        showTitle.setForeground(UIConstants.COLOR_TEXT_DARK);
        showtimesCard.add(showTitle, BorderLayout.NORTH);

        showsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        showsContainer.setOpaque(false);

        JScrollPane showScroll = new JScrollPane(showsContainer);
        showScroll.setBorder(null);
        showtimesCard.add(showScroll, BorderLayout.CENTER);

        contentPanel.add(showtimesCard, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setMovie(Movie movie) {
        this.currentMovie = movie;
        titleLabel.setText(movie.getTitle());
        metaLabel.setText(movie.getGenre() + " - " + movie.getDurationMinutes() + " mins - " +
                movie.getCertification() + " - " + movie.getLanguage());
        synopsisArea.setText("Synopsis:\n" + movie.getDescription());

        showsContainer.removeAll();
        List<Show> movieShows = showService.getShowsForMovie(movie.getId());

        if (movieShows.isEmpty()) {
            JLabel noShows = new JLabel("No scheduled showtimes available for this title.");
            noShows.setFont(UIConstants.FONT_REGULAR);
            noShows.setForeground(Color.GRAY);
            showsContainer.add(noShows);
        } else {
            for (Show show : movieShows) {
                showsContainer.add(createShowtimeButton(show));
            }
        }

        showsContainer.revalidate();
        showsContainer.repaint();
    }

    private JPanel createShowtimeButton(Show show) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setPreferredSize(new Dimension(240, 110));
        card.setBackground(UIConstants.COLOR_BG_LIGHT);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_PRIMARY, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel timeLabel = new JLabel(show.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));
        timeLabel.setFont(UIConstants.FONT_BOLD);
        timeLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel priceLabel = new JLabel(String.format("Base Ticket: $%.2f", show.getBasePrice()));
        priceLabel.setFont(UIConstants.FONT_REGULAR);

        JLabel seatLabel = new JLabel(show.getAvailableSeatsCount() + " of " + show.getTotalSeatsCount() + " seats available");
        seatLabel.setFont(UIConstants.FONT_SMALL);
        seatLabel.setForeground(new Color(25, 135, 84));

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.add(timeLabel);
        textPanel.add(priceLabel);
        textPanel.add(seatLabel);
        card.add(textPanel, BorderLayout.CENTER);

        JButton selectBtn = new JButton("Select Seats");
        selectBtn.setFont(UIConstants.FONT_SMALL);
        selectBtn.setBackground(UIConstants.COLOR_PRIMARY);
        selectBtn.setForeground(Color.WHITE);
        selectBtn.setFocusPainted(false);
        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectBtn.addActionListener(e -> parent.navigateToSeatSelection(show));
        card.add(selectBtn, BorderLayout.SOUTH);

        return card;
    }
}
