import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

// Module 2: MovieDetailsPanel (Screen 2 - Modern Movie Details & Showtime Selection)
public class MovieDetailsPanel extends JPanel {
    private final MainFrame parent;
    private final ShowService showService;

    private Movie currentMovie;
    private JLabel posterLabel;
    private JLabel titleLabel;
    private JPanel badgesPanel;
    private JLabel durationLabel;
    private JTextArea synopsisArea;
    private JPanel showsContainer;

    public MovieDetailsPanel(MainFrame parent, ShowService showService) {
        this.parent = parent;
        this.showService = showService;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Navigation Top Bar
        JPanel topNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topNav.setOpaque(false);

        JButton backBtn = new JButton("← Back to Movie Catalogue");
        backBtn.setFont(UIConstants.FONT_BOLD);
        backBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        backBtn.addActionListener(e -> parent.showMoviesScreen());
        topNav.add(backBtn);
        add(topNav, BorderLayout.NORTH);

        // Center Content: Hero Details Card + Showtimes Card
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // 1. Movie Hero Card (Split: Poster on Left, Details on Right)
        JPanel heroCard = new JPanel(new BorderLayout(24, 0));
        heroCard.setBackground(Color.WHITE);
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Poster on left
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(200, 290));
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        heroCard.add(posterLabel, BorderLayout.WEST);

        // Details on right
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        titleLabel = new JLabel("Movie Title");
        titleLabel.setFont(UIConstants.FONT_HERO);
        titleLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        durationLabel = new JLabel();
        durationLabel.setFont(UIConstants.FONT_REGULAR);
        durationLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);
        durationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel synopsisHeading = new JLabel("SYNOPSIS");
        synopsisHeading.setFont(UIConstants.FONT_SMALL);
        synopsisHeading.setForeground(UIConstants.COLOR_ACCENT);
        synopsisHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        synopsisArea = new JTextArea();
        synopsisArea.setFont(UIConstants.FONT_REGULAR);
        synopsisArea.setForeground(new Color(51, 65, 85));
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setEditable(false);
        synopsisArea.setOpaque(false);
        synopsisArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(badgesPanel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(durationLabel);
        detailsPanel.add(Box.createVerticalStrut(14));
        detailsPanel.add(new JSeparator());
        detailsPanel.add(Box.createVerticalStrut(12));
        detailsPanel.add(synopsisHeading);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(synopsisArea);

        heroCard.add(detailsPanel, BorderLayout.CENTER);
        mainContent.add(heroCard);
        mainContent.add(Box.createVerticalStrut(18));

        // 2. Showtimes Card
        JPanel showtimesCard = new JPanel(new BorderLayout(0, 14));
        showtimesCard.setBackground(Color.WHITE);
        showtimesCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JPanel showHeader = new JPanel();
        showHeader.setLayout(new BoxLayout(showHeader, BoxLayout.Y_AXIS));
        showHeader.setOpaque(false);

        JLabel showTitle = new JLabel("Available Showtimes & Screens");
        showTitle.setFont(UIConstants.FONT_TITLE);
        showTitle.setForeground(UIConstants.COLOR_TEXT_DARK);

        JLabel showSubtitle = new JLabel("Select your preferred screening session to choose seats in the auditorium");
        showSubtitle.setFont(UIConstants.FONT_SMALL);
        showSubtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        showHeader.add(showTitle);
        showHeader.add(Box.createVerticalStrut(2));
        showHeader.add(showSubtitle);
        showtimesCard.add(showHeader, BorderLayout.NORTH);

        showsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        showsContainer.setOpaque(false);
        showtimesCard.add(showsContainer, BorderLayout.CENTER);

        mainContent.add(showtimesCard);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setMovie(Movie movie) {
        this.currentMovie = movie;

        // Load 200x290 poster
        posterLabel.setIcon(UIConstants.getMoviePoster(movie.getId(), 200, 290));

        titleLabel.setText(movie.getTitle());

        badgesPanel.removeAll();
        badgesPanel.add(UIConstants.createBadge(movie.getGenre(), new Color(238, 242, 255), UIConstants.COLOR_ACCENT));
        badgesPanel.add(UIConstants.createBadge(movie.getCertification(), new Color(254, 243, 199), new Color(180, 83, 9)));
        badgesPanel.add(UIConstants.createBadge(movie.getLanguage(), new Color(241, 245, 249), UIConstants.COLOR_TEXT_DARK));

        durationLabel.setText("⏱ Runtime: " + movie.getDurationMinutes() + " minutes (" + (movie.getDurationMinutes() / 60) + "h " + (movie.getDurationMinutes() % 60) + "m)");
        synopsisArea.setText(movie.getDescription());

        showsContainer.removeAll();
        List<Show> movieShows = showService.getShowsForMovie(movie.getId());

        if (movieShows.isEmpty()) {
            JLabel noShows = new JLabel("No scheduled screenings currently available for this title.");
            noShows.setFont(UIConstants.FONT_REGULAR);
            noShows.setForeground(UIConstants.COLOR_TEXT_MUTED);
            showsContainer.add(noShows);
        } else {
            for (Show show : movieShows) {
                showsContainer.add(createShowtimeCard(show));
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createShowtimeCard(Show show) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(280, 140));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel screenLabel = new JLabel(show.getScreen().getName());
        screenLabel.setFont(UIConstants.FONT_BOLD);
        screenLabel.setForeground(UIConstants.COLOR_PRIMARY);

        JLabel timeLabel = new JLabel("📅 " + show.getShowDateTime().format(UIConstants.DATE_TIME_FORMATTER));
        timeLabel.setFont(UIConstants.FONT_REGULAR);
        timeLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        JLabel priceLabel = new JLabel(String.format("Base Ticket: $%.2f", show.getBasePrice()));
        priceLabel.setFont(UIConstants.FONT_SMALL);
        priceLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);

        int avail = show.getAvailableSeatsCount();
        int total = show.getTotalSeatsCount();
        Color availColor = (avail > 5) ? UIConstants.COLOR_SUCCESS : UIConstants.COLOR_WARNING;
        JLabel seatLabel = new JLabel("● " + avail + " / " + total + " seats available");
        seatLabel.setFont(UIConstants.FONT_SMALL);
        seatLabel.setForeground(availColor);

        top.add(screenLabel);
        top.add(Box.createVerticalStrut(3));
        top.add(timeLabel);
        top.add(Box.createVerticalStrut(3));
        top.add(priceLabel);
        top.add(Box.createVerticalStrut(3));
        top.add(seatLabel);

        card.add(top, BorderLayout.CENTER);

        JButton selectBtn = new JButton("Select Seats →");
        selectBtn.setFont(UIConstants.FONT_BOLD);
        selectBtn.setBackground(UIConstants.COLOR_ACCENT);
        selectBtn.setForeground(Color.WHITE);
        selectBtn.setFocusPainted(false);
        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_ACCENT_HOVER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        selectBtn.addActionListener(e -> parent.navigateToSeatSelection(show));
        card.add(selectBtn, BorderLayout.SOUTH);

        return card;
    }
}
