import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Set clean styling
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['DejaVu Sans', 'Arial']

def create_er_diagram():
    fig, ax = plt.subplots(figsize=(10, 7), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Draw entity box helper
    def draw_entity(x, y, w, h, title, fields):
        # Header box
        header = patches.Rectangle((x, y + h - 4), w, 4, facecolor='#E2E8F0', edgecolor='#0F172A', linewidth=1.5)
        ax.add_patch(header)
        ax.text(x + w / 2, y + h - 2, title, ha='center', va='center', fontsize=9, fontweight='bold', color='#0F172A')
        # Body box
        body = patches.Rectangle((x, y), w, h - 4, facecolor='#FFFFFF', edgecolor='#0F172A', linewidth=1.5)
        ax.add_patch(body)
        # Fields text
        text_y = y + h - 6.5
        for field in fields:
            ax.text(x + 1, text_y, field, ha='left', va='center', fontsize=7.5, color='#1E293B')
            text_y -= 2.8

    # Draw relation line helper
    def draw_rel(x1, y1, x2, y2, label1, label2, rel_label=""):
        ax.plot([x1, x2], [y1, y2], color='#0F172A', linewidth=1.2)
        if label1:
            ax.text(x1 + (2 if x2 > x1 else -2), y1 + 1, label1, fontsize=8, fontweight='bold')
        if label2:
            ax.text(x2 + (-2 if x2 > x1 else 2), y2 + 1, label2, fontsize=8, fontweight='bold')
        if rel_label:
            mid_x = (x1 + x2) / 2
            mid_y = (x1 + x2) / 2
            ax.text((x1 + x2) / 2, (y1 + y2) / 2 + 1.5, rel_label, fontsize=7.5, fontstyle='italic', ha='center')

    # Title
    ax.text(50, 97, "FIGURE 4.1: ENTITY RELATIONSHIP DIAGRAM", ha='center', va='center', fontsize=12, fontweight='bold')

    # Tables
    # 1. USERS
    draw_entity(5, 68, 26, 24, "USERS", [
        "PK  id  TEXT",
        "    username  TEXT (UNIQUE)",
        "    password  TEXT",
        "    name  TEXT",
        "    email  TEXT",
        "    role  TEXT",
        "    loyalty_points  INTEGER",
        "    access_level  TEXT"
    ])

    # 2. MOVIES
    draw_entity(38, 68, 25, 24, "MOVIES", [
        "PK  id  TEXT",
        "    title  TEXT",
        "    genre  TEXT",
        "    duration  INTEGER",
        "    language  TEXT",
        "    certification  TEXT",
        "    description  TEXT"
    ])

    # 3. SCREENS
    draw_entity(70, 68, 25, 24, "SCREENS", [
        "PK  id  TEXT",
        "    name  TEXT",
        "    seat_rows  INTEGER",
        "    seat_cols  INTEGER"
    ])

    # 4. SHOWS
    draw_entity(54, 34, 28, 24, "SHOWS", [
        "PK  id  TEXT",
        "FK  movie_id  TEXT",
        "FK  screen_id  TEXT",
        "    show_time  TEXT",
        "    base_price  REAL"
    ])

    # 5. BOOKINGS
    draw_entity(16, 30, 29, 28, "BOOKINGS", [
        "PK  id  TEXT",
        "FK  customer_id  TEXT",
        "FK  show_id  TEXT",
        "    seat_ids  TEXT (CSV)",
        "    total_amount  REAL",
        "    created_at  TEXT",
        "    expires_at  TEXT",
        "    status  TEXT",
        "    cancellation_reason  TEXT"
    ])

    # 6. TRANSACTIONS
    draw_entity(16, 2, 29, 22, "TRANSACTIONS", [
        "PK  id  TEXT",
        "FK  booking_id  TEXT",
        "    payment_method  TEXT",
        "    amount  REAL",
        "    timestamp  TEXT",
        "    status  TEXT",
        "    reference  TEXT"
    ])

    # 7. AUDIT_LOGS
    draw_entity(58, 4, 30, 18, "AUDIT_LOGS", [
        "PK  id  INTEGER (AUTO)",
        "    timestamp  TEXT",
        "    action  TEXT"
    ])

    # Relationships
    # USERS -> BOOKINGS (1 : N)
    draw_rel(18, 68, 24, 58, "1", "N", "books")
    # MOVIES -> SHOWS (1 : N)
    draw_rel(50, 68, 60, 58, "1", "N", "screens")
    # SCREENS -> SHOWS (1 : N)
    draw_rel(82, 68, 76, 58, "1", "N", "hosts")
    # SHOWS -> BOOKINGS (1 : N)
    draw_rel(54, 45, 45, 45, "1", "N", "reserved in")
    # BOOKINGS -> TRANSACTIONS (1 : 1..N)
    draw_rel(30, 30, 30, 24, "1", "1..N", "paid via")

    # Legend / Annotation note
    note_box = patches.Rectangle((56, 24), 38, 7, facecolor='#F8FAFC', edgecolor='#94A3B8', linestyle='--')
    ax.add_patch(note_box)
    ax.text(75, 27.5, "1 : N = One-to-Many Relationship\nAUDIT_LOGS maintains standalone security event audit trail.",
            ha='center', va='center', fontsize=7, color='#475569')

    plt.tight_layout()
    plt.savefig('assets/report/fig4_1_er_diagram.png', bbox_inches='tight')
    plt.close()
    print("ER Diagram generated!")

def create_backend_uml():
    fig, ax = plt.subplots(figsize=(10, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    def draw_uml_class(x, y, w, h, name, attrs, methods, is_abstract=False, is_interface=False):
        header_h = 4
        tag = "<<interface>>\n" if is_interface else ("<<abstract>>\n" if is_abstract else "")
        header = patches.Rectangle((x, y + h - header_h), w, header_h, facecolor='#F1F5F9', edgecolor='#0F172A', linewidth=1.5)
        ax.add_patch(header)
        ax.text(x + w / 2, y + h - header_h / 2, tag + name, ha='center', va='center', fontsize=8, fontweight='bold', color='#0F172A')

        body = patches.Rectangle((x, y), w, h - header_h, facecolor='#FFFFFF', edgecolor='#0F172A', linewidth=1.5)
        ax.add_patch(body)

        curr_y = y + h - header_h - 2.5
        for a in attrs:
            ax.text(x + 1, curr_y, a, fontsize=6.8, color='#334155')
            curr_y -= 2.3

        if methods:
            ax.plot([x, x + w], [curr_y, curr_y], color='#CBD5E1', linewidth=1)
            curr_y -= 2.2
            for m in methods:
                ax.text(x + 1, curr_y, m, fontsize=6.8, color='#0F172A')
                curr_y -= 2.3

    ax.text(50, 97.5, "FIGURE 5.1: UML CLASS DIAGRAM (BACK-END & DOMAIN MODEL)", ha='center', va='center', fontsize=11, fontweight='bold')

    # User hierarchy
    draw_uml_class(5, 78, 25, 17, "User", ["- id, username, password", "- name, email, role"], ["+ getDashboardGreeting()*"], is_abstract=True)
    draw_uml_class(2, 60, 15, 11, "Customer", ["- loyaltyPoints: int"], ["+ addLoyaltyPoints()"])
    draw_uml_class(19, 60, 16, 11, "AdminUser", ["- accessLevel: String"], ["+ getAccessLevel()"])
    # Inheritance lines
    ax.annotate('', xy=(17.5, 78), xytext=(9.5, 71), arrowprops=dict(facecolor='white', edgecolor='#0F172A', arrowstyle='-|>'))
    ax.annotate('', xy=(17.5, 78), xytext=(27, 71), arrowprops=dict(facecolor='white', edgecolor='#0F172A', arrowstyle='-|>'))

    # Core models
    draw_uml_class(37, 77, 26, 18, "Movie", ["- id, title, genre", "- durationMinutes, language"], ["+ matches(query): boolean", "+ matches(t, g): boolean"])
    draw_uml_class(69, 77, 28, 18, "Screen", ["- id, name", "- rows, columns: int"], ["+ getTotalCapacity(): int"])
    draw_uml_class(53, 49, 30, 21, "Show", ["- id, movie, screen", "- showDateTime, basePrice", "- seats: Seat[][]"], ["+ findSeat(id): Seat", "+ calculateSeatPrice()"])
    draw_uml_class(86, 50, 12, 19, "Seat", ["- id, row, col", "- category, status"], ["+ isAvailable()", "+ reserve()", "+ confirmBooking()"])

    # Booking & Cancellable
    draw_uml_class(4, 30, 26, 16, "Cancellable", [], ["+ isCancellable(): boolean", "+ cancel(reason)*"], is_interface=True)
    draw_uml_class(4, 3, 31, 23, "Booking", ["- id, customer, show", "- seatIds: List<String>", "- totalAmount, status"], ["+ confirm()", "+ isExpired(): boolean", "+ cancel(reason)"])
    ax.annotate('', xy=(17, 30), xytext=(17, 26), arrowprops=dict(facecolor='white', edgecolor='#0F172A', linestyle='--', arrowstyle='-|>'))

    # Payment Strategy
    draw_uml_class(40, 24, 25, 15, "PaymentMethod", ["- methodName: String"], ["+ executeTransaction()*"], is_abstract=True)
    draw_uml_class(38, 4, 13, 13, "MockCardPayment", ["- cardNumber", "- cardHolder"], ["+ executeTransaction()"])
    draw_uml_class(53, 4, 13, 13, "MockUpiPayment", ["- vpaId: String"], ["+ executeTransaction()"])
    ax.annotate('', xy=(52.5, 24), xytext=(44.5, 17), arrowprops=dict(facecolor='white', edgecolor='#0F172A', arrowstyle='-|>'))
    ax.annotate('', xy=(52.5, 24), xytext=(59.5, 17), arrowprops=dict(facecolor='white', edgecolor='#0F172A', arrowstyle='-|>'))

    # DatabaseManager
    draw_uml_class(70, 4, 28, 29, "DatabaseManager", ["- instance: DatabaseManager", "- DB_URL: String"], [
        "+ getInstance(): DatabaseManager",
        "+ authenticate(u, p): User",
        "+ loadAllMovies(): List<Movie>",
        "+ loadAllShows(): List<Show>",
        "+ insertBooking(b)",
        "+ insertTransaction(t)"
    ])

    plt.tight_layout()
    plt.savefig('assets/report/fig5_1_backend_uml.png', bbox_inches='tight')
    plt.close()
    print("Backend UML generated!")

def create_swing_uml():
    fig, ax = plt.subplots(figsize=(10, 7.5), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    def draw_box(x, y, w, h, title, sub=""):
        rect = patches.Rectangle((x, y), w, h, facecolor='#F8FAFC', edgecolor='#0F172A', linewidth=1.5)
        ax.add_patch(rect)
        ax.text(x + w / 2, y + h / 2 + (1 if sub else 0), title, ha='center', va='center', fontsize=8.5, fontweight='bold', color='#0F172A')
        if sub:
            ax.text(x + w / 2, y + h / 2 - 2, sub, ha='center', va='center', fontsize=7, color='#64748B')

    ax.text(50, 97, "FIGURE 5.2: UML CLASS DIAGRAM (SWING UI ARCHITECTURE)", ha='center', va='center', fontsize=11, fontweight='bold')

    # Entry point
    draw_box(40, 87, 20, 6, "CineBookApp", "main(args) / Entry Point")

    # Login dialog
    draw_box(15, 72, 24, 8, "LoginDialog", "extends JDialog / Modal Auth")

    # Main Frame
    draw_box(55, 68, 32, 13, "MainFrame", "extends JFrame (CardLayout Controller)\n[showMoviesScreen, navigateToSeatSelection, ...]")

    # Connection lines
    ax.annotate('', xy=(27, 80), xytext=(45, 87), arrowprops=dict(facecolor='#0F172A', arrowstyle='->', linewidth=1.2))
    ax.annotate('', xy=(65, 81), xytext=(55, 87), arrowprops=dict(facecolor='#0F172A', arrowstyle='->', linewidth=1.2))
    ax.annotate('', xy=(55, 74), xytext=(39, 74), arrowprops=dict(facecolor='#0F172A', linestyle='--', arrowstyle='->', linewidth=1.2))

    # Panels managed by CardLayout
    ax.text(50, 52, "CardLayout Content Views (Modular Sub-Panels extending JPanel)", ha='center', fontsize=9, fontweight='bold', color='#4F46E5')

    panels = [
        ("MoviePanel", "Screen 1: Catalogue & Filtering", 4, 34),
        ("MovieDetailsPanel", "Screen 2: Details & Showtimes", 36, 34),
        ("SeatSelectionPanel", "Screen 3: 2D Grid & Pricing", 68, 34),
        ("CheckoutPanel", "Screen 4: Strategy Payment", 4, 16),
        ("BookingConfirmationPanel", "Screen 5: Admission Receipt", 36, 16),
        ("BookingHistoryPanel", "Screen 6: E-Ticket Ledger", 68, 16),
        ("AdminPanel", "Screen 7: Operations Console & KPI Analytics", 20, 2)
    ]

    for p_name, p_desc, px, py in panels:
        w = 58 if "AdminPanel" in p_name else 28
        h = 10
        draw_box(px, py, w, h, p_name, p_desc)
        # Arrow from MainFrame
        ax.annotate('', xy=(px + w / 2, py + h), xytext=(70, 68),
                    arrowprops=dict(facecolor='#94A3B8', edgecolor='#94A3B8', arrowstyle='->', linewidth=0.8, linestyle=':'))

    plt.tight_layout()
    plt.savefig('assets/report/fig5_2_swing_uml.png', bbox_inches='tight')
    plt.close()
    print("Swing UML generated!")

if __name__ == '__main__':
    create_er_diagram()
    create_backend_uml()
    create_swing_uml()
