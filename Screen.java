// Module 1: Screen Model
public class Screen {
    private final String id;
    private final String name;
    private final int rows;
    private final int columns;

    public Screen(String id, String name, int rows, int columns) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getTotalCapacity() { return rows * columns; }

    @Override
    public String toString() {
        return name + " (" + getTotalCapacity() + " seats)";
    }
}
