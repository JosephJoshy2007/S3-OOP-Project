import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

// Module 1: MoviePanel (Screen 1)
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
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        initializeUI();
    }

    private void initializeUI() {
        JPanel filterCard = new JPanel(new BorderLayout(12, 12));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        JLabel sectionTitle = new JLabel("Now Showing in Cinemas");
        sectionTitle.setFont(UIConstants.FONT_TITLE);
        sectionTitle.setForeground(UIConstants.COLOR_TEXT_DARK);
        titlePanel.add(sectionTitle);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setToolTipText("Search by movie title or keyword");

        genreFilter = new JComboBox<>(new String[]{"All", "Sci-Fi", "Action", "Comedy", "Thriller", "Drama", "Animation", "Romance"});
        genreFilter.setFont(UIConstants.FONT_REGULAR);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(UIConstants.FONT_BOLD);
        searchBtn.setBackground(UIConstants.COLOR_PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> filterMovies());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(UIConstants.FONT_REGULAR);
        resetBtn.setBackground(Color.WHITE);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            genreFilter.setSelectedIndex(0);
            filterMovies();
        });

        controlsPanel.add(new JLabel("Title:"));
        controlsPanel.add(searchField);
        controlsPanel.add(new JLabel("Genre:"));
        controlsPanel.add(genreFilter);
        controlsPanel.add(searchBtn);
        controlsPanel.add(resetBtn);

        filterCard.add(titlePanel, BorderLayout.WEST);
        filterCard.add(controlsPanel, BorderLayout.EAST);
        add(filterCard, BorderLayout.NORTH);

        moviesGridPanel = new JPanel();
        moviesGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
        moviesGridPanel.setBackground(UIConstants.COLOR_BG_LIGHT);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.COLOR_BG_LIGHT);
        wrapper.add(moviesGridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        resultsCountLabel = new JLabel("Loading movies...");
        resultsCountLabel.setFont(UIConstants.FONT_SMALL);
        resultsCountLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        add(resultsCountLabel, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() { filterMovies(); }

    private void filterMovies() {
        String query = searchField.getText().trim();
        String genre = (String) genreFilter.getSelectedItem();

        List<Movie> matched = movieService.search(query, genre);
        moviesGridPanel.removeAll();

        if (matched.isEmpty()) {
            moviesGridPanel.setLayout(new BorderLayout());
            JLabel noData = new JLabel("No movies found matching your criteria.", SwingConstants.CENTER);
            noData.setFont(UIConstants.FONT_HEADING);
            noData.setForeground(Color.GRAY);
            noData.setBorder(new EmptyBorder(50, 0, 50, 0));
            moviesGridPanel.add(noData, BorderLayout.CENTER);
        } else {
            moviesGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
            for (Movie m : matched) {
                moviesGridPanel.add(createMovieCard(m));
            }
        }

        resultsCountLabel.setText("Showing " + matched.size() + " movie(s)");
        moviesGridPanel.revalidate();
        moviesGridPanel.repaint();
    }

    private JPanel createMovieCard(Movie movie) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(UIConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel title = new JLabel(movie.getTitle());
        title.setFont(UIConstants.FONT_HEADING);
        title.setForeground(UIConstants.COLOR_TEXT_DARK);

        JLabel meta = new JLabel(movie.getGenre() + " - " + movie.getDurationMinutes() + " mins - " + movie.getCertification());
        meta.setFont(UIConstants.FONT_SMALL);
        meta.setForeground(new Color(100, 110, 120));

        JLabel lang = new JLabel("Language: " + movie.getLanguage());
        lang.setFont(UIConstants.FONT_SMALL);
        lang.setForeground(Color.GRAY);

        JTextArea desc = new JTextArea(movie.getDescription());
        desc.setFont(UIConstants.FONT_REGULAR);
        desc.setForeground(new Color(70, 70, 70));
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setRows(3);
        desc.setBorder(new EmptyBorder(6, 0, 6, 0));

        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(meta);
        infoPanel.add(lang);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(desc);

        card.add(infoPanel, BorderLayout.CENTER);

        JButton bookBtn = new JButton("View Details & Shows");
        bookBtn.setFont(UIConstants.FONT_BOLD);
        bookBtn.setBackground(UIConstants.COLOR_PRIMARY);
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFocusPainted(false);
        bookBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bookBtn.addActionListener(e -> parent.navigateToMovieDetails(movie));

        card.add(bookBtn, BorderLayout.SOUTH);
        return card;
    }
}
