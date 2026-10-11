// Module 1: Movie Model
public class Movie {
    private final String id;
    private String title;
    private String genre;
    private int durationMinutes;
    private String language;
    private String certification;
    private String description;

    public Movie(String id, String title, String genre, int durationMinutes,
                 String language, String certification, String description) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.certification = certification;
        this.description = description;
    }

    public Movie(String id, String title, String genre, int durationMinutes) {
        this(id, title, genre, durationMinutes, "English", "PG-13", "Feature film presentation.");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean matches(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String lower = keyword.trim().toLowerCase();
        return title.toLowerCase().contains(lower) || genre.toLowerCase().contains(lower);
    }

    public boolean matches(String titleQuery, String genreFilter) {
        boolean matchesTitle = (titleQuery == null || titleQuery.trim().isEmpty())
                || title.toLowerCase().contains(titleQuery.trim().toLowerCase());
        boolean matchesGenre = (genreFilter == null || genreFilter.equalsIgnoreCase("All") || genreFilter.trim().isEmpty())
                || genre.equalsIgnoreCase(genreFilter.trim());
        return matchesTitle && matchesGenre;
    }

    @Override
    public String toString() {
        return title + " (" + certification + ", " + durationMinutes + " mins)";
    }
}
