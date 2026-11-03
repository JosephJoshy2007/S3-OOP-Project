import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public final class UIConstants {
    private UIConstants() {} // Utility class

    // DateTime Formatters
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // Modern Design Colors (Slate & Indigo Palette)
    public static final Color COLOR_PRIMARY = new Color(15, 23, 42);           // Slate 900
    public static final Color COLOR_PRIMARY_DARK = new Color(2, 6, 23);         // Slate 950
    public static final Color COLOR_ACCENT = new Color(79, 70, 229);            // Indigo 600
    public static final Color COLOR_ACCENT_HOVER = new Color(67, 56, 202);      // Indigo 700
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);          // Emerald 500
    public static final Color COLOR_WARNING = new Color(245, 158, 11);          // Amber 500
    public static final Color COLOR_DANGER = new Color(239, 68, 68);            // Rose 500
    public static final Color COLOR_BG_LIGHT = new Color(248, 250, 252);        // Slate 50
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_DARK = new Color(15, 23, 42);          // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);      // Slate 500
    public static final Color COLOR_BORDER = new Color(226, 232, 240);          // Slate 200

    // Seating Grid Colors
    public static final Color COLOR_SEAT_AVAILABLE = new Color(220, 252, 231);  // Emerald 100
    public static final Color COLOR_SEAT_AVAILABLE_BORDER = new Color(134, 239, 172); // Emerald 300
    public static final Color COLOR_SEAT_SELECTED = new Color(16, 185, 129);    // Emerald 500
    public static final Color COLOR_SEAT_RESERVED = new Color(254, 240, 138);   // Yellow 200
    public static final Color COLOR_SEAT_BOOKED = new Color(203, 213, 225);     // Slate 300

    // Typography
    public static final Font FONT_HEADER_LOGO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    // Image Cache
    private static final Map<String, ImageIcon> posterCache = new HashMap<>();

    /**
     * Loads, smoothly scales, and caches a movie poster image.
     * Falls back to a clean procedural modern poster card if file not found.
     */
    public static ImageIcon getMoviePoster(String movieId, int width, int height) {
        String cacheKey = movieId + "_" + width + "x" + height;
        if (posterCache.containsKey(cacheKey)) {
            return posterCache.get(cacheKey);
        }

        File imgFile = new File("images" + File.separator + movieId + ".jpg");
        if (imgFile.exists()) {
            try {
                BufferedImage originalImage = ImageIO.read(imgFile);
                if (originalImage != null) {
                    Image scaled = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaled);
                    posterCache.put(cacheKey, icon);
                    return icon;
                }
            } catch (Exception e) {
                System.err.println("[POSTER ERROR] Could not load " + imgFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        // Elegant fallback placeholder
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = placeholder.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), width, height, new Color(15, 23, 42));
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // Border
        g2.setColor(COLOR_ACCENT);
        g2.drawRect(0, 0, width - 1, height - 1);

        // Cinema Icon / Monogram
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, width / 6)));
        FontMetrics fm = g2.getFontMetrics();
        String label = (movieId != null ? movieId : "FILM");
        int textX = (width - fm.stringWidth(label)) / 2;
        int textY = (height / 2) + (fm.getAscent() / 4);
        g2.drawString(label, textX, textY);

        g2.dispose();
        ImageIcon fallbackIcon = new ImageIcon(placeholder);
        posterCache.put(cacheKey, fallbackIcon);
        return fallbackIcon;
    }

    /**
     * Helper to create modern badge/pill panel
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(FONT_BADGE);
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60), 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }
}
