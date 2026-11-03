import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

// Module 1: MoviePanel (Screen 1 - Modern Catalogue Browsing)
public class MoviePanel extends JPanel {
    private final MainFrame parent;
    private final MovieService movieService;

    private JTextField searchField;
    private JComboBox<String> genreFilter;
    private JPanel moviesGridPanel;
    private JLabel resultsCountLabel;

    public MoviePanel(MainFrame parent, MovieService movieService) {
        this.parent = parent;
        this.movieService = movieService;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        initializeUI();
    }

    private void initializeUI() {
        // Modern Filter Card
        JPanel filterCard = new JPanel(new BorderLayout(16, 12));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel sectionTitle = new JLabel("Now Showing in Theaters");
        sectionTitle.setFont(UIConstants.FONT_TITLE);
        sectionTitle.setForeground(UIConstants.COLOR_TEXT_DARK);

        JLabel sectionSubtitle = new JLabel("Browse active movies, select showtimes, and book seats in real time");
        sectionSubtitle.setFont(UIConstants.FONT_SMALL);
        sectionSubtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        titlePanel.add(sectionTitle);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(sectionSubtitle);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UIConstants.FONT_BOLD);
        searchLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        searchField = new JTextField(15);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setToolTipText("Filter by title or keywords");

        JLabel genreLabel = new JLabel("Genre:");
        genreLabel.setFont(UIConstants.FONT_BOLD);
        genreLabel.setForeground(UIConstants.COLOR_TEXT_DARK);

        genreFilter = new JComboBox<>(new String[]{"All", "Sci-Fi", "Action", "Comedy", "Thriller", "Drama", "Animation", "Romance"});
        genreFilter.setFont(UIConstants.FONT_REGULAR);

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(UIConstants.FONT_BOLD);
        searchBtn.setBackground(UIConstants.COLOR_ACCENT);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.addActionListener(e -> filterMovies());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(UIConstants.FONT_REGULAR);
        resetBtn.setBackground(new Color(241, 245, 249));
        resetBtn.setForeground(UIConstants.COLOR_TEXT_DARK);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            genreFilter.setSelectedIndex(0);
            filterMovies();
        });

        // Trigger filter on Enter in search field
        searchField.addActionListener(e -> filterMovies());

        controlsPanel.add(searchLabel);
        controlsPanel.add(searchField);
        controlsPanel.add(genreLabel);
        controlsPanel.add(genreFilter);
        controlsPanel.add(searchBtn);
        controlsPanel.add(resetBtn);

        filterCard.add(titlePanel, BorderLayout.WEST);
        filterCard.add(controlsPanel, BorderLayout.EAST);
        add(filterCard, BorderLayout.NORTH);

        // Responsive 3-Column Movie Cards Grid
        moviesGridPanel = new JPanel();
        moviesGridPanel.setLayout(new GridLayout(0, 3, 18, 18));
        moviesGridPanel.setBackground(UIConstants.COLOR_BG_LIGHT);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.COLOR_BG_LIGHT);
        wrapper.add(moviesGridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        resultsCountLabel = new JLabel("Loading movies...");
        resultsCountLabel.setFont(UIConstants.FONT_SMALL);
        resultsCountLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);
        resultsCountLabel.setBorder(new EmptyBorder(6, 4, 0, 0));
        add(resultsCountLabel, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        filterMovies();
    }

    private void filterMovies() {
        String query = searchField.getText().trim();
        String genre = (String) genreFilter.getSelectedItem();

        List<Movie> matched = movieService.search(query, genre);
        moviesGridPanel.removeAll();

        if (matched.isEmpty()) {
            moviesGridPanel.setLayout(new BorderLayout());
            JLabel noData = new JLabel("No movies found matching your search criteria.", SwingConstants.CENTER);
            noData.setFont(UIConstants.FONT_HEADING);
            noData.setForeground(UIConstants.COLOR_TEXT_MUTED);
            noData.setBorder(new EmptyBorder(80, 0, 80, 0));
            moviesGridPanel.add(noData, BorderLayout.CENTER);
        } else {
            moviesGridPanel.setLayout(new GridLayout(0, 3, 18, 18));
            for (Movie m : matched) {
                moviesGridPanel.add(createMovieCard(m));
            }
        }

        resultsCountLabel.setText("Showing " + matched.size() + " featured title(s)");
        moviesGridPanel.revalidate();
        moviesGridPanel.repaint();
    }

    private JPanel createMovieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Poster image thumbnail on the left
        ImageIcon posterIcon = UIConstants.getMoviePoster(movie.getId(), 110, 160);
        JLabel posterLabel = new JLabel(posterIcon);
        posterLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        card.add(posterLabel, BorderLayout.WEST);

        // Content details on the right
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><b>" + movie.getTitle() + "</b></html>");
        titleLabel.setFont(UIConstants.FONT_HEADING);
        titleLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Chips row for Genre & Cert
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel genreBadge = UIConstants.createBadge(movie.getGenre(), new Color(238, 242, 255), UIConstants.COLOR_ACCENT);
        JLabel certBadge = UIConstants.createBadge(movie.getCertification(), new Color(254, 243, 199), new Color(180, 83, 9));
        badgesPanel.add(genreBadge);
        badgesPanel.add(certBadge);

        // Duration & Language
        JLabel metaLabel = new JLabel("⏱ " + movie.getDurationMinutes() + " mins  •  " + movie.getLanguage());
        metaLabel.setFont(UIConstants.FONT_SMALL);
        metaLabel.setForeground(UIConstants.COLOR_TEXT_MUTED);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Short synopsis preview
        String desc = movie.getDescription();
        if (desc.length() > 85) desc = desc.substring(0, 82) + "...";
        JLabel descLabel = new JLabel("<html><body style='width: 170px; color: #64748B; font-size: 10px;'>" + desc + "</body></html>");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(6));
        detailsPanel.add(badgesPanel);
        detailsPanel.add(Box.createVerticalStrut(6));
        detailsPanel.add(metaLabel);
        detailsPanel.add(Box.createVerticalStrut(6));
        detailsPanel.add(descLabel);

        card.add(detailsPanel, BorderLayout.CENTER);

        // Action Button: View Details & Shows
        JButton actionBtn = new JButton("Book Tickets  →");
        actionBtn.setFont(UIConstants.FONT_BOLD);
        actionBtn.setBackground(UIConstants.COLOR_ACCENT);
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setFocusPainted(false);
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(67, 56, 202), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        actionBtn.addActionListener(e -> parent.navigateToMovieDetails(movie));

        card.add(actionBtn, BorderLayout.SOUTH);
        return card;
    }
}
