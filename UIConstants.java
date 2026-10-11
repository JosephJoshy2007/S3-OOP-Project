import java.awt.Color;
import java.awt.Font;
import java.time.format.DateTimeFormatter;

public final class UIConstants {
    private UIConstants() {} // Utility class

    // DateTime Formatters
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // Colors
    public static final Color COLOR_PRIMARY = new Color(28, 64, 114);
    public static final Color COLOR_PRIMARY_DARK = new Color(18, 42, 75);
    public static final Color COLOR_BG_LIGHT = new Color(248, 249, 250);
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    public static final Color COLOR_BORDER = new Color(222, 226, 230);
    public static final Color COLOR_SEAT_AVAILABLE = new Color(230, 244, 234);
    public static final Color COLOR_SEAT_SELECTED = new Color(25, 135, 84);
    public static final Color COLOR_SEAT_RESERVED = new Color(255, 193, 7);
    public static final Color COLOR_SEAT_BOOKED = new Color(108, 117, 125);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
}
